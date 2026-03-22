package com.easystation.agent.domain;

import com.easystation.agent.domain.enums.OsType;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Agent模板版本实体
 * 支持模板的多版本管理和版本历史
 */
@Entity
@Table(name = "agent_template_version")
@Getter
@Setter
public class AgentTemplateVersion extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    public AgentTemplate template;

    @Column(nullable = false)
    public String version;

    public String description;

    @Column(name = "install_script", columnDefinition = "TEXT")
    public String installScript;

    @Column(name = "config_template", columnDefinition = "TEXT")
    public String configTemplate;

    @Column(name = "dependencies", columnDefinition = "TEXT")
    public String dependencies;

    @Enumerated(EnumType.STRING)
    @Column(name = "os_type")
    public OsType osType;

    @Column(name = "arch_support")
    public String archSupport;

    @Column(name = "is_published")
    public boolean published = false;

    @Column(name = "is_latest")
    public boolean latest = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "published_at")
    public LocalDateTime publishedAt;

    @Column(name = "created_by")
    public String createdBy;
}