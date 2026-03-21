package com.easystation.alert.domain;

import com.easystation.alert.enums.AlertChannelType;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 告警渠道配置
 */
@Entity
@Table(name = "alert_channel")
@Getter
@Setter
public class AlertChannel extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(nullable = false)
    public String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public AlertChannelType type;

    /**
     * 渠道配置（JSON 格式）
     * EMAIL: {smtpHost, smtpPort, smtpUser, smtpPassword, from}
     * WECHAT_WORK: {webhookUrl}
     * DINGTALK: {webhookUrl, secret}
     * WEBHOOK: {url, method, headers}
     * SMS: {provider, apiKey, apiSecret}
     */
    @Column(columnDefinition = "TEXT")
    public String config;

    /**
     * 接收者列表（JSON 数组）
     * EMAIL: ["email1@example.com", "email2@example.com"]
     * WECHAT_WORK/DINGTALK: ["user1", "user2"]
     */
    @Column(columnDefinition = "TEXT")
    public String receivers;

    public boolean enabled = true;

    @CreationTimestamp
    public LocalDateTime createdAt;

    @UpdateTimestamp
    public LocalDateTime updatedAt;
}