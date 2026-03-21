import React, { useRef, useState, useEffect } from "react";
import {
  PlusOutlined,
  EyeOutlined,
  EnvironmentOutlined,
} from "@ant-design/icons";
import type { ActionType, ProColumns } from "@ant-design/pro-components";
import { ProTable } from "@ant-design/pro-components";
import {
  Button,
  message,
  Popconfirm,
  Form,
  Input,
  Switch,
  Tag,
  Space,
  Badge,
  Select,
  ColorPicker,
  Modal,
  Descriptions,
  Table,
  Typography,
} from "antd";
import { DrawerForm } from "../../../components/DrawerForm";
import {
  queryEnvironments,
  saveEnvironment,
  removeEnvironment,
  queryHosts,
} from "../../../services/infra";
import type { Environment, Host } from "../../../types";

const { Option } = Select;
const { Text } = Typography;

const EnvironmentList: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [drawerVisible, setDrawerVisible] = useState(false);
  const [editingItem, setEditingItem] = useState<Partial<Environment> | null>(
    null,
  );
  const [detailVisible, setDetailVisible] = useState(false);
  const [selectedEnv, setSelectedEnv] = useState<Environment | null>(null);
  const [envHosts, setEnvHosts] = useState<Host[]>([]);
  const [hostCounts, setHostCounts] = useState<Record<string, number>>({});
  const [hosts, setHosts] = useState<Host[]>([]);

  // 加载所有主机以计算各环境的主机数量
  useEffect(() => {
    const fetchHosts = async () => {
      try {
        const res = await queryHosts();
        const hostList = Array.isArray(res) ? res : res.data || [];
        setHosts(hostList);

        // 计算每个环境的主机数量
        const counts: Record<string, number> = {};
        hostList.forEach((host) => {
          const envId = host.environmentId || host.environment?.id;
          if (envId) {
            counts[envId] = (counts[envId] || 0) + 1;
          }
        });
        setHostCounts(counts);
      } catch (error) {
        console.error("Failed to fetch hosts", error);
      }
    };
    fetchHosts();
  }, []);

  const handleSave = async (data: any) => {
    try {
      const saveData = {
        ...editingItem,
        ...data,
        color:
          typeof data.color === "string"
            ? data.color
            : data.color?.toHexString?.() || data.color,
      };
      await saveEnvironment(saveData);
      message.success("保存成功");
      setDrawerVisible(false);
      setEditingItem(null);
      actionRef.current?.reload();
    } catch (error: any) {
      message.error(error.message || "保存失败");
    }
  };

  const handleDelete = async (id: string) => {
    try {
      await removeEnvironment(id);
      message.success("删除成功");
      actionRef.current?.reload();
    } catch (error: any) {
      message.error(error.message || "删除失败");
    }
  };

  const handleViewDetail = (env: Environment) => {
    setSelectedEnv(env);
    // 筛选该环境下的主机
    const envHostList = hosts.filter(
      (h) => h.environmentId === env.id || h.environment?.id === env.id,
    );
    setEnvHosts(envHostList);
    setDetailVisible(true);
  };

  const columns: ProColumns<Environment>[] = [
    {
      title: "名称",
      dataIndex: "name",
      key: "name",
      width: 150,
      render: (text: string, record: Environment) => (
        <Space>
          {record.color && (
            <Tag
              color={record.color}
              style={{ width: 8, height: 8, borderRadius: 4, padding: 0 }}
            />
          )}
          <a onClick={() => handleViewDetail(record)}>{text}</a>
        </Space>
      ),
    },
    {
      title: "代码",
      dataIndex: "code",
      key: "code",
      width: 120,
      copyable: true,
    },
    {
      title: "描述",
      dataIndex: "description",
      key: "description",
      ellipsis: true,
    },
    {
      title: "主机数量",
      key: "hostCount",
      width: 100,
      align: "center",
      render: (_, record: Environment) => (
        <Badge
          count={hostCounts[record.id] || 0}
          showZero
          style={{ backgroundColor: "#1890ff" }}
        />
      ),
    },
    {
      title: "状态",
      dataIndex: "enabled",
      key: "enabled",
      width: 80,
      render: (enabled: boolean) => (
        <Tag color={enabled !== false ? "success" : "default"}>
          {enabled !== false ? "启用" : "禁用"}
        </Tag>
      ),
    },
    {
      title: "创建时间",
      dataIndex: "createdAt",
      key: "createdAt",
      valueType: "dateTime",
      width: 170,
    },
    {
      title: "操作",
      valueType: "option",
      width: 180,
      render: (_, record) => [
        <Button
          key="view"
          type="link"
          size="small"
          icon={<EyeOutlined />}
          onClick={() => handleViewDetail(record)}
        >
          详情
        </Button>,
        <Button
          key="edit"
          type="link"
          size="small"
          onClick={() => {
            setEditingItem(record);
            setDrawerVisible(true);
          }}
        >
          编辑
        </Button>,
        <Popconfirm
          key="delete"
          title="确定删除该环境吗？"
          onConfirm={() => handleDelete(record.id)}
        >
          <Button type="link" size="small" danger>
            删除
          </Button>
        </Popconfirm>,
      ],
    },
  ];

  const hostColumns = [
    {
      title: "主机名",
      dataIndex: "hostname",
      key: "hostname",
    },
    {
      title: "IP",
      dataIndex: "name",
      key: "name",
    },
    {
      title: "操作系统",
      dataIndex: "os",
      key: "os",
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      render: (status: string) => {
        const statusConfig: Record<string, { color: string; text: string }> = {
          UNCONNECTED: { color: "default", text: "未连接" },
          OFFLINE: { color: "error", text: "离线" },
          ONLINE: { color: "success", text: "在线" },
          EXCEPTION: { color: "warning", text: "异常" },
          MAINTENANCE: { color: "processing", text: "维护中" },
        };
        const config = statusConfig[status] || {
          color: "default",
          text: status,
        };
        return <Tag color={config.color}>{config.text}</Tag>;
      },
    },
    {
      title: "最后心跳",
      dataIndex: "lastHeartbeat",
      key: "lastHeartbeat",
      render: (time: string) => (time ? new Date(time).toLocaleString() : "-"),
    },
  ];

  return (
    <>
      <ProTable<Environment>
        actionRef={actionRef}
        rowKey="id"
        columns={columns}
        request={async (params) => {
          const res = await queryEnvironments({
            current: params.current,
            pageSize: params.pageSize,
          });
          const data = Array.isArray(res) ? res : res.data || [];
          return {
            data,
            total: data.length,
            success: true,
          };
        }}
        toolBarRender={() => [
          <Button
            key="create"
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => {
              setEditingItem(null);
              setDrawerVisible(true);
            }}
          >
            新建环境
          </Button>,
        ]}
        search={false}
      />

      <DrawerForm
        title={editingItem ? "编辑环境" : "新建环境"}
        open={drawerVisible}
        onClose={() => {
          setDrawerVisible(false);
          setEditingItem(null);
        }}
        onFinish={handleSave}
        initialValues={editingItem || { enabled: true }}
      >
        <Form.Item
          name="name"
          label="名称"
          rules={[{ required: true, message: "请输入名称" }]}
        >
          <Input placeholder="请输入环境名称" />
        </Form.Item>
        <Form.Item
          name="code"
          label="代码"
          rules={[
            { required: true, message: "请输入代码" },
            { pattern: /^[A-Z_]+$/, message: "代码只能包含大写字母和下划线" },
          ]}
        >
          <Input placeholder="如: DEV, TEST, PROD" disabled={!!editingItem} />
        </Form.Item>
        <Form.Item name="description" label="描述">
          <Input.TextArea rows={3} placeholder="请输入描述" />
        </Form.Item>
        <Form.Item name="color" label="颜色标签">
          <ColorPicker format="hex" />
        </Form.Item>
        <Form.Item
          name="enabled"
          label="状态"
          valuePropName="checked"
          tooltip="禁用后该环境下的主机将无法被选择"
        >
          <Switch checkedChildren="启用" unCheckedChildren="禁用" />
        </Form.Item>
      </DrawerForm>

      <Modal
        title={
          <Space>
            <EnvironmentOutlined />
            <span>{selectedEnv?.name} - 详情</span>
          </Space>
        }
        open={detailVisible}
        onCancel={() => setDetailVisible(false)}
        footer={null}
        width={800}
      >
        {selectedEnv && (
          <>
            <Descriptions
              column={2}
              bordered
              size="small"
              style={{ marginBottom: 16 }}
            >
              <Descriptions.Item label="名称">
                {selectedEnv.name}
              </Descriptions.Item>
              <Descriptions.Item label="代码">
                <Text copyable>{selectedEnv.code}</Text>
              </Descriptions.Item>
              <Descriptions.Item label="描述">
                {selectedEnv.description || "-"}
              </Descriptions.Item>
              <Descriptions.Item label="状态">
                <Tag
                  color={selectedEnv.enabled !== false ? "success" : "default"}
                >
                  {selectedEnv.enabled !== false ? "启用" : "禁用"}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="主机数量">
                <Badge
                  count={hostCounts[selectedEnv.id] || 0}
                  showZero
                  style={{ backgroundColor: "#1890ff" }}
                />
              </Descriptions.Item>
              <Descriptions.Item label="创建时间">
                {selectedEnv.createdAt
                  ? new Date(selectedEnv.createdAt).toLocaleString()
                  : "-"}
              </Descriptions.Item>
            </Descriptions>

            <Typography.Title level={5} style={{ marginBottom: 12 }}>
              关联主机 ({envHosts.length})
            </Typography.Title>
            <Table
              rowKey="id"
              columns={hostColumns}
              dataSource={envHosts}
              size="small"
              pagination={{ pageSize: 5 }}
              locale={{ emptyText: "暂无主机" }}
            />
          </>
        )}
      </Modal>
    </>
  );
};

export default EnvironmentList;
