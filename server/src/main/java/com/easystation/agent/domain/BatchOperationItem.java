package com.easystation.agent.domain;

import com.easystation.agent.domain.enums.BatchOperationStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "batch_operation_item")
@Getter
@Setter
public class BatchOperationItem extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_operation_id", nullable = false)
    public BatchOperation batchOperation;

    @Column(name = "target_id", nullable = false)
    public UUID targetId;

    @Column(name = "target_type", nullable = false)
    public String targetType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public BatchOperationStatus status = BatchOperationStatus.PENDING;

    @Column(name = "error_message", columnDefinition = "TEXT")
    public String errorMessage;

    @Column(name = "started_at")
    public LocalDateTime startedAt;

    @Column(name = "completed_at")
    public LocalDateTime completedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    /**
     * Find items by batch operation.
     */
    public static java.util.List<BatchOperationItem> findByBatchOperationId(UUID batchOperationId) {
        return find("batchOperation.id = ?1 order by createdAt", batchOperationId).list();
    }

    /**
     * Find items by status.
     */
    public static java.util.List<BatchOperationItem> findByStatus(BatchOperationStatus status) {
        return find("status = ?1 order by createdAt", status).list();
    }

    /**
     * Count items by batch operation and status.
     */
    public static long countByBatchOperationIdAndStatus(UUID batchOperationId, BatchOperationStatus status) {
        return count("batchOperation.id = ?1 and status = ?2", batchOperationId, status);
    }
}