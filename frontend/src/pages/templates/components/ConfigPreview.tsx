import React from "react";
import {
  Card,
  Descriptions,
  Typography,
  Tag,
  Space,
  Table,
  Divider,
  Alert,
  Collapse,
} from "antd";
import { CheckCircleOutlined, InfoCircleOutlined } from "@ant-design/icons";
import type { AgentTemplate, Host } from "../../../types";
import type { InstanceConfig } from "../types";

const { Text } = Typography;

// Template category mapping
const CATEGORY_MAP: Record<string, { text: string; color: string }> = {
  MONITORING: { text: "监控", color: "blue" },
  DEPLOYMENT: { text: "部署", color: "green" },
  BACKUP: { text: "备份", color: "orange" },
  SECURITY: { text: "安全", color: "red" },
  DATABASE: { text: "数据库", color: "purple" },
  NETWORK: { text: "网络", color: "cyan" },
  UTILITY: { text: "工具", color: "geekblue" },
  CUSTOM: { text: "自定义", color: "default" },
};

// Host status mapping
const statusColors: Record<string, string> = {
  ONLINE: "green",
  OFFLINE: "red",
  UNCONNECTED: "orange",
  EXCEPTION: "error",
  MAINTENANCE: "blue",
};

const statusText: Record<string, string> = {
  ONLINE: "在线",
  OFFLINE: "离线",
  UNCONNECTED: "未连接",
  EXCEPTION: "异常",
  MAINTENANCE: "维护中",
};

interface ConfigPreviewProps {
  template: AgentTemplate | null;
  instanceConfig: InstanceConfig;
  hosts: Host[];
}

