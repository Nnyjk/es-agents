package com.easystation.common.utils;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 密码强度校验工具
 */
@ApplicationScoped
public class PasswordStrengthUtil {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 128;
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]");

    /**
     * 校验密码强度
     * @param password 待校验的密码
     * @return 校验结果
     */
    public PasswordStrengthResult validate(String password) {
        List<String> errors = new ArrayList<>();
        int score = 0;

        if (password == null || password.isEmpty()) {
            errors.add("密码不能为空");
            return new PasswordStrengthResult(0, "非常弱", errors, false);
        }

        // 长度检查
        if (password.length() < MIN_LENGTH) {
            errors.add("密码长度至少为 " + MIN_LENGTH + " 个字符");
        } else if (password.length() >= 12) {
            score += 2;
        } else {
            score += 1;
        }

        if (password.length() > MAX_LENGTH) {
            errors.add("密码长度不能超过 " + MAX_LENGTH + " 个字符");
        }

        // 字符类型检查
        boolean hasLower = LOWERCASE.matcher(password).find();
        boolean hasUpper = UPPERCASE.matcher(password).find();
        boolean hasDigit = DIGIT.matcher(password).find();
        boolean hasSpecial = SPECIAL.matcher(password).find();

        if (hasLower) score += 1;
        if (hasUpper) score += 1;
        if (hasDigit) score += 1;
        if (hasSpecial) score += 2;

        // 提示信息
        if (!hasLower) errors.add("建议包含小写字母");
        if (!hasUpper) errors.add("建议包含大写字母");
        if (!hasDigit) errors.add("建议包含数字");
        if (!hasSpecial) errors.add("建议包含特殊字符");

        // 计算强度等级
        String level;
        boolean acceptable;
        if (score <= 2) {
            level = "非常弱";
            acceptable = false;
        } else if (score <= 4) {
            level = "弱";
            acceptable = false;
        } else if (score <= 6) {
            level = "中等";
            acceptable = true;
        } else if (score <= 8) {
            level = "强";
            acceptable = true;
        } else {
            level = "非常强";
            acceptable = true;
        }

        // 检查常见弱密码
        if (isCommonPassword(password)) {
            errors.add("密码过于常见，请使用更复杂的密码");
            acceptable = false;
        }

        return new PasswordStrengthResult(score, level, errors, acceptable);
    }

    /**
     * 检查是否为常见弱密码
     */
    private boolean isCommonPassword(String password) {
        String lower = password.toLowerCase();
        String[] commonPasswords = {
            "password", "123456", "12345678", "qwerty", "abc123",
            "monkey", "master", "dragon", "111111", "baseball",
            "iloveyou", "trustno1", "sunshine", "princess", "admin",
            "welcome", "shadow", "superman", "michael", "password1"
        };
        for (String common : commonPasswords) {
            if (lower.equals(common) || lower.contains(common)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 密码强度校验结果
     */
    public record PasswordStrengthResult(
        int score,           // 评分 0-10
        String level,        // 强度等级描述
        List<String> hints,   // 提示信息
        boolean acceptable   // 是否可接受
    ) {}
}