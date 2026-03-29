# M5 Phase 4 - Issue #352: 优雅关闭支持

## 目标

实现 Quarkus 服务优雅关闭，避免在途请求丢失和数据损坏。

## 功能范围

1. **SIGTERM/SIGINT 信号处理**
   - 捕获关闭信号
   - 停止接收新请求
   - 等待在途请求完成

2. **在途请求跟踪**
   - 请求计数器
   - 请求完成等待

3. **资源清理**
   - 数据库连接池关闭
   - Redis 连接关闭
   - 缓存数据持久化
   - 日志刷新

4. **关闭超时控制**
   - 最大等待时间 30 秒
   - 超时强制关闭

## 技术方案

### 1. Quarkus Shutdown 配置

在 `application.properties` 中添加：

```properties
# 关闭超时时间（秒）
quarkus.shutdown.timeout=30

# 关闭前等待时间
quarkus.shutdown.wait=5
```

### 2. @BeforeShutdown Listener

创建 `GracefulShutdownListener.java`：

```java
@ApplicationScoped
public class GracefulShutdownListener {
    
    @Inject
    Logger log;
    
    private final AtomicInteger activeRequests = new AtomicInteger(0);
    
    @BeforeShutdown
    void beforeShutdown() {
        log.info("开始优雅关闭，等待 {} 个在途请求完成...", activeRequests.get());
        
        // 等待在途请求完成
        int timeout = 30;
        while (activeRequests.get() > 0 && timeout > 0) {
            try {
                Thread.sleep(100);
                timeout--;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        log.info("在途请求处理完成，开始资源清理...");
    }
}
```

### 3. 请求拦截器

创建 `RequestCountInterceptor.java`：

```java
@Provider
@Priority(Priorities.USER)
public class RequestCountInterceptor implements ContainerRequestFilter, ContainerResponseFilter {
    
    @Inject
    GracefulShutdownListener shutdownListener;
    
    @Override
    public void filter(ContainerRequestContext requestContext) {
        shutdownListener.incrementActiveRequests();
    }
    
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        shutdownListener.decrementActiveRequests();
    }
}
```

### 4. 资源清理 Bean

创建 `ResourceCleanupBean.java`：

```java
@ApplicationScoped
public class ResourceCleanupBean {
    
    @Inject
    Logger log;
    
    @Inject
    DataSource dataSource;
    
    @Inject
    RedisClient redisClient;
    
    @BeforeShutdown
    void cleanupResources() {
        log.info("清理资源...");
        
        // 清理缓存
        // 刷新日志
        // 关闭连接
        
        log.info("资源清理完成");
    }
}
```

## 实施步骤

### Step 1: 配置 Quarkus Shutdown

- [ ] 修改 `server/src/main/resources/application.properties`
- [ ] 添加 shutdown timeout 配置

### Step 2: 创建优雅关闭监听器

- [ ] 创建 `GracefulShutdownListener.java`
- [ ] 实现 `@BeforeShutdown` 方法
- [ ] 添加在途请求跟踪

### Step 3: 创建请求拦截器

- [ ] 创建 `RequestCountInterceptor.java`
- [ ] 实现请求计数
- [ ] 注册为 JAX-RS Provider

### Step 4: 创建资源清理 Bean

- [ ] 创建 `ResourceCleanupBean.java`
- [ ] 实现资源清理逻辑
- [ ] 添加日志记录

### Step 5: 测试验证

- [ ] 编译测试
- [ ] 手动测试关闭流程
- [ ] 验证日志输出

## 文件结构

```
server/src/main/java/com/easystation/shutdown/
├── GracefulShutdownListener.java
├── RequestCountInterceptor.java
└── ResourceCleanupBean.java
```

## 验收标准

- [ ] 编译成功 (`mvn compile`)
- [ ] 关闭时间 < 30s
- [ ] 在途请求不丢失
- [ ] 资源完全释放
- [ ] 关闭日志完整
- [ ] PR 创建并推送

## 依赖

- Quarkus 核心框架
- JAX-RS 过滤器
- CDI 生命周期回调

## 备注

- 生产环境必备功能
- 避免数据丢失
- 提升用户体验
