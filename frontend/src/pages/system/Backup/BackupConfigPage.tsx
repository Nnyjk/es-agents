import React, { useRef, useState, useEffect } from "react";
import { PlusOutlined, CheckCircleOutlined, CloseCircleOutlined } from "@ant-design/icons";
import type { ActionType, ProColumns } from "@ant-design/pro-components";
import { ProTable, ProForm } from "@ant-design/pro-components";
import {
  Button,
  message,
  Popconfirm,
  Tag,
  Space,
  Card,
  Tabs,
  Form,
  Input,
  Select,
  InputNumber,
  Switch,
  Divider,
  Alert,
} from "antd";
import { DrawerForm } from "../../../components/DrawerForm";
import {
  getStorageConfigs,
  createStorageConfig,
  updateStorageConfig,
  deleteStorageConfig,
  testStorageConfig,
  getAlertConfig,
  updateAlertConfig,
} from "../../../services/backup";
import type { StorageConfig, StorageConfigRequest, StorageType, BackupAlertConfig } from "../../../types/backup";

const storageTypeTexts: Record<StorageType, string> = {
  LOCAL: "本地存储",
  S3: "对象存储(S3)",
  NFS: "NFS存储",
  FTP: "FTP存储",
};

const BackupConfigPage: React.FC = () => {
  // 存储配置相关
  const storageActionRef = useRef<ActionType>();
  const [storageDrawerVisible, setStorageDrawerVisible] = useState(false);
  const [editingStorage, setEditingStorage] = useState<StorageConfig | null>(null);
  const [testingStorage, setTestingStorage] = useState<number | null>(null);

  // 告警配置相关
  const [alertConfig, setAlertConfig] = useState<BackupAlertConfig | null>(null);
  const [alertForm] = Form.useForm();
  const [alertLoading, setAlertLoading] = useState(false);

  useEffect(() => {
    loadAlertConfig();
  }, []);

  const loadAlertConfig = async () => {
    try {
      const data = await getAlertConfig();
      setAlertConfig(data);
      alertForm.setFieldsValue(data);
    } catch (error) {
      // 忽略错误，可能是首次加载
    }
  };

  // 存储配置操作
  const handleCreateStorage = () => {
    setEditingStorage(null);
    setStorageDrawerVisible(true);
  };

  const handleEditStorage = (record: StorageConfig) => {
    setEditingStorage(record);
    setStorageDrawerVisible(true);
  };

  const handleDeleteStorage = async (id: number) => {
    try {
      await deleteStorageConfig(id);
      message.success("删除成功");
      storageActionRef.current?.reload();
    } catch (error) {
      message.error("删除失败");
    }
  };

  const handleTestStorage = async (id: number) => {
    setTestingStorage(id);
    try {
      const result = await testStorageConfig(id);
      if (result.success) {
        message.success(`连接测试成功：${result.message}`);
      } else {
        message.error(`连接测试失败：${result.message}`);
      }
    } catch (error) {
      message.error("连接测试失败");
    } finally {
      setTestingStorage(null);
    }
  };

  const handleSubmitStorage = async (values: StorageConfigRequest) => {
    try {
      if (editingStorage) {
        await updateStorageConfig(editingStorage.id, values);
        message.success("更新成功");
      } else {
        await createStorageConfig(values);
        message.success("创建成功");
      }
      setStorageDrawerVisible(false);
      storageActionRef.current?.reload();
      return true;
    } catch (error) {
      message.error(editingStorage ? "更新失败" : "创建失败");
      return false;
    }
  };

  // 告警配置操作
  const handleSaveAlertConfig = async () => {
    try {
      const values = await alertForm.validateFields();
      setAlertLoading(true);
      const data = await updateAlertConfig(values);
      setAlertConfig(data);
      message.success("保存成功");
    } catch (error) {
      message.error("保存失败");
    } finally {
      setAlertLoading(false);
    }
  };

  // 存储配置表格列
  const storageColumns: ProColumns<StorageConfig>[] = [
    {
      title: "配置名称",
      dataIndex: "name",
      key: "name",
      width: 150,
    },
    {
      title: "存储类型",
      dataIndex: "storageType",
      key: "storageType",
      width: 120,
      render: (_, record) => storageTypeTexts[record.storageType],
    },
    {
      title: "配置详情",
      key: "config",
      width: 300,
      ellipsis: true,
      render: (_, record) => {
        const config = record.config;
        if (record.storageType === "LOCAL") {
          return config.path || "-";
        } else if (record.storageType === "S3") {
          return `${config.bucket}/${config.region || "default"}`;
        } else if (record.storageType === "NFS") {
          return config.server || "-";
        } else if (record.storageType === "FTP") {
          return config.host || "-";
        }
        return "-";
      },
    },
    {
      title: "默认",
      dataIndex: "isDefault",
      key: "isDefault",
      width: 80,
      render: (_, record) =>
        record.isDefault ? (
          <Tag color="blue">默认</Tag>
        ) : null,
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 80,
      render: (_, record) => (
        <Tag color={record.status === "ACTIVE" ? "success" : "default"}>
          {record.status === "ACTIVE" ? "启用" : "禁用"}
        </Tag>
      ),
    },
    {
      title: "操作",
      key: "action",
      width: 180,
      fixed: "right",
      render: (_, record) => (
        <Space size="small">
          <Button type="link" size="small" onClick={() => handleEditStorage(record)}>
            编辑
          </Button>
          <Button
            type="link"
            size="small"
            onClick={() => handleTestStorage(record.id)}
            loading={testingStorage === record.id}
          >
            测试连接
          </Button>
          <Popconfirm
            title="确定要删除此存储配置吗？"
            onConfirm={() => handleDeleteStorage(record.id)}
          >
            <Button type="link" size="small" danger>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  // 根据存储类型渲染不同的配置表单
  const renderStorageConfigFields = (storageType: StorageType) => {
    switch (storageType) {
      case "LOCAL":
        return (
          <>
            <ProForm.Item
              name={["config", "path"]}
              label="存储路径"
              rules={[{ required: true, message: "请输入存储路径" }]}
            >
              <Input placeholder="/data/backups" />
            </ProForm.Item>
            <ProForm.Item
              name={["config", "maxSize"]}
              label="最大存储空间(GB)"
            >
              <InputNumber min={1} style={{ width: "100%" }} placeholder="不限制" />
            </ProForm.Item>
          </>
        );
      case "S3":
        return (
          <>
            <ProForm.Item
              name={["config", "endpoint"]}
              label="Endpoint"
              rules={[{ required: true, message: "请输入Endpoint" }]}
            >
              <Input placeholder="https://s3.amazonaws.com" />
            </ProForm.Item>
            <ProForm.Item
              name={["config", "region"]}
              label="Region"
            >
              <Input placeholder="us-east-1" />
            </ProForm.Item>
            <ProForm.Item
              name={["config", "bucket"]}
              label="Bucket"
              rules={[{ required: true, message: "请输入Bucket名称" }]}
            >
              <Input placeholder="my-backup-bucket" />
            </ProForm.Item>
            <ProForm.Item
              name={["config", "accessKey"]}
              label="Access Key"
              rules={[{ required: true, message: "请输入Access Key" }]}
            >
              <Input.Password />
            </ProForm.Item>
            <ProForm.Item
              name={["config", "secretKey"]}
              label="Secret Key"
              rules={[{ required: true, message: "请输入Secret Key" }]}
            >
              <Input.Password />
            </ProForm.Item>
          </>
        );
      case "NFS":
        return (
          <>
            <ProForm.Item
              name={["config", "server"]}
              label="NFS服务器"
              rules={[{ required: true, message: "请输入NFS服务器地址" }]}
            >
              <Input placeholder="192.168.1.100" />
            </ProForm.Item>
            <ProForm.Item
              name={["config", "path"]}
              label="共享路径"
              rules={[{ required: true, message: "请输入共享路径" }]}
            >
              <Input placeholder="/data/backups" />
            </ProForm.Item>
            <ProForm.Item
              name={["config", "mountOptions"]}
              label="挂载选项"
            >
              <Input placeholder="rw,sync" />
            </ProForm.Item>
          </>
        );
      case "FTP":
        return (
          <>
            <ProForm.Item
              name={["config", "host"]}
              label="FTP服务器"
              rules={[{ required: true, message: "请输入FTP服务器地址" }]}
            >
              <Input placeholder="ftp.example.com" />
            </ProForm.Item>
            <ProForm.Item
              name={["config", "port"]}
              label="端口"
              initialValue={21}
            >
              <InputNumber min={1} max={65535} style={{ width: "100%" }} />
            </ProForm.Item>
            <ProForm.Item
              name={["config", "username"]}
              label="用户名"
              rules={[{ required: true, message: "请输入用户名" }]}
            >
              <Input />
            </ProForm.Item>
            <ProForm.Item
              name={["config", "password"]}
              label="密码"
              rules={[{ required: true, message: "请输入密码" }]}
            >
              <Input.Password />
            </ProForm.Item>
            <ProForm.Item
              name={["config", "path"]}
              label="存储路径"
            >
              <Input placeholder="/backups" />
            </ProForm.Item>
          </>
        );
      default:
        return null;
    }
  };

  return (
    <Tabs defaultActiveKey="storage">
      <Tabs.TabPane tab="存储配置" key="storage">
        <ProTable<StorageConfig>
          columns={storageColumns}
          actionRef={storageActionRef}
          rowKey="id"
          request={async () => {
            try {
              const data = await getStorageConfigs();
              return {
                data,
                success: true,
              };
            } catch (error) {
              return {
                data: [],
                success: false,
              };
            }
          }}
          toolBarRender={() => [
            <Button
              key="create"
              type="primary"
              icon={<PlusOutlined />}
              onClick={handleCreateStorage}
            >
              新建配置
            </Button>,
          ]}
          search={false}
          pagination={false}
        />

        <DrawerForm
          title={editingStorage ? "编辑存储配置" : "新建存储配置"}
          open={storageDrawerVisible}
          onClose={() => setStorageDrawerVisible(false)}
          onFinish={handleSubmitStorage}
          initialValues={editingStorage || { storageType: "LOCAL", status: "ACTIVE" }}
          width={600}
        >
          <ProForm.Item
            name="name"
            label="配置名称"
            rules={[{ required: true, message: "请输入配置名称" }]}
          >
            <Input placeholder="如：本地备份存储" />
          </ProForm.Item>
          <ProForm.Item
            name="storageType"
            label="存储类型"
            rules={[{ required: true, message: "请选择存储类型" }]}
          >
            <Select
              options={[
                { label: "本地存储", value: "LOCAL" },
                { label: "对象存储(S3)", value: "S3" },
                { label: "NFS存储", value: "NFS" },
                { label: "FTP存储", value: "FTP" },
              ]}
            />
          </ProForm.Item>
          <Form.Item noStyle shouldUpdate>
            {({ getFieldValue }) =>
              renderStorageConfigFields(getFieldValue("storageType"))
            }
          </Form.Item>
          <ProForm.Item
            name="isDefault"
            label="设为默认"
            valuePropName="checked"
          >
            <Switch />
          </ProForm.Item>
        </DrawerForm>
      </Tabs.TabPane>

      <Tabs.TabPane tab="告警配置" key="alert">
        <Card>
          <Alert
            message="告警通知配置"
            description="配置备份任务执行结果的告警通知方式，当备份成功或失败时发送通知。"
            type="info"
            showIcon
            style={{ marginBottom: 24 }}
          />
          <Form form={alertForm} layout="vertical">
            <Form.Item name="notifyOnSuccess" label="备份成功通知" valuePropName="checked">
              <Switch checkedChildren="开启" unCheckedChildren="关闭" />
            </Form.Item>
            <Form.Item name="notifyOnFailure" label="备份失败通知" valuePropName="checked">
              <Switch checkedChildren="开启" unCheckedChildren="关闭" />
            </Form.Item>
            <Divider />
            <Form.Item name="notifyEmails" label="通知邮箱">
              <Select
                mode="tags"
                placeholder="输入邮箱地址后按回车添加"
                tokenSeparators={[","]}
              />
            </Form.Item>
            <Form.Item name="notifyWebhooks" label="Webhook通知">
              <Select
                mode="tags"
                placeholder="输入Webhook URL后按回车添加"
                tokenSeparators={[","]}
              />
            </Form.Item>
            <Form.Item>
              <Button type="primary" onClick={handleSaveAlertConfig} loading={alertLoading}>
                保存配置
              </Button>
            </Form.Item>
          </Form>
        </Card>
      </Tabs.TabPane>
    </Tabs>
  );
};

export default BackupConfigPage;