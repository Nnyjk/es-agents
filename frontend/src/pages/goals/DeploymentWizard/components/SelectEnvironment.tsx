import React, { useEffect, useState } from "react";
import { List, Card, Radio, Typography, Empty, Spin } from "antd";
import { queryEnvironments } from "../../../../services/infra";
import type { Environment } from "../../../../types";

const { Text } = Typography;

interface SelectEnvironmentProps {
  value: Environment | null;
  onChange: (env: Environment | null) => void;
}

export const SelectEnvironment: React.FC<SelectEnvironmentProps> = ({
  value,
  onChange,
}) => {
  const [environments, setEnvironments] = useState<Environment[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchEnvironments();
  }, []);

  const fetchEnvironments = async () => {
    try {
      setLoading(true);
      const data = await queryEnvironments({ current: 1, pageSize: 100 });
      const envList = Array.isArray(data) ? data : (data as any)?.data || [];
      setEnvironments(envList);
    } catch (error) {
      console.error("Failed to fetch environments:", error);
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

  if (environments.length === 0) {
    return (
      <Empty description="暂无可用环境" image={Empty.PRESENTED_IMAGE_SIMPLE}>
        <Text type="secondary">请先在基础设施 {">"} 环境中创建环境</Text>
      </Empty>
    );
  }

  return (
    <Radio.Group
      value={value?.id}
      onChange={(e) => {
        const env = environments.find((item) => item.id === e.target.value);
        onChange(env || null);
      }}
      style={{ width: "100%" }}
    >
      <List
        grid={{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 4 }}
        dataSource={environments}
        renderItem={(env) => (
          <List.Item>
            <Card
              hoverable
              style={{
                border: value?.id === env.id ? "#1677ff 2px solid" : undefined,
              }}
            >
              <Radio value={env.id}>
                <div style={{ paddingLeft: 8 }}>
                  <Text strong>{env.name}</Text>
                  <br />
                  <Text type="secondary">{env.code}</Text>
                  {env.description && (
                    <>
                      <br />
                      <Text type="secondary" style={{ fontSize: 12 }}>
                        {env.description}
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
