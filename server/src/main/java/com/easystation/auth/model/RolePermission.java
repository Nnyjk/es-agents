package com.easystation.auth.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.util.UUID;

/**
 * 角色权限关联实体
 */
@Entity(name = "AuthModelRolePermission")
@Table(name = "role_permissions", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"role_id", "permission_id"}))
public class RolePermission extends PanacheEntityBase {

    @Id
    @Column(columnDefinition = "uuid")
    public UUID id;

    /**
     * 角色 ID
     */
    @Column(name = "role_id", nullable = false, columnDefinition = "uuid")
    public UUID roleId;

    /**
     * 权限 ID
     */
    @Column(name = "permission_id", nullable = false, columnDefinition = "uuid")
    public UUID permissionId;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
