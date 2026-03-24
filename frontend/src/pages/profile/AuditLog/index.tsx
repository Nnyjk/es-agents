import React, { useRef, useState } from "react";
import { ProTable, ModalForm } from "@ant-design/pro-components";
import {
  Card,
  Tag,
  Button,
  Descriptions,
  Typography,
  Statistic,
  Row,
  Col,
  message,
} from "antd";
import type { ProColumns, ActionType } from "@ant-design/pro-components";
import {
  EyeOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  ClockCircleOutlined,
  DownloadOutlined,
} from "@ant-design/icons";
import dayjs from "dayjs";
import {
  getAuditLogs,
  getAuditLogDetail,
  getAuditLogSummary,
  exportAuditLogs,
} from "@/services/audit";
import type { AuditLog, AuditLogQuery, AuditLogSummary } from "@/types/audit";

const { Text } = Typography;

// 操作类型配置
const actionConfig: Record<string, { color: string; label: string }> = {
  CREATE: { color: "green", label: "创建" },
  UPDATE: { color: "blue", label: "更新" },
  DELETE: { color: "red", label: "删除" },
  LOGIN: { color: "cyan", label: "登录" },
  LOGOUT: { color: "orange", label: "登出" },
  EXPORT: { color: "purple", label: "导出" },
  IMPORT: { color: "magenta", label: "导入" },
  DEPLOY: { color: "geekblue", label: "部署" },
  EXECUTE: { color: "gold", label: "执行" },
};

// 状态配置
const statusConfig: Record<string, { color: string; icon: React.ReactNode }> = {
  SUCCESS: { color: "success", icon: <CheckCircleOutlined /> },
  FAILED: { color: "error", icon: <CloseCircleOutlined /> },
  PENDING: { color: "processing", icon: <ClockCircleOutlined /> },
};

