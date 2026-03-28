import React, { useEffect, useState } from "react";
import {
  List,
  Card,
  Radio,
  Typography,
  Empty,
  Spin,
  Tag,
  Input,
  Space,
} from "antd";
import { SearchOutlined } from "@ant-design/icons";
import { queryAgentTemplates } from "../../../services/agent";
import type { AgentTemplate, TemplateCategory, OsType } from "../../../types";

const { Text } = Typography;

// Template category mapping
const CATEGORY_MAP: Record<TemplateCategory, { text: string; color: string }> =
  {
    MONITORING: { text: "监控", color: "blue" },
    DEPLOYMENT: { text: "部署", color: "green" },
    BACKUP: { text: "备份", color: "orange" },
    SECURITY: { text: "安全", color: "red" },
    DATABASE: { text: "数据库", color: "purple" },
    NETWORK: { text: "网络", color: "cyan" },
    UTILITY: { text: "工具", color: "geekblue" },
    CUSTOM: { text: "自定义", color: "default" },
  };

// OS type mapping
const OS_TYPE_MAP: Record<OsType, string> = {
  ALL: "全部",
  LINUX: "Linux",
  WINDOWS: "Windows",
  MACOS: "macOS",
  LINUX_DOCKER: "Linux (Docker)",
};

interface TemplateSelectorProps {
  value: AgentTemplate | null;
  onChange: (template: AgentTemplate | null) => void;
}

export const TemplateSelector: React.FC<TemplateSelectorProps> = ({
  value,
  onChange,
}) => {
  const [templates, setTemplates] = useState<AgentTemplate[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchText, setSearchText] = useState("");

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

  // Filter templates by search text
  const filteredTemplates = templates.filter((template) => {
    if (!searchText) return true;
    const searchLower = searchText.toLowerCase();
    return (
      template.name.toLowerCase().includes(searchLower) ||
      template.description?.toLowerCase().includes(searchLower) ||
      CATEGORY_MAP[template.category as TemplateCategory]?.text
        .toLowerCase()
        .includes(searchLower)
    );
  });

  if (loading) {
    return (
      <div style={{ textAlign: "center", padding: 50 }}>
        <Spin size="large" />
      </div>
    );
  }

  if (templates.length === 0) {
    return (
      <Empty description="暂无可用模板" image={Empty.PRESENTED_IMAGE_SIMPLE}>
        <Text type="secondary">请先在 Agent 管理 {">"} 模板中创建模板</Text>
      </Empty>
    );
  }

  return (
    <div>
      <Space style={{ marginBottom: 16, width: "100%" }}>
        <Input
          placeholder="搜索模板名称、描述或分类"
          prefix={<SearchOutlined />}
          value={searchText}
          onChange={(e) => setSearchText(e.target.value)}
          style={{ width: 300 }}
          allowClear
        />
        <Text type="secondary">共 {filteredTemplates.length} 个模板</Text>
      </Space>

      <Radio.Group
        value={value?.id}
        onChange={(e) => {
          const template = templates.find((item) => item.id === e.target.value);
          onChange(template || null);
        }}
        style={{ width: "100%" }}
      >
        <List
          grid={{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 3, xl: 4 }}
          dataSource={filteredTemplates}
          renderItem={(template) => {
            const category =
              CATEGORY_MAP[template.category as TemplateCategory];
            const osType = OS_TYPE_MAP[template.osType as OsType];
            return (
              <List.Item>
                <Card
                  hoverable
                  style={{
                    border:
                      value?.id === template.id
                        ? "#1677ff 2px solid"
                        : undefined,
                    height: "100%",
                  }}
                >
                  <Radio value={template.id}>
                    <div style={{ paddingLeft: 8 }}>
                      <Text strong style={{ fontSize: 14 }}>
                        {template.name}
                      </Text>
                      {category && (
                        <Tag color={category.color} style={{ marginLeft: 8 }}>
                          {category.text}
                        </Tag>
                      )}
                      <br />
                      {template.description && (
                        <Text
                          type="secondary"
                          style={{ fontSize: 12 }}
                          ellipsis
                        >
                          {template.description}
                        </Text>
                      )}
                      <br />
                      <Space size={4} style={{ marginTop: 4 }}>
                        {osType && (
                          <Tag color="default" style={{ fontSize: 11 }}>
                            {osType}
                          </Tag>
                        )}
                        {template.commands && template.commands.length > 0 && (
                          <Tag color="blue" style={{ fontSize: 11 }}>
                            {template.commands.length} 命令
                          </Tag>
                        )}
                      </Space>
                    </div>
                  </Radio>
                </Card>
              </List.Item>
            );
          }}
        />
      </Radio.Group>

      {value && (
        <Card style={{ marginTop: 16 }} title="已选择模板" size="small">
          <Space direction="vertical" size="small">
            <Text>
              <Text strong>名称：</Text>
              {value.name}
            </Text>
            {value.description && (
              <Text>
                <Text strong>描述：</Text>
                {value.description}
              </Text>
            )}
            <Text>
              <Text strong>操作系统：</Text>
              {OS_TYPE_MAP[value.osType as OsType] || value.osType || "-"}
            </Text>
            {value.archSupport && (
              <Text>
                <Text strong>架构：</Text>
                {value.archSupport}
              </Text>
            )}
            {value.source?.name && (
              <Text>
                <Text strong>来源：</Text>
                {value.source.name}
              </Text>
            )}
          </Space>
        </Card>
      )}
    </div>
  );
};

export default TemplateSelector;
