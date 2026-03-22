package com.easystation.agent.domain;

import com.easystation.agent.domain.enums.OsType;
import com.easystation.agent.domain.enums.TemplateCategory;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "agent_template")
@Getter
@Setter
public class AgentTemplate extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(nullable = false)
    public String name;

    public String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    public TemplateCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "os_type")
    public OsType osType;

    @Column(name = "arch_support")
    public String archSupport;

    @Column(name = "install_script", columnDefinition = "TEXT")
    public String installScript;

    @Column(name = "config_template", columnDefinition = "TEXT")
    public String configTemplate;

    @Column(name = "dependencies", columnDefinition = "TEXT")
    public String dependencies;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "source_id")
    public AgentSource source;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    public List<AgentCommand> commands = new ArrayList<>();

    @Column(name = "deployment_count")
    public Integer deploymentCount = 0;

    @Column(name = "success_count")
    public Integer successCount = 0;

    @CreationTimestamp
    public LocalDateTime createdAt;

    @UpdateTimestamp
    public LocalDateTime updatedAt;
}
