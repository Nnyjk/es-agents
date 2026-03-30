package com.easystation.plugin.loader;

import com.easystation.plugin.core.*;
import com.easystation.plugin.core.impl.PluginDescriptorParserImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * 插件加载器实现
 * 
 * 基于文件系统扫描和 Java ClassLoader 实现插件加载。
 */
public class PluginLoaderImpl implements PluginLoader {
    
    private static final Logger log = LoggerFactory.getLogger(PluginLoaderImpl.class);
    
    private static final String PLUGIN_JSON = "plugin.json";
    private static final String PLUGIN_DIR = "plugins";
    
    private final Path pluginsDir;
    private final ObjectMapper objectMapper;
    private final PluginDescriptorParser parser;
    
    /** 已加载插件的 ClassLoader 映射 */
    private final Map<String, PluginClassLoader> classLoaders = new HashMap<>();
    
    /**
     * 创建插件加载器
     * 
     * @param pluginsDir 插件目录路径
     */
    public PluginLoaderImpl(Path pluginsDir) {
        this.pluginsDir = pluginsDir;
        this.objectMapper = new ObjectMapper();
        this.parser = new PluginDescriptorParserImpl();
        
        // 确保插件目录存在
        try {
            Files.createDirectories(pluginsDir);
        } catch (IOException e) {
            log.error("创建插件目录失败：{}", pluginsDir, e);
        }
    }
    
    @Override
    public List<PluginDescriptor> discoverPlugins() {
        List<PluginDescriptor> descriptors = new ArrayList<>();
        
        if (!Files.exists(pluginsDir)) {
            log.warn("插件目录不存在：{}", pluginsDir);
            return descriptors;
        }
        
        try {
            Files.walkFileTree(pluginsDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (file.getFileName().toString().equals(PLUGIN_JSON)) {
                        try {
                            PluginDescriptor descriptor = parser.parse(file);
                            if (isValidDescriptor(descriptor)) {
                                descriptors.add(descriptor);
                                log.info("发现插件：{} v{}", descriptor.getId(), descriptor.getVersion());
                            } else {
                                log.warn("无效的插件描述符：{}", file);
                            }
                        } catch (PluginException e) {
                            log.error("解析插件描述符失败：{}", file, e);
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            log.error("扫描插件目录失败：{}", pluginsDir, e);
        }
        
        // 按依赖顺序排序（简单的拓扑排序）
        sortPluginsByDependency(descriptors);
        
        return descriptors;
    }
    
    @Override
    public Plugin loadPlugin(PluginDescriptor descriptor) throws PluginException {
        String pluginId = descriptor.getId();
        
        try {
            // 定位插件目录
            Path pluginDir = findPluginDir(pluginId);
            if (pluginDir == null) {
                throw new PluginException("找不到插件目录：" + pluginId);
            }
            
            // 创建 ClassLoader
            List<URL> urls = collectPluginUrls(pluginDir);
            PluginClassLoader classLoader = new PluginClassLoader(
                pluginId,
                urls.toArray(new URL[0]),
                getClass().getClassLoader()
            );
            classLoaders.put(pluginId, classLoader);
            
            // 加载插件类
            String className = descriptor.getMainClass();
            Class<?> pluginClass = classLoader.loadClass(className);
            
            // 实例化插件
            Object instance = pluginClass.getDeclaredConstructor().newInstance();
            
            if (!(instance instanceof Plugin)) {
                throw new PluginException("插件类必须实现 Plugin 接口：" + className);
            }
            
            log.info("插件加载成功：{} (类：{})", pluginId, className);
            return (Plugin) instance;
            
        } catch (PluginException e) {
            throw e;
        } catch (Exception e) {
            throw new PluginException("加载插件失败：" + pluginId, e);
        }
    }
    
    @Override
    public void unloadPlugin(String pluginId) {
        PluginClassLoader classLoader = classLoaders.remove(pluginId);
        if (classLoader != null) {
            try {
                classLoader.close();
                log.info("插件 ClassLoader 已关闭：{}", pluginId);
            } catch (Exception e) {
                log.error("关闭 ClassLoader 失败：{}", pluginId, e);
            }
        }
    }
    
    /**
     * 查找插件目录
     */
    private Path findPluginDir(String pluginId) throws IOException {
        final Path[] result = new Path[1];
        
        Files.walkFileTree(pluginsDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (file.getFileName().toString().equals(PLUGIN_JSON)) {
                    try {
                        PluginDescriptor descriptor = parser.parse(file);
                        if (pluginId.equals(descriptor.getId())) {
                            result[0] = file.getParent();
                            return FileVisitResult.TERMINATE;
                        }
                    } catch (PluginException e) {
                        // 忽略解析错误
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
        
        return result[0];
    }
    
    /**
     * 收集插件的 JAR 和类路径 URL
     */
    private List<URL> collectPluginUrls(Path pluginDir) throws IOException {
        List<URL> urls = new ArrayList<>();
        
        // 添加 classes 目录
        Path classesDir = pluginDir.resolve("classes");
        if (Files.exists(classesDir)) {
            urls.add(classesDir.toUri().toURL());
        }
        
        // 添加 lib 目录下的所有 JAR
        Path libDir = pluginDir.resolve("lib");
        if (Files.exists(libDir)) {
            Files.list(libDir)
                .filter(p -> p.toString().endsWith(".jar"))
                .forEach(p -> {
                    try {
                        urls.add(p.toUri().toURL());
                    } catch (Exception e) {
                        log.warn("添加 JAR URL 失败：{}", p, e);
                    }
                });
        }
        
        // 如果没有 classes 或 lib，添加插件根目录
        if (urls.isEmpty()) {
            urls.add(pluginDir.toUri().toURL());
        }
        
        return urls;
    }
    
    /**
     * 验证插件描述符是否有效
     */
    private boolean isValidDescriptor(PluginDescriptor descriptor) {
        return descriptor != null
            && descriptor.getId() != null
            && !descriptor.getId().isEmpty()
            && descriptor.getVersion() != null
            && descriptor.getMainClass() != null;
    }
    
    /**
     * 按依赖顺序排序插件（简单的拓扑排序）
     */
    private void sortPluginsByDependency(List<PluginDescriptor> descriptors) {
        // TODO: 实现完整的拓扑排序
        // 目前简单排序：有依赖的排在后面
        descriptors.sort(Comparator.comparingInt(d -> 
            d.getDependencies() == null ? 0 : d.getDependencies().size()
        ));
    }
}
