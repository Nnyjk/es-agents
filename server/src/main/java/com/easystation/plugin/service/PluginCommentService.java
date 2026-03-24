package com.easystation.plugin.service;

import com.easystation.plugin.dto.PluginCommentRecord;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PluginCommentService {

    PluginCommentRecord create(PluginCommentRecord.Create create, UUID userId);

    PluginCommentRecord update(UUID id, PluginCommentRecord.Update update);

    Optional<PluginCommentRecord> findById(UUID id);

    List<PluginCommentRecord> findByPluginId(UUID pluginId);

    List<PluginCommentRecord> findRootComments(UUID pluginId);

    List<PluginCommentRecord> findReplies(UUID parentId);

    List<PluginCommentRecord> search(PluginCommentRecord.Query query);

    PluginCommentRecord pin(UUID id);

    PluginCommentRecord unpin(UUID id);

    PluginCommentRecord hide(UUID id);

    PluginCommentRecord show(UUID id);

    PluginCommentRecord markAsDeveloperReply(UUID id);

    void like(UUID id);

    void unlike(UUID id);

    void delete(UUID id);

    long countByPluginId(UUID pluginId);

    long countReplies(UUID parentId);
}