package com.easystation.alert.enums;

/**
 * 告警渠道类型
 */
public enum AlertChannelType {
    EMAIL,          // 邮件
    WECHAT_WORK,     // 企业微信
    DINGTALK,        // 钉钉
    WEBHOOK,         // 自定义 Webhook
    SMS              // 短信
}