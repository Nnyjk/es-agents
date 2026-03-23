package com.easystation.plugin.service.impl;

import com.easystation.plugin.domain.Plugin;
import com.easystation.plugin.domain.PluginCategory;
import com.easystation.plugin.domain.PluginVersion;
import com.easystation.plugin.domain.enums.PluginStatus;
import com.easystation.plugin.dto.PluginRecord;
import com.easystation.plugin.dto.PluginVersionRecord;
import com.easystation.plugin.mapper.PluginMapper;
import com.easystation.plugin.mapper.PluginVersionMapper;
import com.easystation.plugin.repository.PluginCategoryRepository;
import com.easystation.plugin.repository.PluginRepository;
import com.easystation.plugin.repository.PluginVersionRepository;
import com.easystation.plugin.service.PluginService;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class PluginServiceImpl implements PluginService {

    @Inject
    PluginRepository pluginRepository;

    @Inject
    PluginVersionRepository versionRepository;

    @Inject
    PluginCategoryRepository categoryRepository;

    @Inject
    PluginMapper pluginMapper;

    @Inject
    PluginVersionMapper versionMapper;

    @Override
    @Transactional
    public PluginRecord create(PluginRecord.Create create) {
        // Check if code already exists
        if (create.code() != null && pluginRepository.existsByCode(create.code())) {
            throw new BadRequestException("Plugin code already exists: " + create.code());
        }

        Plugin plugin = new Plugin();
        plugin.setName(create.name());
        plugin.setCode(create.code() != null ? create.code() : generateCode(create.name()));
        
        if (create.categoryId() != null) {
            PluginCategory category = categoryRepository.findById(create.categoryId())
                .orElseThrow(() -> new NotFoundException("Category not found: " + create.categoryId()));
            plugin.setCategory(category);
        }
        
        plugin.setIcon(create.icon());
        plugin.setDescription(create.description());
        plugin.setReadme(create.readme());
        plugin.setSourceUrl(create.sourceUrl());
        plugin.setDocUrl(create.docUrl());
        plugin.setHomepageUrl(create.homepageUrl());
        plugin.setIsFreeFlag(create.isFree() != null ? create.isFree() : true);
        plugin.setPrice(create.price() != null ? create.price() : BigDecimal.ZERO);
        plugin.setMinPlatformVersion(create.minPlatformVersion());
        plugin.setMaxPlatformVersion(create.maxPlatformVersion());
        plugin.setSupportedPlatforms(create.supportedPlatforms());
        plugin.setPermissionsRequired(create.permissionsRequired());
        plugin.setConfigSchema(create.configSchema());
        plugin.setTagsAsList(create.tags());
        plugin.setStatus(PluginStatus.DRAFT);
        plugin.setTotalDownloads(0L);
        plugin.setTotalInstalls(0L);
        plugin.setAverageRating(BigDecimal.ZERO);
        plugin.setRatingCount(0);
        plugin.setCommentCount(0);
        plugin.setFavoriteCount(0);
        plugin.setCreatedAt(LocalDateTime.now());
        plugin.setUpdatedAt(LocalDateTime.now());

        pluginRepository.persist(plugin);
        return pluginMapper.toRecord(plugin);
    }

    @Override
    @Transactional
    public PluginRecord update(UUID id, PluginRecord.Update update) {
        Plugin plugin = pluginRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Plugin not found: " + id));

        if (update.name() != null) {
            plugin.setName(update.name());
        }
        if (update.categoryId() != null) {
            PluginCategory category = categoryRepository.findById(update.categoryId())
                .orElseThrow(() -> new NotFoundException("Category not found: " + update.categoryId()));
            plugin.setCategory(category);
        }
        if (update.icon() != null) {
            plugin.setIcon(update.icon());
        }
        if (update.description() != null) {
            plugin.setDescription(update.description());
        }
        if (update.readme() != null) {
            plugin.setReadme(update.readme());
        }
        if (update.sourceUrl() != null) {
            plugin.setSourceUrl(update.sourceUrl());
        }
        if (update.docUrl() != null) {
            plugin.setDocUrl(update.docUrl());
        }
        if (update.homepageUrl() != null) {
            plugin.setHomepageUrl(update.homepageUrl());
        }
        if (update.isFree() != null) {
            plugin.setIsFree(update.isFree());
        }
        if (update.price() != null) {
            plugin.setPrice(update.price());
        }
        if (update.minPlatformVersion() != null) {
            plugin.setMinPlatformVersion(update.minPlatformVersion());
        }
        if (update.maxPlatformVersion() != null) {
            plugin.setMaxPlatformVersion(update.maxPlatformVersion());
        }
        if (update.supportedPlatforms() != null) {
            plugin.setSupportedPlatforms(update.supportedPlatforms());
        }
        if (update.permissionsRequired() != null) {
            plugin.setPermissionsRequired(update.permissionsRequired());
        }
        if (update.configSchema() != null) {
            plugin.setConfigSchema(update.configSchema());
        }
        if (update.tags() != null) {
            plugin.setTagsAsList(update.tags());
        }
        plugin.setUpdatedAt(LocalDateTime.now());

        pluginRepository.persist(plugin);
        return pluginMapper.toRecord(plugin);
    }

    @Override
    public Optional<PluginRecord> findById(UUID id) {
        return pluginRepository.findById(id)
            .map(plugin -> {
                PluginRecord record = pluginMapper.toRecord(plugin);
                // Load latest version
                Optional<PluginVersion> latestVersion = versionRepository.findLatestByPluginId(id);
                if (latestVersion.isPresent()) {
                    PluginVersion v = latestVersion.get();
                    PluginVersionRecord versionRecord = versionMapper.toRecord(v);
                    // Create a new record with version info
                    return new PluginRecord(
                        record.id(), record.developerId(), record.developerName(),
                        record.categoryId(), record.categoryName(), record.name(),
                        record.code(), record.icon(), record.description(),
                        record.readme(), record.sourceUrl(), record.docUrl(),
                        record.homepageUrl(), record.status(), record.isFree(),
                        record.price(), record.minPlatformVersion(), record.maxPlatformVersion(),
                        record.supportedPlatforms(), record.permissionsRequired(),
                        record.configSchema(), record.totalDownloads(), record.totalInstalls(),
                        record.averageRating(), record.ratingCount(), record.commentCount(),
                        record.favoriteCount(), record.publishedAt(), record.createdAt(),
                        record.updatedAt(), record.tags(),
                        new PluginRecord.PluginVersionInfo(
                            versionRecord.id(), versionRecord.version(),
                            versionRecord.downloadUrl(), versionRecord.packageSize(),
                            versionRecord.publishedAt()
                        )
                    );
                }
                return record;
            });
    }

    @Override
    public Optional<PluginRecord> findByCode(String code) {
        return pluginRepository.findByCode(code)
            .map(pluginMapper::toRecord);
    }

    @Override
    public List<PluginRecord> search(PluginRecord.Query query) {
        StringBuilder queryBuilder = new StringBuilder("1=1");
        java.util.List<Object> params = new java.util.ArrayList<>();
        int paramIndex = 1;

        if (query.keyword() != null && !query.keyword().isBlank()) {
            queryBuilder.append(" and (LOWER(name) LIKE LOWER(?").append(paramIndex)
                .append(") or LOWER(description) LIKE LOWER(?").append(paramIndex).append("))");
            params.add("%" + query.keyword() + "%");
            paramIndex++;
        }

        if (query.categoryId() != null) {
            queryBuilder.append(" and category.id = ?").append(paramIndex);
            params.add(query.categoryId());
            paramIndex++;
        }

        if (query.tag() != null && !query.tag().isBlank()) {
            queryBuilder.append(" and tags LIKE ?").append(paramIndex);
            params.add("%" + query.tag() + "%");
            paramIndex++;
        }

        if (query.status() != null) {
            queryBuilder.append(" and status = ?").append(paramIndex);
            params.add(query.status());
            paramIndex++;
        }

        if (query.developerId() != null) {
            queryBuilder.append(" and developer.id = ?").append(paramIndex);
            params.add(query.developerId());
            paramIndex++;
        }

        if (query.isFree() != null) {
            queryBuilder.append(" and isFree = ?").append(paramIndex);
            params.add(query.isFree());
            paramIndex++;
        }

        String sortField = query.sortBy() != null ? query.sortBy() : "createdAt";
        String sortOrder = "DESC".equalsIgnoreCase(query.sortOrder()) ? "DESC" : "ASC";
        queryBuilder.append(" ORDER BY ").append(sortField).append(" ").append(sortOrder);

        int page = query.page() != null ? query.page() : 0;
        int size = query.size() != null ? query.size() : 20;

        return pluginRepository.find(queryBuilder.toString(), params.toArray())
            .page(Page.of(page, size))
            .list()
            .stream()
            .map(pluginMapper::toRecord)
            .collect(Collectors.toList());
    }

    @Override
    public List<PluginRecord> findByDeveloperId(UUID developerId) {
        return pluginRepository.findByDeveloperId(developerId).stream()
            .map(pluginMapper::toRecord)
            .collect(Collectors.toList());
    }

    @Override
    public List<PluginRecord> findByCategoryId(UUID categoryId) {
        return pluginRepository.findByCategoryId(categoryId).stream()
            .map(pluginMapper::toRecord)
            .collect(Collectors.toList());
    }

    @Override
    public List<PluginRecord> findByStatus(PluginStatus status) {
        return pluginRepository.findByStatus(status).stream()
            .map(pluginMapper::toRecord)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PluginRecord publish(UUID id) {
        Plugin plugin = pluginRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Plugin not found: " + id));

        if (plugin.getStatus() == PluginStatus.PUBLISHED) {
            throw new BadRequestException("Plugin is already published");
        }

        plugin.setStatus(PluginStatus.PUBLISHED);
        plugin.setPublishedAt(LocalDateTime.now());
        plugin.setUpdatedAt(LocalDateTime.now());

        pluginRepository.persist(plugin);
        return pluginMapper.toRecord(plugin);
    }

    @Override
    @Transactional
    public PluginRecord suspend(UUID id, String reason) {
        Plugin plugin = pluginRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Plugin not found: " + id));

        plugin.setStatus(PluginStatus.SUSPENDED);
        plugin.setUpdatedAt(LocalDateTime.now());

        pluginRepository.persist(plugin);
        return pluginMapper.toRecord(plugin);
    }

    @Override
    @Transactional
    public PluginRecord delete(UUID id) {
        Plugin plugin = pluginRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Plugin not found: " + id));

        plugin.setStatus(PluginStatus.DELETED);
        plugin.setUpdatedAt(LocalDateTime.now());

        pluginRepository.persist(plugin);
        return pluginMapper.toRecord(plugin);
    }

    @Override
    @Transactional
    public void incrementDownloadCount(UUID id) {
        pluginRepository.findById(id).ifPresent(plugin -> {
            plugin.setTotalDownloads(plugin.getTotalDownloads() + 1);
            pluginRepository.persist(plugin);
        });
    }

    @Override
    @Transactional
    public void incrementInstallCount(UUID id) {
        pluginRepository.findById(id).ifPresent(plugin -> {
            plugin.setTotalInstalls(plugin.getTotalInstalls() + 1);
            pluginRepository.persist(plugin);
        });
    }

    @Override
    @Transactional
    public void updateStatistics(UUID id) {
        pluginRepository.findById(id).ifPresent(plugin -> {
            // This would be called after rating/comment changes
            plugin.setUpdatedAt(LocalDateTime.now());
            pluginRepository.persist(plugin);
        });
    }

    @Override
    public PluginRecord.Summary getSummary() {
        long totalCount = pluginRepository.count();
        long publishedCount = pluginRepository.countByStatus(PluginStatus.PUBLISHED);
        long pendingCount = pluginRepository.countByStatus(PluginStatus.PENDING_REVIEW);
        long totalDownloads = pluginRepository.findAll()
            .stream()
            .mapToLong(Plugin::getTotalDownloads)
            .sum();

        return new PluginRecord.Summary(totalCount, publishedCount, pendingCount, totalDownloads);
    }

    @Override
    @Transactional
    public PluginVersionRecord createVersion(UUID pluginId, PluginVersionRecord.Create create) {
        Plugin plugin = pluginRepository.findById(pluginId)
            .orElseThrow(() -> new NotFoundException("Plugin not found: " + pluginId));

        // Check if version already exists
        if (versionRepository.existsByPluginIdAndVersion(pluginId, create.version())) {
            throw new BadRequestException("Version already exists: " + create.version());
        }

        // Parse version and calculate version code
        int versionCode = parseVersionCode(create.version());

        PluginVersion version = new PluginVersion();
        version.setPlugin(plugin);
        version.setVersion(create.version());
        version.setVersionCode(versionCode);
        version.setChangelog(create.changelog());
        version.setDownloadUrl(create.downloadUrl());
        version.setPackageSize(create.packageSize());
        version.setPackageHash(create.packageHash());
        version.setMinPlatformVersion(create.minPlatformVersion() != null ? 
            create.minPlatformVersion() : plugin.getMinPlatformVersion());
        version.setMaxPlatformVersion(create.maxPlatformVersion() != null ? 
            create.maxPlatformVersion() : plugin.getMaxPlatformVersion());
        version.setDependenciesAsString(create.dependencies());
        version.setResourceRequirements(create.resourceRequirements());
        version.setIsPrerelease(create.isPrerelease() != null ? create.isPrerelease() : false);
        version.setIsLatest(false);
        version.setDownloadCount(0L);
        version.setInstallCount(0L);
        version.setStatus(PluginStatus.DRAFT);
        version.setCreatedAt(LocalDateTime.now());
        version.setUpdatedAt(LocalDateTime.now());

        versionRepository.persist(version);
        return versionMapper.toRecord(version);
    }

    @Override
    @Transactional
    public PluginVersionRecord updateVersion(UUID versionId, PluginVersionRecord.Create update) {
        PluginVersion version = versionRepository.findById(versionId)
            .orElseThrow(() -> new NotFoundException("Version not found: " + versionId));

        if (update.changelog() != null) {
            version.setChangelog(update.changelog());
        }
        if (update.downloadUrl() != null) {
            version.setDownloadUrl(update.downloadUrl());
        }
        if (update.packageSize() != null) {
            version.setPackageSize(update.packageSize());
        }
        if (update.packageHash() != null) {
            version.setPackageHash(update.packageHash());
        }
        if (update.dependencies() != null) {
            version.setDependenciesAsString(update.dependencies());
        }
        if (update.resourceRequirements() != null) {
            version.setResourceRequirements(update.resourceRequirements());
        }
        version.setUpdatedAt(LocalDateTime.now());

        versionRepository.persist(version);
        return versionMapper.toRecord(version);
    }

    @Override
    public Optional<PluginVersionRecord> findVersionById(UUID versionId) {
        return versionRepository.findById(versionId)
            .map(versionMapper::toRecord);
    }

    @Override
    public Optional<PluginVersionRecord> findLatestVersion(UUID pluginId) {
        return versionRepository.findLatestByPluginId(pluginId)
            .map(versionMapper::toRecord);
    }

    @Override
    public List<PluginVersionRecord> findVersionsByPluginId(UUID pluginId) {
        return versionRepository.findByPluginIdOrderByVersionCodeDesc(pluginId).stream()
            .map(versionMapper::toRecord)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PluginVersionRecord publishVersion(UUID versionId) {
        PluginVersion version = versionRepository.findById(versionId)
            .orElseThrow(() -> new NotFoundException("Version not found: " + versionId));

        if (version.getStatus() == PluginStatus.PUBLISHED) {
            throw new BadRequestException("Version is already published");
        }

        // Clear previous latest flag
        versionRepository.clearLatestFlag(version.getPlugin().getId());

        // Set this version as latest
        version.setIsLatest(true);
        version.setStatus(PluginStatus.PUBLISHED);
        version.setPublishedAt(LocalDateTime.now());
        version.setUpdatedAt(LocalDateTime.now());

        versionRepository.persist(version);
        return versionMapper.toRecord(version);
    }

    @Override
    @Transactional
    public PluginVersionRecord deleteVersion(UUID versionId) {
        PluginVersion version = versionRepository.findById(versionId)
            .orElseThrow(() -> new NotFoundException("Version not found: " + versionId));

        version.setStatus(PluginStatus.DELETED);
        version.setUpdatedAt(LocalDateTime.now());

        versionRepository.persist(version);
        return versionMapper.toRecord(version);
    }

    @Override
    @Transactional
    public void incrementVersionDownloadCount(UUID versionId) {
        versionRepository.findById(versionId).ifPresent(version -> {
            version.setDownloadCount(version.getDownloadCount() + 1);
            versionRepository.persist(version);
            
            // Also increment plugin download count
            incrementDownloadCount(version.getPlugin().getId());
        });
    }

    private String generateCode(String name) {
        return name.toLowerCase()
            .replaceAll("[^a-z0-9]", "-")
            .replaceAll("-+", "-")
            .replaceAll("^-|-$", "") + 
            "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private int parseVersionCode(String version) {
        try {
            String[] parts = version.split("[-+]")[0].split("\\.");
            int code = 0;
            int multiplier = 1000000;
            for (String part : parts) {
                code += Integer.parseInt(part) * multiplier;
                multiplier /= 1000;
            }
            return code;
        } catch (Exception e) {
            return 0;
        }
    }
}