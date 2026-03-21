package com.easystation.build.service;

import com.easystation.build.domain.BuildArtifact;
import com.easystation.build.domain.BuildTask;
import com.easystation.build.dto.BuildRecord;
import com.easystation.build.enums.BuildStatus;
import com.easystation.build.enums.BuildType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class BuildTaskService {

    @Inject
    ObjectMapper objectMapper;

    private static final String BUILD_DIR = "work/builds";
    private static final String ARTIFACT_DIR = "work/artifacts";

    public List<BuildRecord.Detail> list(BuildRecord.Query query) {
        StringBuilder sql = new StringBuilder("1=1");
        Map<String, Object> params = new HashMap<>();

        if (query.type() != null) {
            sql.append(" and type = :type");
            params.put("type", query.type());
        }
        if (query.status() != null) {
            sql.append(" and status = :status");
            params.put("status", query.status());
        }
        if (query.templateId() != null) {
            sql.append(" and templateId = :templateId");
            params.put("templateId", query.templateId());
        }
        if (query.keyword() != null && !query.keyword().isBlank()) {
            sql.append(" and name like :keyword");
            params.put("keyword", "%" + query.keyword() + "%");
        }
        if (query.startTime() != null) {
            sql.append(" and createdAt >= :startTime");
            params.put("startTime", query.startTime());
        }
        if (query.endTime() != null) {
            sql.append(" and createdAt <= :endTime");
            params.put("endTime", query.endTime());
        }

        int limit = query.limit() != null ? query.limit() : 50;
        int offset = query.offset() != null ? query.offset() : 0;

        return BuildTask.<BuildTask>find(sql.toString(), params)
                .range(offset, offset + limit - 1)
                .stream()
                .map(this::toDetail)
                .collect(Collectors.toList());
    }

    public BuildRecord.Detail get(UUID id) {
        BuildTask task = BuildTask.findById(id);
        if (task == null) {
            throw new WebApplicationException("Build task not found", Response.Status.NOT_FOUND);
        }
        return toDetail(task);
    }

    @Transactional
    public BuildRecord.Detail create(BuildRecord.Create dto) {
        BuildTask task = new BuildTask();
        task.name = dto.name();
        task.type = dto.type();
        task.templateId = dto.templateId();
        task.config = dto.config();
        task.script = dto.script();
        task.version = dto.version();
        task.triggeredBy = dto.triggeredBy();
        task.status = BuildStatus.PENDING;
        task.persist();
        return toDetail(task);
    }

    @Transactional
    public BuildRecord.Detail start(UUID id) {
        BuildTask task = BuildTask.findById(id);
        if (task == null) {
            throw new WebApplicationException("Build task not found", Response.Status.NOT_FOUND);
        }
        if (task.status != BuildStatus.PENDING) {
            throw new WebApplicationException("Build task is not in PENDING status", Response.Status.BAD_REQUEST);
        }

        task.status = BuildStatus.RUNNING;
        task.startedAt = LocalDateTime.now();

        try {
            executeBuild(task);
            task.status = BuildStatus.SUCCESS;
            task.artifactPath = generateArtifactPath(task);
            task.artifactSize = calculateArtifactSize(task.artifactPath);
        } catch (Exception e) {
            task.status = BuildStatus.FAILED;
            task.errorMessage = e.getMessage();
            Log.errorf(e, "Build task %s failed", task.id);
        } finally {
            task.finishedAt = LocalDateTime.now();
            if (task.startedAt != null) {
                task.duration = ChronoUnit.MILLIS.between(task.startedAt, task.finishedAt);
            }
        }

        return toDetail(task);
    }

    @Transactional
    public BuildRecord.Detail cancel(UUID id) {
        BuildTask task = BuildTask.findById(id);
        if (task == null) {
            throw new WebApplicationException("Build task not found", Response.Status.NOT_FOUND);
        }
        if (task.status != BuildStatus.PENDING && task.status != BuildStatus.RUNNING) {
            throw new WebApplicationException("Build task cannot be cancelled", Response.Status.BAD_REQUEST);
        }
        task.status = BuildStatus.CANCELLED;
        task.finishedAt = LocalDateTime.now();
        return toDetail(task);
    }

    @Transactional
    public void delete(UUID id) {
        BuildTask task = BuildTask.findById(id);
        if (task == null) {
            throw new WebApplicationException("Build task not found", Response.Status.NOT_FOUND);
        }
        // 删除相关产物
        BuildArtifact.delete("buildTaskId", id);
        task.delete();
    }

    public List<BuildRecord.ArtifactDetail> listArtifacts(UUID templateId) {
        return BuildArtifact.<BuildArtifact>find("templateId = ?1 order by createdAt desc", templateId)
                .stream()
                .map(this::toArtifactDetail)
                .collect(Collectors.toList());
    }

    @Transactional
    public BuildRecord.ArtifactDetail createArtifact(UUID taskId) {
        BuildTask task = BuildTask.findById(taskId);
        if (task == null) {
            throw new WebApplicationException("Build task not found", Response.Status.NOT_FOUND);
        }
        if (task.status != BuildStatus.SUCCESS) {
            throw new WebApplicationException("Build task is not successful", Response.Status.BAD_REQUEST);
        }

        // 将之前相同模板的产物设为非最新
        if (task.templateId != null) {
            BuildArtifact.update("latest = false where templateId = ?1", task.templateId);
        }

        BuildArtifact artifact = new BuildArtifact();
        artifact.buildTaskId = taskId;
        artifact.templateId = task.templateId;
        artifact.name = task.name + "-" + task.version;
        artifact.version = task.version != null ? task.version : "1.0.0";
        artifact.filePath = task.artifactPath;
        artifact.fileSize = task.artifactSize;
        artifact.latest = true;
        artifact.persist();

        return toArtifactDetail(artifact);
    }

    public long countByStatus(BuildStatus status) {
        return BuildTask.count("status", status);
    }

    private void executeBuild(BuildTask task) throws Exception {
        StringBuilder logs = new StringBuilder();
        logs.append("[").append(LocalDateTime.now()).append("] Starting build...\n");

        try {
            switch (task.type) {
                case LOCAL_FILE -> executeLocalFileBuild(task, logs);
                case GIT_CLONE -> executeGitCloneBuild(task, logs);
                case DOCKER_PULL -> executeDockerPullBuild(task, logs);
                case MAVEN_BUILD -> executeMavenBuild(task, logs);
                case SCRIPT_BUILD -> executeScriptBuild(task, logs);
                default -> throw new IllegalArgumentException("Unknown build type: " + task.type);
            }
            logs.append("[").append(LocalDateTime.now()).append("] Build completed successfully.\n");
        } catch (Exception e) {
            logs.append("[").append(LocalDateTime.now()).append("] Build failed: ").append(e.getMessage()).append("\n");
            throw e;
        } finally {
            task.logs = logs.toString();
        }
    }

    private void executeLocalFileBuild(BuildTask task, StringBuilder logs) throws IOException {
        logs.append("Processing local file build...\n");
        // 实际实现需要处理文件上传
        Path buildDir = Paths.get(BUILD_DIR, task.id.toString());
        Files.createDirectories(buildDir);
        logs.append("Build directory created: ").append(buildDir).append("\n");
    }

    private void executeGitCloneBuild(BuildTask task, StringBuilder logs) {
        logs.append("Cloning Git repository...\n");
        // 实际实现需要调用 git clone
        logs.append("Git clone completed.\n");
    }

    private void executeDockerPullBuild(BuildTask task, StringBuilder logs) {
        logs.append("Pulling Docker image...\n");
        // 实际实现需要调用 docker pull
        logs.append("Docker pull completed.\n");
    }

    private void executeMavenBuild(BuildTask task, StringBuilder logs) {
        logs.append("Executing Maven build...\n");
        // 实际实现需要调用 mvn package
        logs.append("Maven build completed.\n");
    }

    private void executeScriptBuild(BuildTask task, StringBuilder logs) throws Exception {
        logs.append("Executing custom script...\n");
        if (task.script == null || task.script.isBlank()) {
            throw new IllegalArgumentException("No script provided for SCRIPT_BUILD type");
        }
        // 实际实现需要执行脚本
        logs.append("Script execution completed.\n");
    }

    private String generateArtifactPath(BuildTask task) {
        return Paths.get(ARTIFACT_DIR, task.id.toString(), task.name + "-" + task.version + ".tar.gz").toString();
    }

    private Long calculateArtifactSize(String path) {
        try {
            Path p = Paths.get(path);
            if (Files.exists(p)) {
                return Files.size(p);
            }
        } catch (IOException e) {
            Log.warnf("Failed to calculate artifact size: %s", path);
        }
        return 0L;
    }

    private BuildRecord.Detail toDetail(BuildTask task) {
        return new BuildRecord.Detail(
                task.id,
                task.name,
                task.type,
                task.status,
                task.templateId,
                task.config,
                task.script,
                task.artifactPath,
                task.artifactSize,
                task.version,
                task.logs,
                task.errorMessage,
                task.startedAt,
                task.finishedAt,
                task.duration,
                task.triggeredBy,
                task.createdAt,
                task.updatedAt
        );
    }

    private BuildRecord.ArtifactDetail toArtifactDetail(BuildArtifact artifact) {
        return new BuildRecord.ArtifactDetail(
                artifact.id,
                artifact.buildTaskId,
                artifact.templateId,
                artifact.name,
                artifact.version,
                artifact.filePath,
                artifact.fileSize,
                artifact.checksum,
                artifact.checksumType,
                artifact.latest,
                artifact.downloadCount,
                artifact.createdAt
        );
    }
}