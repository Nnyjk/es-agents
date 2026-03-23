package com.easystation.profile.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_audit_log")
@Getter
@Setter
public class UserAuditLog extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "user_id", nullable = false)
    public UUID userId;

    @Column(nullable = false, length = 100)
    public String action;

    @Column(name = "resource_type", length = 100)
    public String resourceType;

    @Column(name = "resource_id")
    public String resourceId;

    @Column(columnDefinition = "TEXT")
    public String description;

    @Column(name = "ip_address", length = 50)
    public String ipAddress;

    @Column(name = "user_agent", length = 500)
    public String userAgent;

    @Column(length = 20)
    public String status = "SUCCESS";

    @Column(name = "error_message", columnDefinition = "TEXT")
    public String errorMessage;

    @Column(name = "request_data", columnDefinition = "TEXT")
    public String requestData;

    @Column(name = "response_data", columnDefinition = "TEXT")
    public String responseData;

    @Column(name = "duration_ms")
    public Long durationMs;

    @CreationTimestamp
    @Column(name = "created_at")
    public LocalDateTime createdAt;
}