import React, { useRef, useState, useEffect } from "react";
import {
  PlusOutlined,
  UploadOutlined,
  ExportOutlined,
  EyeOutlined,
  CodeOutlined,
} from "@ant-design/icons";
import type { ActionType, ProColumns } from "@ant-design/pro-components";
import { ProTable } from "@ant-design/pro-components";
import {
  Button,
  message,
  Popconfirm,
  Form,
  Select,
  Input,
  Modal,
  Space,
  Tag,
  Divider,
  Card,
  Descriptions,
  Upload,
  Tabs,
  InputNumber,
} from "antd";
import { DrawerForm } from "../../../components/DrawerForm";
import {
  queryAgentTemplates,
  getAgentTemplate,
  createAgentTemplate,
  updateAgentTemplate,
  removeAgentTemplate,
  importAgentTemplate,
  exportAgentTemplate,
  queryAgentResources,
} from "../../../services/agent";
import type {
  AgentTemplate,
  AgentTemplateCreate,
  AgentTemplateUpdate,
  AgentResource,
  TemplateCategory,
  OsType,
  AgentCommand,
} from "../../../types";

const { TextArea } = Input;

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

const AgentTemplateList: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [drawerVisible, setDrawerVisible] = useState(false);
  const [editingItem, setEditingItem] = useState<Partial<AgentTemplate> | null>(
    null,
  );
  const [resources, setResources] = useState<AgentResource[]>([]);
  const [previewVisible, setPreviewVisible] = useState(false);
  const [previewTemplate, setPreviewTemplate] = useState<AgentTemplate | null>(
    null,
  );
  const [importing, setImporting] = useState(false);
  const [exportingId, setExportingId] = useState<string | null>(null);
  const [form] = Form.useForm();

  useEffect(() => {
    queryAgentResources().then(setResources);
  }, []);

  // 打开新建/编辑抽屉
  const openDrawer = (item?: AgentTemplate) => {
    setEditingItem(item || null);
    if (item) {
      form.setFieldsValue({
        ...item,
        sourceId: item.sourceId || item.source?.id,
        commands: item.commands || [],
      });
    } else {
      form.resetFields();
    }
    setDrawerVisible(true);
  };

  // 关闭抽屉
  const closeDrawer = () => {
    setDrawerVisible(false);
    setEditingItem(null);
    form.resetFields();
  };

  // 保存模板
  const handleSave = async () => {
    try {
      const values = await form.validateFields();
      const commandData = (values.commands || []).map((cmd: AgentCommand) => ({
        name: cmd.name,
        script: cmd.script,
        timeout: cmd.timeout || 30000,
        defaultArgs: cmd.defaultArgs,
      }));

      if (editingItem?.id) {
        // 更新
        const updateData: AgentTemplateUpdate = {
          name: values.name,
          description: values.description,
          category: values.category,
          osType: values.osType,
          archSupport: values.archSupport,
          installScript: values.installScript,
          configTemplate: values.configTemplate,
          dependencies: values.dependencies,
          sourceId: values.sourceId,
          commands: commandData,
        };
        await updateAgentTemplate(editingItem.id, updateData);
      } else {
        // 创建
        const createData: AgentTemplateCreate = {
          name: values.name,
          description: values.description,
          category: values.category,
          osType: values.osType,
          archSupport: values.archSupport,
          installScript: values.installScript,
          configTemplate: values.configTemplate,
          dependencies: values.dependencies,
          sourceId: values.sourceId,
          commands: commandData,
        };
        await createAgentTemplate(createData);
      }
      message.success("保存成功");
      closeDrawer();
      actionRef.current?.reload();
    } catch (error) {
      console.error(error);
      message.error("保存失败");
    }
  };

  // 删除模板
  const handleDelete = async (id: string) => {
    try {
      await removeAgentTemplate(id);
      message.success("删除成功");
      actionRef.current?.reload();
    } catch (error) {
      console.error(error);
      message.error("删除失败");
    }
  };

  // 预览模板
  const handlePreview = async (id: string) => {
    try {
      const template = await getAgentTemplate(id);
      setPreviewTemplate(template);
      setPreviewVisible(true);
    } catch (error) {
      console.error(error);
      message.error("获取模板详情失败");
    }
  };

  // 导入模板
  const handleImport = async (file: File) => {
    setImporting(true);
    try {
      await importAgentTemplate(file);
      message.success("导入成功");
      actionRef.current?.reload();
    } catch (error) {
      console.error(error);
      message.error("导入失败");
    } finally {
      setImporting(false);
    }
    return false;
  };

  // 导出模板
  const handleExport = async (id: string) => {
    setExportingId(id);
    try {
      await exportAgentTemplate(id);
      message.success("导出成功");
    } catch (error) {
      console.error(error);
      message.error("导出失败");
    } finally {
      setExportingId(null);
    }
  };

  const columns: ProColumns<AgentTemplate>[] = [
    {
      title: "模板名称",
      dataIndex: "name",
      ellipsis: true,
      copyable: true,
      width: 180,
    },
    {
      title: "分类",
      dataIndex: "category",
      width: 100,
      render: (_, record) => {
        const category = CATEGORY_MAP[record.category as TemplateCategory];
        return category ? (
          <Tag color={category.color}>{category.text}</Tag>
        ) : (
          "-"
        );
      },
      valueEnum: Object.entries(CATEGORY_MAP).reduce(
        (acc, [key, value]) => ({ ...acc, [key]: { text: value.text } }),
        {},
      ),
    },
    {
      title: "操作系统",
      dataIndex: "osType",
      width: 120,
      render: (_, record) => OS_TYPE_MAP[record.osType as OsType] || "-",
      valueEnum: OS_TYPE_MAP,
    },
    {
      title: "描述",
      dataIndex: "description",
      ellipsis: true,
      width: 200,
    },
    {
      title: "来源资源",
      dataIndex: "source",
      width: 150,
      render: (_, record) => record.source?.name || "-",
    },
    {
      title: "命令数",
      dataIndex: "commands",
      width: 80,
      render: (_, record) => (
        <Tag color={record.commands?.length ? "blue" : "default"}>
          {record.commands?.length || 0}
        </Tag>
      ),
    },
    {
      title: "部署统计",
      width: 150,
      render: (_, record) => {
        if (!record.deploymentCount) return "-";
        return (
          <span>
            {record.successCount}/{record.deploymentCount}
            {record.successRate !== undefined && (
              <span style={{ color: "green", marginLeft: 4 }}>
                ({(record.successRate * 100).toFixed(0)}%)
              </span>
            )}
          </span>
        );
      },
    },
    {
      title: "创建时间",
      dataIndex: "createdAt",
      valueType: "dateTime",
      width: 160,
    },
    {
      title: "操作",
      valueType: "option",
      width: 200,
      fixed: "right",
      render: (_text, record) => [
        <a key="preview" onClick={() => handlePreview(record.id)}>
          <EyeOutlined /> 预览
        </a>,
        <a
          key="export"
          onClick={() => handleExport(record.id)}
          style={{ opacity: exportingId === record.id ? 0.5 : 1 }}
        >
          <ExportOutlined spin={exportingId === record.id} /> 导出
        </a>,
        <a key="edit" onClick={() => openDrawer(record)}>
          编辑
        </a>,
        <Popconfirm
          key="delete"
          title="确定删除该模板吗？"
          onConfirm={() => handleDelete(record.id)}
        >
          <a style={{ color: "red" }}>删除</a>
        </Popconfirm>,
      ],
    },
  ];

  return (
    <>
      <ProTable<AgentTemplate>
        headerTitle="模板列表"
        actionRef={actionRef}
        rowKey="id"
        search={{ labelWidth: 120 }}
        scroll={{ x: 1400 }}
        toolBarRender={() => [
          <Upload
            key="import"
            accept=".json"
            showUploadList={false}
            beforeUpload={handleImport}
          >
            <Button icon={<UploadOutlined />} loading={importing}>
              导入模板
            </Button>
          </Upload>,
          <Button
            key="button"
            icon={<PlusOutlined />}
            type="primary"
            onClick={() => openDrawer()}
          >
            新建模板
          </Button>,
        ]}
        request={async (params) => {
          const res = await queryAgentTemplates({
            osType: params.osType as OsType,
            category: params.category as TemplateCategory,
          });
          return {
            data: res,
            success: true,
          };
        }}
        columns={columns}
      />

      {/* 新建/编辑模板抽屉 */}
      <DrawerForm
        visible={drawerVisible}
        title={editingItem ? "编辑模板" : "新建模板"}
        width={720}
        onClose={closeDrawer}
        onSave={handleSave}
        form={form}
      >
        <Tabs
          defaultActiveKey="basic"
          items={[
            {
              key: "basic",
              label: "基本信息",
              children: (
                <>
                  <Form.Item
                    name="name"
                    label="模板名称"
                    rules={[{ required: true, message: "请输入模板名称" }]}
                  >
                    <Input placeholder="请输入模板名称" />
                  </Form.Item>
                  <Form.Item name="description" label="描述">
                    <TextArea rows={2} placeholder="请输入描述" />
                  </Form.Item>
                  <Form.Item name="category" label="分类">
                    <Select placeholder="请选择分类" allowClear>
                      {Object.entries(CATEGORY_MAP).map(([key, value]) => (
                        <Select.Option key={key} value={key}>
                          <Tag color={value.color} style={{ marginRight: 0 }}>
                            {value.text}
                          </Tag>
                        </Select.Option>
                      ))}
                    </Select>
                  </Form.Item>
                  <Form.Item name="osType" label="操作系统">
                    <Select placeholder="请选择操作系统" allowClear>
                      {Object.entries(OS_TYPE_MAP).map(([key, value]) => (
                        <Select.Option key={key} value={key}>
                          {value}
                        </Select.Option>
                      ))}
                    </Select>
                  </Form.Item>
                  <Form.Item name="archSupport" label="架构支持">
                    <Input placeholder="如: x86_64,arm64 (多个用逗号分隔)" />
                  </Form.Item>
                  <Form.Item
                    name="sourceId"
                    label="来源资源"
                    rules={[{ required: true, message: "请选择来源资源" }]}
                  >
                    <Select placeholder="请选择资源">
                      {resources.map((r) => (
                        <Select.Option key={r.id} value={r.id}>
                          {r.name} ({r.type})
                        </Select.Option>
                      ))}
                    </Select>
                  </Form.Item>
                </>
              ),
            },
            {
              key: "scripts",
              label: "脚本配置",
              children: (
                <>
                  <Form.Item
                    name="installScript"
                    label={
                      <span>
                        <CodeOutlined style={{ marginRight: 4 }} />
                        安装脚本
                      </span>
                    }
                  >
                    <TextArea
                      rows={8}
                      placeholder="#!/bin/bash&#10;# 输入安装脚本..."
                      style={{ fontFamily: "monospace" }}
                    />
                  </Form.Item>
                  <Form.Item name="configTemplate" label="配置模板">
                    <TextArea
                      rows={8}
                      placeholder="# 配置模板内容&#10;# 可使用 {{变量}} 占位符"
                      style={{ fontFamily: "monospace" }}
                    />
                  </Form.Item>
                  <Form.Item name="dependencies" label="依赖">
                    <TextArea
                      rows={3}
                      placeholder="每行一个依赖，如：&#10;docker>=20.0&#10;python>=3.8"
                    />
                  </Form.Item>
                </>
              ),
            },
            {
              key: "commands",
              label: "命令配置",
              children: (
                <Form.List name="commands">
                  {(fields, { add, remove }) => (
                    <>
                      {fields.map(({ key, name, ...restField }) => (
                        <Card
                          key={key}
                          size="small"
                          style={{ marginBottom: 16 }}
                          title={`命令 ${name + 1}`}
                          extra={
                            <Button
                              type="link"
                              danger
                              onClick={() => remove(name)}
                            >
                              删除
                            </Button>
                          }
                        >
                          <Form.Item
                            {...restField}
                            name={[name, "name"]}
                            label="命令名称"
                            rules={[{ required: true }]}
                          >
                            <Input placeholder="如: start, stop, restart" />
                          </Form.Item>
                          <Form.Item
                            {...restField}
                            name={[name, "script"]}
                            label="执行脚本"
                            rules={[{ required: true }]}
                          >
                            <TextArea
                              rows={4}
                              placeholder="#!/bin/bash&#10;# 执行脚本内容"
                              style={{ fontFamily: "monospace" }}
                            />
                          </Form.Item>
                          <Form.Item
                            {...restField}
                            name={[name, "timeout"]}
                            label="超时时间(ms)"
                          >
                            <InputNumber
                              min={1000}
                              max={3600000}
                              placeholder="30000"
                              style={{ width: "100%" }}
                            />
                          </Form.Item>
                          <Form.Item
                            {...restField}
                            name={[name, "defaultArgs"]}
                            label="默认参数"
                          >
                            <Input placeholder="命令默认参数" />
                          </Form.Item>
                        </Card>
                      ))}
                      <Button type="dashed" onClick={() => add()} block>
                        <PlusOutlined /> 添加命令
                      </Button>
                    </>
                  )}
                </Form.List>
              ),
            },
          ]}
        />
      </DrawerForm>

      {/* 模板预览模态框 */}
      <Modal
        title="模板预览"
        open={previewVisible}
        onCancel={() => setPreviewVisible(false)}
        footer={null}
        width={800}
      >
        {previewTemplate && (
          <Tabs
            items={[
              {
                key: "info",
                label: "基本信息",
                children: (
                  <Descriptions column={2} bordered size="small">
                    <Descriptions.Item label="模板名称" span={2}>
                      {previewTemplate.name}
                    </Descriptions.Item>
                    <Descriptions.Item label="分类">
                      {CATEGORY_MAP[previewTemplate.category as TemplateCategory]
                        ?.text || "-"}
                    </Descriptions.Item>
                    <Descriptions.Item label="操作系统">
                      {OS_TYPE_MAP[previewTemplate.osType as OsType] || "-"}
                    </Descriptions.Item>
                    <Descriptions.Item label="架构支持" span={2}>
                      {previewTemplate.archSupport || "-"}
                    </Descriptions.Item>
                    <Descriptions.Item label="描述" span={2}>
                      {previewTemplate.description || "-"}
                    </Descriptions.Item>
                    <Descriptions.Item label="来源资源">
                      {previewTemplate.source?.name || "-"}
                    </Descriptions.Item>
                    <Descriptions.Item label="部署统计">
                      {previewTemplate.deploymentCount ? (
                        <span>
                          成功 {previewTemplate.successCount}/
                          {previewTemplate.deploymentCount} (
                          {((previewTemplate.successRate || 0) * 100).toFixed(0)}
                          %)
                        </span>
                      ) : (
                        "-"
                      )}
                    </Descriptions.Item>
                  </Descriptions>
                ),
              },
              {
                key: "scripts",
                label: "脚本配置",
                children: (
                  <>
                    <Divider orientation="left">安装脚本</Divider>
                    <pre
                      style={{
                        background: "#f5f5f5",
                        padding: 12,
                        borderRadius: 4,
                        overflow: "auto",
                      }}
                    >
                      {previewTemplate.installScript || "# 无安装脚本"}
                    </pre>
                    <Divider orientation="left">配置模板</Divider>
                    <pre
                      style={{
                        background: "#f5f5f5",
                        padding: 12,
                        borderRadius: 4,
                        overflow: "auto",
                      }}
                    >
                      {previewTemplate.configTemplate || "# 无配置模板"}
                    </pre>
                    <Divider orientation="left">依赖</Divider>
                    <pre
                      style={{
                        background: "#f5f5f5",
                        padding: 12,
                        borderRadius: 4,
                      }}
                    >
                      {previewTemplate.dependencies || "# 无依赖"}
                    </pre>
                  </>
                ),
              },
              {
                key: "commands",
                label: `命令 (${previewTemplate.commands?.length || 0})`,
                children: previewTemplate.commands?.length ? (
                  <Space direction="vertical" style={{ width: "100%" }}>
                    {previewTemplate.commands.map((cmd, idx) => (
                      <Card
                        key={cmd.id || idx}
                        size="small"
                        title={cmd.name}
                        style={{ marginBottom: 8 }}
                      >
                        <Descriptions column={1} size="small">
                          <Descriptions.Item label="执行脚本">
                            <pre
                              style={{
                                background: "#f5f5f5",
                                padding: 8,
                                borderRadius: 4,
                                margin: 0,
                              }}
                            >
                              {cmd.script}
                            </pre>
                          </Descriptions.Item>
                          <Descriptions.Item label="超时时间">
                            {cmd.timeout}ms
                          </Descriptions.Item>
                          {cmd.defaultArgs && (
                            <Descriptions.Item label="默认参数">
                              {cmd.defaultArgs}
                            </Descriptions.Item>
                          )}
                        </Descriptions>
                      </Card>
                    ))}
                  </Space>
                ) : (
                  <div style={{ color: "#999", textAlign: "center", padding: 24 }}>
                    暂无命令
                  </div>
                ),
              },
            ]}
          />
        )}
      </Modal>
    </>
  );
};

export default AgentTemplateList;