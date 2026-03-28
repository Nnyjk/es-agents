# Server 模块测试规范

## 测试框架

Server 模块使用以下测试框架和工具：

- **JUnit 5**：单元测试框架（通过 Quarkus BOM 管理）
- **Quarkus Test**：集成测试支持 (`@QuarkusTest`)
- **Rest Assured**：API 端点测试
- **JaCoCo**：代码覆盖率统计

## 测试命名规范

### 测试类命名

测试类命名遵循 `{ClassName}Test.java` 格式：

```
src/test/java/com/easystation/
├── infra/service/HostServiceTest.java
├── deployment/service/EnvironmentServiceTest.java
└── export/service/ExportServiceTest.java
```

### 测试方法命名

测试方法使用描述性命名，清晰表达测试意图：

```java
@Test
void testCreateHostWithValidData() { ... }

@Test
void testCreateHostThrowsExceptionWhenEnvironmentNotFound() { ... }
```

## 测试注解使用

### 单元测试

使用 `@QuarkusTest` 进行需要 CDI 容器支持的测试：

```java
@QuarkusTest
class HostServiceTest {
    @Inject
    HostService hostService;

    @Test
    void testListHosts() { ... }
}
```

### 事务测试

涉及数据库操作的测试方法使用 `@Transactional`：

```java
@Test
@Transactional
void testCreateHost() { ... }
```

### Mock 测试

使用 `@InjectMock` 或 `@Mock` 进行依赖模拟：

```java
@QuarkusTest
class ServiceTest {
    @InjectMock
    DependencyService dependencyService;

    @Test
    void testMethod() {
        when(dependencyService.method()).thenReturn(value);
        // ...
    }
}
```

## 测试结构

每个测试类应遵循以下结构：

```java
@QuarkusTest
class ExampleServiceTest {

    @Inject
    ExampleService service;

    // 1. Setup / Helper methods

    // 2. Test methods grouped by functionality

    // 3. Cleanup methods (if needed)
}
```

## 运行测试

### 运行所有测试

```bash
cd server
mvn test -DskipITs
```

### 运行特定测试类

```bash
mvn test -Dtest=HostServiceTest
```

### 运行集成测试

```bash
mvn verify
```

### 生成覆盖率报告

```bash
mvn test jacoco:report
# 报告位置：target/site/jacoco/index.html
```

## 覆盖率要求

- 新代码覆盖率目标：≥ 70%
- 关键业务逻辑覆盖率目标：≥ 80%
- 覆盖率报告通过 CI 自动生成

## 测试数据库

测试使用 H2 内存数据库：

```properties
# application-test.properties
quarkus.datasource.db-kind=h2
quarkus.datasource.jdbc.url=jdbc:h2:mem:test
```

## 最佳实践

1. **测试独立性**：每个测试方法应独立运行，不依赖其他测试
2. **数据隔离**：使用事务回滚或测试前清理数据
3. **边界测试**：覆盖正常、异常和边界情况
4. **断言明确**：使用具体的断言方法，避免模糊断言
5. **避免过度 Mock**：优先使用真实依赖进行集成测试

## 示例测试

参考 `ExportServiceTest.java` 作为测试编写示例。
