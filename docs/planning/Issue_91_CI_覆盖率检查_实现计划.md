# Issue #91 实现计划 - CI 增加单元测试覆盖率检查

## 目标

在 CI 流水线中增加单元测试覆盖率检查，确保代码质量。

## 任务分解

### Step 1: Server 模块 JaCoCo 覆盖率配置

- [x] JaCoCo 插件已配置 (server/pom.xml)
- [ ] 添加覆盖率检查阈值 (70%)
- [ ] 生成 XML 报告用于 CI 上传

### Step 2: Agent 模块覆盖率配置

- [x] coverage.sh 脚本已创建
- [ ] 修改 CI 使用 coverage.sh
- [ ] 生成 cobertura 格式报告

### Step 3: Frontend 模块覆盖率配置

- [ ] 修改 vite.config.ts 添加覆盖率配置
- [ ] 生成 lcov 格式报告

### Step 4: CI 工作流更新

- [ ] 修改 .github/workflows/pr-checks.yml
  - Server: 添加 JaCoCo 覆盖率检查
  - Agent: 使用 coverage.sh 运行测试
  - Frontend: 添加覆盖率收集
- [ ] 添加覆盖率报告上传步骤
- [ ] 添加 PR 评论覆盖率结果

### Step 5: 测试验证

- [ ] 本地运行测试验证覆盖率生成
- [ ] 创建 PR 验证 CI 工作流

## 验收标准

- [ ] CI 自动统计三个模块的单元测试覆盖率
- [ ] 覆盖率低于 70% 时拦截 PR 合并
- [ ] 生成覆盖率报告并在 PR 评论中展示

## 技术细节

### Server (JaCoCo)

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <id>jacoco-check</id>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <element>BUNDLE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.70</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### Agent (coverage.sh)

```bash
#!/bin/bash
go test ./... -coverprofile=coverage.out -covermode=atomic
go tool cover -func=coverage.out  # 文本摘要
go tool cover -html=coverage.out -o coverage.html  # HTML 报告
```

### Frontend (Vitest)

```typescript
// vite.config.ts
test: {
  coverage: {
    provider: 'v8',
    reporter: ['text', 'lcov'],
    threshold: {
      lines: 70,
      functions: 70,
      branches: 70,
      statements: 70
    }
  }
}
```

### CI 工作流

```yaml
- name: Upload coverage reports
  uses: codecov/codecov-action@v4
  with:
    files: ./server/target/site/jacoco/jacoco.xml,./agent/coverage.xml,./frontend/coverage/lcov.info
    token: ${{ secrets.CODECOV_TOKEN }}
```

## 依赖

- Issue #123 (单元测试框架) - ✅ 已完成
