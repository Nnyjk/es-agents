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
        Plugin plugin = pluginRepository.findByIdOptional(create.pluginId())
            .orElseThrow(() -> new NotFoundException("Plugin not found: " + create.pluginId()));

        PluginComment comment = new PluginComment();
        comment.pluginId = plugin.id;
        comment.userId = userId;
        comment.content = create.content();
        comment.isDeveloperReply = false;
        comment.isPinned = false;
        comment.isHidden = false;
        comment.likeCount = 0;
        comment.replyCount = 0;
        comment.createdAt = LocalDateTime.now();
        comment.updatedAt = LocalDateTime.now();

        // Handle reply
        if (create.parentId() != null) {
            PluginComment parent = commentRepository.findByIdOptional(create.parentId())
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
        PluginComment comment = commentRepository.findByIdOptional(id)
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
        return commentRepository.findByIdOptional(id)
            .map(commentMapper::toRecord);
    }

    @Override
    public List<PluginCommentRecord> findByPluginId(UUID pluginId) {
        return commentRepository.findByPluginId(pluginId).stream()
            .map(commentMapper::toRecord)
            .collect(Collectors.toList());
    }

    @Override
    public List<PluginCommentRecord> findRootComments(UUID pluginId) {
        return commentRepository.findByPluginIdAndParentIdIsNull(pluginId).stream()
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
        PluginComment comment = commentRepository.findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Comment not found: " + id));

        comment.isPinned = true;
        comment.updatedAt = LocalDateTime.now();

        commentRepository.persist(comment);
        return commentMapper.toRecord(comment);
    }

    @Override
    @Transactional
    public PluginCommentRecord unpin(UUID id) {
        PluginComment comment = commentRepository.findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Comment not found: " + id));

        comment.isPinned = false;
        comment.updatedAt = LocalDateTime.now();

        commentRepository.persist(comment);
        return commentMapper.toRecord(comment);
    }

    @Override
    @Transactional
    public PluginCommentRecord hide(UUID id) {
        PluginComment comment = commentRepository.findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Comment not found: " + id));

        comment.isHidden = true;
        comment.updatedAt = LocalDateTime.now();

        commentRepository.persist(comment);
        return commentMapper.toRecord(comment);
    }

    @Override
    @Transactional
    public PluginCommentRecord show(UUID id) {
        PluginComment comment = commentRepository.findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Comment not found: " + id));

        comment.isHidden = false;
        comment.updatedAt = LocalDateTime.now();

        commentRepository.persist(comment);
        return commentMapper.toRecord(comment);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        PluginComment comment = commentRepository.findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Comment not found: " + id));

        // Update parent reply count if this is a reply
        if (comment.parentId != null) {
            commentRepository.findByIdOptional(comment.parentId).ifPresent(parent -> {
                parent.replyCount = parent.replyCount - 1;
                commentRepository.persist(parent);
            });
        }

        // Update plugin comment count
        pluginRepository.findByIdOptional(comment.pluginId).ifPresent(plugin -> {
            plugin.commentCount = plugin.commentCount - 1;
            plugin.updatedAt = LocalDateTime.now();
            pluginRepository.persist(plugin);
        });

        commentRepository.delete(comment);
    }

    @Override
    @Transactional
    public void like(UUID id) {
        PluginComment comment = commentRepository.findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Comment not found: " + id));

        comment.likeCount = comment.likeCount + 1;
        comment.updatedAt = LocalDateTime.now();

        commentRepository.persist(comment);
    }

    @Override
    @Transactional
    public void unlike(UUID id) {
        PluginComment comment = commentRepository.findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Comment not found: " + id));

        if (comment.likeCount > 0) {
            comment.likeCount = comment.likeCount - 1;
        }
        comment.updatedAt = LocalDateTime.now();

        commentRepository.persist(comment);
    }

    @Override
    @Transactional
    public PluginCommentRecord markAsDeveloperReply(UUID id) {
        PluginComment comment = commentRepository.findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Comment not found: " + id));

        comment.isDeveloperReply = true;
        comment.updatedAt = LocalDateTime.now();

        commentRepository.persist(comment);
        return commentMapper.toRecord(comment);
    }

    @Override
    public List<PluginCommentRecord> search(PluginCommentRecord.Query query) {
        StringBuilder queryBuilder = new StringBuilder("1=1");
        List<Object> params = new java.util.ArrayList<>();
        int paramIndex = 1;

        if (query.pluginId() != null) {
            queryBuilder.append(" and pluginId = ?").append(paramIndex);
            params.add(query.pluginId());
            paramIndex++;
        }

        if (query.userId() != null) {
            queryBuilder.append(" and userId = ?").append(paramIndex);
            params.add(query.userId());
            paramIndex++;
        }

        if (query.isDeveloperReply() != null) {
            queryBuilder.append(" and isDeveloperReply = ?").append(paramIndex);
            params.add(query.isDeveloperReply());
            paramIndex++;
        }

        if (query.includeHidden() == null || !query.includeHidden()) {
            queryBuilder.append(" and isHidden = false");
        }

        // Add sorting
        String sortBy = query.sortBy() != null ? query.sortBy() : "createdAt";
        String sortOrder = query.sortOrder() != null ? query.sortOrder() : "DESC";
        queryBuilder.append(" ORDER BY ").append(sortBy).append(" ").append(sortOrder);

        int page = query.page() != null ? query.page() : 0;
        int size = query.size() != null ? query.size() : 20;

        return commentRepository.find(queryBuilder.toString(), params.toArray())
            .page(Page.of(page, size))
            .list()
            .stream()
            .map(commentMapper::toRecord)
            .collect(Collectors.toList());
    }

    @Override
    public long countReplies(UUID parentId) {
        return commentRepository.countByParentId(parentId);
    }
}