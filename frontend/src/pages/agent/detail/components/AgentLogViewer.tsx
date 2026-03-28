import React, { useEffect, useRef, useState, useCallback } from "react";
import {
  Card,
  Select,
  Button,
  Space,
  Input,
  Spin,
  Empty,
  Tag,
  Tooltip,
  Statistic,
  Row,
  Col,
  Divider,
} from "antd";
import {
  SearchOutlined,
  ReloadOutlined,
  PauseCircleOutlined,
  PlayCircleOutlined,
  ClearOutlined,
  DownloadOutlined,
} from "@ant-design/icons";
import type { LogLevel, LogEntry } from "@/types/agentMonitoring";
import styles from "../AgentDetail.module.css";

interface AgentLogViewerProps {
  agentId: string;
  wsConnected?: boolean;
}

const LogLevelConfig: Record<LogLevel, { color: string; className: string }> = {
  DEBUG: { color: "default", className: styles.logLevelDebug },
  INFO: { color: "blue", className: styles.logLevelInfo },
  WARN: { color: "orange", className: styles.logLevelWarn },
  ERROR: { color: "red", className: styles.logLevelError },
};

/**
 * Agent 日志查看器组件
 * 支持实时日志流展示、日志级别过滤、自动滚动、暂停/继续功能
 */
