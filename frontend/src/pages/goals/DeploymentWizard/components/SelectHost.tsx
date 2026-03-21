import React, { useEffect, useState } from "react";
import { List, Card, Radio, Typography, Empty, Spin, Tag } from "antd";
import { queryHosts } from "../../../../services/infra";
import type { Host } from "../../../../types";

const { Text } = Typography;

const statusColors: Record<string, string> = {
  ONLINE: "green",
  OFFLINE: "red",
  UNCONNECTED: "orange",
  EXCEPTION: "error",
  MAINTENANCE: "blue",
  UNKNOWN: "default",
};

const statusText: Record<string, string> = {
  ONLINE: "在线",
  OFFLINE: "离线",
  UNCONNECTED: "未连接",
  EXCEPTION: "异常",
  MAINTENANCE: "维护中",
  UNKNOWN: "未知",
};

interface SelectHostProps {
  environmentId?: string;
  value: Host | null;
  onChange: (host: Host | null) => void;
}

export const SelectHost: React.FC<SelectHostProps> = ({
  environmentId,
  value,
  onChange,
}) => {
  const [hosts, setHosts] = useState<Host[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchHosts();
  }, [environmentId]);

  const fetchHosts = async () => {
    try {
      setLoading(true);
      const params = environmentId
        ? { environmentId, current: 1, pageSize: 100 }
        : { current: 1, pageSize: 100 };
      const data = await queryHosts(params);
      const hostList = Array.isArray(data) ? data : (data as any)?.data || [];
      // Filter by environment if provided
      const filtered = environmentId
        ? hostList.filter((h: Host) => h.environmentId === environmentId)
        : hostList;
      setHosts(filtered);
    } catch (error) {
      console.error("Failed to fetch hosts:", error);
    } finally {
      setLoading(false);
    }
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
        {environmentId ? (
          <Text type="secondary">
            所选环境下没有主机，请在基础设施 {">"} 主机中添加
          </Text>
        ) : (
          <Text type="secondary">请先在基础设施 {">"} 主机中添加主机</Text>
        )}
      </Empty>
    );
  }

  return (
    <Radio.Group
      value={value?.id}
      onChange={(e) => {
        const host = hosts.find((item) => item.id === e.target.value);
        onChange(host || null);
      }}
      style={{ width: "100%" }}
    >
      <List
        grid={{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 4 }}
        dataSource={hosts}
        renderItem={(host) => (
          <List.Item>
            <Card
              hoverable
              style={{
                border: value?.id === host.id ? "#1677ff 2px solid" : undefined,
              }}
            >
              <Radio value={host.id}>
                <div style={{ paddingLeft: 8 }}>
                  <Text strong>{host.name}</Text>
                  <br />
                  <Text type="secondary" style={{ fontSize: 12 }}>
                    {host.hostname}
                  </Text>
                  <br />
                  <Tag color={statusColors[host.status] || "default"}>
                    {statusText[host.status] || host.status}
                  </Tag>
                  {host.os && (
                    <>
                      <br />
                      <Text type="secondary" style={{ fontSize: 12 }}>
                        OS: {host.os}
                      </Text>
                    </>
                  )}
                </div>
              </Radio>
            </Card>
          </List.Item>
        )}
      />
    </Radio.Group>
  );
};