export const ConfigPreview: React.FC<ConfigPreviewProps> = ({
  template,
  instanceConfig,
  hosts,
}) => {
  if (!template) {
    return (
      <Alert
        message="缺少模板信息"
        description="请返回第一步选择模板"
        type="error"
        showIcon
      />
    );
  }

  if (hosts.length === 0) {
    return (
      <Alert
        message="未选择主机"
        description="请返回第三步选择目标主机"
        type="error"
        showIcon
      />
    );
  }

  const category = CATEGORY_MAP[template.category as string];

  // Calculate summary stats
  const onlineCount = hosts.filter((h) => h.status === "ONLINE").length;
  const offlineCount = hosts.filter((h) => h.status === "OFFLINE").length;
  const otherCount = hosts.length - onlineCount - offlineCount;

  // Host table columns
  const hostColumns = [
    {
      title: "序号",
      key: "index",
      width: 60,
      render: (_: any, __: Host, index: number) => index + 1,
    },
    {
      title: "主机名称",
      dataIndex: "name",
      width: 120,
    },
    {
      title: "实例名称",
      key: "instanceName",
      width: 200,
      render: (_: any, host: Host) => (
        <Text>
          {instanceConfig.name || "agent"}-{template.name}-{host.name}
        </Text>
      ),
    },
    {
      title: "主机地址",
      dataIndex: "hostname",
      width: 150,
      ellipsis: true,
    },
    {
      title: "环境",
      dataIndex: "environmentName",
      width: 80,
      render: (envName: string) =>
        envName ? <Tag color="blue">{envName}</Tag> : "-",
    },
    {
      title: "状态",
      dataIndex: "status",
      width: 80,
      render: (status: string) => (
        <Tag color={statusColors[status] || "default"}>
          {statusText[status] || status}
        </Tag>
      ),
    },
    {
      title: "Gateway",
      dataIndex: "gatewayUrl",
      ellipsis: true,
      render: (url: string) => (
        <Text type="secondary" style={{ fontSize: 12 }}>
          {url}
        </Text>
      ),
    },
  ];

  return (
    <div>
      <Alert
        message="请确认以下配置信息"
        description='确认无误后点击"开始创建"按钮，将批量创建 Agent 实例并绑定到所选主机'
        type="info"
        showIcon
        icon={<InfoCircleOutlined />}
        style={{ marginBottom: 16 }}
      />

      {/* Template Summary */}
      <Card
        title={
          <Space>
            <CheckCircleOutlined style={{ color: "#52c41a" }} />
            <span>模板信息</span>
          </Space>
        }
        style={{ marginBottom: 16 }}
        size="small"
      >
        <Descriptions column={3} size="small">
          <Descriptions.Item label="模板名称">
            <Text strong>{template.name}</Text>
          </Descriptions.Item>
          <Descriptions.Item label="分类">
            {category ? <Tag color={category.color}>{category.text}</Tag> : "-"}
          </Descriptions.Item>
          <Descriptions.Item label="操作系统">
            {template.osType || "-"}
          </Descriptions.Item>
          {template.description && (
            <Descriptions.Item label="描述" span={3}>
              {template.description}
            </Descriptions.Item>
          )}
          {template.source?.name && (
            <Descriptions.Item label="来源">
              {template.source.name} ({template.source.type})
            </Descriptions.Item>
          )}
          <Descriptions.Item label="预置命令">
            <Tag color="blue">{template.commands?.length || 0} 个</Tag>
          </Descriptions.Item>
        </Descriptions>
      </Card>

      {/* Instance Config Summary */}
      <Card
        title={
          <Space>
            <CheckCircleOutlined style={{ color: "#52c41a" }} />
            <span>实例配置</span>
          </Space>
        }
        style={{ marginBottom: 16 }}
        size="small"
      >
        <Descriptions column={2} size="small">
          <Descriptions.Item label="名称前缀">
            <Text strong>{instanceConfig.name || "agent"}</Text>
          </Descriptions.Item>
          <Descriptions.Item label="目标数量">
            <Text strong style={{ color: "#1677ff" }}>
              {hosts.length} 个实例
            </Text>
          </Descriptions.Item>
          {instanceConfig.environmentId && (
            <Descriptions.Item label="部署环境">
              <Tag color="blue">已指定</Tag>
            </Descriptions.Item>
          )}
        </Descriptions>

        {Object.keys(instanceConfig.config).length > 0 && (
          <>
            <Divider style={{ margin: "12px 0" }} />
            <Text type="secondary">自定义配置：</Text>
            <div style={{ marginTop: 8 }}>
              {Object.entries(instanceConfig.config).map(([key, val]) => (
                <Tag key={key} style={{ marginBottom: 4 }}>
                  {key}: {String(val)}
                </Tag>
              ))}
            </div>
          </>
        )}
      </Card>

      {/* Host Binding Summary */}
      <Card
        title={
          <Space>
            <CheckCircleOutlined style={{ color: "#52c41a" }} />
            <span>主机绑定</span>
          </Space>
        }
        style={{ marginBottom: 16 }}
        size="small"
      >
        <Space style={{ marginBottom: 12 }}>
          <Tag color="green">在线 {onlineCount}</Tag>
          <Tag color="red">离线 {offlineCount}</Tag>
          {otherCount > 0 && <Tag color="orange">其他 {otherCount}</Tag>}
          <Text type="secondary">共 {hosts.length} 台主机</Text>
        </Space>

        <Table
          rowKey="id"
          dataSource={hosts}
          columns={hostColumns}
          pagination={
            hosts.length > 10 ? { pageSize: 10, showSizeChanger: true } : false
          }
          size="small"
          scroll={{ x: 900 }}
        />
      </Card>

      {/* Additional Info */}
      {template.configTemplate && (
        <Collapse
          ghost
          items={[
            {
              key: "config",
              label: "配置模板预览",
              children: (
                <pre
                  style={{
                    background: "#f5f5f5",
                    padding: 12,
                    borderRadius: 4,
                    fontSize: 12,
                    maxHeight: 200,
                    overflow: "auto",
                  }}
                >
                  {template.configTemplate}
                </pre>
              ),
            },
            {
              key: "script",
              label: "安装脚本预览",
              children: (
                <pre
                  style={{
                    background: "#f5f5f5",
                    padding: 12,
                    borderRadius: 4,
                    fontSize: 12,
                    maxHeight: 200,
                    overflow: "auto",
                  }}
                >
                  {template.installScript || "# 无安装脚本"}
                </pre>
              ),
            },
          ]}
        />
      )}
    </div>
  );
};

export default ConfigPreview;
