package com.easystation.auth.record;

import com.easystation.system.record.UserRecord;
import java.util.Set;

/**
 * 登录响应
 * @param token JWT Token
 * @param userInfo 用户基本信息
 * @param permissions 权限集合
 */
public record LoginResponse(
    String token,
    UserRecord userInfo,
    PermissionRecord permissions
) {
    /**
     * 权限数据
     * @param menus 菜单编码集合
     * @param actions 操作编码集合
     */
    public record PermissionRecord(
        Set<String> menus,
        Set<String> actions
    ) {}
}
