package com.easystation.export.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "export_task")
@Getter
@Setter
public class ExportTask extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "user_id", nullable = false)
    public UUID userId;

    @Column(name = "export_type", nullable = false, length = 20)
    public String exportType;

    @Column(name = "data_type", nullable = false, length = 50)
    public String dataType;

    @Column(nullable = false, length = 20)
    public String status;

    @Column(name = "file_path", length = 500)
    public String filePath;

    @Column(name = "file_name", length = 255)
    public String fileName;

    @Column(name = "total_records")
    public Integer totalRecords;

    @Column(name = "error_message", columnDefinition = "TEXT")
    public String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at")
    public LocalDateTime createdAt;

    @Column(name = "completed_at")
    public LocalDateTime completedAt;

    @Column(name = "query_params", columnDefinition = "TEXT")
    public String queryParams;
}