package com.easystation.auth.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sys_api_token_access_log")
@Getter
@Setter
public class ApiTokenAccessLog extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "token_id", nullable = false)
    public UUID tokenId;

    @Column(name = "access_time", nullable = false)
    public LocalDateTime accessTime;

    @Column(name = "client_ip")
    public String clientIp;

    @Column(name = "request_method")
    public String requestMethod;

    @Column(name = "request_path")
    public String requestPath;

    @Column(name = "response_status")
    public Integer responseStatus;

    @Column(name = "response_time_ms")
    public Long responseTimeMs;

    @Column(columnDefinition = "TEXT")
    public String requestBody;

    @Column(columnDefinition = "TEXT")
    public String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at")
    public LocalDateTime createdAt;
}