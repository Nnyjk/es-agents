# 任务：全链路数据导出能力 #122

**优先级**: medium  
**里程碑**: M3 - 日志与监控  
**截止**: 2026-05-31

## 需求描述

实现各模块数据导出能力：
1. 支持部署历史、命令执行记录、审计日志、告警记录导出为 Excel
2. 支持日志、部署报告导出为 PDF
3. 导出任务异步执行，完成后通知用户下载
4. 导出权限控制，仅授权用户可导出敏感数据

## 技术选型

- **Excel 导出**: Apache POI 5.x
- **PDF 导出**: OpenPDF 1.3.x (开源，LGPL/MPL)
- **异步执行**: Quarkus @Asynchronous + @Blocking
- **任务跟踪**: 数据库表 export_task 记录状态

## 实现步骤

### 1. 添加依赖 (server/pom.xml)

```xml
<!-- Apache POI for Excel -->
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.5</version>
</dependency>

<!-- OpenPDF for PDF -->
<dependency>
    <groupId>com.github.librepdf</groupId>
    <artifactId>openpdf</artifactId>
    <version>1.3.43</version>
</dependency>
```

### 2. 创建导出任务实体

文件：`server/src/main/java/com/easy/station/entity/ExportTask.java`

字段：
- id (UUID)
- userId (String)
- exportType (String: EXCEL/PDF)
- dataType (String: DEPLOYMENT_HISTORY/COMMAND_LOG/AUDIT_LOG/ALERT)
- status (String: PENDING/PROCESSING/COMPLETED/FAILED)
- filePath (String)
- fileName (String)
- errorMessage (String)
- createdAt (Instant)
- completedAt (Instant)

### 3. 创建 ExportService

文件：`server/src/main/java/com/easy/station/service/ExportService.java`

方法：
- `ExportTask createExportTask(String userId, String exportType, String dataType, Map<String, Object> params)`
- `void processExportTask(ExportTask task)`
- `ExportTask getExportTask(UUID id)`
- `File downloadExportFile(UUID id)`

### 4. 创建 ExportResource API

文件：`server/src/main/java/com/easy/station/resource/ExportResource.java`

接口：
- `POST /api/v1/exports/excel` - 创建 Excel 导出任务
- `POST /api/v1/exports/pdf` - 创建 PDF 导出任务
- `GET /api/v1/exports/{id}` - 查询导出任务状态
- `GET /api/v1/exports/{id}/download` - 下载导出文件
- `GET /api/v1/exports` - 查询用户导出历史

### 5. 创建 Excel 导出工具

文件：`server/src/main/java/com/easy/station/util/ExcelExporter.java`

方法：
- `byte[] exportDeploymentHistory(List<DeploymentHistory> records)`
- `byte[] exportCommandLogs(List<CommandExecution> records)`
- `byte[] exportAuditLogs(List<AuditLog> records)`

### 6. 创建 PDF 导出工具

文件：`server/src/main/java/com/easy/station/util/PdfExporter.java`

方法：
- `byte[] exportDeploymentReport(Deployment deployment)`
- `byte[] exportSystemLogs(List<SystemEventLog> logs)`

### 7. 前端 - 导出按钮组件

文件：`frontend/src/components/ExportButton.tsx`

功能：
- 导出按钮（支持 Excel/PDF）
- 导出任务状态提示
- 下载链接展示

### 8. 前端 - 集成到各页面

- 部署历史页面
- 命令执行记录页面
- 审计日志页面
- 系统事件日志页面

## 验收标准

- [ ] Excel 导出部署历史
- [ ] Excel 导出命令执行记录
- [ ] Excel 导出审计日志
- [ ] PDF 导出部署报告
- [ ] PDF 导出系统日志
- [ ] 导出任务异步执行
- [ ] 导出任务状态可查询
- [ ] 导出文件可下载
- [ ] 权限控制正常（仅授权用户可下载）

## 注意事项

1. 大文件导出使用流式处理，避免内存溢出
2. 导出文件设置过期时间（如 7 天后自动清理）
3. 敏感数据导出需要额外权限验证
4. 导出任务失败需要记录错误信息
