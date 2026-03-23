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
        Plugin plugin = pluginRepository.findById(create.pluginId())
            .orElseThrow(() -> new NotFoundException("Plugin not found: " + create.pluginId()));

        PluginComment comment = new PluginComment();
        comment.setPlugin(plugin);
        comment.setUserId(userId);

        if (create.parentId() != null) {
            PluginComment parent = commentRepository.findById(create.parentId())
                .orElseThrow(() -> new NotFoundException("Parent comment not found: " + create.parentId()));
            comment.setParent(parent);

            if (create.replyToUserId() != null) {
                comment.setReplyToUserId(create.replyToUserId());
            }
        }

        comment.setContent(create.content());
        comment.setIsDeveloperReply(false);
        comment.setIsPinned(false);
        comment.setIsHidden(false);
        comment.setLikeCount(0);
        comment.setReplyCount(0);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());

        commentRepository.persist(comment);

        // Update parent reply count
        if (create.parentId() != null) {
            commentRepository.findById(create.parentId()).ifPresent(parent -> {
                parent.setReplyCount(parent.getReplyCount() + 1);
                commentRepository.persist(parent);
            });
        }

        // Update plugin comment count
        plugin.setCommentCount(plugin.getCommentCount() + 1);
        plugin.setUpdatedAt(LocalDateTime.now());
        pluginRepository.persist(plugin);

        return commentMapper.toRecord(comment);
    }

    @Override
    @Transactional
    public PluginCommentRecord update(UUID id, PluginCommentRecord.Update update) {
        PluginComment comment = commentRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Comment not found: " + id));

        comment.setContent(update.content());
        comment.setUpdatedAt(LocalDateTime.now());

        commentRepository.persist(comment);
        return commentMapper.toRecord(comment);
    }

    @Override
    public Optional<PluginCommentRecord> findById(UUID id) {
        return commentRepository.findById(id)
            .map(this::toRecordWithReplies);
    }

    @Override
    public List<PluginCommentRecord> findByPluginId(UUID pluginId) {
        return commentRepository.findByPluginIdOrderByCreatedAtDesc(pluginId).stream()
            .map(commentMapper::toRecord)
            .collect(Collectors.toList());
    }

    @Override
    public List<PluginCommentRecord> findRootComments(UUID pluginId) {
        return commentRepository.findByPluginIdAndParentIdIsNullOrderByPinnedAndCreatedAt(pluginId).stream()
            .map(this::toRecordWithReplies)
            .collect(Collectors.toList());
    }

    @Override
    public List<PluginCommentRecord> findReplies(UUID parentId) {
        return commentRepository.findByParentIdOrderByCreatedAtAsc(parentId).stream()
            .map(commentMapper::toRecord)
            .collect(Collectors.toList());
    }

    @Override
    public List<PluginCommentRecord> search(PluginCommentRecord.Query query) {
        StringBuilder queryBuilder = new StringBuilder("1=1");
        java.util.List<Object> params = new java.util.ArrayList<>();
        int paramIndex = 1;

        if (query.pluginId() != null) {
            queryBuilder.append(" and plugin.id = ?").append(paramIndex);
            params.add(query.pluginId());
            paramIndex++;
        }

        if (query.userId() != null) {
            queryBuilder.append(" and user.id = ?").append(paramIndex);
            params.add(query.userId());
            paramIndex++;
        }

        if (query.isDeveloperReply() != null) {
            queryBuilder.append(" and isDeveloperReply = ?").append(paramIndex);
            params.add(query.isDeveloperReply());
            paramIndex++;
        }

        if (!Boolean.TRUE.equals(query.includeHidden())) {
            queryBuilder.append(" and isHidden = false");
        }

        String sortField = query.sortBy() != null ? query.sortBy() : "createdAt";
        String sortOrder = "DESC".equalsIgnoreCase(query.sortOrder()) ? "DESC" : "ASC";
        queryBuilder.append(" ORDER BY ").append(sortField).append(" ").append(sortOrder);

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
    @Transactional
    public PluginCommentRecord pin(UUID id) {
        PluginComment comment = commentRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Comment not found: " + id));

        comment.setIsPinned(true);
        comment.setUpdatedAt(LocalDateTime.now());

        commentRepository.persist(comment);
        return commentMapper.toRecord(comment);
    }

    @Override
    @Transactional
    public PluginCommentRecord unpin(UUID id) {
        PluginComment comment = commentRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Comment not found: " + id));

        comment.setIsPinned(false);
        comment.setUpdatedAt(LocalDateTime.now());

        commentRepository.persist(comment);
        return commentMapper.toRecord(comment);
    }

    @Override
    @Transactional
    public PluginCommentRecord hide(UUID id) {
        PluginComment comment = commentRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Comment not found: " + id));

        comment.setIsHidden(true);
        comment.setUpdatedAt(LocalDateTime.now());

        commentRepository.persist(comment);
        return commentMapper.toRecord(comment);
    }

    @Override
    @Transactional
    public PluginCommentRecord show(UUID id) {
        PluginComment comment = commentRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Comment not found: " + id));

        comment.setIsHidden(false);
        comment.setUpdatedAt(LocalDateTime.now());

        commentRepository.persist(comment);
        return commentMapper.toRecord(comment);
    }

    @Override
    @Transactional
    public PluginCommentRecord markAsDeveloperReply(UUID id) {
        PluginComment comment = commentRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Comment not found: " + id));

        comment.setIsDeveloperReply(true);
        comment.setUpdatedAt(LocalDateTime.now());

        commentRepository.persist(comment);
        return commentMapper.toRecord(comment);
    }

    @Override
    @Transactional
    public void like(UUID id) {
        commentRepository.findById(id).ifPresent(comment -> {
            comment.setLikeCount(comment.getLikeCount() + 1);
            commentRepository.persist(comment);
        });
    }

    @Override
    @Transactional
    public void unlike(UUID id) {
        commentRepository.findById(id).ifPresent(comment -> {
            if (comment.getLikeCount() > 0) {
                comment.setLikeCount(comment.getLikeCount() - 1);
                commentRepository.persist(comment);
            }
        });
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        commentRepository.findById(id).ifPresent(comment -> {
            UUID pluginId = comment.getPlugin().getId();
            UUID parentId = comment.getParent() != null ? comment.getParent().getId() : null;

            commentRepository.delete(comment);

            // Update parent reply count
            if (parentId != null) {
                commentRepository.findById(parentId).ifPresent(parent -> {
                    if (parent.getReplyCount() > 0) {
                        parent.setReplyCount(parent.getReplyCount() - 1);
                        commentRepository.persist(parent);
                    }
                });
            }

            // Update plugin comment count
            pluginRepository.findById(pluginId).ifPresent(plugin -> {
                if (plugin.getCommentCount() > 0) {
                    plugin.setCommentCount(plugin.getCommentCount() - 1);
                    plugin.setUpdatedAt(LocalDateTime.now());
                    pluginRepository.persist(plugin);
                }
            });
        });
    }

    @Override
    public long countByPluginId(UUID pluginId) {
        return commentRepository.countByPluginId(pluginId);
    }

    @Override
    public long countReplies(UUID parentId) {
        return commentRepository.countByParentId(parentId);
    }

    private PluginCommentRecord toRecordWithReplies(PluginComment comment) {
        PluginCommentRecord record = commentMapper.toRecord(comment);
        
        // Load replies
        List<PluginCommentRecord> replies = commentRepository
            .findByParentIdOrderByCreatedAtAsc(comment.getId())
            .stream()
            .map(commentMapper::toRecord)
            .collect(Collectors.toList());

        return new PluginCommentRecord(
            record.id(), record.pluginId(), record.pluginName(),
            record.userId(), record.userName(), record.userAvatar(),
            record.parentId(), record.replyToUserId(), record.replyToUserName(),
            record.content(), record.isDeveloperReply(), record.isPinned(),
            record.isHidden(), record.likeCount(), record.replyCount(),
            record.createdAt(), record.updatedAt(), replies
        );
    }
}