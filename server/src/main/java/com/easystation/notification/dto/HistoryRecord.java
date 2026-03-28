package com.easystation.notification.dto;

import com.easystation.notification.enums.NotificationStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record HistoryRecord(
    UUID id,
    UUID channelId,
    UUID templateId,
    String recipient,
    String title,
    String content,
    NotificationStatus status,
    LocalDateTime sentAt,
    String errorMessage,
    Integer retryCount,
    LocalDateTime createdAt
) {
    /**
     * 查询参数类（普通 class 以支持 @BeanParam）
     */
    public static class Query {
        private UUID channelId;
        private NotificationStatus status;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Integer limit;
        private Integer offset;

        public Query() {}

        public UUID getChannelId() { return channelId; }
        public void setChannelId(UUID channelId) { this.channelId = channelId; }

        public NotificationStatus getStatus() { return status; }
        public void setStatus(NotificationStatus status) { this.status = status; }

        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

        public Integer getLimit() { return limit; }
        public void setLimit(Integer limit) { this.limit = limit; }

        public Integer getOffset() { return offset; }
        public void setOffset(Integer offset) { this.offset = offset; }
    }
}
