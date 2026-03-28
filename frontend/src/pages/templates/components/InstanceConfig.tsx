import React, { useEffect, useState } from "react";
import {
  Form,
  Card,
  Select,
  Input,
  Typography,
  Descriptions,
  Alert,
  Divider,
  Tag,
  Space,
} from "antd";
import { queryEnvironments } from "../../../services/infra";
import type { AgentTemplate, Environment } from "../../../types";
import type { InstanceConfig as InstanceConfigType } from "../types";

const { Text } = Typography;

interface InstanceConfigProps {
  template: AgentTemplate | null;
  value: InstanceConfigType;
  onChange: (config: InstanceConfigType) => void;
}

const InstanceConfig: React.FC<InstanceConfigProps> = ({
  template,
  value,
  onChange,
}) => {
  const [environments, setEnvironments] = useState<Environment[]>([]);
  const [loadingEnvs, setLoadingEnvs] = useState(true);

  useEffect(() => {
    fetchEnvironments();
  }, []);

  const fetchEnvironments = async () => {
    try {
      setLoadingEnvs(true);
      const data = await queryEnvironments({ current: 1, pageSize: 100 });
      const envList = Array.isArray(data) ? data : (data as any)?.data || [];
      setEnvironments(envList);
    } catch (error) {
      console.error("Failed to fetch environments:", error);
    } finally {
      setLoadingEnvs(false);
    }
  };

  if (!template) {
    return (
      <Alert
        message="请先选择模板"
        description="请返回上一步选择 Agent 模板"
        type="warning"
        showIcon
      />
    );
  }

  // Parse template config template to extract configurable variables
  const parseConfigTemplate = (configTemplate?: string): string[] => {
    if (!configTemplate) return [];
    const regex = /\{\{(\w+)\}\}/g;
    const matches = configTemplate.matchAll(regex);
    const variables: string[] = [];
    for (const match of matches) {
      variables.push(match[1]);
    }
    return [...new Set(variables)];
  };

  const configVariables = parseConfigTemplate(template.configTemplate);

  return (
    <div>
      <Card title="模板信息" style={{ marginBottom: 16 }}>
        <Descriptions column={2} size="small">
          <Descriptions.Item label="模板名称">
            {template.name}
          </Descriptions.Item>
          <Descriptions.Item label="操作系统">
            {template.osType || "-"}
          </Descriptions.Item>
          {template.description && (
            <Descriptions.Item label="描述" span={2}>
              {template.description}
            </Descriptions.Item>
          )}
          {template.source?.name && (
            <Descriptions.Item label="来源资源">
              {template.source.name} ({template.source.type})
            </Descriptions.Item>
          )}
        </Descriptions>
      </Card>

      <Card title="实例配置" style={{ marginBottom: 16 }}>
        <Form layout="vertical">
          <Form.Item label="实例名称前缀" required>
            <Input
              placeholder="输入实例名称前缀，如 my-agent"
              value={value.name}
              onChange={(e) => onChange({ ...value, name: e.target.value })}
            />
            <Text type="secondary" style={{ fontSize: 12 }}>
              实例将自动命名为：{value.name || "前缀"}-{template.name}-主机名
            </Text>
          </Form.Item>

          <Form.Item label="部署环境">
            <Select
              placeholder="选择部署环境（可选）"
              value={value.environmentId}
              onChange={(envId) => onChange({ ...value, environmentId: envId })}
              loading={loadingEnvs}
              allowClear
              style={{ width: "100%" }}
            >
              {environments.map((env) => (
                <Select.Option key={env.id} value={env.id}>
                  <Space>
                    {env.color && <Tag color={env.color}>{env.name}</Tag>}
                    {!env.color && <span>{env.name}</span>}
                    <Text type="secondary" style={{ fontSize: 12 }}>
                      ({env.code})
                    </Text>
                  </Space>
                </Select.Option>
              ))}
            </Select>
            <Text type="secondary" style={{ fontSize: 12 }}>
              选择环境后，将优先筛选该环境下的主机
            </Text>
          </Form.Item>
        </Form>
      </Card>

      {configVariables.length > 0 && (
        <Card title="自定义配置" style={{ marginBottom: 16 }}>
          <Alert
            message="配置变量"
            description={`模板包含 ${configVariables.length} 个可配置变量，请在下方填写`}
            type="info"
            showIcon
            style={{ marginBottom: 16 }}
          />
          <Form layout="vertical">
            {configVariables.map((variable) => (
              <Form.Item key={variable} label={variable}>
                <Input
                  placeholder={`输入 ${variable} 的值`}
                  value={value.config?.[variable] || ""}
                  onChange={(e) =>
                    onChange({
                      ...value,
                      config: { ...value.config, [variable]: e.target.value },
                    })
                  }
                />
              </Form.Item>
            ))}
          </Form>
          <Divider />
          <Text type="secondary" style={{ fontSize: 12 }}>
            配置模板预览：
          </Text>
          <pre
            style={{
              background: "#f5f5f5",
              padding: 12,
              borderRadius: 4,
              marginTop: 8,
              fontSize: 12,
              maxHeight: 200,
              overflow: "auto",
            }}
          >
            {template.configTemplate || "# 无配置模板"}
          </pre>
        </Card>
      )}

      {template.commands && template.commands.length > 0 && (
        <Card title="预置命令" size="small">
          <Descriptions column={1} size="small">
            {template.commands.map((cmd) => (
              <Descriptions.Item key={cmd.id} label={cmd.name}>
                <Tag color="blue">{cmd.timeout || 30000}ms</Tag>
                {cmd.defaultArgs && (
                  <Text type="secondary" style={{ marginLeft: 8 }}>
                    默认参数: {cmd.defaultArgs}
                  </Text>
                )}
              </Descriptions.Item>
            ))}
          </Descriptions>
        </Card>
      )}
    </div>
  );
};

export default InstanceConfig;
