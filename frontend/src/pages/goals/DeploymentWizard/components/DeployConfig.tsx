import React, { useEffect, useState } from "react";
import {
  Form,
  Card,
  Select,
  Input,
  Typography,
  Divider,
  Descriptions,
  Alert,
  Tag,
} from "antd";
import { queryAgentTemplates } from "../../../../services/agent";
import type { AgentTemplate, Host, DeployParams, TemplateCategory, OsType } from "../../../../types";

const { Text } = Typography;
const { TextArea } = Input;

interface DeployConfigProps {
  host: Host | null;
  value: AgentTemplate | null;
  deployParams: DeployParams;
  onTemplateChange: (template: AgentTemplate | null) => void;
  onParamsChange: (params: DeployParams) => void;
}

// 模板分类映射
const CATEGORY_MAP: Record<TemplateCategory, { text: string; color: string }> = {
  MONITORING: { text: "监控", color: "blue" },
  DEPLOYMENT: { text: "部署", color: "green" },
  BACKUP: { text: "备份", color: "orange" },
  SECURITY: { text: "安全", color: "red" },
  DATABASE: { text: "数据库", color: "purple" },
  NETWORK: { text: "网络", color: "cyan" },
  UTILITY: { text: "工具", color: "geekblue" },
  CUSTOM: { text: "自定义", color: "default" },
};

// 操作系统类型映射
const OS_TYPE_MAP: Record<OsType, string> = {
  ALL: "全部",
  LINUX: "Linux",
  WINDOWS: "Windows",
  MACOS: "macOS",
  LINUX_DOCKER: "Linux (Docker)",
};

export const DeployConfig: React.FC<DeployConfigProps> = ({
  host,
  value,
  deployParams,
  onTemplateChange,
  onParamsChange,
}) => {
  const [templates, setTemplates] = useState<AgentTemplate[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchTemplates();
  }, []);

  const fetchTemplates = async () => {
    try {
      setLoading(true);
      const data = await queryAgentTemplates({});
      const templateList = Array.isArray(data)
        ? data
        : (data as any)?.data || [];
      setTemplates(templateList);
    } catch (error) {
      console.error("Failed to fetch templates:", error);
    } finally {
      setLoading(false);
    }
  };

  if (!host) {
    return (
      <Alert
        message="请先选择主机"
        description="请返回上一步选择目标主机"
        type="warning"
        showIcon
      />
    );
  }

  return (
    <div>
      <Card title="部署目标" style={{ marginBottom: 16 }}>
        <Descriptions column={2} size="small">
          <Descriptions.Item label="主机名称">{host.name}</Descriptions.Item>
          <Descriptions.Item label="主机地址">
            {host.hostname}
          </Descriptions.Item>
          <Descriptions.Item label="操作系统">
            {host.os || "未知"}
          </Descriptions.Item>
          <Descriptions.Item label="状态">{host.status}</Descriptions.Item>
        </Descriptions>
      </Card>

      <Card title="选择 Agent 模板" style={{ marginBottom: 16 }}>
        <Form layout="vertical">
          <Form.Item label="Agent 模板" required>
            <Select
              placeholder="请选择 Agent 模板"
              value={value?.id}
              onChange={(id) => {
                const template = templates.find((t) => t.id === id);
                onTemplateChange(template || null);
              }}
              loading={loading}
              style={{ width: "100%" }}
            >
              {templates.map((template) => {
                const category = CATEGORY_MAP[template.category as TemplateCategory];
                return (
                  <Select.Option key={template.id} value={template.id}>
                    <div>
                      <Text strong>{template.name}</Text>
                      {category && (
                        <Tag color={category.color} style={{ marginLeft: 8 }}>
                          {category.text}
                        </Tag>
                      )}
                    </div>
                    {template.description && (
                      <div>
                        <Text type="secondary" style={{ fontSize: 12 }}>
                          {template.description}
                        </Text>
                      </div>
                    )}
                  </Select.Option>
                );
              })}
            </Select>
          </Form.Item>

          {value && (
            <>
              <Divider />
              <Descriptions column={1} size="small">
                <Descriptions.Item label="模板分类">
                  {CATEGORY_MAP[value.category as TemplateCategory]?.text || value.category || "-"}
                </Descriptions.Item>
                <Descriptions.Item label="操作系统">
                  {OS_TYPE_MAP[value.osType as OsType] || value.osType || "-"}
                </Descriptions.Item>
                {value.archSupport && (
                  <Descriptions.Item label="架构支持">
                    {value.archSupport}
                  </Descriptions.Item>
                )}
                {value.description && (
                  <Descriptions.Item label="描述">
                    {value.description}
                  </Descriptions.Item>
                )}
              </Descriptions>
            </>
          )}
        </Form>
      </Card>

      <Card title="部署参数配置">
        <Form layout="vertical">
          <Form.Item label="部署版本" required>
            <Input
              placeholder="例如: latest, v1.0.0"
              value={deployParams.version}
              onChange={(e) =>
                onParamsChange({ ...deployParams, version: e.target.value })
              }
            />
          </Form.Item>
          <Form.Item label="备注">
            <TextArea
              placeholder="可选填写部署备注"
              rows={2}
              value={deployParams.remarks || ""}
              onChange={(e) =>
                onParamsChange({ ...deployParams, remarks: e.target.value })
              }
            />
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};
