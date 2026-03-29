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
            null,
            "INIT_REVIEW",
            "插件功能完整，符合规范"
        );

        PluginReviewRecord result = reviewService.createReview(create);

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
            null,
            "INIT_REVIEW",
            "查找测试评论"
        );
        PluginReviewRecord created = reviewService.createReview(create);

        // Then find it
        Optional<PluginReviewRecord> found = reviewService.findById(created.id());

        assertTrue(found.isPresent());
        assertEquals(created.id(), found.get().id());
        assertEquals("查找测试评论", found.get().comment());
    }

    @Test
    void testFindByPluginId() {
        // Create test reviews for the same plugin
        PluginReviewRecord.Create create1 = new PluginReviewRecord.Create(
            testPluginId,
            null,
            "INIT_REVIEW",
            "评论一"
        );
        reviewService.createReview(create1);

        PluginReviewRecord.Create create2 = new PluginReviewRecord.Create(
            testPluginId,
            null,
            "INIT_REVIEW",
            "评论二"
        );
        reviewService.createReview(create2);

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
            null,
            "INIT_REVIEW",
            "审批测试评论"
        );
        PluginReviewRecord created = reviewService.createReview(create);

        // Approve the review
        PluginReviewRecord.Approve approve = new PluginReviewRecord.Approve(
            "符合发布标准",
            "passed",
            "compatible",
            "all tests passed"
        );
        PluginReviewRecord approved = reviewService.approve(created.id(), approve);

        assertNotNull(approved);
        assertEquals(ReviewStatus.APPROVED, approved.status());
        assertNotNull(approved.reviewedAt());
    }

    @Test
    void testRejectReview() {
        // First create a review
        PluginReviewRecord.Create create = new PluginReviewRecord.Create(
            testPluginId,
            null,
            "INIT_REVIEW",
            "拒绝测试评论"
        );
        PluginReviewRecord created = reviewService.createReview(create);

        // Reject the review
        PluginReviewRecord.Reject reject = new PluginReviewRecord.Reject("存在严重问题需要修复");
        PluginReviewRecord rejected = reviewService.reject(created.id(), reject);

        assertNotNull(rejected);
        assertEquals(ReviewStatus.REJECTED, rejected.status());
        assertNotNull(rejected.reviewedAt());
    }
}
