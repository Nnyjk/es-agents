package com.easystation.notification.service;

import com.easystation.notification.domain.NotificationMessage;
import com.easystation.notification.dto.NotificationRecord;
import com.easystation.notification.enums.MessageLevel;
import com.easystation.notification.enums.MessageType;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class NotificationMessageServiceTest {

    @Inject
    NotificationMessageService notificationService;

    private UUID testUserId;
    private String testUsername;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUsername = "testuser";
    }

    @Test
    void testCreateNotification() {
        NotificationRecord.Create createRequest = new NotificationRecord.Create(
                testUserId,
                testUsername,
                "测试通知",
                "这是一条测试消息内容",
                MessageType.SYSTEM,
                MessageLevel.INFO,
                null,
                null,
                null
        );

        NotificationMessage message = notificationService.create(createRequest);

        assertNotNull(message);
        assertNotNull(message.id);
        assertEquals(testUserId, message.userId);
        assertEquals("测试通知", message.title);
        assertEquals("这是一条测试消息内容", message.content);
        assertEquals(MessageType.SYSTEM, message.type);
        assertEquals(MessageLevel.INFO, message.level);
        assertFalse(message.isRead);
        assertNotNull(message.createdAt);
    }

    @Test
    void testFindById() {
        // 先创建一条消息
        NotificationRecord.Create createRequest = new NotificationRecord.Create(
                testUserId,
                testUsername,
                "查找测试",
                "测试内容",
                MessageType.SYSTEM,
                MessageLevel.INFO,
                null,
                null,
                null
        );
        NotificationMessage created = notificationService.create(createRequest);

        // 查找
        NotificationMessage found = notificationService.findById(created.id);

        assertNotNull(found);
        assertEquals(created.id, found.id);
        assertEquals("查找测试", found.title);
    }

    @Test
    void testFindAll() {
        // 先创建几条消息
        for (int i = 0; i < 3; i++) {
            NotificationRecord.Create createRequest = new NotificationRecord.Create(
                    testUserId,
                    testUsername,
                    "批量测试" + i,
                    "内容" + i,
                    MessageType.SYSTEM,
                    MessageLevel.INFO,
                    null,
                    null,
                    null
            );
            notificationService.create(createRequest);
        }

        // 查询所有
        List<NotificationMessage> messages = notificationService.findAll(testUserId, null, null, null, null, null, 0, 10);

        assertNotNull(messages);
        assertTrue(messages.size() >= 3);
    }

    @Test
    void testMarkAsRead() {
        // 先创建一条消息
        NotificationRecord.Create createRequest = new NotificationRecord.Create(
                testUserId,
                testUsername,
                "已读测试",
                "测试内容",
                MessageType.SYSTEM,
                MessageLevel.INFO,
                null,
                null,
                null
        );
        NotificationMessage created = notificationService.create(createRequest);

        // 标记为已读
        notificationService.markAsRead(created.id);

        // 验证
        NotificationMessage updated = notificationService.findById(created.id);
        assertTrue(updated.isRead);
        assertNotNull(updated.readAt);
    }

    @Test
    void testMarkBatchAsRead() {
        // 先创建几条消息
        List<UUID> messageIds = new java.util.ArrayList<>();
        for (int i = 0; i < 3; i++) {
            NotificationRecord.Create createRequest = new NotificationRecord.Create(
                    testUserId,
                    testUsername,
                    "批量已读测试" + i,
                    "内容" + i,
                    MessageType.SYSTEM,
                    MessageLevel.INFO,
                    null,
                    null,
                    null
            );
            NotificationMessage created = notificationService.create(createRequest);
            messageIds.add(created.id);
        }

        // 批量标记为已读
        notificationService.markBatchAsRead(messageIds);

        // 验证
        for (UUID id : messageIds) {
            NotificationMessage message = notificationService.findById(id);
            assertTrue(message.isRead);
        }
    }

    @Test
    void testDelete() {
        // 先创建一条消息
        NotificationRecord.Create createRequest = new NotificationRecord.Create(
                testUserId,
                testUsername,
                "删除测试",
                "测试内容",
                MessageType.SYSTEM,
                MessageLevel.INFO,
                null,
                null,
                null
        );
        NotificationMessage created = notificationService.create(createRequest);

        // 删除
        notificationService.delete(created.id);

        // 验证已删除
        NotificationMessage deleted = notificationService.findById(created.id);
        assertNull(deleted);
    }

    @Test
    void testDeleteBatch() {
        // 先创建几条消息
        List<UUID> messageIds = new java.util.ArrayList<>();
        for (int i = 0; i < 3; i++) {
            NotificationRecord.Create createRequest = new NotificationRecord.Create(
                    testUserId,
                    testUsername,
                    "批量删除测试" + i,
                    "内容" + i,
                    MessageType.SYSTEM,
                    MessageLevel.INFO,
                    null,
                    null,
                    null
            );
            NotificationMessage created = notificationService.create(createRequest);
            messageIds.add(created.id);
        }

        // 批量删除
        notificationService.deleteBatch(messageIds);

        // 验证已删除
        for (UUID id : messageIds) {
            NotificationMessage message = notificationService.findById(id);
            assertNull(message);
        }
    }

    @Test
    void testGetUnreadCount() {
        // 先创建几条消息
        for (int i = 0; i < 5; i++) {
            NotificationRecord.Create createRequest = new NotificationRecord.Create(
                    testUserId,
                    testUsername,
                    "未读数测试" + i,
                    "内容" + i,
                    MessageType.SYSTEM,
                    MessageLevel.INFO,
                    null,
                    null,
                    null
            );
            notificationService.create(createRequest);
        }

        // 获取未读数
        int unreadCount = notificationService.getUnreadCount(testUserId);

        assertTrue(unreadCount >= 5);
    }

    @Test
    void testGetStatistics() {
        // 先创建几条不同类型的消息
        notificationService.create(new NotificationRecord.Create(
                testUserId, testUsername, "系统消息", "内容",
                MessageType.SYSTEM, MessageLevel.INFO, null, null, null
        ));
        notificationService.create(new NotificationRecord.Create(
                testUserId, testUsername, "告警消息", "内容",
                MessageType.ALERT, MessageLevel.WARNING, null, null, null
        ));
        notificationService.create(new NotificationRecord.Create(
                testUserId, testUsername, "操作消息", "内容",
                MessageType.OPERATION, MessageLevel.INFO, null, null, null
        ));

        // 获取统计
        Map<String, Object> statistics = notificationService.getStatistics(testUserId);

        assertNotNull(statistics);
        assertTrue((int) statistics.get("totalCount") >= 3);
        assertNotNull(statistics.get("unreadCount"));
        assertNotNull(statistics.get("typeDistribution"));
        assertNotNull(statistics.get("levelDistribution"));
    }
}
