package com.easystation.shutdown;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.jboss.logging.Logger;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 优雅关闭监听器
 * 
 * 负责：
 * 1. 跟踪在途请求数量
 * 2. 在关闭时等待在途请求完成
 * 3. 记录关闭日志
 */
@ApplicationScoped
public class GracefulShutdownListener {

    private static final Logger log = Logger.getLogger(GracefulShutdownListener.class);

    /** 在途请求计数器 */
    private final AtomicInteger activeRequests = new AtomicInteger(0);
    
    /** 关闭锁存器 */
    private CountDownLatch shutdownLatch;
    
    /** 是否正在关闭 */
    private volatile boolean isShuttingDown = false;

    /**
     * 应用启动时初始化
     */
    void onStart(@Observes StartupEvent ev) {
        log.info("应用启动，优雅关闭监听器已就绪");
        shutdownLatch = new CountDownLatch(0);
    }

    /**
     * 应用关闭前回调
     * 
     * 使用 @BeforeShutdown 注解的方法会在 Quarkus 关闭流程开始前执行
     */
    void beforeShutdown() {
        isShuttingDown = true;
        int activeCount = activeRequests.get();
        
        if (activeCount > 0) {
            log.infof("开始优雅关闭，等待 %d 个在途请求完成...", activeCount);
            
            // 等待在途请求完成，最多等待 25 秒（Quarkus shutdown timeout 是 30 秒）
            int timeout = 25;
            while (activeRequests.get() > 0 && timeout > 0) {
                try {
                    Thread.sleep(100);
                    timeout--;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("关闭等待被中断");
                    break;
                }
            }
            
            int remaining = activeRequests.get();
            if (remaining > 0) {
                log.warnf("关闭超时，仍有 %d 个请求未完成，强制关闭", remaining);
            } else {
                log.info("所有在途请求处理完成");
            }
        } else {
            log.info("没有在途请求，直接关闭");
        }
    }

    /**
     * 应用关闭时回调
     */
    void onShutdown(@Observes ShutdownEvent ev) {
        log.info("应用已关闭");
    }

    /**
     * 增加在途请求计数
     */
    public void incrementActiveRequests() {
        if (!isShuttingDown) {
            activeRequests.incrementAndGet();
        }
    }

    /**
     * 减少在途请求计数
     */
    public void decrementActiveRequests() {
        int count = activeRequests.decrementAndGet();
        if (count < 0) {
            log.warn("在途请求计数出现负值，已重置为 0");
            activeRequests.set(0);
        }
    }

    /**
     * 获取当前在途请求数量
     */
    public int getActiveRequests() {
        return activeRequests.get();
    }

    /**
     * 检查是否正在关闭
     */
    public boolean isShuttingDown() {
        return isShuttingDown;
    }
}
