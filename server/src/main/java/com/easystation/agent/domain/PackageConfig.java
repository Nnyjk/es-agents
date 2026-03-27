package com.easystation.agent.domain;

import com.easystation.agent.domain.enums.PackageType;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 打包配置实体
 * 管理 Agent 打包的各种来源配置
 */
@Entity
@Table(name = "package_config")
@Getter
@Setter
@SQLDelete(sql = "UPDATE package_config SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class PackageConfig extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    /** 配置名称 */
    @Column(nullable = false, unique = true)
    public String name;

    /** 配置描述 */
    @Column(columnDefinition = "TEXT")
    public String description;

    /** 打包类型 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public PackageType type;

    // ========== 本地配置 ==========
    /** 本地文件路径（LOCAL 类型） */
    public String localPath;

    // ========== Git 配置 ==========
    /** Git 仓库地址（GIT 类型） */
    public String gitUrl;
    /** Git 分支 */
    public String gitBranch;
    /** Git 凭证 ID */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "git_credential_id")
    public AgentCredential gitCredential;

    // ========== Docker 配置 ==========
    /** Docker 镜像地址（DOCKER 类型） */
    public String dockerImage;
    /** Docker 镜像 Tag */
    public String dockerTag;
    /** Docker Registry 凭证 ID */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "docker_credential_id")
    public AgentCredential dockerCredential;

    // ========== 阿里云配置 ==========
    /** 阿里云仓库地址（ALIYUN 类型） */
    public String aliyunRepoUrl;
    /** 阿里云命名空间 */
    public String aliyunNamespace;
    /** 阿里云凭证 ID */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "aliyun_credential_id")
    public AgentCredential aliyunCredential;

    // ========== 通用配置 ==========
    /** 构建命令 */
    public String buildCommand;
    /** 输出目录 */
    public String outputDir;
    /** 环境变量（JSON 格式） */
    @Column(columnDefinition = "TEXT")
    public String envVariables;

    /** 配置版本 */
    @Column(nullable = false)
    public Integer version = 1;

    /** 是否启用 */
    @Column(nullable = false)
    public Boolean enabled = true;

    /** 是否删除（软删除） */
    @Column(nullable = false)
    public Boolean deleted = false;

    /** 创建时间 */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    public LocalDateTime createdAt;

    /** 更新时间 */
    @UpdateTimestamp
    @Column(nullable = false)
    public LocalDateTime updatedAt;

    /** 创建者 */
    public String createdBy;

    /** 更新者 */
    public String updatedBy;
}