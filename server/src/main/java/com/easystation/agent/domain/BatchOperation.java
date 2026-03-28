package com.easystation.agent.domain;

import com.easystation.agent.domain.enums.BatchOperationStatus;
import com.easystation.agent.domain.enums.OperationType;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "batch_operation")
@Getter
@Setter
public class BatchOperation extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false)
    public OperationType operationType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public BatchOperationStatus status = BatchOperationStatus.PENDING;

    @Column(name = "operator_id", nullable = false)
    public UUID operatorId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "completed_at")
    public LocalDateTime completedAt;

    @Column(name = "total_items", nullable = false)
    public Integer totalItems = 0;

    @Column(name = "success_count")
    public Integer successCount = 0;

    @Column(name = "failed_count")
    public Integer failedCount = 0;

    @OneToMany(mappedBy = "batchOperation", cascade = CascadeType.ALL)
    public List<BatchOperationItem> items = new ArrayList<>();

    /**
     * Find batch operations by status.
     */
    public static List<BatchOperation> findByStatus(BatchOperationStatus status) {
        return find("status = ?1 order by createdAt desc", status).list();
    }

    /**
     * Find batch operations by operator.
     */
    public static List<BatchOperation> findByOperatorId(UUID operatorId) {
        return find("operatorId = ?1 order by createdAt desc", operatorId).list();
    }

    /**
     * Find recent batch operations.
     */
    public static List<BatchOperation> findRecent(int limit) {
        return find("order by createdAt desc").page(0, limit).list();
    }
}