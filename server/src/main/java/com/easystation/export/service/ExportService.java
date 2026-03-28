package com.easystation.export.service;

import com.easystation.export.domain.ExportTask;
import com.easystation.export.dto.ExportRequest;
import com.easystation.export.dto.ExportTaskDTO;
import com.easystation.export.dto.ExportTaskListResponse;
import com.easystation.export.enums.ExportStatus;
import com.easystation.export.enums.ExportType;
import com.easystation.export.repository.ExportTaskRepository;
import com.easystation.agent.domain.CommandExecution;
import com.easystation.agent.domain.DeploymentHistory;
import com.easystation.audit.domain.AuditLog;
import com.easystation.alert.domain.AlertEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jboss.logging.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class ExportService {

    private static final Logger LOG = Logger.getLogger(ExportService.class);
    private static final String EXPORT_DIR = "exports";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @Inject
    ExportTaskRepository exportTaskRepository;

    @Inject
    ObjectMapper objectMapper;

    /**
     * Create a new export task
     */
    @Transactional
    public UUID createExportTask(UUID userId, ExportRequest request) {
        ExportTask task = new ExportTask();
        task.userId = userId;
        task.exportType = request.exportType();
        task.dataType = request.dataType();
        task.status = ExportStatus.PENDING.name();
        task.createdAt = LocalDateTime.now();

        try {
            task.queryParams = objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            LOG.warnf("Failed to serialize query params: %s", e.getMessage());
        }

        exportTaskRepository.persist(task);
        LOG.infof("Created export task %s for user %s", task.id, userId);

        // Trigger async export
        processExportAsync(task.id);

        return task.id;
    }

    /**
     * Process export task asynchronously
     */
    public void processExportAsync(UUID taskId) {
        new Thread(() -> {
            try {
                processExport(taskId);
            } catch (Exception e) {
                LOG.errorf("Export task %s failed: %s", taskId, e.getMessage());
                updateTaskStatus(taskId, ExportStatus.FAILED, null, null, e.getMessage());
            }
        }).start();
    }

    /**
     * Process export task
     */
    @Transactional
    public void processExport(UUID taskId) throws Exception {
        ExportTask task = exportTaskRepository.findById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("Export task not found: " + taskId);
        }

        updateTaskStatus(taskId, ExportStatus.PROCESSING, null, null, null);

        ExportRequest request = parseRequest(task.queryParams);
        List<?> data = fetchData(task.dataType, request);
        task.totalRecords = data.size();

        String fileName = generateFileName(task.dataType, task.exportType);
        String filePath = getExportFilePath(fileName);

        if (ExportType.EXCEL.name().equals(task.exportType)) {
            exportToExcel(data, task.dataType, filePath);
        } else if (ExportType.PDF.name().equals(task.exportType)) {
            exportToPdf(data, task.dataType, filePath);
        } else {
            throw new IllegalArgumentException("Unsupported export type: " + task.exportType);
        }

        task.fileName = fileName;
        updateTaskStatus(taskId, ExportStatus.COMPLETED, filePath, fileName, null);
        LOG.infof("Export task %s completed, file: %s", taskId, fileName);
    }

    /**
     * Update task status
     */
    @Transactional
    public void updateTaskStatus(UUID taskId, ExportStatus status, String filePath, String fileName, String errorMessage) {
        ExportTask task = exportTaskRepository.findById(taskId);
        if (task != null) {
            task.status = status.name();
            if (filePath != null) {
                task.filePath = filePath;
            }
            if (fileName != null) {
                task.fileName = fileName;
            }
            if (errorMessage != null) {
                task.errorMessage = errorMessage;
            }
            if (status == ExportStatus.COMPLETED || status == ExportStatus.FAILED) {
                task.completedAt = LocalDateTime.now();
            }
            exportTaskRepository.persist(task);
        }
    }

    /**
     * Get task status
     */
    public ExportTaskDTO getTaskStatus(UUID taskId) {
        ExportTask task = exportTaskRepository.findById(taskId);
        if (task == null) {
            return null;
        }
        return toDTO(task);
    }

    /**
     * Get user's export tasks
     */
    public ExportTaskListResponse getUserTasks(UUID userId) {
        List<ExportTask> tasks = exportTaskRepository.findRecentByUserId(userId, 20);
        List<ExportTaskDTO> dtos = tasks.stream()
            .map(this::toDTO)
            .collect(Collectors.toList());

        long total = exportTaskRepository.countByUserId(userId);
        int pendingCount = (int) exportTaskRepository.countByUserIdAndStatus(userId, ExportStatus.PENDING.name());
        int processingCount = (int) exportTaskRepository.countByUserIdAndStatus(userId, ExportStatus.PROCESSING.name());
        int completedCount = (int) exportTaskRepository.countByUserIdAndStatus(userId, ExportStatus.COMPLETED.name());
        int failedCount = (int) exportTaskRepository.countByUserIdAndStatus(userId, ExportStatus.FAILED.name());

        return new ExportTaskListResponse(dtos, total, pendingCount, processingCount, completedCount, failedCount);
    }

    /**
     * Get export file content
     */
    public byte[] getExportFile(UUID taskId) throws IOException {
        ExportTask task = exportTaskRepository.findById(taskId);
        if (task == null || task.filePath == null) {
            throw new FileNotFoundException("Export file not found");
        }

        Path path = Paths.get(task.filePath);
        if (!Files.exists(path)) {
            throw new FileNotFoundException("Export file not found: " + task.filePath);
        }

        return Files.readAllBytes(path);
    }

    /**
     * Convert task to DTO
     */
    private ExportTaskDTO toDTO(ExportTask task) {
        return new ExportTaskDTO(
            task.id,
            task.exportType,
            task.dataType,
            task.status,
            task.fileName,
            task.totalRecords,
            task.errorMessage,
            task.createdAt,
            task.completedAt
        );
    }

    /**
     * Parse request from JSON
     */
    private ExportRequest parseRequest(String json) {
        if (json == null || json.isEmpty()) {
            return new ExportRequest(null, null, null, null, null, null, null, null, null, null);
        }
        try {
            return objectMapper.readValue(json, ExportRequest.class);
        } catch (Exception e) {
            LOG.warnf("Failed to parse request: %s", e.getMessage());
            return new ExportRequest(null, null, null, null, null, null, null, null, null, null);
        }
    }

    /**
     * Fetch data based on type
     */
    private List<?> fetchData(String dataType, ExportRequest request) {
        LocalDateTime startTime = request.startTime();
        LocalDateTime endTime = request.endTime();
        String status = request.status();
        int limit = request.limit() != null ? request.limit() : 1000;

        LocalDateTime defaultStart = LocalDateTime.now().minusDays(30);
        LocalDateTime defaultEnd = LocalDateTime.now();

        switch (dataType) {
            case "DEPLOYMENT_HISTORY":
                return DeploymentHistory.find("createdAt >= ?1 AND createdAt <= ?2 ORDER BY createdAt DESC",
                    startTime != null ? startTime : defaultStart,
                    endTime != null ? endTime : defaultEnd)
                    .page(0, limit).list();

            case "COMMAND_LOG":
                return CommandExecution.find("createdAt >= ?1 AND createdAt <= ?2 ORDER BY createdAt DESC",
                    startTime != null ? startTime : defaultStart,
                    endTime != null ? endTime : defaultEnd)
                    .page(0, limit).list();

            case "AUDIT_LOG":
                List<AuditLog> auditLogs = AuditLog.find("createdAt >= ?1 AND createdAt <= ?2 ORDER BY createdAt DESC",
                    startTime != null ? startTime : defaultStart,
                    endTime != null ? endTime : defaultEnd)
                    .page(0, limit).list();
                return auditLogs;

            case "ALERT":
                List<AlertEvent> alertEvents = AlertEvent.find("createdAt >= ?1 AND createdAt <= ?2 ORDER BY createdAt DESC",
                    startTime != null ? startTime : defaultStart,
                    endTime != null ? endTime : defaultEnd)
                    .page(0, limit).list();
                return alertEvents;

            default:
                throw new IllegalArgumentException("Unsupported data type: " + dataType);
        }
    }

    /**
     * Export data to Excel
     */
    private void exportToExcel(List<?> data, String dataType, String filePath) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(getSheetName(dataType));

        // Create header style
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        // Create headers based on data type
        Row headerRow = sheet.createRow(0);
        String[] headers = getHeaders(dataType);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Fill data rows
        int rowNum = 1;
        for (Object item : data) {
            Row row = sheet.createRow(rowNum++);
            fillRow(row, item, dataType);
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write to file
        try (OutputStream out = new FileOutputStream(filePath)) {
            workbook.write(out);
        }
        workbook.close();
    }

    /**
     * Export data to PDF (simplified text-based format)
     */
    private void exportToPdf(List<?> data, String dataType, String filePath) throws Exception {
        StringBuilder content = new StringBuilder();
        content.append(getReportTitle(dataType)).append("\n");
        content.append("生成时间: ").append(LocalDateTime.now().format(DATE_FORMATTER)).append("\n");
        content.append("记录数量: ").append(data.size()).append("\n\n");
        content.append("========================================\n\n");

        for (Object item : data) {
            content.append(formatPdfContent(item, dataType)).append("\n");
            content.append("----------------------------------------\n");
        }

        // Write plain text as PDF (simplified approach using OpenPDF)
        com.lowagie.text.Document document = new com.lowagie.text.Document();
        com.lowagie.text.pdf.PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        com.lowagie.text.Font titleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 16, com.lowagie.text.Font.BOLD);
        com.lowagie.text.Paragraph title = new com.lowagie.text.Paragraph(getReportTitle(dataType), titleFont);
        title.setAlignment(com.lowagie.text.Paragraph.ALIGN_CENTER);
        document.add(title);

        com.lowagie.text.Font normalFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10);
        com.lowagie.text.Paragraph meta = new com.lowagie.text.Paragraph(
            "生成时间: " + LocalDateTime.now().format(DATE_FORMATTER) + "\n记录数量: " + data.size(),
            normalFont);
        document.add(meta);
        document.add(new com.lowagie.text.Paragraph("\n"));

        for (Object item : data) {
            com.lowagie.text.Paragraph para = new com.lowagie.text.Paragraph(formatPdfContent(item, dataType), normalFont);
            document.add(para);
            document.add(new com.lowagie.text.Paragraph("------------------------------------------------"));
        }

        document.close();
    }

    /**
     * Get sheet name for Excel
     */
    private String getSheetName(String dataType) {
        switch (dataType) {
            case "DEPLOYMENT_HISTORY":
                return "部署历史";
            case "COMMAND_LOG":
                return "命令执行";
            case "AUDIT_LOG":
                return "审计日志";
            case "ALERT":
                return "告警记录";
            default:
                return "数据";
        }
    }

    /**
     * Get headers for Excel columns
     */
    private String[] getHeaders(String dataType) {
        switch (dataType) {
            case "DEPLOYMENT_HISTORY":
                return new String[]{"ID", "Agent模板", "版本", "状态", "描述", "开始时间", "结束时间", "创建人", "创建时间"};
            case "COMMAND_LOG":
                return new String[]{"ID", "命令", "状态", "输出", "错误信息", "开始时间", "结束时间", "执行人", "创建时间"};
            case "AUDIT_LOG":
                return new String[]{"ID", "用户名", "操作", "结果", "描述", "资源类型", "IP地址", "请求路径", "执行时长(ms)", "创建时间"};
            case "ALERT":
                return new String[]{"ID", "类型", "级别", "状态", "标题", "消息", "资源类型", "确认人", "解决人", "创建时间"};
            default:
                return new String[]{"ID", "数据"};
        }
    }

    /**
     * Fill Excel row with data
     */
    private void fillRow(Row row, Object item, String dataType) {
        if (item instanceof DeploymentHistory) {
            DeploymentHistory dh = (DeploymentHistory) item;
            row.createCell(0).setCellValue(dh.id != null ? dh.id.toString() : "");
            String agentName = "";
            if (dh.agentInstance != null && dh.agentInstance.template != null) {
                agentName = dh.agentInstance.template.name != null ? dh.agentInstance.template.name : "";
            }
            row.createCell(1).setCellValue(agentName);
            row.createCell(2).setCellValue(dh.version != null ? dh.version : "");
            row.createCell(3).setCellValue(dh.status != null ? dh.status.name() : "");
            row.createCell(4).setCellValue(dh.description != null ? dh.description : "");
            row.createCell(5).setCellValue(dh.startedAt != null ? dh.startedAt.format(DATE_FORMATTER) : "");
            row.createCell(6).setCellValue(dh.finishedAt != null ? dh.finishedAt.format(DATE_FORMATTER) : "");
            row.createCell(7).setCellValue(dh.createdBy != null ? dh.createdBy : "");
            row.createCell(8).setCellValue(dh.createdAt != null ? dh.createdAt.format(DATE_FORMATTER) : "");
        } else if (item instanceof CommandExecution) {
            CommandExecution ce = (CommandExecution) item;
            row.createCell(0).setCellValue(ce.id != null ? ce.id.toString() : "");
            row.createCell(1).setCellValue(ce.command != null ? ce.command : "");
            row.createCell(2).setCellValue(ce.status != null ? ce.status.name() : "");
            row.createCell(3).setCellValue(ce.output != null ? truncate(ce.output, 500) : "");
            row.createCell(4).setCellValue(ce.errorMessage != null ? truncate(ce.errorMessage, 500) : "");
            row.createCell(5).setCellValue(ce.startedAt != null ? ce.startedAt.format(DATE_FORMATTER) : "");
            row.createCell(6).setCellValue(ce.finishedAt != null ? ce.finishedAt.format(DATE_FORMATTER) : "");
            row.createCell(7).setCellValue(ce.executedBy != null ? ce.executedBy : "");
            row.createCell(8).setCellValue(ce.createdAt != null ? ce.createdAt.format(DATE_FORMATTER) : "");
        } else if (item instanceof AuditLog) {
            AuditLog al = (AuditLog) item;
            row.createCell(0).setCellValue(al.id != null ? al.id.toString() : "");
            row.createCell(1).setCellValue(al.username != null ? al.username : "");
            row.createCell(2).setCellValue(al.action != null ? al.action.name() : "");
            row.createCell(3).setCellValue(al.result != null ? al.result.name() : "");
            row.createCell(4).setCellValue(al.description != null ? al.description : "");
            row.createCell(5).setCellValue(al.resourceType != null ? al.resourceType : "");
            row.createCell(6).setCellValue(al.clientIp != null ? al.clientIp : "");
            row.createCell(7).setCellValue(al.requestPath != null ? al.requestPath : "");
            row.createCell(8).setCellValue(al.duration != null ? al.duration : 0);
            row.createCell(9).setCellValue(al.createdAt != null ? al.createdAt.format(DATE_FORMATTER) : "");
        } else if (item instanceof AlertEvent) {
            AlertEvent ae = (AlertEvent) item;
            row.createCell(0).setCellValue(ae.id != null ? ae.id.toString() : "");
            row.createCell(1).setCellValue(ae.eventType != null ? ae.eventType.name() : "");
            row.createCell(2).setCellValue(ae.level != null ? ae.level.name() : "");
            row.createCell(3).setCellValue(ae.status != null ? ae.status.name() : "");
            row.createCell(4).setCellValue(ae.title != null ? ae.title : "");
            row.createCell(5).setCellValue(ae.message != null ? truncate(ae.message, 500) : "");
            row.createCell(6).setCellValue(ae.resourceType != null ? ae.resourceType : "");
            row.createCell(7).setCellValue(ae.acknowledgedBy != null ? ae.acknowledgedBy : "");
            row.createCell(8).setCellValue(ae.resolvedBy != null ? ae.resolvedBy : "");
            row.createCell(9).setCellValue(ae.createdAt != null ? ae.createdAt.format(DATE_FORMATTER) : "");
        }
    }

    /**
     * Get report title for PDF
     */
    private String getReportTitle(String dataType) {
        switch (dataType) {
            case "DEPLOYMENT_HISTORY":
                return "部署历史报告";
            case "COMMAND_LOG":
                return "命令执行报告";
            case "AUDIT_LOG":
                return "审计日志报告";
            case "ALERT":
                return "告警记录报告";
            default:
                return "数据导出报告";
        }
    }

    /**
     * Format content for PDF
     */
    private String formatPdfContent(Object item, String dataType) {
        if (item instanceof DeploymentHistory) {
            DeploymentHistory dh = (DeploymentHistory) item;
            String agentName = "";
            if (dh.agentInstance != null && dh.agentInstance.template != null) {
                agentName = dh.agentInstance.template.name != null ? dh.agentInstance.template.name : "";
            }
            return String.format("ID: %s\nAgent模板: %s\n版本: %s\n状态: %s\n描述: %s\n开始时间: %s\n结束时间: %s\n创建人: %s",
                dh.id, agentName, dh.version, dh.status, dh.description,
                dh.startedAt != null ? dh.startedAt.format(DATE_FORMATTER) : "",
                dh.finishedAt != null ? dh.finishedAt.format(DATE_FORMATTER) : "",
                dh.createdBy);
        } else if (item instanceof CommandExecution) {
            CommandExecution ce = (CommandExecution) item;
            return String.format("ID: %s\n命令: %s\n状态: %s\n开始时间: %s\n结束时间: %s\n执行人: %s",
                ce.id, truncate(ce.command, 200), ce.status,
                ce.startedAt != null ? ce.startedAt.format(DATE_FORMATTER) : "",
                ce.finishedAt != null ? ce.finishedAt.format(DATE_FORMATTER) : "",
                ce.executedBy);
        } else if (item instanceof AuditLog) {
            AuditLog al = (AuditLog) item;
            return String.format("ID: %s\n用户: %s\n操作: %s\n结果: %s\n描述: %s\nIP: %s\n时间: %s",
                al.id, al.username, al.action, al.result, al.description, al.clientIp,
                al.createdAt != null ? al.createdAt.format(DATE_FORMATTER) : "");
        } else if (item instanceof AlertEvent) {
            AlertEvent ae = (AlertEvent) item;
            return String.format("ID: %s\n类型: %s\n级别: %s\n状态: %s\n标题: %s\n消息: %s\n时间: %s",
                ae.id, ae.eventType, ae.level, ae.status, ae.title,
                truncate(ae.message, 200),
                ae.createdAt != null ? ae.createdAt.format(DATE_FORMATTER) : "");
        }
        return item.toString();
    }

    /**
     * Generate file name
     */
    private String generateFileName(String dataType, String exportType) {
        String extension = ExportType.EXCEL.name().equals(exportType) ? ".xlsx" : ".pdf";
        String typePrefix = dataType.toLowerCase().replace("_", "-");
        return typePrefix + "_" + LocalDateTime.now().format(FILE_DATE_FORMATTER) + extension;
    }

    /**
     * Get export file path
     */
    private String getExportFilePath(String fileName) throws IOException {
        Path exportDir = Paths.get(EXPORT_DIR);
        if (!Files.exists(exportDir)) {
            Files.createDirectories(exportDir);
        }
        return exportDir.resolve(fileName).toString();
    }

    /**
     * Truncate string to max length
     */
    private String truncate(String str, int maxLength) {
        if (str == null) {
            return "";
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...";
    }
}