# 任务: 实现 #397 诊断报告生成 - Phase 1 (Domain Model)

## 仓库信息
- 路径: /home/esa-runner/es-agents
- 分支: feature/diagnostic-report-397
- Issue: #397

## 背景
诊断报告生成功能的 Phase 1，需要创建基础的领域模型。

## 任务描述
创建诊断报告系统的核心 Domain 实体和枚举。

## 执行步骤

### 1. 创建枚举类

在 `server/src/main/java/com/easystation/diagnostic/enums/` 目录下创建：

**DiagnosticCategory.java**:
```java
package com.easystation.diagnostic.enums;

/**
 * 诊断类别
 */
public enum DiagnosticCategory {
    PERFORMANCE("性能", "性能相关问题"),
    SECURITY("安全", "安全配置问题"),
    CONFIGURATION("配置", "系统配置问题"),
    RESOURCE("资源", "资源使用问题"),
    AVAILABILITY("可用性", "服务可用性问题");
    
    private final String label;
    private final String description;
    
    DiagnosticCategory(String label, String description) {
        this.label = label;
        this.description = description;
    }
    
    public String getLabel() { return label; }
    public String getDescription() { return description; }
}
```

**ReportStatus.java**:
```java
package com.easystation.diagnostic.enums;

/**
 * 报告状态
 */
public enum ReportStatus {
    GENERATING("生成中"),
    COMPLETED("已完成"),
    FAILED("失败");
    
    private final String label;
    
    ReportStatus(String label) {
        this.label = label;
    }
    
    public String getLabel() { return label; }
}
```

**FindingSeverity.java**:
```java
package com.easystation.diagnostic.enums;

/**
 * 发现严重程度
 */
public enum FindingSeverity {
    INFO("信息", 1),
    WARNING("警告", 2),
    CRITICAL("严重", 3),
    FATAL("致命", 4);
    
    private final String label;
    private final int level;
    
    FindingSeverity(String label, int level) {
        this.label = label;
        this.level = level;
    }
    
    public String getLabel() { return label; }
    public int getLevel() { return level; }
}
```

### 2. 创建 Domain 实体

在 `server/src/main/java/com/easystation/diagnostic/domain/` 目录下创建：

**DiagnosticRule.java**:
```java
package com.easystation.diagnostic.domain;

import com.easystation.diagnostic.enums.DiagnosticCategory;
import com.easystation.diagnostic.enums.FindingSeverity;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 诊断规则实体
 */
@Entity
@Table(name = "diagnostic_rules")
public class DiagnosticRule extends PanacheEntity {
    
    @Column(name = "rule_id", unique = true, nullable = false, length = 36)
    public String ruleId;
    
    @Column(nullable = false, length = 100)
    public String name;
    
    @Column(length = 500)
    public String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public DiagnosticCategory category;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    public String condition; // SpEL 或 JSON 格式的条件表达式
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public FindingSeverity severity;
    
    @Column(columnDefinition = "TEXT")
    public String recommendation; // 修复建议模板
    
    public boolean enabled = true;
    
    @Column(name = "created_at")
    public LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
    
    @PrePersist
    void prePersist() {
        if (ruleId == null) ruleId = java.util.UUID.randomUUID().toString();
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

**DiagnosticFinding.java**:
```java
package com.easystation.diagnostic.domain;

import com.easystation.diagnostic.enums.FindingSeverity;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 诊断发现实体
 */
@Entity
@Table(name = "diagnostic_findings")
public class DiagnosticFinding extends PanacheEntity {
    
    @Column(name = "finding_id", unique = true, nullable = false, length = 36)
    public String findingId;
    
    @Column(name = "report_id", nullable = false, length = 36)
    public String reportId;
    
    @Column(name = "rule_id", length = 36)
    public String ruleId;
    
    @Column(nullable = false, length = 200)
    public String title;
    
    @Column(columnDefinition = "TEXT")
    public String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public FindingSeverity severity;
    
    @Column(name = "metric_name", length = 100)
    public String metricName;
    
    @Column(name = "metric_value")
    public Double metricValue;
    
    @Column(name = "threshold_value")
    public Double thresholdValue;
    
    @Column(columnDefinition = "TEXT")
    public String impact; // 影响分析
    
    @Column(columnDefinition = "TEXT")
    public String recommendation; // 修复建议
    
    @Column(name = "created_at")
    public LocalDateTime createdAt;
    
    @PrePersist
    void prePersist() {
        if (findingId == null) findingId = java.util.UUID.randomUUID().toString();
        createdAt = LocalDateTime.now();
    }
}
```

**DiagnosticReport.java**:
```java
package com.easystation.diagnostic.domain;

import com.easystation.diagnostic.enums.ReportStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 诊断报告实体
 */
@Entity
@Table(name = "diagnostic_reports")
public class DiagnosticReport extends PanacheEntity {
    
    @Column(name = "report_id", unique = true, nullable = false, length = 36)
    public String reportId;
    
    @Column(nullable = false, length = 200)
    public String title;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public ReportStatus status;
    
    @Column(name = "started_at")
    public LocalDateTime startedAt;
    
    @Column(name = "completed_at")
    public LocalDateTime completedAt;
    
    @Column(name = "total_findings")
    public int totalFindings;
    
    @Column(name = "info_count")
    public int infoCount;
    
    @Column(name = "warning_count")
    public int warningCount;
    
    @Column(name = "critical_count")
    public int criticalCount;
    
    @Column(name = "fatal_count")
    public int fatalCount;
    
    @Column(columnDefinition = "TEXT")
    public String summary; // 报告摘要
    
    @Column(name = "created_by", length = 100)
    public String createdBy;
    
    @Column(name = "created_at")
    public LocalDateTime createdAt;
    
    @PrePersist
    void prePersist() {
        if (reportId == null) reportId = java.util.UUID.randomUUID().toString();
        createdAt = LocalDateTime.now();
        if (startedAt == null) startedAt = LocalDateTime.now();
    }
}
```

### 3. 验证编译

运行 `./mvnw compile -pl server -q` 确保代码编译通过。

## 约束条件
- 遵循项目现有代码风格（参考 alert/ 目录的实体类）
- 使用 PanacheEntity 作为基类
- 使用 @PrePersist 自动生成 UUID 和时间戳
- 枚举使用 STRING 类型存储

## 预期产出
- 3 个枚举类：DiagnosticCategory, ReportStatus, FindingSeverity
- 3 个实体类：DiagnosticRule, DiagnosticFinding, DiagnosticReport
- 编译通过，无错误