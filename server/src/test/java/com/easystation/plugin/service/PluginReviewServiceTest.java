package com.easystation.plugin.service;

import com.easystation.plugin.dto.PluginReviewRecord;
import com.easystation.plugin.domain.enums.ReviewStatus;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class PluginReviewServiceTest {

    @Inject
    PluginReviewService reviewService;

    private UUID testPluginId;
    private UUID testReviewerId;
    private String testReviewerName;

    @BeforeEach
    void setUp() {
        testPluginId = UUID.randomUUID();
        testReviewerId = UUID.randomUUID();
        testReviewerName = "test-reviewer";
    }

    @Test
    void testCreateReview() {
        PluginReviewRecord.Create create = new PluginReviewRecord.Create(
            testPluginId,
            "插件功能完整，符合规范",
            ReviewStatus.PENDING
        );

        PluginReviewRecord result = reviewService.create(create);

        assertNotNull(result);
        assertNotNull(result.id());
        assertEquals(testPluginId, result.pluginId());
        assertEquals(ReviewStatus.PENDING, result.status());
    }

    @Test
    void testFindById() {
        // First create a review
        PluginReviewRecord.Create create = new PluginReviewRecord.Create(
            testPluginId,
            "查找测试评论",
            ReviewStatus.PENDING
        );
        PluginReviewRecord created = reviewService.create(create);

        // Then find it
        Optional<PluginReviewRecord> found = reviewService.findById(created.id());

        assertTrue(found.isPresent());
        assertEquals(created.id(), found.get().id());
        assertEquals("查找测试评论", found.get().content());
    }

    @Test
    void testFindByPluginId() {
        // Create test reviews for the same plugin
        PluginReviewRecord.Create create1 = new PluginReviewRecord.Create(
            testPluginId,
            "评论一",
            ReviewStatus.PENDING
        );
        reviewService.create(create1);

        PluginReviewRecord.Create create2 = new PluginReviewRecord.Create(
            testPluginId,
            "评论二",
            ReviewStatus.PENDING
        );
        reviewService.create(create2);

        // Find reviews by plugin ID
        List<PluginReviewRecord> reviews = reviewService.findByPluginId(testPluginId);

        assertNotNull(reviews);
        assertTrue(reviews.size() >= 2);
        assertTrue(reviews.stream().allMatch(r -> r.pluginId().equals(testPluginId)));
    }

    @Test
    void testApproveReview() {
        // First create a review
        PluginReviewRecord.Create create = new PluginReviewRecord.Create(
            testPluginId,
            "审批测试评论",
            ReviewStatus.PENDING
        );
        PluginReviewRecord created = reviewService.create(create);

        // Approve the review
        PluginReviewRecord approved = reviewService.approve(created.id(), "符合发布标准");

        assertNotNull(approved);
        assertEquals(ReviewStatus.APPROVED, approved.status());
        assertNotNull(approved.approvedAt());
    }

    @Test
    void testRejectReview() {
        // First create a review
        PluginReviewRecord.Create create = new PluginReviewRecord.Create(
            testPluginId,
            "拒绝测试评论",
            ReviewStatus.PENDING
        );
        PluginReviewRecord created = reviewService.create(create);

        // Reject the review
        PluginReviewRecord rejected = reviewService.reject(created.id(), "存在严重问题需要修复");

        assertNotNull(rejected);
        assertEquals(ReviewStatus.REJECTED, rejected.status());
        assertNotNull(rejected.rejectedAt());
        assertEquals("存在严重问题需要修复", rejected.rejectionReason());
    }
}
