package com.easystation.deployment.service;

import com.easystation.deployment.domain.DeploymentProgress;
import com.easystation.deployment.domain.DeploymentProgressHistory;
import com.easystation.deployment.dto.DeploymentProgressDTO;
import com.easystation.deployment.dto.DeploymentProgressHistoryDTO;
import com.easystation.deployment.enums.ProgressStatus;
import com.easystation.deployment.mapper.DeploymentProgressMapper;
import com.easystation.deployment.websocket.DeploymentWebSocket;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 部署进展服务
 */
@ApplicationScoped
public class DeploymentProgressService {

    @Inject
    DeploymentWebSocket deploymentWebSocket;

    /**
     * 创建进展记录
     */
    @Transactional
    public DeploymentProgressDTO createProgress(UUID deploymentId, String stage, ProgressStatus status, String message) {
        DeploymentProgress progress = new DeploymentProgress();
        progress.deploymentId = deploymentId;
        progress.stage = stage;
        progress.status = status;
        progress.message = message;
        progress.progressPercent = 0;

        if (status == ProgressStatus.RUNNING) {
            progress.startedAt = LocalDateTime.now();
        }

        progress.persist();

        // 推送 WebSocket 消息
        pushProgressUpdate(deploymentId, stage, status, 0, message);

        return DeploymentProgressMapper.toDTO(progress);
    }

    /**
     * 更新进展
     */
    @Transactional
    public DeploymentProgressDTO updateProgress(UUID progressId, ProgressStatus status, Integer progressPercent, String message) {
        DeploymentProgress progress = DeploymentProgress.findById(progressId);
        if (progress == null) {
            throw new IllegalArgumentException("Progress not found: " + progressId);
        }

        ProgressStatus oldStatus = progress.status;
        progress.status = status;
        progress.progressPercent = progressPercent;
        progress.message = message;
        progress.updatedAt = LocalDateTime.now();

        if (status == ProgressStatus.RUNNING && progress.startedAt == null) {
            progress.startedAt = LocalDateTime.now();
        }

        if (status == ProgressStatus.SUCCESS || status == ProgressStatus.FAILED) {
            progress.completedAt = LocalDateTime.now();
        }

        // 记录历史
        DeploymentProgressHistory history = new DeploymentProgressHistory();
        history.deploymentId = progress.deploymentId;
        history.stage = progress.stage;
        history.oldStatus = oldStatus.name();
        history.newStatus = status.name();
        history.message = message;
        history.persist();

        // 推送 WebSocket 消息
        pushProgressUpdate(progress.deploymentId, progress.stage, status, progressPercent, message);

        return DeploymentProgressMapper.toDTO(progress);
    }

    /**
     * 获取进展历史
     */
    public List<DeploymentProgressDTO> getProgressHistory(UUID deploymentId) {
        return DeploymentProgressMapper.toDTOList(DeploymentProgress.findByDeploymentId(deploymentId));
    }

    /**
     * 获取当前进展
     */
    public DeploymentProgressDTO getCurrentProgress(UUID deploymentId) {
        return DeploymentProgressMapper.toDTO(DeploymentProgress.findCurrentByDeploymentId(deploymentId));
    }

    /**
     * 获取状态变更历史
     */
    public List<DeploymentProgressHistoryDTO> getStatusHistory(UUID deploymentId) {
        return DeploymentProgressMapper.toHistoryDTOList(DeploymentProgressHistory.findByDeploymentId(deploymentId));
    }

    /**
     * 计算总体进度百分比
     */
    public int calculateOverallProgress(UUID deploymentId) {
        List<DeploymentProgress> progresses = DeploymentProgress.findByDeploymentId(deploymentId);
        if (progresses.isEmpty()) {
            return 0;
        }

        int totalPercent = 0;
        int count = 0;
        for (DeploymentProgress progress : progresses) {
            if (progress.progressPercent != null && progress.progressPercent > 0) {
                totalPercent += progress.progressPercent;
                count++;
            }
        }

        return count > 0 ? totalPercent / count : 0;
    }

    /**
     * 标记阶段完成
     */
    @Transactional
    public DeploymentProgressDTO markStageComplete(UUID deploymentId, String stage, String message) {
        DeploymentProgress progress = DeploymentProgress.findByDeploymentIdAndStage(deploymentId, stage);
        if (progress == null) {
            // 如果不存在，创建新的
            return createProgress(deploymentId, stage, ProgressStatus.SUCCESS, message);
        }

        return updateProgress(progress.id, ProgressStatus.SUCCESS, 100, message);
    }

    /**
     * 标记阶段失败
     */
    @Transactional
    public DeploymentProgressDTO markStageFailed(UUID deploymentId, String stage, String message) {
        DeploymentProgress progress = DeploymentProgress.findByDeploymentIdAndStage(deploymentId, stage);
        if (progress == null) {
            // 如果不存在，创建新的
            return createProgress(deploymentId, stage, ProgressStatus.FAILED, message);
        }

        return updateProgress(progress.id, ProgressStatus.FAILED, progress.progressPercent, message);
    }

    /**
     * 推送进度更新到 WebSocket
     */
    private void pushProgressUpdate(UUID deploymentId, String stage, ProgressStatus status, Integer progressPercent, String message) {
        if (deploymentWebSocket != null) {
            if (status == ProgressStatus.FAILED) {
                deploymentWebSocket.pushError(deploymentId, message, stage);
            } else {
                int progress = progressPercent != null ? progressPercent : 0;
                deploymentWebSocket.pushProgress(deploymentId, progress, stage, message);
                deploymentWebSocket.pushStatus(deploymentId, status.name(), stage, message);
            }
        }
    }

    /**
     * 手动推送进度（供外部调用）
     */
    public void pushProgress(UUID deploymentId, int progress, String stage, String message) {
        deploymentWebSocket.pushProgress(deploymentId, progress, stage, message);
    }

    /**
     * 手动推送状态（供外部调用）
     */
    public void pushStatus(UUID deploymentId, String status, String stage, String message) {
        deploymentWebSocket.pushStatus(deploymentId, status, stage, message);
    }

    /**
     * 手动推送错误（供外部调用）
     */
    public void pushError(UUID deploymentId, String error, String stage) {
        deploymentWebSocket.pushError(deploymentId, error, stage);
    }
}