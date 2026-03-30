package com.easystation.diagnostic.service;

import com.easystation.diagnostic.domain.DiagnosticRule;
import com.easystation.diagnostic.dto.DiagnosticRuleRecord;
import com.easystation.diagnostic.enums.DiagnosticCategory;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 诊断规则服务
 */
@ApplicationScoped
public class DiagnosticRuleService {

    public List<DiagnosticRuleRecord.Summary> listAll() {
        return DiagnosticRule.<DiagnosticRule>listAll().stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    public List<DiagnosticRuleRecord.Detail> list() {
        return DiagnosticRule.<DiagnosticRule>listAll().stream()
                .map(this::toDetail)
                .collect(Collectors.toList());
    }

    public List<DiagnosticRuleRecord.Summary> listByCategory(String category) {
        DiagnosticCategory cat = DiagnosticCategory.valueOf(category.toUpperCase());
        return DiagnosticRule.<DiagnosticRule>find("category", cat).stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    public DiagnosticRuleRecord.Detail get(String ruleId) {
        DiagnosticRule rule = DiagnosticRule.find("ruleId", ruleId).firstResult();
        if (rule == null) {
            throw new WebApplicationException("Rule not found: " + ruleId, Response.Status.NOT_FOUND);
        }
        return toDetail(rule);
    }

    @Transactional
    public DiagnosticRuleRecord.Detail create(DiagnosticRuleRecord.Create create) {
        DiagnosticRule rule = new DiagnosticRule();
        rule.name = create.name();
        rule.description = create.description();
        rule.category = create.category();
        rule.condition = create.condition();
        rule.severity = create.severity();
        rule.recommendation = create.recommendation();
        rule.enabled = create.enabled();
        rule.persist();
        return toDetail(rule);
    }

    @Transactional
    public DiagnosticRuleRecord.Detail update(String ruleId, DiagnosticRuleRecord.Update update) {
        DiagnosticRule rule = DiagnosticRule.find("ruleId", ruleId).firstResult();
        if (rule == null) {
            throw new WebApplicationException("Rule not found: " + ruleId, Response.Status.NOT_FOUND);
        }
        if (update.name() != null) rule.name = update.name();
        if (update.description() != null) rule.description = update.description();
        if (update.category() != null) rule.category = update.category();
        if (update.condition() != null) rule.condition = update.condition();
        if (update.severity() != null) rule.severity = update.severity();
        if (update.recommendation() != null) rule.recommendation = update.recommendation();
        if (update.enabled() != null) rule.enabled = update.enabled();
        return toDetail(rule);
    }

    @Transactional
    public void delete(String ruleId) {
        long deleted = DiagnosticRule.delete("ruleId", ruleId);
        if (deleted == 0) {
            throw new WebApplicationException("Rule not found: " + ruleId, Response.Status.NOT_FOUND);
        }
    }

    @Transactional
    public void enable(String ruleId) {
        DiagnosticRule rule = DiagnosticRule.find("ruleId", ruleId).firstResult();
        if (rule == null) {
            throw new WebApplicationException("Rule not found: " + ruleId, Response.Status.NOT_FOUND);
        }
        rule.enabled = true;
    }

    @Transactional
    public void disable(String ruleId) {
        DiagnosticRule rule = DiagnosticRule.find("ruleId", ruleId).firstResult();
        if (rule == null) {
            throw new WebApplicationException("Rule not found: " + ruleId, Response.Status.NOT_FOUND);
        }
        rule.enabled = false;
    }

    private DiagnosticRuleRecord.Detail toDetail(DiagnosticRule rule) {
        return new DiagnosticRuleRecord.Detail(
                rule.ruleId,
                rule.name,
                rule.description,
                rule.category,
                rule.condition,
                rule.severity,
                rule.recommendation,
                rule.enabled,
                rule.createdAt,
                rule.updatedAt
        );
    }

    private DiagnosticRuleRecord.Summary toSummary(DiagnosticRule rule) {
        return new DiagnosticRuleRecord.Summary(
                rule.ruleId,
                rule.name,
                rule.category,
                rule.severity,
                rule.enabled
        );
    }
}
