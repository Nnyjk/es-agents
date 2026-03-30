-- 诊断规则表
CREATE TABLE diagnostic_rules (
    id BIGSERIAL PRIMARY KEY,
    rule_id VARCHAR(64) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(32) NOT NULL,
    condition VARCHAR(512) NOT NULL,
    severity VARCHAR(16) NOT NULL,
    recommendation TEXT,
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 诊断报告表
CREATE TABLE diagnostic_reports (
    id BIGSERIAL PRIMARY KEY,
    report_id VARCHAR(64) UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    total_findings INTEGER DEFAULT 0,
    info_count INTEGER DEFAULT 0,
    warning_count INTEGER DEFAULT 0,
    critical_count INTEGER DEFAULT 0,
    fatal_count INTEGER DEFAULT 0,
    summary TEXT,
    created_by VARCHAR(64),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 诊断发现表
CREATE TABLE diagnostic_findings (
    id BIGSERIAL PRIMARY KEY,
    finding_id VARCHAR(64) UNIQUE NOT NULL,
    report_id VARCHAR(64) NOT NULL,
    rule_id VARCHAR(64),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    severity VARCHAR(16) NOT NULL,
    metric_name VARCHAR(128),
    metric_value DOUBLE PRECISION,
    threshold_value DOUBLE PRECISION,
    impact TEXT,
    recommendation TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_finding_report FOREIGN KEY (report_id) REFERENCES diagnostic_reports(report_id) ON DELETE CASCADE
);

-- 索引
CREATE INDEX idx_diagnostic_rules_category ON diagnostic_rules(category);
CREATE INDEX idx_diagnostic_rules_enabled ON diagnostic_rules(enabled);
CREATE INDEX idx_diagnostic_reports_status ON diagnostic_reports(status);
CREATE INDEX idx_diagnostic_reports_created_at ON diagnostic_reports(created_at DESC);
CREATE INDEX idx_diagnostic_findings_report_id ON diagnostic_findings(report_id);
CREATE INDEX idx_diagnostic_findings_severity ON diagnostic_findings(severity);

-- 初始化默认诊断规则
INSERT INTO diagnostic_rules (rule_id, name, description, category, condition, severity, recommendation, enabled) VALUES
('RULE-001', 'CPU使用率过高', '检测系统CPU使用率是否超过阈值', 'SYSTEM', 'system.cpu.usage > 80', 'WARNING', '检查是否有异常进程占用CPU，考虑扩容或优化应用性能', true),
('RULE-002', 'CPU使用率严重过高', '检测系统CPU使用率是否严重超标', 'SYSTEM', 'system.cpu.usage > 95', 'CRITICAL', '立即检查系统进程，可能需要重启服务或扩容资源', true),
('RULE-003', '内存使用率过高', '检测系统内存使用率是否超过阈值', 'SYSTEM', 'system.memory.usage > 85', 'WARNING', '检查内存泄漏，考虑增加JVM堆内存或优化应用', true),
('RULE-004', '内存使用率严重过高', '检测系统内存使用率是否严重超标', 'SYSTEM', 'system.memory.usage > 95', 'CRITICAL', '立即检查内存使用情况，可能需要重启服务', true),
('RULE-005', '磁盘使用率过高', '检测磁盘使用率是否超过阈值', 'SYSTEM', 'system.disk.usage > 85', 'WARNING', '清理磁盘空间，检查日志文件大小', true),
('RULE-006', '磁盘使用率严重过高', '检测磁盘使用率是否严重超标', 'SYSTEM', 'system.disk.usage > 95', 'CRITICAL', '立即清理磁盘空间，可能影响系统稳定性', true),
('RULE-007', '告警数量过多', '检测最近24小时告警数量是否过多', 'ALERT', 'alerts.count.24h > 50', 'WARNING', '检查告警来源，优化监控阈值减少误报', true),
('RULE-008', '严重告警存在', '检测是否存在未处理的严重告警', 'ALERT', 'alerts.count.critical > 0', 'CRITICAL', '立即处理严重告警，检查系统核心功能', true);
