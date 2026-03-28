# Agent 模块测试规范

## 测试框架

Agent 模块使用 Go 内置测试框架：

- **Go Testing**：标准测试包 (`testing`)
- **覆盖率工具**：`go test -cover`
- **无外部依赖**：保持测试简单和快速

## 测试命名规范

### 测试文件命名

测试文件命名遵循 `{filename}_test.go` 格式：

```
agent/
├── internal/
│   ├── config/
│   │   └── config.go
│   │   └── config_test.go
│   ├── client/
│   │   └── http_client.go
│   │   └── http_client_test.go
│   ├── resource/
│   │   ├── fetcher.go
│   │   └── fetcher_test.go
```

### 测试函数命名

测试函数使用 `Test{FunctionName}` 或 `Test{FunctionName}_{Scenario}` 格式：

```go
func TestLoad(t *testing.T) { ... }
func TestLoad_MissingFile(t *testing.T) { ... }
func TestLoad_InvalidYAML(t *testing.T) { ... }
```

## 测试结构

每个测试文件应遵循 Go 测试标准结构：

```go
package config // 与源文件同包

import (
    "testing"
)

func TestFunction(t *testing.T) {
    // Setup
    input := "test"

    // Execute
    result := Function(input)

    // Verify
    if result != expected {
        t.Errorf("expected %v, got %v", expected, result)
    }
}
```

## 运行测试

### 运行所有测试

```bash
cd agent
go test ./...
```

### 运行特定包测试

```bash
go test ./internal/config
```

### 运行特定测试函数

```bash
go test ./internal/config -run TestLoad
```

### 查看覆盖率

```bash
go test ./... -cover
```

### 生成详细覆盖率报告

```bash
./coverage.sh
# 报告位置：coverage.html
```

## 覆盖率脚本

项目提供 `coverage.sh` 脚本用于生成详细覆盖率报告：

```bash
#!/bin/bash
# 运行覆盖率脚本
./coverage.sh
```

脚本功能：
- 运行所有测试并收集覆盖率数据
- 生成 HTML 格式的覆盖率报告
- 显示总体覆盖率百分比

## 覆盖率要求

- 新代码覆盖率目标：≥ 70%
- 关键逻辑覆盖率目标：≥ 80%
- 纯工具函数覆盖率目标：≥ 90%

## 测试最佳实践

### 1. 表驱动测试

使用表驱动测试覆盖多种场景：

```go
func TestLoad(t *testing.T) {
    tests := []struct {
        name    string
        path    string
        want    *Config
        wantErr bool
    }{
        {"valid config", "config.yaml", &Config{Port: 9090}, false},
        {"missing file", "nonexistent.yaml", nil, true},
    }

    for _, tt := range tests {
        t.Run(tt.name, func(t *testing.T) {
            got, err := Load(tt.path)
            if (err != nil) != tt.wantErr {
                t.Errorf("Load() error = %v, wantErr %v", err, tt.wantErr)
            }
        })
    }
}
```

### 2. 子测试

使用 `t.Run()` 组织相关测试：

```go
func TestConfig(t *testing.T) {
    t.Run("Load", func(t *testing.T) {
        // ...
    })
    t.Run("Validate", func(t *testing.T) {
        // ...
    })
}
```

### 3. 测试隔离

每个测试应独立运行：

```go
func TestFunction(t *testing.T) {
    // 使用临时文件或目录
    tmpDir := t.TempDir()
    // t.TempDir() 在测试结束后自动清理
}
```

### 4. Mock 和接口

对于外部依赖，使用接口和 mock：

```go
type HttpClient interface {
    Get(url string) ([]byte, error)
}

// 测试时使用 mock 实现
type MockHttpClient struct {
    Response []byte
    Error    error
}

func (m *MockHttpClient) Get(url string) ([]byte, error) {
    return m.Response, m.Error
}
```

## 测试辅助函数

常用测试辅助函数：

```go
// 断言函数
func assertEqual(t *testing.T, got, want interface{}) {
    t.Helper() // 标记为辅助函数
    if got != want {
        t.Errorf("got %v, want %v", got, want)
    }
}

// 错误断言
func assertError(t *testing.T, err error, wantErr bool) {
    t.Helper()
    if (err != nil) != wantErr {
        t.Errorf("error = %v, wantErr %v", err, wantErr)
    }
}
```

## CI 集成

测试通过 CI 自动运行：

- 每次提交触发测试
- PR 合并前必须通过测试
- 覆盖率报告自动生成