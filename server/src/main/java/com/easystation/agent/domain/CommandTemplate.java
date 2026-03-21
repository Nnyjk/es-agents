package com.easystation.agent.domain;

import com.easystation.agent.domain.enums.CommandCategory;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "command_template")
@Getter
@Setter
public class CommandTemplate extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(nullable = false)
    public String name;

    @Column(columnDefinition = "TEXT")
    public String description;

    @Column(nullable = false, columnDefinition = "TEXT")
    public String script;

    @Enumerated(EnumType.STRING)
    public CommandCategory category = CommandCategory.CUSTOM;

    @Column(columnDefinition = "TEXT")
    public String tags; // JSON array of tags

    @Column(columnDefinition = "TEXT")
    public String parameters; // JSON schema for parameters

    public Long timeout = 300L; // seconds, default 5 minutes

    @Column(name = "retry_count")
    public Integer retryCount = 0;

    @Column(name = "is_active")
    public Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    @Column(name = "created_by")
    public String createdBy;

    /**
     * Find all active command templates.
     */
    public static java.util.List<CommandTemplate> listActive() {
        return list("isActive", true);
    }

    /**
     * Find by category.
     */
    public static java.util.List<CommandTemplate> findByCategory(CommandCategory category) {
        return list("category and isActive = true", category);
    }

    /**
     * Find by name (for uniqueness check).
     */
    public static CommandTemplate findByName(String name) {
        return find("name", name).firstResult();
    }
}