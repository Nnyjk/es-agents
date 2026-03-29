package com.easystation.agent.validator;

import com.easystation.agent.dto.AgentTemplateRecord;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;

import java.util.regex.Pattern;

/**
 * Agent 模板校验器
 * 负责校验模板数据的格式和必填字段
 */
@ApplicationScoped
public class AgentTemplateValidator {

    private static final Pattern VERSION_PATTERN = Pattern.compile("^\\d+\\.\\d+\\.\\d+(-[a-zA-Z0-9]+)?$");
    private static final int MAX_NAME_LENGTH = 200;
    private static final int MAX_DESCRIPTION_LENGTH = 2000;

    /**
     * 校验模板创建数据
     */
    public void validateCreate(AgentTemplateRecord.Create record) {
        // 名称校验
        if (record.name() == null || record.name().isBlank()) {
            throw new BadRequestException("模板名称不能为空");
        }
        if (record.name().length() > MAX_NAME_LENGTH) {
            throw new BadRequestException("模板名称不能超过 " + MAX_NAME_LENGTH + " 个字符");
        }

        // 描述校验
        if (record.description() != null && record.description().length() > MAX_DESCRIPTION_LENGTH) {
            throw new BadRequestException("描述不能超过 " + MAX_DESCRIPTION_LENGTH + " 个字符");
        }

        // OS 类型校验
        if (record.osType() == null) {
            throw new BadRequestException("操作系统类型不能为空");
        }

        // 分类校验
        if (record.category() == null) {
            throw new BadRequestException("模板分类不能为空");
        }

        // 安装脚本校验
        if (record.installScript() == null || record.installScript().isBlank()) {
            throw new BadRequestException("安装脚本不能为空");
        }
    }

    /**
     * 校验模板更新数据
     */
    public void validateUpdate(AgentTemplateRecord.Update record) {
        // 名称校验（如果提供）
        if (record.name() != null && record.name().length() > MAX_NAME_LENGTH) {
            throw new BadRequestException("模板名称不能超过 " + MAX_NAME_LENGTH + " 个字符");
        }

        // 描述校验（如果提供）
        if (record.description() != null && record.description().length() > MAX_DESCRIPTION_LENGTH) {
            throw new BadRequestException("描述不能超过 " + MAX_DESCRIPTION_LENGTH + " 个字符");
        }
    }

    /**
     * 校验版本号格式
     *
     * @param version 版本号
     * @throws BadRequestException 校验失败时抛出
     */
    public void validateVersion(String version) {
        if (version == null || version.isBlank()) {
            throw new BadRequestException("版本号不能为空");
        }
        if (!VERSION_PATTERN.matcher(version).matches()) {
            throw new BadRequestException("版本号格式不正确，应为语义化版本 (如 1.0.0, 2.1.0-beta)");
        }
    }

    /**
     * 校验 YAML/JSON 格式
     *
     * @param content 内容
     * @param format 格式 (YAML 或 JSON)
     * @throws BadRequestException 校验失败时抛出
     */
    public void validateFormat(String content, String format) {
        if (content == null || content.isBlank()) {
            throw new BadRequestException(format + " 内容不能为空");
        }

        // 简单的格式校验
        if ("JSON".equalsIgnoreCase(format)) {
            if (!content.trim().startsWith("{") && !content.trim().startsWith("[")) {
                throw new BadRequestException("JSON 格式不正确");
            }
        } else if ("YAML".equalsIgnoreCase(format)) {
            // YAML 没有简单的校验方式，至少检查是否为空
            if (content.contains("\t")) {
                throw new BadRequestException("YAML 不支持 Tab 缩进，请使用空格");
            }
        }
    }
}