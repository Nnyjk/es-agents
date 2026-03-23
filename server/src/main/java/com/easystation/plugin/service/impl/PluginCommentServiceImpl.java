package com.easystation.plugin.service.impl;

import com.easystation.plugin.domain.Plugin;
import com.easystation.plugin.domain.PluginComment;
import com.easystation.plugin.dto.PluginCommentRecord;
import com.easystation.plugin.mapper.PluginCommentMapper;
import com.easystation.plugin.repository.PluginCommentRepository;
import com.easystation.plugin.repository.PluginRepository;
import com.easystation.plugin.service.PluginCommentService;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class PluginCommentServiceImpl implements PluginCommentService {

    @Inject
    PluginCommentRepository commentRepository;

    @Inject
    PluginRepository pluginRepository;

    @Inject
    PluginCommentMapper commentMapper;

    @Override
    @Transactional
    public PluginCommentRecord create(PluginCommentRecord.Create create, UUID userId) {
        // Validate plugin exists
        Plugin plugin = pluginRepository.findById(create.pluginId())
            .orElseThrow(() -> new NotFoundException("Plugin not found: " + create.pluginId()));

        PluginComment comment = new PluginComment();
        comment.pluginId = plugin.id;
        comment.userId = userId;
        comment.content = create.content();
        comment.isDeveloperReply = create.isDeveloperReply() != null ? create.isDeveloperReply() : false;
        comment.isPinned = false;
        comment.isHidden = false;
        comment.likeCount = 0;
        comment.replyCount = 0;
        comment.createdAt = LocalDateTime.now();
        comment.updatedAt = LocalDateTime.now();

        // Handle reply
        if (create.parentId() != null) {
            PluginComment parent = commentRepository.findById(create.parentId())
                .orElseThrow(() -> new NotFoundException("Parent comment not found: " + create.parentId()));
            comment.parentId = parent.id;
            comment.replyToUserId = create.replyToUserId();
        }

        commentRepository.persist(comment);

        // Update plugin comment count
        plugin.commentCount = plugin.commentCount + 1;
        pluginRepository.persist(plugin);

        return commentMapper.toRecord(comment);
    }

    @Override
    @Transactional
    public PluginCommentRecord update(UUID id, PluginCommentRecord.Update update) {
        PluginComment comment = commentRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Comment not found: " + id));

        if (update.content() != null) {
            comment.content = update.content();
        }
        comment.updatedAt = LocalDateTime.now();

        commentRepository.persist(comment);
        return commentMapper.toRecord(comment);
    }

    @Override
    public Optional<PluginCommentRecord> findById(UUID id) {
        return commentRepository.findById(id)
            .map(commentMapper::toRecord);
    }

    @Override
    public List<PluginCommentRecord> findByPluginId(UUID pluginId, Boolean includeReplies, int page, int size) {
        if (includeReplies != null && includeReplies) {
            return commentRepository.findByPluginIdOrderByCreatedAtDesc(pluginId, Page.of(page, size)).stream()
                .map(commentMapper::toRecord)
                .collect(Collectors.toList());
        } else {
            return commentRepository.findRootCommentsByPluginId(pluginId, Page.of(page, size)).stream()
                .map(commentMapper::toRecord)
                .collect(Collectors.toList());
        }
    }

    @Override
    public List<PluginCommentRecord> findByUserId(UUID userId) {
        return commentRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
            .map(commentMapper::toRecord)
            .collect(Collectors.toList());
    }

    @Override
    public List<PluginCommentRecord> findReplies(UUID parentId) {
        return commentRepository.findByParentIdOrderByCreatedAtAsc(parentId).stream()
            .map(commentMapper::toRecord)
            .collect(Collectors.toList());
    }

    @Override
    public long countByPluginId(UUID pluginId) {
        return commentRepository.countByPluginId(pluginId);
    }

    @Override
    @Transactional
    public PluginCommentRecord pin(UUID id) {
        PluginComment comment = commentRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Comment not found: " + id));

        comment.isPinned = true;
        comment.updatedAt = LocalDateTime.now();

        commentRepository.persist(comment);
        return commentMapper.toRecord(comment);
    }

    @Override
    @Transactional
    public PluginCommentRecord unpin(UUID id) {
        PluginComment comment = commentRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Comment not found: " + id));

        comment.isPinned = false;
        comment.updatedAt = LocalDateTime.now();

        commentRepository.persist(comment);
        return commentMapper.toRecord(comment);
    }

    @Override
    @Transactional
    public PluginCommentRecord hide(UUID id) {
        PluginComment comment = commentRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Comment not found: " + id));

        comment.isHidden = true;
        comment.updatedAt = LocalDateTime.now();

        commentRepository.persist(comment);
        return commentMapper.toRecord(comment);
    }

    @Override
    @Transactional
    public PluginCommentRecord show(UUID id) {
        PluginComment comment = commentRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Comment not found: " + id));

        comment.isHidden = false;
        comment.updatedAt = LocalDateTime.now();

        commentRepository.persist(comment);
        return commentMapper.toRecord(comment);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        PluginComment comment = commentRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Comment not found: " + id));

        // Update parent reply count if this is a reply
        if (comment.parentId != null) {
            commentRepository.findById(comment.parentId).ifPresent(parent -> {
                parent.replyCount = parent.replyCount - 1;
                commentRepository.persist(parent);
            });
        }

        // Update plugin comment count
        pluginRepository.findById(comment.pluginId).ifPresent(plugin -> {
            plugin.commentCount = plugin.commentCount - 1;
            plugin.updatedAt = LocalDateTime.now();
            pluginRepository.persist(plugin);
        });

        commentRepository.delete(comment);
    }

    @Override
    @Transactional
    public PluginCommentRecord like(UUID id) {
        PluginComment comment = commentRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Comment not found: " + id));

        comment.likeCount = comment.likeCount + 1;
        comment.updatedAt = LocalDateTime.now();

        commentRepository.persist(comment);
        return commentMapper.toRecord(comment);
    }

    @Override
    @Transactional
    public PluginCommentRecord unlike(UUID id) {
        PluginComment comment = commentRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Comment not found: " + id));

        if (comment.likeCount > 0) {
            comment.likeCount = comment.likeCount - 1;
        }
        comment.updatedAt = LocalDateTime.now();

        commentRepository.persist(comment);
        return commentMapper.toRecord(comment);
    }

    @Override
    @Transactional
    public PluginCommentRecord markAsDeveloperReply(UUID id) {
        PluginComment comment = commentRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Comment not found: " + id));

        comment.isDeveloperReply = true;
        comment.updatedAt = LocalDateTime.now();

        commentRepository.persist(comment);
        return commentMapper.toRecord(comment);
    }
}