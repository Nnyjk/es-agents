/**
 * 命令模板管理页面
 * 用于管理可复用的命令模板，支持创建、编辑、删除和执行测试
 */

import React, { useRef, useState, useEffect } from "react";
import { PlusOutlined } from "@ant-design/icons";
import type { ActionType, ProColumns } from "@ant-design/pro-components";
import { ProTable } from "@ant-design/pro-components";
import {
  Button,
  message,
  Popconfirm,
  Form,
  Input,
  InputNumber,
  Space,
  Tag,
  Modal,
  Typography,
  Select,
  Switch,
} from "antd";
import { DrawerForm } from "../../components/DrawerForm";
import {
  queryCommandTemplates,
  getCommandTemplate,
  saveCommandTemplate,
  removeCommandTemplate,
  executeCommandTemplate,
} from "../../services/commandTemplate";
import type {
  CommandTemplate,
  CommandTemplateDetail,
  CommandTemplateParameter,
  CommandCategory,
} from "../../types/command";
import {
  categoryLabels as labels,
  categoryColors as colors,
} from "../../types/command";
import { queryAgentInstances } from "../../services/agent";
import type { AgentInstance } from "../../types";

const CommandTemplatePage: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [drawerVisible, setDrawerVisible] = useState(false);
  const [editingItem, setEditingItem] =
    useState<Partial<CommandTemplateDetail> | null>(null);
  const [searchKeyword, setSearchKeyword] = useState<string>("");
  const [executeModalVisible, setExecuteModalVisible] = useState(false);
  const [executingTemplate, setExecutingTemplate] =
    useState<CommandTemplateDetail | null>(null);
  const [executeResult, setExecuteResult] = useState<string>("");
  const [executeForm] = Form.useForm();
  const [agentInstances, setAgentInstances] = useState<AgentInstance[]>([]);

  useEffect(() => {
    queryAgentInstances()
      .then((res) => {
        const instances = Array.isArray(res) ? res : res.data;
        setAgentInstances(instances);
      })
      .catch(console.error);
  }, []);

  const columns: ProColumns<CommandTemplate>[] = [
    {
      title: "名称",
      dataIndex: "name",
      ellipsis: true,
      copyable: true,
      sorter: true,
    },
    {
      title: "描述",
      dataIndex: "description",
      ellipsis: true,
      search: false,
      render: (_, record) =>
        record.description ? (
          <Typography.Text ellipsis={{ tooltip: record.description }}>
            {record.description}
          </Typography.Text>
        ) : (
          "-"
        ),
    },
    {
      title: "分类",
      dataIndex: "category",
      search: false,
      render: (_, record) =>
        record.category ? (
          <Tag color={colors[record.category]}>{labels[record.category]}</Tag>
        ) : (
          "-"
        ),
    },
    {
      title: "标签",
      dataIndex: "tags",
      search: false,
      render: (_, record) =>
        record.tags ? (
          <Space size={4}>
            {record.tags.split(",").map((tag) => (
              <Tag key={tag}>{tag.trim()}</Tag>
            ))}
          </Space>
        ) : (
          "-"
        ),
    },
    {
      title: "超时时间",
      dataIndex: "timeout",
      search: false,
      render: (_, record) => (record.timeout ? `${record.timeout}秒` : "-"),
    },
    {
      title: "状态",
      dataIndex: "isActive",
      search: false,
      render: (_, record) => (
        <Tag color={record.isActive ? "green" : "default"}>
          {record.isActive ? "启用" : "禁用"}
        </Tag>
      ),
    },
    {
      title: "创建时间",
      dataIndex: "createdAt",
      valueType: "dateTime",
      search: false,
      sorter: true,
    },
    {
      title: "更新时间",
      dataIndex: "updatedAt",
      valueType: "dateTime",
      search: false,
      sorter: true,
    },
    {
      title: "操作",
      valueType: "option",
      width: 200,
      render: (_text, record, _, action) => [
        <a
          key="execute"
          onClick={async () => {
            // 获取详情以显示参数
            try {
              const detail = await getCommandTemplate(record.id);
              setExecutingTemplate(detail);
              setExecuteResult("");
              executeForm.resetFields();
              // 解析参数并设置默认值
              if (detail.parameters) {
                try {
                  const params: CommandTemplateParameter[] = JSON.parse(
                    detail.parameters,
                  );
                  const defaults: Record<string, unknown> = {};
                  params.forEach((param) => {
                    if (param.defaultValue) {
                      defaults[param.name] = param.defaultValue;
                    }
                  });
                  executeForm.setFieldsValue(defaults);
                } catch {
                  // 参数解析失败，忽略
                }
              }
              setExecuteModalVisible(true);
            } catch {
              message.error("获取模板详情失败");
            }
          }}
        >
          测试执行
        </a>,
        <a
          key="edit"
          onClick={async () => {
            try {
              const detail = await getCommandTemplate(record.id);
              setEditingItem(detail);
              setDrawerVisible(true);
            } catch {
              message.error("获取模板详情失败");
            }
          }}
        >
          编辑
        </a>,
        <Popconfirm
          key="delete"
          title="确定删除该命令模板吗？"
          description="删除后无法恢复，请确认操作"
          onConfirm={async () => {
            await removeCommandTemplate(record.id);
            message.success("删除成功");
            action?.reload();
          }}
        >
          <a style={{ color: "red" }}>删除</a>
        </Popconfirm>,
      ],
    },
  ];

  const buildInitialValues = (): Record<string, unknown> => {
    if (!editingItem) {
      return { isActive: true, timeout: 60 };
    }
    return {
      ...editingItem,
    };
  };

  const handleSave = async (data: Record<string, unknown>): Promise<void> => {
    try {
      const payload: Partial<CommandTemplateDetail> = {
        id: editingItem?.id,
        name: data.name as string,
        description: data.description as string,
        script: data.script as string,
        category: data.category as CommandCategory,
        tags: data.tags as string,
        parameters: data.parameters as string,
        timeout: data.timeout as number,
        retryCount: data.retryCount as number,
        isActive: data.isActive as boolean,
      };

      await saveCommandTemplate(payload);
      message.success("保存成功");
      setDrawerVisible(false);
      setEditingItem(null);
      actionRef.current?.reload();
    } catch (error) {
      console.error(error);
      message.error("保存失败");
    }
  };

  const handleClose = (): void => {
    setDrawerVisible(false);
    setEditingItem(null);
  };

  const handleExecute = async (): Promise<void> => {
    if (!executingTemplate) return;
    try {
      const values = await executeForm.validateFields();
      if (!values.agentInstanceId) {
        message.error("请选择执行目标的 Agent 实例");
        return;
      }
      // 将参数值转换为 JSON 字符串
      const paramValues: Record<string, unknown> = {};
      if (executingTemplate.parameters) {
        try {
          const params: CommandTemplateParameter[] = JSON.parse(
            executingTemplate.parameters,
          );
          params.forEach((param) => {
            if (values[param.name] !== undefined) {
              paramValues[param.name] = values[param.name];
            }
          });
        } catch {
          // 参数解析失败
        }
      }
      const result = await executeCommandTemplate(executingTemplate.id, {
        agentInstanceId: values.agentInstanceId,
        parameters: JSON.stringify(paramValues),
      });
      setExecuteResult(`${result.message} (执行ID: ${result.executionId})`);
    } catch (error) {
      console.error(error);
      message.error("执行失败");
    }
  };

  // 解析参数定义
  const parseParameters = (
    parametersJson?: string,
  ): CommandTemplateParameter[] => {
    if (!parametersJson) return [];
    try {
      return JSON.parse(parametersJson);
    } catch {
      return [];
    }
  };

  return (
    <>
      <ProTable<CommandTemplate>
        headerTitle="命令模板列表"
        actionRef={actionRef}
        rowKey="id"
        search={{
          labelWidth: 120,
          optionRender: (_searchConfig, _formProps, dom) => [
            ...dom.reverse(),
            <Button
              key="reset"
              onClick={() => {
                setSearchKeyword("");
                actionRef.current?.reload();
              }}
            >
              重置
            </Button>,
          ],
        }}
        toolBarRender={() => [
          <Input.Search
            key="search"
            style={{ width: 200 }}
            placeholder="搜索名称/描述"
            allowClear
            value={searchKeyword}
            onChange={(e) => setSearchKeyword(e.target.value)}
            onSearch={() => actionRef.current?.reload()}
          />,
          <Button
            key="button"
            icon={<PlusOutlined />}
            type="primary"
            onClick={() => {
              setEditingItem(null);
              setDrawerVisible(true);
            }}
          >
            新建命令模板
          </Button>,
        ]}
        request={async (params) => {
          const res = await queryCommandTemplates();
          let filteredData = res;

          // 关键字搜索（搜索名称和描述）
          if (params.name || searchKeyword) {
            const keyword = params.name || searchKeyword;
            filteredData = filteredData.filter(
              (item) =>
                item.name.toLowerCase().includes(keyword.toLowerCase()) ||
                (item.description &&
                  item.description
                    .toLowerCase()
                    .includes(keyword.toLowerCase())),
            );
          }

          return {
            data: filteredData,
            success: true,
            total: filteredData.length,
          };
        }}
        columns={columns}
        pagination={{
          defaultPageSize: 10,
          showSizeChanger: true,
          showQuickJumper: true,
        }}
      />

      <DrawerForm
        visible={drawerVisible}
        title={editingItem ? "编辑命令模板" : "新建命令模板"}
        width={600}
        onClose={handleClose}
        onSave={handleSave}
        initialValues={buildInitialValues()}
      >
        <Form.Item
          name="name"
          label="名称"
          rules={[{ required: true, message: "请输入名称" }]}
        >
          <Input placeholder="请输入命令模板名称" />
        </Form.Item>
        <Form.Item name="description" label="描述">
          <Input.TextArea rows={2} placeholder="请输入描述（可选）" />
        </Form.Item>
        <Form.Item
          name="script"
          label="命令内容"
          rules={[{ required: true, message: "请输入命令内容" }]}
          extra={
            <Typography.Text type="secondary">
              支持变量语法：${`{paramName}`}，例如：echo ${`{message}`}
            </Typography.Text>
          }
        >
          <Input.TextArea
            rows={4}
            placeholder="请输入命令内容，支持 ${param} 变量语法"
          />
        </Form.Item>
        <Form.Item
          name="parameters"
          label="参数定义"
          extra={
            <Typography.Text type="secondary">
              JSON 格式数组，每个参数包含 name, type, required, defaultValue
              等字段
            </Typography.Text>
          }
        >
          <Input.TextArea
            rows={4}
            placeholder='[{"name": "param1", "type": "STRING", "required": true, "defaultValue": ""}]'
          />
        </Form.Item>
        <Form.Item name="category" label="分类">
          <Select placeholder="请选择分类（可选）" allowClear>
            {Object.entries(labels).map(([key, label]) => (
              <Select.Option key={key} value={key}>
                {label}
              </Select.Option>
            ))}
          </Select>
        </Form.Item>
        <Form.Item
          name="tags"
          label="标签"
          extra={
            <Typography.Text type="secondary">
              多个标签用逗号分隔，例如：tag1,tag2
            </Typography.Text>
          }
        >
          <Input placeholder="tag1,tag2,tag3" />
        </Form.Item>
        <Form.Item name="timeout" label="超时时间（秒）">
          <InputNumber min={1} max={3600} placeholder="默认60秒" />
        </Form.Item>
        <Form.Item name="retryCount" label="重试次数">
          <InputNumber min={0} max={10} placeholder="默认0次" />
        </Form.Item>
        <Form.Item name="isActive" label="是否启用" valuePropName="checked">
          <Switch />
        </Form.Item>
      </DrawerForm>

      {/* 执行测试模态框 */}
      <Modal
        title={`测试执行: ${executingTemplate?.name || ""}`}
        open={executeModalVisible}
        onCancel={() => setExecuteModalVisible(false)}
        onOk={handleExecute}
        width={600}
        okText="执行"
      >
        <Form form={executeForm} layout="vertical">
          <Form.Item
            name="agentInstanceId"
            label="执行目标"
            rules={[{ required: true, message: "请选择 Agent 实例" }]}
          >
            <Select placeholder="请选择要执行命令的 Agent 实例">
              {agentInstances.map((agent) => (
                <Select.Option key={agent.id} value={agent.id}>
                  {agent.host?.name || agent.id} ({agent.status})
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
          {parseParameters(executingTemplate?.parameters).map(
            (param: CommandTemplateParameter) => (
              <Form.Item
                key={param.name}
                name={param.name}
                label={param.name}
                rules={[
                  { required: param.required, message: `请输入 ${param.name}` },
                ]}
                extra={param.description}
              >
                <Input
                  placeholder={param.defaultValue || `请输入 ${param.name}`}
                />
              </Form.Item>
            ),
          )}
          {parseParameters(executingTemplate?.parameters).length === 0 && (
            <Typography.Text type="secondary">此模板无参数定义</Typography.Text>
          )}
        </Form>
        {executeResult && (
          <div style={{ marginTop: 16 }}>
            <Typography.Text strong>执行结果：</Typography.Text>
            <pre
              style={{
                background: "#f5f5f5",
                padding: 8,
                borderRadius: 4,
                maxHeight: 200,
                overflow: "auto",
              }}
            >
              {executeResult}
            </pre>
          </div>
        )}
      </Modal>
    </>
  );
};

export default CommandTemplatePage;
