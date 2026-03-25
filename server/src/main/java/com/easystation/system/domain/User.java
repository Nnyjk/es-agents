package com.easystation.system.domain;

import com.easystation.system.domain.enums.UserStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "sys_user")
@Getter
@Setter
public class User extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(unique = true, nullable = false)
    public String username;

    @Column(nullable = false)
    public String password;

    @Column
    public String email;

    @Column(length = 50)
    public String phone;

    @Column
    public String nickname;

    @Column(length = 500)
    public String avatar;

    @Column
    public Boolean mfaEnabled = false;

    @Column
    public String mfaSecret;

    @Enumerated(EnumType.STRING)
    public UserStatus status = UserStatus.ACTIVE;

    @Column(name = "password_changed_at")
    public LocalDateTime passwordChangedAt;

    @Column(name = "last_login_at")
    public LocalDateTime lastLoginAt;

    @CreationTimestamp
    public LocalDateTime createdAt;

    @UpdateTimestamp
    public LocalDateTime updatedAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "sys_user_role",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    public Set<Role> roles = new HashSet<>();
}
