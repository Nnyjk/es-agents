package com.easystation.profile.dto;

import com.easystation.system.domain.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record ProfileRecord(
    UUID id,
    String username,
    String email,
    String phone,
    String nickname,
    String avatar,
    Boolean mfaEnabled,
    UserStatus status,
    Set<RoleInfo> roles,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public record RoleInfo(
        UUID id,
        String code,
        String name
    ) {}

    public record Update(
        @Email(message = "Invalid email format")
        String email,
        
        @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "Invalid phone format")
        String phone,
        
        @Size(max = 255, message = "Nickname too long")
        String nickname,
        
        @Size(max = 500, message = "Avatar URL too long")
        String avatar
    ) {}

    public record PasswordChange(
        @NotBlank(message = "Current password is required")
        String currentPassword,
        
        @NotBlank(message = "New password is required")
        @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", 
                 message = "Password must contain at least one uppercase letter, one lowercase letter, and one digit")
        String newPassword
    ) {}
}