const AuditLogList: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [detailVisible, setDetailVisible] = useState(false);
  const [currentLog, setCurrentLog] = useState<AuditLog | null>(null);
  const [summary, setSummary] = useState<AuditLogSummary | null>(null);
  const lastQueryRef = useRef<AuditLogQuery>({});

  // 加载统计数据
  const loadSummary = async () => {
    try {
      const data = await getAuditLogSummary();
      setSummary(data);
    } catch (error) {
      console.error("加载统计数据失败:", error);
    }
  };

  // 查看详情
  const handleViewDetail = async (record: AuditLog) => {
    try {
      const detail = await getAuditLogDetail(record.id);
      setCurrentLog(detail);
      setDetailVisible(true);
    } catch (error) {
      message.error("加载详情失败");
    }
  };

  // 导出
  const handleExport = async (params: AuditLogQuery) => {
    try {
      const blob = await exportAuditLogs(params);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `audit-logs-${dayjs().format("YYYY-MM-DD-HHmmss")}.csv`;
      a.click();
      window.URL.revokeObjectURL(url);
      message.success("导出成功");
    } catch (error) {
      message.error("导出失败");
    }
  };

  const columns: ProColumns<AuditLog>[] = [
    {
      title: "时间",
      dataIndex: "createdAt",
      key: "createdAt",
      width: 180,
      render: (_, record) =>
        dayjs(record.createdAt).format("YYYY-MM-DD HH:mm:ss"),
      sorter: true,
    },
    {
      title: "操作类型",
      dataIndex: "action",
      key: "action",
      width: 100,
      render: (_, record) => {
        const config = actionConfig[record.action] || {
          color: "default",
          label: record.action,
        };
        return <Tag color={config.color}>{config.label}</Tag>;
      },
      valueEnum: Object.entries(actionConfig).reduce(
        (acc, [key, value]) => ({
          ...acc,
          [key]: { text: value.label },
        }),
        {},
      ),
    },
    {
      title: "资源类型",
      dataIndex: "resourceType",
      key: "resourceType",
      width: 120,
    },
    {
      title: "资源ID",
      dataIndex: "resourceId",
      key: "resourceId",
      width: 200,
      ellipsis: true,
      copyable: true,
    },
    {
      title: "描述",
      dataIndex: "description",
      key: "description",
      ellipsis: true,
    },
    {
      title: "IP地址",
      dataIndex: "ipAddress",
      key: "ipAddress",
      width: 140,
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 100,
      render: (_, record) => {
        const config = statusConfig[record.status] || statusConfig.PENDING;
        return (
          <Tag icon={config.icon} color={config.color}>
            {record.status}
          </Tag>
        );
      },
      valueEnum: {
        SUCCESS: { text: "成功", status: "Success" },
        FAILED: { text: "失败", status: "Error" },
        PENDING: { text: "处理中", status: "Processing" },
      },
    },
    {
      title: "耗时",
      dataIndex: "durationMs",
      key: "durationMs",
      width: 100,
      render: (_, record) =>
        record.durationMs ? `${record.durationMs}ms` : "-",
    },
    {
      title: "操作",
      key: "action",
      width: 80,
      fixed: "right",
      render: (_, record) => (
        <Button
          type="link"
          size="small"
          icon={<EyeOutlined />}
          onClick={() => handleViewDetail(record)}
        >
          详情
        </Button>
      ),
    },
  ];

  React.useEffect(() => {
    loadSummary();
  }, []);

  return (
    <div>
      {/* 统计卡片 */}
      <Card style={{ marginBottom: 16 }}>
        <Row gutter={24}>
          <Col span={6}>
            <Statistic title="总操作数" value={summary?.totalCount || 0} />
          </Col>
          <Col span={6}>
            <Statistic
              title="成功操作"
              value={summary?.successCount || 0}
              valueStyle={{ color: "#3f8600" }}
            />
          </Col>
          <Col span={6}>
            <Statistic
              title="失败操作"
              value={summary?.failedCount || 0}
              valueStyle={{ color: "#cf1322" }}
            />
          </Col>
          <Col span={6}>
            <Statistic
              title="成功率"
              value={
                summary?.totalCount
                  ? ((summary.successCount / summary.totalCount) * 100).toFixed(
                      1,
                    )
                  : "0"
              }
              suffix="%"
            />
          </Col>
        </Row>
      </Card>

      {/* 日志列表 */}
      <ProTable<AuditLog, AuditLogQuery>
        columns={columns}
        actionRef={actionRef}
        rowKey="id"
        scroll={{ x: 1200 }}
        request={async (params) => {
          const { current, pageSize, ...rest } = params;
          const query = { ...rest, current, pageSize };
          lastQueryRef.current = query;
          const response = await getAuditLogs(query);
          return {
            data: response.data,
            total: response.total,
            success: response.success,
          };
        }}
        search={{
          labelWidth: "auto",
        }}
        toolBarRender={() => [
          <Button
            key="export"
            icon={<DownloadOutlined />}
            onClick={() => handleExport(lastQueryRef.current)}
          >
            导出
          </Button>,
        ]}
        options={{
          density: true,
          fullScreen: true,
          reload: true,
        }}
        pagination={{
          defaultPageSize: 20,
          showSizeChanger: true,
          showQuickJumper: true,
        }}
      />

      {/* 详情弹窗 */}
      <ModalForm
        title="审计日志详情"
        open={detailVisible}
        onOpenChange={setDetailVisible}
        submitter={false}
        width={700}
      >
        {currentLog && (
          <Descriptions column={2} bordered>
            <Descriptions.Item label="日志ID" span={2}>
              <Text copyable>{currentLog.id}</Text>
            </Descriptions.Item>
            <Descriptions.Item label="操作类型">
              <Tag color={actionConfig[currentLog.action]?.color || "default"}>
                {actionConfig[currentLog.action]?.label || currentLog.action}
              </Tag>
            </Descriptions.Item>
            <Descriptions.Item label="状态">
              <Tag
                icon={statusConfig[currentLog.status]?.icon}
                color={statusConfig[currentLog.status]?.color}
              >
                {currentLog.status}
              </Tag>
            </Descriptions.Item>
            <Descriptions.Item label="资源类型">
              {currentLog.resourceType}
            </Descriptions.Item>
            <Descriptions.Item label="资源ID">
              <Text copyable>{currentLog.resourceId}</Text>
            </Descriptions.Item>
            <Descriptions.Item label="描述" span={2}>
              {currentLog.description}
            </Descriptions.Item>
            <Descriptions.Item label="IP地址">
              {currentLog.ipAddress}
            </Descriptions.Item>
            <Descriptions.Item label="耗时">
              {currentLog.durationMs ? `${currentLog.durationMs}ms` : "-"}
            </Descriptions.Item>
            <Descriptions.Item label="User-Agent" span={2}>
              <Text
                style={{ maxWidth: 500 }}
                ellipsis={{ tooltip: currentLog.userAgent }}
              >
                {currentLog.userAgent}
              </Text>
            </Descriptions.Item>
            <Descriptions.Item label="操作时间" span={2}>
              {dayjs(currentLog.createdAt).format("YYYY-MM-DD HH:mm:ss")}
            </Descriptions.Item>
            {currentLog.errorMessage && (
              <Descriptions.Item label="错误信息" span={2}>
                <Text type="danger">{currentLog.errorMessage}</Text>
              </Descriptions.Item>
            )}
          </Descriptions>
        )}
      </ModalForm>
    </div>
  );
};

export default AuditLogList;
