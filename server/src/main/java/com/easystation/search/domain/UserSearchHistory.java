package com.easystation.search.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 用户搜索历史实体
 */
@Entity
@Table(name = "user_search_history")
public class UserSearchHistory extends PanacheEntityBase {

    @Id
    @Column(name = "id", nullable = false)
    public UUID id;

    @Column(name = "user_id", nullable = false)
    public UUID userId;

    @Column(name = "query", nullable = false, length = 500)
    public String query;

    @Column(name = "result_count")
    public Integer resultCount;

    @Column(name = "created_at")
    public LocalDateTime createdAt;

    /**
     * 查找指定用户的搜索历史
     */
    public static java.util.List<UserSearchHistory> findByUserId(UUID userId, int limit) {
        return find("userId = ?1 order by createdAt desc", userId)
            .page(0, limit)
            .list();
    }

    /**
     * 查找指定用户的搜索历史（按查询关键词）
     */
    public static java.util.List<UserSearchHistory> findByUserIdAndQuery(UUID userId, String query) {
        return find("userId = ?1 AND query LIKE ?2", userId, "%" + query + "%").list();
    }

    /**
     * 删除指定用户的搜索历史
     */
    public static long deleteByUserId(UUID userId) {
        return delete("userId = ?1", userId);
    }

    /**
     * 删除指定的搜索历史
     */
    public static long deleteByUserIdAndId(UUID userId, UUID id) {
        return delete("userId = ?1 AND id = ?2", userId, id);
    }
}
