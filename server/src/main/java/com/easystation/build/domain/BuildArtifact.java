package com.easystation.build.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 构建产物
 */
@Entity
@Table(name = "build_artifact")
@Getter
@Setter
public class BuildArtifact extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    /**
     * 关联的构建任务 ID
     */
    @Column(nullable = false)
    public UUID buildTaskId;

    /**
     * 关联的 Agent 模板 ID
     */
    public UUID templateId;

    /**
     * 产物名称
     */
    @Column(nullable = false)
    public String name;

    /**
     * 版本号
     */
    @Column(nullable = false)
    public String version;

    /**
     * 文件路径
     */
    @Column(nullable = false)
    public String filePath;

    /**
     * 文件大小（字节）
     */
    public Long fileSize;

    /**
     * 文件校验和（MD5/SHA256）
     */
    public String checksum;

    /**
     * 校验和类型
     */
    public String checksumType;

    /**
     * 是否为最新版本
     */
    public boolean latest = true;

    /**
     * 下载次数
     */
    public int downloadCount = 0;

    @CreationTimestamp
    public LocalDateTime createdAt;

    @UpdateTimestamp
    public LocalDateTime updatedAt;
}