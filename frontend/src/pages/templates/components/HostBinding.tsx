import React, { useEffect, useState } from "react";
import {
  Card,
  Table,
  Typography,
  Empty,
  Spin,
  Tag,
  Space,
  Input,
  Button,
  Alert,
  Descriptions,
} from "antd";
import {
  SearchOutlined,
  ReloadOutlined,
  CheckCircleOutlined,
} from "@ant-design/icons";
import { queryHosts } from "../../../services/infra";
import type { Host } from "../../../types";
import type { InstanceConfig } from "../types";

const { Text } = Typography;

// Host status color mapping
const statusColors: Record<string, string> = {
  ONLINE: "green",
  OFFLINE: "red",
  UNCONNECTED: "orange",
  EXCEPTION: "error",
  MAINTENANCE: "blue",
  UNKNOWN: "default",
};

// Host status text mapping
const statusText: Record<string, string> = {
  ONLINE: "在线",
  OFFLINE: "离线",
  UNCONNECTED: "未连接",
  EXCEPTION: "异常",
  MAINTENANCE: "维护中",
  UNKNOWN: "未知",
};

interface HostBindingProps {
  instanceConfig: InstanceConfig;
  value: Host[];
  onChange: (hosts: Host[]) => void;
}

export const HostBinding: React.FC<HostBindingProps> = ({
  instanceConfig,
  value,
  onChange,
}) => {
  const [hosts, setHosts] = useState<Host[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchText, setSearchText] = useState("");

  useEffect(() => {
    fetchHosts();
  }, [instanceConfig.environmentId]);

  const fetchHosts = async () => {
    try {
      setLoading(true);
      const params: any = { current: 1, pageSize: 200 };
      if (instanceConfig.environmentId) {
        params.environmentId = instanceConfig.environmentId;
      }
      const data = await queryHosts(params);
      const hostList = Array.isArray(data) ? data : (data as any)?.data || [];

      // Filter hosts that have gatewayUrl configured (required for instance binding)
      const validHosts = hostList.filter(
        (h: Host) => h.gatewayUrl && h.gatewayUrl.trim() !== "",
      );

      setHosts(validHosts);
    } catch (error) {
      console.error("Failed to fetch hosts:", error);
    } finally {
      setLoading(false);
    }
  };

  // Filter hosts by search text
  const filteredHosts = hosts.filter((host) => {
    if (!searchText) return true;
    const searchLower = searchText.toLowerCase();
    return (
      host.name.toLowerCase().includes(searchLower) ||
      host.hostname.toLowerCase().includes(searchLower) ||
      host.os?.toLowerCase().includes(searchLower) ||
      host.environmentName?.toLowerCase().includes(searchLower)
    );
  });

  // Handle row selection
  const selectedRowKeys = value.map((h) => h.id);

  const handleSelectChange = (selectedKeys: React.Key[]) => {
    const selectedHosts = hosts.filter((h) => selectedKeys.includes(h.id));
    onChange(selectedHosts);
  };

  // Quick select all online hosts
  const handleSelectOnline = () => {
    const onlineHosts = hosts.filter((h) => h.status === "ONLINE");
    onChange(onlineHosts);
  };

  // Clear selection
  const handleClearSelection = () => {
    onChange([]);
  };

  if (loading) {
    return (
      <div style={{ textAlign: "center", padding: 50 }}>
        <Spin size="large" />
      </div>
    );
  }

  if (hosts.length === 0) {
    return (
      <Empty description="暂无可用主机" image={Empty.PRESENTED_IMAGE_SIMPLE}>
        <Text type="secondary">
          {instanceConfig.environmentId
            ? "所选环境下没有可用主机，或主机未配置 Gateway URL"
            : "请先在基础设施 {>} 主机中添加主机并配置 Gateway URL"}
        </Text>
      </Empty>
    );
  }

  const columns = [
    {
      title: "主机名称",
      dataIndex: "name",
      width: 150,
      render: (name: string, record: Host) => (
        <Space>
          {value.find((h) => h.id === record.id) && (
            <CheckCircleOutlined style={{ color: "#52c41a" }} />
          )}
          <Text strong>{name}</Text>
        </Space>
      ),
    },
    {
      title: "主机地址",
      dataIndex: "hostname",
      width: 150,
      ellipsis: true,
    },
    {
      title: "操作系统",
      dataIndex: "os",
      width: 100,
      render: (os: string) => os || "-",
    },
    {
      title: "环境",
      dataIndex: "environmentName",
      width: 100,
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
      width: 180,
      ellipsis: true,
      render: (url: string) =>
        url ? (
          <Text type="secondary" style={{ fontSize: 12 }}>
            {url}
          </Text>
        ) : (
          <Tag color="red">未配置</Tag>
        ),
    },
  ];

  return (
    <div>
      {instanceConfig.environmentId && (
        <Alert
          message={`已选择环境：${instanceConfig.environmentId}`}
          description="仅显示该环境下的主机"
          type="info"
          showIcon
          style={{ marginBottom: 16 }}
          closable
        />
      )}

      <Space style={{ marginBottom: 16, width: "100%" }} wrap>
        <Input
          placeholder="搜索主机名称、地址或操作系统"
          prefix={<SearchOutlined />}
          value={searchText}
          onChange={(e) => setSearchText(e.target.value)}
          style={{ width: 280 }}
          allowClear
        />
        <Button icon={<ReloadOutlined />} onClick={fetchHosts}>
          刷新
        </Button>
        <Button type="dashed" onClick={handleSelectOnline}>
          快速选择在线主机
        </Button>
        {value.length > 0 && (
          <Button danger onClick={handleClearSelection}>
            清空选择
          </Button>
        )}
        <Text type="secondary">
          已选择{" "}
          <Text strong style={{ color: "#1677ff" }}>
            {value.length}
          </Text>{" "}
          台主机， 可用 {filteredHosts.length} 台
        </Text>
      </Space>

      <Card style={{ marginBottom: 16 }}>
        <Table
          rowKey="id"
          dataSource={filteredHosts}
          columns={columns}
          rowSelection={{
            type: "checkbox",
            selectedRowKeys,
            onChange: handleSelectChange,
            selections: [
              Table.SELECTION_ALL,
              Table.SELECTION_INVERT,
              {
                key: "select-online",
                text: "选择在线主机",
                onSelect: (_changeableRowKeys) => {
                  const onlineKeys = filteredHosts
                    .filter((h) => h.status === "ONLINE")
                    .map((h) => h.id);
                  onChange(
                    filteredHosts.filter((h) => onlineKeys.includes(h.id)),
                  );
                },
              },
            ],
          }}
          pagination={{
            pageSize: 10,
            showSizeChanger: true,
            showTotal: (total) => `共 ${total} 台主机`,
          }}
          size="small"
          scroll={{ x: 800 }}
        />
      </Card>

      {value.length > 0 && (
        <Card title="已选主机预览" size="small">
          <Descriptions column={4} size="small">
            {value.slice(0, 8).map((host) => (
              <Descriptions.Item key={host.id} label={host.name}>
                <Space size={4}>
                  <Tag color={statusColors[host.status]}>
                    {statusText[host.status]}
                  </Tag>
                  <Text type="secondary" style={{ fontSize: 11 }}>
                    {host.hostname}
                  </Text>
                </Space>
              </Descriptions.Item>
            ))}
            {value.length > 8 && (
              <Descriptions.Item label="...">
                <Text type="secondary">还有 {value.length - 8} 台主机</Text>
              </Descriptions.Item>
            )}
          </Descriptions>
        </Card>
      )}
    </div>
  );
};

export default HostBinding;
