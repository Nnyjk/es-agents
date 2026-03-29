package com.easystation.plugin.sandbox.domain;

/**
 * 资源限制配置
 */
public record ResourceLimit(
    long maxMemory,
    int maxCpuTime,
    int maxExecutionTime,
    int maxFileOperations,
    int maxNetworkCalls
) {
    
    public static ResourceLimit defaults() {
        return new ResourceLimit(
            256 * 1024 * 1024L,  // 256MB
            5,                    // 5 秒 CPU 时间
            30,                   // 30 秒执行时间
            100,                  // 100 次文件操作
            50                    // 50 次网络调用
        );
    }
    
    public static ResourceLimit relaxed() {
        return new ResourceLimit(
            512 * 1024 * 1024L,
            10,
            60,
            500,
            200
        );
    }
    
    public static ResourceLimit strict() {
        return new ResourceLimit(
            64 * 1024 * 1024L,
            2,
            10,
            20,
            10
        );
    }
    
    public boolean checkMemory(long bytes) {
        return bytes <= maxMemory;
    }
    
    public boolean checkFileOperation(int count) {
        return count <= maxFileOperations;
    }
    
    public boolean checkNetworkCall(int count) {
        return count <= maxNetworkCalls;
    }
}