const AgentLogViewer: React.FC<AgentLogViewerProps> = ({
  agentId,
  wsConnected = false,
}) => {
  const [logs, setLogs] = useState<LogEntry[]>([]);
  const [filteredLogs, setFilteredLogs] = useState<LogEntry[]>([]);
  const [levelFilter, setLevelFilter] = useState<LogLevel | undefined>();
  const [keywordFilter, setKeywordFilter] = useState<string>("");
  const [isPaused, setIsPaused] = useState(false);
  const [autoScroll, setAutoScroll] = useState(true);
  const [loading, setLoading] = useState(false);
  const [wsLogs, setWsLogs] = useState<boolean>(false);
  const logContainerRef = useRef<HTMLDivElement>(null);
  const wsRef = useRef<WebSocket | null>(null);

  // WebSocket 连接用于实时日志
  useEffect(() => {
    if (!wsConnected) return;

    const wsUrl = `${window.location.protocol === "https:" ? "wss:" : "ws:"}//${window.location.host}/ws/console/${agentId}`;
    const ws = new WebSocket(wsUrl);

    ws.onopen = () => {
      console.log(`Log WebSocket connected to agent ${agentId}`);
      // 请求历史日志
      ws.send(JSON.stringify({ type: "FETCH_LOGS" }));
      setWsLogs(true);
    };

    ws.onmessage = (event) => {
      try {
        const message = JSON.parse(event.data);
        if (message.type === "LOG_HISTORY" && Array.isArray(message.content)) {
          const historyLogs: LogEntry[] = message.content.map(
            (line: string, index: number) => ({
              lineNumber: index + 1,
              timestamp: new Date().toISOString(),
              level: "INFO" as LogLevel,
              message: line,
            }),
          );
          setLogs(historyLogs);
        } else if (message.type === "LOG_APPEND" && message.logs) {
          if (!isPaused) {
            setLogs((prev) => [...prev, ...message.logs]);
          }
        }
      } catch (e) {
        // 非 JSON 格式，作为原始日志处理
        if (!isPaused) {
          const newLog: LogEntry = {
            lineNumber: logs.length + 1,
            timestamp: new Date().toISOString(),
            level: "INFO",
            message: event.data,
          };
          setLogs((prev) => [...prev, newLog]);
        }
      }
    };

    ws.onclose = () => {
      setWsLogs(false);
    };

    ws.onerror = () => {
      setWsLogs(false);
    };

    wsRef.current = ws;

    return () => {
      ws.close();
    };
  }, [agentId, wsConnected, isPaused]);

  // 过滤日志
  useEffect(() => {
    let filtered = logs;
    if (levelFilter) {
      filtered = filtered.filter((log) => log.level === levelFilter);
    }
    if (keywordFilter) {
      filtered = filtered.filter((log) =>
        log.message.toLowerCase().includes(keywordFilter.toLowerCase()),
      );
    }
    setFilteredLogs(filtered);
  }, [logs, levelFilter, keywordFilter]);

  // 自动滚动到底部
  useEffect(() => {
    if (autoScroll && !isPaused && logContainerRef.current) {
      logContainerRef.current.scrollTop = logContainerRef.current.scrollHeight;
    }
  }, [filteredLogs, autoScroll, isPaused]);

  // 加载历史日志（API 方式）
  const loadHistoryLogs = useCallback(async () => {
    setLoading(true);
    try {
      // 这里使用 agentMonitoringService.getLogs
      // 由于 WebSocket 可能未连接，作为备选方案
      const response = await fetch(`/api/agents/logs/${agentId}?limit=200`);
      if (response.ok) {
        const data = await response.json();
        setLogs(data.logs || []);
      }
    } catch (e) {
      console.error("Failed to load logs", e);
    } finally {
      setLoading(false);
    }
  }, [agentId]);

  // 初始加载历史日志
  useEffect(() => {
    if (!wsConnected) {
      loadHistoryLogs();
    }
  }, [agentId, wsConnected, loadHistoryLogs]);

  // 清空日志
  const handleClear = () => {
    setLogs([]);
    setFilteredLogs([]);
  };

  // 下载日志
  const handleDownload = () => {
    const content = filteredLogs
      .map((log) => `[${log.timestamp}] [${log.level}] ${log.message}`)
      .join("\n");
    const blob = new Blob([content], { type: "text/plain" });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = `agent-${agentId}-logs-${new Date().toISOString().slice(0, 10)}.txt`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  };

  // 计算日志统计
  const logStats = {
    total: logs.length,
    error: logs.filter((l) => l.level === "ERROR").length,
    warn: logs.filter((l) => l.level === "WARN").length,
    info: logs.filter((l) => l.level === "INFO").length,
    debug: logs.filter((l) => l.level === "DEBUG").length,
  };

  // 渲染单行日志
  const renderLogLine = (log: LogEntry) => {
    const levelConfig = LogLevelConfig[log.level];
    return (
      <div
        key={`${log.lineNumber}-${log.timestamp}`}
        className={styles.logLine}
      >
        <span className={styles.logLineNumber}>{log.lineNumber}</span>
        <span className={styles.logTimestamp}>
          {log.timestamp ? new Date(log.timestamp).toLocaleTimeString() : ""}
        </span>
        <span className={`${styles.logLevel} ${levelConfig.className}`}>
          [{log.level}]
        </span>
        <span className={styles.logMessage}>{log.message}</span>
      </div>
    );
  };

  return (
    <Card
      className={styles.logViewer}
      title={
        <Space>
          <span>实时日志</span>
          {wsLogs ? (
            <Tag color="success">实时</Tag>
          ) : (
            <Tag color="default">历史</Tag>
          )}
          {isPaused && <Tag color="warning">已暂停</Tag>}
        </Space>
      }
      extra={
        <Space>
          <Tooltip title={isPaused ? "继续" : "暂停"}>
            <Button
              icon={isPaused ? <PlayCircleOutlined /> : <PauseCircleOutlined />}
              onClick={() => setIsPaused(!isPaused)}
              size="small"
            />
          </Tooltip>
          <Tooltip title="清空">
            <Button
              icon={<ClearOutlined />}
              onClick={handleClear}
              size="small"
            />
          </Tooltip>
          <Tooltip title="下载">
            <Button
              icon={<DownloadOutlined />}
              onClick={handleDownload}
              size="small"
            />
          </Tooltip>
          <Tooltip title="刷新">
            <Button
              icon={<ReloadOutlined />}
              onClick={loadHistoryLogs}
              loading={loading}
              size="small"
            />
          </Tooltip>
        </Space>
      }
    >
      {/* 日志统计 */}
      <Row gutter={16} style={{ marginBottom: 12 }}>
        <Col span={4}>
          <Statistic title="总条数" value={logStats.total} />
        </Col>
        <Col span={4}>
          <Statistic
            title="错误"
            value={logStats.error}
            valueStyle={{ color: "#cf1322" }}
          />
        </Col>
        <Col span={4}>
          <Statistic
            title="警告"
            value={logStats.warn}
            valueStyle={{ color: "#fa8c16" }}
          />
        </Col>
        <Col span={4}>
          <Statistic title="信息" value={logStats.info} />
        </Col>
        <Col span={4}>
          <Statistic title="调试" value={logStats.debug} />
        </Col>
      </Row>

      <Divider style={{ margin: "12px 0" }} />

      {/* 日志过滤 */}
      <div className={styles.logHeader}>
        <div className={styles.logFilters}>
          <Select
            placeholder="日志级别"
            allowClear
            style={{ width: 100 }}
            onChange={setLevelFilter}
            options={[
              { label: "ERROR", value: "ERROR" },
              { label: "WARN", value: "WARN" },
              { label: "INFO", value: "INFO" },
              { label: "DEBUG", value: "DEBUG" },
            ]}
          />
          <Input
            placeholder="搜索关键词"
            allowClear
            prefix={<SearchOutlined />}
            onChange={(e) => setKeywordFilter(e.target.value)}
            style={{ width: 200 }}
          />
          <Tooltip title={autoScroll ? "停止自动滚动" : "开启自动滚动"}>
            <Button
              type={autoScroll ? "primary" : "default"}
              size="small"
              onClick={() => setAutoScroll(!autoScroll)}
            >
              自动滚动
            </Button>
          </Tooltip>
        </div>
      </div>

      {/* 日志内容 */}
      <Spin spinning={loading}>
        <div
          ref={logContainerRef}
          className={styles.logContainer}
          style={{ position: "relative" }}
        >
          {filteredLogs.length > 0 ? (
            filteredLogs.map(renderLogLine)
          ) : (
            <Empty description="暂无日志" />
          )}
          {isPaused && (
            <div className={styles.logPausedOverlay}>
              <Space>
                <PauseCircleOutlined />
                <span>日志已暂停，点击继续按钮恢复</span>
              </Space>
            </div>
          )}
        </div>
      </Spin>
    </Card>
  );
};

export default AgentLogViewer;
