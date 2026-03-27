package com.easystation.auth.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "api_key_usage_logs")
@Getter
@Setter
public class ApiKeyUsageLog extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "key_id", nullable = false)
    public UUID keyId;

    @Column(name = "usage_time", nullable = false)
    public LocalDateTime usageTime;

    @Column(name = "client_ip", length = 50)
    public String clientIp;

    @Column(name = "request_method", length = 10)
    public String requestMethod;

    @Column(name = "request_path", length = 500)
    public String requestPath;

    @Column(name = "response_status")
    public Integer responseStatus;

    @Column(name = "response_time_ms")
    public Long responseTimeMs;

    @Column(name = "permission_used", length = 255)
    public String permissionUsed;

    @Column(name = "error_message")
    public String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at")
    public LocalDateTime createdAt;
}