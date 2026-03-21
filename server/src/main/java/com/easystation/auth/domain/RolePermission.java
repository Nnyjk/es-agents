package com.easystation.auth.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sys_role_permission")
@Getter
@Setter
public class RolePermission extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "role_id", nullable = false)
    public UUID roleId;

    @Column(name = "permission_id", nullable = false)
    public UUID permissionId;

    @CreationTimestamp
    @Column(name = "created_at")
    public LocalDateTime createdAt;
}