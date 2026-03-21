package com.easystation.build.domain;

import com.easystation.build.enums.BuildStatus;
import com.easystation.build.enums.BuildType;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 构建任务
 */
@Entity
@Table(name = "build_task")
@Getter
@Setter
public class BuildTask extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(nullable = false)
    public String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public BuildType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public BuildStatus status = BuildStatus.PENDING;

    /**
     * 关联的 Agent 模板 ID
     */
    public UUID templateId;

    /**
     * 构建配置（JSON 格式）
     */
    @Column(columnDefinition = "TEXT")
    public String config;

    /**
     * 构建脚本
     */
    @Column(columnDefinition = "TEXT")
    public String script;

    /**
     * 构建产物路径
     */
    public String artifactPath;

    /**
     * 构建产物大小（字节）
     */
    public Long artifactSize;

    /**
     * 构建产物版本
     */
    public String version;

    /**
     * 构建日志
     */
    @Column(columnDefinition = "TEXT")
    public String logs;

    /**
     * 错误信息
     */
    @Column(columnDefinition = "TEXT")
    public String errorMessage;

    /**
     * 开始时间
     */
    public LocalDateTime startedAt;

    /**
     * 结束时间
     */
    public LocalDateTime finishedAt;

    /**
     * 执行时长（毫秒）
     */
    public Long duration;

    /**
     * 触发人
     */
    public String triggeredBy;

    @CreationTimestamp
    public LocalDateTime createdAt;

    @UpdateTimestamp
    public LocalDateTime updatedAt;
}