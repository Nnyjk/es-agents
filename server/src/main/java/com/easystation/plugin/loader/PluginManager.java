package com.easystation.plugin.loader;

import com.easystation.plugin.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

/**
 * 插件管理器
 * 
 * 负责插件的生命周期管理，包括加载、初始化、启动、停止和卸载。
 * 协调 PluginLoader、PluginRegistry 和 PluginContext 的工作。
 */
public class PluginManager {
    
    private static final Logger log = LoggerFactory.getLogger(PluginManager.class);
    
    private final PluginLoader pluginLoader;
    private final PluginRegistry pluginRegistry;
    private final PluginContextFactory contextFactory;
    private final ExtensionRegistry extensionRegistry;
    private final Path pluginsDir;
    
    /**
     * 创建插件管理器
     * 
     * @param pluginsDir 插件目录路径
     */
    public PluginManager(Path pluginsDir) {
        this.pluginsDir = pluginsDir;
        this.pluginRegistry = new PluginRegistry();
        this.extensionRegistry = new ExtensionRegistryImpl();
        this.contextFactory = new PluginContextFactoryImpl(extensionRegistry);
        this.pluginLoader = new PluginLoaderImpl(pluginsDir);
    }
    
    /**
     * 创建插件管理器（自定义组件）
     * 
     * @param pluginsDir 插件目录路径
     * @param pluginLoader 插件加载器
     * @param pluginRegistry 插件注册表
     * @param contextFactory 上下文工厂
     * @param extensionRegistry 扩展注册表
     */
    public PluginManager(Path pluginsDir, 
                         PluginLoader pluginLoader,
                         PluginRegistry pluginRegistry,
                         PluginContextFactory contextFactory,
                         ExtensionRegistry extensionRegistry) {
        this.pluginsDir = pluginsDir;
        this.pluginLoader = pluginLoader;
        this.pluginRegistry = pluginRegistry;
        this.contextFactory = contextFactory;
        this.extensionRegistry = extensionRegistry;
    }
    
    /**
     * 加载并启动所有插件
     * 
     * 扫描 plugins 目录，加载所有有效插件并启动。
     */
    public void loadAllPlugins() {
        log.info("开始加载插件目录：{}", pluginsDir);
        
        List<PluginDescriptor> descriptors = pluginLoader.discoverPlugins();
        log.info("发现 {} 个插件", descriptors.size());
        
        for (PluginDescriptor descriptor : descriptors) {
            try {
                loadPlugin(descriptor);
            } catch (Exception e) {
                log.error("加载插件失败：{}", descriptor.getId(), e);
            }
        }
        
        log.info("插件加载完成，共加载 {} 个插件", pluginRegistry.size());
    }
    
    /**
     * 加载单个插件
     * 
     * @param descriptor 插件描述符
     * @throws PluginException 加载失败时抛出
     */
    public void loadPlugin(PluginDescriptor descriptor) throws PluginException {
        String pluginId = descriptor.getId();
        log.info("加载插件：{} v{}", pluginId, descriptor.getVersion());
        
        // 检查是否已加载
        if (pluginRegistry.isRegistered(pluginId)) {
            log.warn("插件已加载，跳过：{}", pluginId);
            return;
        }
        
        // 检查依赖
        if (!checkDependencies(descriptor)) {
            throw new PluginException("插件依赖不满足：" + pluginId);
        }
        
        // 加载插件类
        Plugin plugin = pluginLoader.loadPlugin(descriptor);
        
        // 创建插件上下文
        PluginContext context = contextFactory.create(pluginId, descriptor);
        
        // 初始化插件
        plugin.initialize(context);
        
        // 注册插件
        pluginRegistry.register(plugin);
        
        // 启动插件
        startPlugin(pluginId);
        
        log.info("插件加载成功：{} v{}", pluginId, descriptor.getVersion());
    }
    
    /**
     * 启动插件
     * 
     * @param pluginId 插件 ID
     * @throws PluginException 启动失败时抛出
     */
    public void startPlugin(String pluginId) throws PluginException {
        Plugin plugin = pluginRegistry.getPlugin(pluginId)
            .orElseThrow(() -> new PluginException("插件未找到：" + pluginId));
        
        log.info("启动插件：{}", pluginId);
        plugin.start();
    }
    
    /**
     * 停止插件
     * 
     * @param pluginId 插件 ID
     */
    public void stopPlugin(String pluginId) {
        Plugin plugin = pluginRegistry.getPlugin(pluginId).orElse(null);
        if (plugin != null) {
            log.info("停止插件：{}", pluginId);
            plugin.stop();
        }
    }
    
    /**
     * 卸载插件
     * 
     * @param pluginId 插件 ID
     */
    public void unloadPlugin(String pluginId) {
        stopPlugin(pluginId);
        Plugin plugin = pluginRegistry.unregister(pluginId);
        if (plugin != null) {
            log.info("插件已卸载：{}", pluginId);
        }
    }
    
    /**
     * 检查插件依赖
     * 
     * @param descriptor 插件描述符
     * @return 依赖是否满足
     */
    private boolean checkDependencies(PluginDescriptor descriptor) {
        List<PluginDependency> dependencies = descriptor.getDependencies();
        if (dependencies == null || dependencies.isEmpty()) {
            return true;
        }
        
        for (PluginDependency dep : dependencies) {
            if (!pluginRegistry.isRegistered(dep.getPluginId())) {
                log.error("插件依赖不满足：{} 需要 {}", descriptor.getId(), dep.getPluginId());
                return false;
            }
            // TODO: 检查版本兼容性
        }
        
        return true;
    }
    
    /**
     * 获取插件注册表
     * 
     * @return 插件注册表
     */
    public PluginRegistry getRegistry() {
        return pluginRegistry;
    }
    
    /**
     * 获取扩展注册表
     * 
     * @return 扩展注册表
     */
    public ExtensionRegistry getExtensionRegistry() {
        return extensionRegistry;
    }
    
    /**
     * 关闭插件管理器，卸载所有插件
     */
    public void shutdown() {
        log.info("关闭插件管理器...");
        
        // 停止并卸载所有插件（逆序）
        List<Plugin> plugins = new ArrayList<>(pluginRegistry.getAllPlugins());
        Collections.reverse(plugins);
        
        for (Plugin plugin : plugins) {
            try {
                unloadPlugin(plugin.getDescriptor().getId());
            } catch (Exception e) {
                log.error("卸载插件失败：{}", plugin.getDescriptor().getId(), e);
            }
        }
        
        log.info("插件管理器已关闭");
    }
}
