import React, { useRef, useState } from "react";
import {
  PlusOutlined,
  HistoryOutlined,
  SyncOutlined,
  SwapOutlined,
  DeleteOutlined,
  EditOutlined,
} from "@ant-design/icons";
import type { ActionType, ProColumns } from "@ant-design/pro-components";
import { ProTable } from "@ant-design/pro-components";
import {
  Button,
  message,
  Popconfirm,
  Form,
  Input,
  Select,
  Switch,
  Tag,
  Space,
  Modal,
  Table,
  Typography,
  Divider,
  Descriptions,
  Tooltip,
} from "antd";
import { DrawerForm } from "../../components/DrawerForm";
import {
  listConfigs,
  createConfig,
  updateConfig,
  deleteConfig,
  getConfigVersions,
  rollbackConfig,
  compareConfigs,
  listEnvironments,
} from "../../services/config";
import type {
  ConfigItem,
  ConfigItemCreate,
  ConfigVersion,
  ConfigDiffResult,
  ConfigDiffItem,
  ConfigEnvironment,
} from "../../types/config";
import dayjs from "dayjs";

const { Text } = Typography;
const { TextArea } = Input;

const ConfigPage: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [drawerVisible, setDrawerVisible] = useState(false);
  const [editingItem, setEditingItem] = useState<ConfigItem | null>(null);
  const [versionModalVisible, setVersionModalVisible] = useState(false);
  const [selectedConfig, setSelectedConfig] = useState<ConfigItem | null>(null);
  const [versions, setVersions] = useState<ConfigVersion[]>([]);
  const [versionsLoading, setVersionsLoading] = useState(false);
  const [compareModalVisible, setCompareModalVisible] = useState(false);
  const [diffResult, setDiffResult] = useState<ConfigDiffResult | null>(null);
  const [diffLoading, setDiffLoading] = useState(false);
  const [sourceEnv, setSourceEnv] = useState<string>("");
  const [targetEnv, setTargetEnv] = useState<string>("");
  const [environments, setEnvironments] = useState<ConfigEnvironment[]>([]);
  const [environmentsLoading, setEnvironmentsLoading] = useState(false);
  const [rollbackModalVisible, setRollbackModalVisible] = useState(false);
  const [rollbackVersion, setRollbackVersion] = useState<ConfigVersion | null>(
    null,
  );

  const [createForm] = Form.useForm();

  // 加载环境列表
  const loadEnvironments = async () => {
    setEnvironmentsLoading(true);
    try {
      const envs = await listEnvironments();
      setEnvironments(envs || []);
    } catch (error: any) {
      message.error(error.message || "获取环境列表失败");
      setEnvironments([]);
    } finally {
      setEnvironmentsLoading(false);
    }
  };

  // 处理创建
  const handleCreate = async (data: ConfigItemCreate) => {
    try {
      await createConfig(data);
      message.success("创建成功");
      setDrawerVisible(false);
      setEditingItem(null);
      createForm.resetFields();
      actionRef.current?.reload();
    } catch (error: any) {
      message.error(error.message || "创建失败");
    }
  };

  // 处理更新
  const handleUpdate = async (data: ConfigItemCreate) => {
    try {
      if (!editingItem?.id) return;
      await updateConfig(editingItem.id, data);
      message.success("更新成功");
      setDrawerVisible(false);
      setEditingItem(null);
      actionRef.current?.reload();
    } catch (error: any) {
      message.error(error.message || "更新失败");
    }
  };

  // 处理删除
  const handleDelete = async (id: string) => {
    try {
      await deleteConfig(id);
      message.success("删除成功");
      actionRef.current?.reload();
    } catch (error: any) {
      message.error(error.message || "删除失败");
    }
  };

  // 查看版本历史
  const handleViewVersions = async (config: ConfigItem) => {
    setSelectedConfig(config);
    setVersionModalVisible(true);
    setVersionsLoading(true);
    try {
      const versionList = await getConfigVersions(config.id);
      setVersions(versionList || []);
    } catch (error: any) {
      message.error(error.message || "获取版本历史失败");
      setVersions([]);
    } finally {
      setVersionsLoading(false);
    }
  };

  // 处理回滚
  const handleRollback = async (reason?: string) => {
    if (!selectedConfig || !rollbackVersion) return;
    try {
      const result = await rollbackConfig(selectedConfig.id, {
        versionId: rollbackVersion.id,
        reason: reason || "",
      });
      if (result.success) {
        message.success(
          `已成功回滚到版本 ${rollbackVersion.version}, 新版本号: ${result.newVersion}`,
        );
        setRollbackModalVisible(false);
        setRollbackVersion(null);
        handleViewVersions(selectedConfig);
        actionRef.current?.reload();
      } else {
        message.error(result.message || "回滚失败");
      }
    } catch (error: any) {
      message.error(error.message || "回滚失败");
    }
  };

  // 环境对比
  const handleCompare = async () => {
    if (!sourceEnv || !targetEnv) {
      message.warning("请选择要对比的环境");
      return;
    }
    if (sourceEnv === targetEnv) {
      message.warning("请选择不同的环境进行对比");
      return;
    }
    setDiffLoading(true);
    try {
      const result = await compareConfigs(sourceEnv, targetEnv);
      setDiffResult(result);
    } catch (error: any) {
      message.error(error.message || "对比失败");
      setDiffResult(null);
    } finally {
      setDiffLoading(false);
    }
  };

  const columns: ProColumns<ConfigItem>[] = [
    {
      title: "Key",
      dataIndex: "key",
      key: "key",
      width: 200,
      copyable: true,
      render: (text) => <Text strong>{text}</Text>,
    },
    {
      title: "Value",
      dataIndex: "value",
      key: "value",
      width: 250,
      ellipsis: true,
      render: (text) => (
        <Tooltip title={text}>
          <Text code>{text}</Text>
        </Tooltip>
      ),
    },
    {
      title: "环境",
      dataIndex: "environment",
      key: "environment",
      width: 100,
      render: (text) => (
        <Tag color={text === "production" ? "red" : "blue"}>{text}</Tag>
      ),
    },
    {
      title: "分组",
      dataIndex: "group",
      key: "group",
      width: 120,
      render: (text) => (text ? <Tag color="purple">{text}</Tag> : "-"),
    },
    {
      title: "状态",
      dataIndex: "active",
      key: "active",
      width: 80,
      render: (_, record) => (
        <Tag color={record.active ? "success" : "default"}>
          {record.active ? "启用" : "禁用"}
        </Tag>
      ),
    },
    {
      title: "版本",
      dataIndex: "version",
      key: "version",
      width: 80,
      render: (text) => <Tag>v{text}</Tag>,
    },
    {
      title: "描述",
      dataIndex: "description",
      key: "description",
      width: 150,
      ellipsis: true,
      render: (text) => text || "-",
    },
    {
      title: "创建时间",
      dataIndex: "createdAt",
      key: "createdAt",
      valueType: "dateTime",
      width: 170,
      render: (time) =>
        time ? dayjs(time as string).format("YYYY-MM-DD HH:mm:ss") : "-",
    },
    {
      title: "操作",
      valueType: "option",
      width: 200,
      render: (_, record) => [
        <Button
          key="history"
          type="link"
          size="small"
          icon={<HistoryOutlined />}
          onClick={() => handleViewVersions(record)}
        >
          历史
        </Button>,
        <Button
          key="edit"
          type="link"
          size="small"
          icon={<EditOutlined />}
          onClick={() => {
            setEditingItem(record);
            setDrawerVisible(true);
          }}
        >
          编辑
        </Button>,
        <Popconfirm
          key="delete"
          title="确定删除该配置项吗？"
          onConfirm={() => handleDelete(record.id)}
        >
          <Button type="link" size="small" danger icon={<DeleteOutlined />}>
            删除
          </Button>
        </Popconfirm>,
      ],
    },
  ];

  const versionColumns = [
    {
      title: "版本",
      dataIndex: "version",
      key: "version",
      width: 80,
      render: (v: number) => <Tag>v{v}</Tag>,
    },
    {
      title: "Value",
      dataIndex: "value",
      key: "value",
      ellipsis: true,
      render: (v: string) => (
        <Tooltip title={v}>
          <Text code style={{ maxWidth: 300 }}>
            {v}
          </Text>
        </Tooltip>
      ),
    },
    {
      title: "状态",
      dataIndex: "active",
      key: "active",
      width: 80,
      render: (active: boolean) => (
        <Tag color={active ? "success" : "default"}>
          {active ? "启用" : "禁用"}
        </Tag>
      ),
    },
    {
      title: "变更原因",
      dataIndex: "changeReason",
      key: "changeReason",
      width: 150,
      ellipsis: true,
      render: (reason: string) => reason || "-",
    },
    {
      title: "创建时间",
      dataIndex: "createdAt",
      key: "createdAt",
      width: 170,
      render: (time: string) =>
        time ? dayjs(time).format("YYYY-MM-DD HH:mm:ss") : "-",
    },
    {
      title: "操作",
      key: "action",
      width: 100,
      render: (_: any, record: ConfigVersion) => (
        <Button
          type="link"
          size="small"
          icon={<SyncOutlined />}
          onClick={() => {
            setRollbackVersion(record);
            setRollbackModalVisible(true);
          }}
        >
          回滚
        </Button>
      ),
    },
  ];

  const diffColumns = [
    {
      title: "Key",
      dataIndex: "key",
      key: "key",
      width: 200,
      render: (key: string) => <Text strong>{key}</Text>,
    },
    {
      title: "分组",
      dataIndex: "group",
      key: "group",
      width: 100,
      render: (group: string) =>
        group ? <Tag color="purple">{group}</Tag> : "-",
    },
    {
      title: "差异类型",
      dataIndex: "diffType",
      key: "diffType",
      width: 100,
      render: (type: string) => {
        const colorMap: Record<string, string> = {
          ADDED: "green",
          REMOVED: "red",
          MODIFIED: "orange",
        };
        const textMap: Record<string, string> = {
          ADDED: "新增",
          REMOVED: "删除",
          MODIFIED: "修改",
        };
        return <Tag color={colorMap[type]}>{textMap[type]}</Tag>;
      },
    },
    {
      title: `${diffResult?.sourceEnvironment || "源环境"} Value`,
      dataIndex: "sourceValue",
      key: "sourceValue",
      width: 200,
      ellipsis: true,
      render: (v: string) =>
        v ? (
          <Tooltip title={v}>
            <Text code>{v}</Text>
          </Tooltip>
        ) : (
          <Text type="secondary">-</Text>
        ),
    },
    {
      title: `${diffResult?.targetEnvironment || "目标环境"} Value`,
      dataIndex: "targetValue",
      key: "targetValue",
      width: 200,
      ellipsis: true,
      render: (v: string) =>
        v ? (
          <Tooltip title={v}>
            <Text code>{v}</Text>
          </Tooltip>
        ) : (
          <Text type="secondary">-</Text>
        ),
    },
  ];

  return (
    <>
      <ProTable<ConfigItem>
        actionRef={actionRef}
        rowKey="id"
        columns={columns}
        request={async (params) => {
          const query: any = {};
          if (params.key) query.key = params.key;
          if (params.environment) query.environment = params.environment;
          if (params.group) query.group = params.group;
          if (params.active !== undefined) query.active = params.active;
          if (params.keyword) query.keyword = params.keyword;

          const res = await listConfigs(query);
          return {
            data: res || [],
            total: res?.length || 0,
            success: true,
          };
        }}
        toolBarRender={() => [
          <Button
            key="compare"
            icon={<SwapOutlined />}
            onClick={() => {
              setCompareModalVisible(true);
              loadEnvironments();
              setDiffResult(null);
            }}
          >
            环境对比
          </Button>,
          <Button
            key="create"
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => {
              setEditingItem(null);
              setDrawerVisible(true);
            }}
          >
            新建配置
          </Button>,
        ]}
        search={{
          filterType: "light",
        }}
        options={{
          density: false,
          fullScreen: false,
          reload: true,
          setting: false,
        }}
      />

      <DrawerForm
        title={editingItem ? "编辑配置" : "新建配置"}
        visible={drawerVisible}
        onClose={() => {
          setDrawerVisible(false);
          setEditingItem(null);
          createForm.resetFields();
        }}
        onSave={editingItem ? handleUpdate : handleCreate}
        initialValues={
          editingItem
            ? {
                key: editingItem.key,
                value: editingItem.value,
                environment: editingItem.environment,
                group: editingItem.group,
                description: editingItem.description,
                active: editingItem.active,
              }
            : { active: true }
        }
        form={createForm}
        width={600}
      >
        <Form.Item
          name="key"
          label="Key"
          rules={[{ required: true, message: "请输入配置 Key" }]}
        >
          <Input placeholder="请输入配置 Key" maxLength={255} />
        </Form.Item>
        <Form.Item
          name="value"
          label="Value"
          rules={[{ required: true, message: "请输入配置 Value" }]}
        >
          <TextArea rows={4} placeholder="请输入配置 Value" />
        </Form.Item>
        <Form.Item
          name="environment"
          label="环境"
          rules={[{ required: true, message: "请选择环境" }]}
        >
          <Select placeholder="请选择环境">
            <Select.Option value="development">development</Select.Option>
            <Select.Option value="testing">testing</Select.Option>
            <Select.Option value="staging">staging</Select.Option>
            <Select.Option value="production">production</Select.Option>
          </Select>
        </Form.Item>
        <Form.Item name="group" label="分组">
          <Input placeholder="请输入分组名称" maxLength={100} />
        </Form.Item>
        <Form.Item name="description" label="描述">
          <TextArea rows={2} placeholder="请输入描述" maxLength={500} />
        </Form.Item>
        <Form.Item name="active" label="状态" valuePropName="checked">
          <Switch checkedChildren="启用" unCheckedChildren="禁用" />
        </Form.Item>
      </DrawerForm>

      <Modal
        title={
          <Space>
            <HistoryOutlined />
            <span>{selectedConfig?.key} - 版本历史</span>
          </Space>
        }
        open={versionModalVisible}
        onCancel={() => {
          setVersionModalVisible(false);
          setSelectedConfig(null);
          setVersions([]);
        }}
        footer={null}
        width={800}
      >
        {selectedConfig && (
          <>
            <Descriptions
              column={2}
              bordered
              size="small"
              style={{ marginBottom: 16 }}
            >
              <Descriptions.Item label="Key">
                {selectedConfig.key}
              </Descriptions.Item>
              <Descriptions.Item label="环境">
                <Tag
                  color={
                    selectedConfig.environment === "production" ? "red" : "blue"
                  }
                >
                  {selectedConfig.environment}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="分组">
                {selectedConfig.group || "-"}
              </Descriptions.Item>
              <Descriptions.Item label="当前版本">
                v{selectedConfig.version}
              </Descriptions.Item>
            </Descriptions>

            <Typography.Title level={5} style={{ marginBottom: 12 }}>
              版本记录 ({versions.length})
            </Typography.Title>
            <Table
              rowKey="id"
              columns={versionColumns}
              dataSource={versions}
              size="small"
              loading={versionsLoading}
              pagination={{ pageSize: 10 }}
              locale={{ emptyText: "暂无版本历史" }}
            />
          </>
        )}
      </Modal>

      <Modal
        title={
          <Space>
            <SwapOutlined />
            <span>环境配置对比</span>
          </Space>
        }
        open={compareModalVisible}
        onCancel={() => {
          setCompareModalVisible(false);
          setDiffResult(null);
          setSourceEnv("");
          setTargetEnv("");
        }}
        footer={[
          <Button
            key="close"
            onClick={() => {
              setCompareModalVisible(false);
              setDiffResult(null);
            }}
          >
            关闭
          </Button>,
        ]}
        width={900}
      >
        <Space style={{ marginBottom: 16 }}>
          <Select
            placeholder="选择源环境"
            style={{ width: 150 }}
            value={sourceEnv}
            onChange={setSourceEnv}
            loading={environmentsLoading}
          >
            {environments.map((env) => (
              <Select.Option key={env.name} value={env.name}>
                {env.name}
              </Select.Option>
            ))}
          </Select>
          <Text>对比</Text>
          <Select
            placeholder="选择目标环境"
            style={{ width: 150 }}
            value={targetEnv}
            onChange={setTargetEnv}
            loading={environmentsLoading}
          >
            {environments.map((env) => (
              <Select.Option key={env.name} value={env.name}>
                {env.name}
              </Select.Option>
            ))}
          </Select>
          <Button
            type="primary"
            onClick={handleCompare}
            loading={diffLoading}
            disabled={!sourceEnv || !targetEnv}
          >
            开始对比
          </Button>
        </Space>

        {diffResult && (
          <>
            <Divider />
            <Descriptions column={2} size="small">
              <Descriptions.Item label="源环境">
                <Tag color="blue">{diffResult.sourceEnvironment}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label="目标环境">
                <Tag color="green">{diffResult.targetEnvironment}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label="差异总数">
                <Text strong>{diffResult.totalDiffs}</Text>
              </Descriptions.Item>
            </Descriptions>

            <Typography.Title
              level={5}
              style={{ marginTop: 16, marginBottom: 12 }}
            >
              差异详情
            </Typography.Title>
            <Table
              rowKey={(record: ConfigDiffItem) =>
                `${record.key}-${record.diffType}`
              }
              columns={diffColumns}
              dataSource={diffResult.diffs}
              size="small"
              pagination={{ pageSize: 10 }}
              locale={{ emptyText: "无差异" }}
            />
          </>
        )}
      </Modal>

      <Modal
        title={
          <Space>
            <SyncOutlined />
            <span>回滚确认</span>
          </Space>
        }
        open={rollbackModalVisible}
        onCancel={() => {
          setRollbackModalVisible(false);
          setRollbackVersion(null);
        }}
        onOk={() => handleRollback()}
        okText="确认回滚"
        okButtonProps={{ danger: true }}
      >
        {rollbackVersion && selectedConfig && (
          <div>
            <Text>
              确定要将配置 <Text strong>{selectedConfig.key}</Text> 回滚到版本
              <Tag style={{ marginLeft: 8 }}>v{rollbackVersion.version}</Tag>
              吗？
            </Text>
            <Descriptions
              column={1}
              bordered
              size="small"
              style={{ marginTop: 16 }}
            >
              <Descriptions.Item label="目标版本">
                v{rollbackVersion.version}
              </Descriptions.Item>
              <Descriptions.Item label="目标 Value">
                <Text code>{rollbackVersion.value}</Text>
              </Descriptions.Item>
              <Descriptions.Item label="当前版本">
                v{selectedConfig.version}
              </Descriptions.Item>
              <Descriptions.Item label="当前 Value">
                <Text code>{selectedConfig.value}</Text>
              </Descriptions.Item>
            </Descriptions>
            <Form.Item
              label="回滚原因"
              style={{ marginTop: 16, marginBottom: 0 }}
            >
              <TextArea
                id="rollback-reason"
                rows={2}
                placeholder="请输入回滚原因（可选）"
              />
            </Form.Item>
          </div>
        )}
      </Modal>
    </>
  );
};

export default ConfigPage;
