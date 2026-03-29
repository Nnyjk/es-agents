package com.easystation.search.repository;

import com.easystation.search.domain.SearchResult;
import com.easystation.search.domain.SearchSuggestion;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 搜索 Repository - 负责跨模块搜索查询
 */
@ApplicationScoped
public class SearchRepository {

    @PersistenceContext
    EntityManager entityManager;

    /**
     * 统一搜索
     * @param query 搜索关键词
     * @param types 搜索类型列表（host, deployment, config, log）
     * @param limit 结果数量限制
     * @param offset 偏移量
     * @return 搜索结果列表
     */
    @Transactional
    public List<SearchResult> search(String query, List<String> types, int limit, int offset) {
        List<SearchResult> results = new ArrayList<>();
        
        String searchQuery = query.toLowerCase();
        
        // 搜索主机
        if (types.isEmpty() || types.contains("host")) {
            List<Object[]> hostResults = entityManager.createQuery(
                "SELECT h.id, h.name, h.identifier, h.description FROM Host h " +
                "WHERE LOWER(h.name) LIKE :query OR LOWER(h.identifier) LIKE :query OR LOWER(h.description) LIKE :query",
                Object[].class)
                .setParameter("query", "%" + searchQuery + "%")
                .setMaxResults(limit)
                .setFirstResult(offset)
                .getResultList();
            
            for (Object[] row : hostResults) {
                SearchResult result = new SearchResult(
                    "host",
                    ((java.util.UUID) row[0]).toString(),
                    (String) row[1],
                    (String) row[3],
                    0.0,
                    null
                );
                results.add(result);
            }
        }
        
        // 搜索应用
        if (types.isEmpty() || types.contains("deployment")) {
            List<Object[]> appResults = entityManager.createQuery(
                "SELECT a.id, a.name, a.project, a.description FROM DeploymentApplication a " +
                "WHERE LOWER(a.name) LIKE :query OR LOWER(a.project) LIKE :query OR LOWER(a.description) LIKE :query",
                Object[].class)
                .setParameter("query", "%" + searchQuery + "%")
                .setMaxResults(limit)
                .setFirstResult(offset)
                .getResultList();
            
            for (Object[] row : appResults) {
                SearchResult result = new SearchResult(
                    "deployment",
                    ((java.util.UUID) row[0]).toString(),
                    (String) row[1],
                    (String) row[3],
                    0.0,
                    null
                );
                results.add(result);
            }
        }
        
        // 搜索配置
        if (types.isEmpty() || types.contains("config")) {
            List<Object[]> configResults = entityManager.createQuery(
                "SELECT c.id, c.key, c.description, c.type FROM ConfigItem c " +
                "WHERE LOWER(c.key) LIKE :query OR LOWER(c.description) LIKE :query",
                Object[].class)
                .setParameter("query", "%" + searchQuery + "%")
                .setMaxResults(limit)
                .setFirstResult(offset)
                .getResultList();
            
            for (Object[] row : configResults) {
                SearchResult result = new SearchResult(
                    "config",
                    ((java.util.UUID) row[0]).toString(),
                    (String) row[1],
                    (String) row[2],
                    0.0,
                    null
                );
                results.add(result);
            }
        }
        
        // 搜索审计日志
        if (types.isEmpty() || types.contains("log")) {
            List<Object[]> logResults = entityManager.createQuery(
                "SELECT a.id, a.description, a.username, a.action FROM AuditLog a " +
                "WHERE LOWER(a.description) LIKE :query OR LOWER(a.username) LIKE :query",
                Object[].class)
                .setParameter("query", "%" + searchQuery + "%")
                .setMaxResults(limit)
                .setFirstResult(offset)
                .getResultList();
            
            for (Object[] row : logResults) {
                SearchResult result = new SearchResult(
                    "log",
                    ((java.util.UUID) row[0]).toString(),
                    (String) row[1],
                    (String) row[2],
                    0.0,
                    null
                );
                results.add(result);
            }
        }
        
        return results;
    }

    /**
     * 获取搜索建议
     * @param query 搜索关键词
     * @param limit 建议数量
     * @return 搜索建议列表
     */
    public List<SearchSuggestion> getSuggestions(String query, int limit) {
        List<SearchSuggestion> suggestions = new ArrayList<>();
        
        String searchQuery = query.toLowerCase();
        
        // 从主机获取建议
        List<String> hostNames = entityManager.createQuery(
            "SELECT h.name FROM Host h WHERE LOWER(h.name) LIKE :query",
            String.class)
            .setParameter("query", searchQuery + "%")
            .setMaxResults(limit)
            .getResultList();
        
        for (String name : hostNames) {
            suggestions.add(new SearchSuggestion(name, "host"));
        }
        
        // 从应用获取建议
        List<String> appNames = entityManager.createQuery(
            "SELECT a.name FROM DeploymentApplication a WHERE LOWER(a.name) LIKE :query",
            String.class)
            .setParameter("query", searchQuery + "%")
            .setMaxResults(limit)
            .getResultList();
        
        for (String name : appNames) {
            suggestions.add(new SearchSuggestion(name, "deployment"));
        }
        
        // 从配置获取建议
        List<String> configKeys = entityManager.createQuery(
            "SELECT c.key FROM ConfigItem c WHERE LOWER(c.key) LIKE :query",
            String.class)
            .setParameter("query", searchQuery + "%")
            .setMaxResults(limit)
            .getResultList();
        
        for (String key : configKeys) {
            suggestions.add(new SearchSuggestion(key, "config"));
        }
        
        return suggestions;
    }

    /**
     * 统计搜索结果数量
     * @param query 搜索关键词
     * @param types 搜索类型列表
     * @return 总结果数量
     */
    @Transactional
    public long countResults(String query, List<String> types) {
        long total = 0;
        String searchQuery = query.toLowerCase();
        
        if (types.isEmpty() || types.contains("host")) {
            total += entityManager.createQuery(
                "SELECT COUNT(h) FROM Host h " +
                "WHERE LOWER(h.name) LIKE :query OR LOWER(h.identifier) LIKE :query OR LOWER(h.description) LIKE :query",
                Long.class)
                .setParameter("query", "%" + searchQuery + "%")
                .getSingleResult();
        }
        
        if (types.isEmpty() || types.contains("deployment")) {
            total += entityManager.createQuery(
                "SELECT COUNT(a) FROM DeploymentApplication a " +
                "WHERE LOWER(a.name) LIKE :query OR LOWER(a.project) LIKE :query OR LOWER(a.description) LIKE :query",
                Long.class)
                .setParameter("query", "%" + searchQuery + "%")
                .getSingleResult();
        }
        
        if (types.isEmpty() || types.contains("config")) {
            total += entityManager.createQuery(
                "SELECT COUNT(c) FROM ConfigItem c " +
                "WHERE LOWER(c.key) LIKE :query OR LOWER(c.description) LIKE :query",
                Long.class)
                .setParameter("query", "%" + searchQuery + "%")
                .getSingleResult();
        }
        
        if (types.isEmpty() || types.contains("log")) {
            total += entityManager.createQuery(
                "SELECT COUNT(a) FROM AuditLog a " +
                "WHERE LOWER(a.description) LIKE :query OR LOWER(a.username) LIKE :query",
                Long.class)
                .setParameter("query", "%" + searchQuery + "%")
                .getSingleResult();
        }
        
        return total;
    }
}
