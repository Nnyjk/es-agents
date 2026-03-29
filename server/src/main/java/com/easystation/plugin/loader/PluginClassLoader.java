package com.easystation.plugin.loader;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * 插件类加载器
 * 
 * 为每个插件创建独立的 ClassLoader，实现插件间的类隔离。
 * 插件的类不会被其他插件或主应用直接访问，只能通过插件接口交互。
 */
public class PluginClassLoader extends URLClassLoader {
    
    private final String pluginId;
    private final ClassLoader parent;
    
    /**
     * 创建插件类加载器
     * 
     * @param pluginId 插件 ID
     * @param urls 插件 JAR/类路径 URL 数组
     * @param parent 父类加载器
     */
    public PluginClassLoader(String pluginId, URL[] urls, ClassLoader parent) {
        super(urls, parent);
        this.pluginId = pluginId;
        this.parent = parent;
    }
    
    /**
     * 获取插件 ID
     * 
     * @return 插件 ID
     */
    public String getPluginId() {
        return pluginId;
    }
    
    /**
     * 重写 loadClass 实现类隔离
     * 
     * 优先从插件自身加载类，找不到时再委托给父加载器。
     * 但核心 Java 类和插件 API 类始终由父加载器加载。
     */
    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // 核心 Java 类和插件 API 类由父加载器加载
        if (isSystemClass(name)) {
            return parent.loadClass(name);
        }
        
        // 优先从插件自身加载
        try {
            Class<?> clazz = findClass(name);
            if (resolve) {
                resolveClass(clazz);
            }
            return clazz;
        } catch (ClassNotFoundException e) {
            // 插件中找不到，委托给父加载器
            return parent.loadClass(name);
        }
    }
    
    /**
     * 判断是否为系统类
     * 
     * 系统类包括：
     * - Java 核心类 (java.*, javax.*)
     * - 插件 API 类 (com.easystation.plugin.core.*)
     * 
     * @param name 类名
     * @return 是否为系统类
     */
    private boolean isSystemClass(String name) {
        if (name.startsWith("java.") || name.startsWith("javax.") || 
            name.startsWith("sun.") || name.startsWith("com.easystation.plugin.core.")) {
            return true;
        }
        return false;
    }
    
    /**
     * 关闭类加载器，释放资源
     */
    public void close() {
        try {
            super.close();
        } catch (Exception e) {
            // 记录日志但不抛出
        }
    }
}
