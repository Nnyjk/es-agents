import React, { useState, useEffect } from "react";
import {
  Card,
  Table,
  Button,
  Space,
  Modal,
  Form,
  Input,
  Select,
  message,
  Popconfirm,
  Tag,
} from "antd";
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  SettingOutlined,
  AppstoreOutlined,
} from "@ant-design/icons";
import type { ColumnsType } from "antd/es/table";
import {
  getResourceTypes,
  createResourceType,
  updateResourceType,
  deleteResourceType,
  addCustomField,
  deleteCustomField,
} from "@/services/cmdb";
import type {
  ResourceTypeConfig,
  CustomFieldDefinition,
  ResourceType,
} from "@/types/cmdb";

const resourceTypeOptions = [
  { value: "server", label: "服务器", color: "blue" },
  { value: "network_device", label: "网络设备", color: "cyan" },
  { value: "database", label: "数据库", color: "orange" },
  { value: "middleware", label: "中间件", color: "purple" },
  { value: "application", label: "应用服务", color: "green" },
  { value: "storage", label: "存储设备", color: "magenta" },
  { value: "other", label: "其他", color: "default" },
];

const fieldTypeOptions = [
  { value: "text", label: "文本" },
  { value: "number", label: "数字" },
  { value: "select", label: "下拉选择" },
  { value: "date", label: "日期" },
  { value: "textarea", label: "多行文本" },
  { value: "boolean", label: "布尔值" },
];

const ResourceTypePage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState<ResourceTypeConfig[]>([]);
  const [typeModalVisible, setTypeModalVisible] = useState(false);
  const [fieldModalVisible, setFieldModalVisible] = useState(false);
  const [currentType, setCurrentType] = useState<ResourceTypeConfig | null>(
    null,
  );
  const [currentFields, setCurrentFields] = useState<CustomFieldDefinition[]>(
    [],
  );
  const [typeForm] = Form.useForm();
  const [fieldForm] = Form.useForm();

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    try {
      const result = await getResourceTypes();
      setData(result);
    } catch {
      message.error("加载资源类型失败");
    } finally {
      setLoading(false);
    }
  };

  const handleCreateType = () => {
    setCurrentType(null);
    typeForm.resetFields();
    setTypeModalVisible(true);
  };

  const handleEditType = (record: ResourceTypeConfig) => {
    setCurrentType(record);
    typeForm.setFieldsValue(record);
    setTypeModalVisible(true);
  };

  const handleDeleteType = async (id: string) => {
    try {
      await deleteResourceType(id);
      message.success("删除成功");
      loadData();
    } catch {
      message.error("删除失败");
    }
  };

  const handleTypeSubmit = async (values: Partial<ResourceTypeConfig>) => {
    try {
      if (currentType) {
        await updateResourceType(currentType.id, values);
        message.success("更新成功");
      } else {
        await createResourceType(
          values as Omit<ResourceTypeConfig, "id" | "createdAt" | "updatedAt">,
        );
        message.success("创建成功");
      }
      setTypeModalVisible(false);
      loadData();
    } catch {
      message.error("操作失败");
    }
  };

  const handleManageFields = (record: ResourceTypeConfig) => {
    setCurrentType(record);
    setCurrentFields(record.customFields || []);
    setFieldModalVisible(true);
  };

  const handleAddField = () => {
    fieldForm.resetFields();
    fieldForm.setFieldsValue({ required: false });
    setCurrentFields([
      ...currentFields,
      {
        id: `temp-${Date.now()}`,
        name: "",
        label: "",
        fieldType: "text",
        required: false,
      },
    ]);
  };

  const handleSaveFields = async () => {
    if (!currentType) return;

    try {
      // 获取新字段（临时 ID 的字段）
      const newFields = currentFields.filter((f) => f.id.startsWith("temp-"));

      // 保存新字段
      for (const field of newFields) {
        if (!field.name || !field.label) {
          message.error("请填写完整的字段信息");
          return;
        }
        await addCustomField(currentType.id, {
          name: field.name,
          label: field.label,
          fieldType: field.fieldType,
          required: field.required,
          options: field.options,
          defaultValue: field.defaultValue,
        });
      }

      message.success("字段配置已保存");
      setFieldModalVisible(false);
      loadData();
    } catch {
      message.error("保存失败");
    }
  };

  const handleDeleteField = async (fieldId: string) => {
    if (!currentType) return;

    if (fieldId.startsWith("temp-")) {
      setCurrentFields(currentFields.filter((f) => f.id !== fieldId));
      return;
    }

    try {
      await deleteCustomField(currentType.id, fieldId);
      message.success("删除字段成功");
      setCurrentFields(currentFields.filter((f) => f.id !== fieldId));
      loadData();
    } catch {
      message.error("删除字段失败");
    }
  };

  const columns: ColumnsType<ResourceTypeConfig> = [
    {
      title: "类型名称",
      dataIndex: "name",
      key: "name",
      width: 150,
    },
    {
      title: "类型代码",
      dataIndex: "code",
      key: "code",
      width: 150,
      render: (code: ResourceType) => {
        const option = resourceTypeOptions.find((o) => o.value === code);
        return (
          <Tag color={option?.color || "default"}>{option?.label || code}</Tag>
        );
      },
    },
    {
      title: "描述",
      dataIndex: "description",
      key: "description",
      ellipsis: true,
    },
    {
      title: "自定义字段数",
      dataIndex: "customFields",
      key: "customFieldsCount",
      width: 120,
      render: (fields: CustomFieldDefinition[]) => fields?.length || 0,
    },
    {
      title: "创建时间",
      dataIndex: "createdAt",
      key: "createdAt",
      width: 180,
      render: (date: string) => new Date(date).toLocaleString(),
    },
    {
      title: "操作",
      key: "action",
      width: 200,
      render: (_, record) => (
        <Space>
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={() => handleEditType(record)}
          >
            编辑
          </Button>
          <Button
            type="link"
            size="small"
            icon={<SettingOutlined />}
            onClick={() => handleManageFields(record)}
          >
            字段配置
          </Button>
          <Popconfirm
            title="确定要删除此资源类型吗？"
            onConfirm={() => handleDeleteType(record.id)}
          >
            <Button type="link" size="small" danger icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  const fieldColumns: ColumnsType<CustomFieldDefinition> = [
    {
      title: "字段名称",
      dataIndex: "name",
      key: "name",
      width: 120,
    },
    {
      title: "显示标签",
      dataIndex: "label",
      key: "label",
      width: 120,
    },
    {
      title: "字段类型",
      dataIndex: "fieldType",
      key: "fieldType",
      width: 100,
      render: (type) => {
        const option = fieldTypeOptions.find((o) => o.value === type);
        return option?.label || type;
      },
    },
    {
      title: "必填",
      dataIndex: "required",
      key: "required",
      width: 80,
      render: (required: boolean) => (
        <Tag color={required ? "red" : "default"}>{required ? "是" : "否"}</Tag>
      ),
    },
    {
      title: "操作",
      key: "action",
      width: 80,
      render: (_, record) => (
        <Popconfirm
          title="确定要删除此字段吗？"
          onConfirm={() => handleDeleteField(record.id)}
        >
          <Button type="link" size="small" danger icon={<DeleteOutlined />}>
            删除
          </Button>
        </Popconfirm>
      ),
    },
  ];

  return (
    <Card
      title={
        <Space>
          <AppstoreOutlined />
          资源类型管理
        </Space>
      }
      extra={
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={handleCreateType}
        >
          新建类型
        </Button>
      }
    >
      <Table
        columns={columns}
        dataSource={data}
        rowKey="id"
        loading={loading}
        pagination={{ pageSize: 10 }}
      />

      {/* 资源类型编辑弹窗 */}
      <Modal
        title={currentType ? "编辑资源类型" : "新建资源类型"}
        open={typeModalVisible}
        onCancel={() => setTypeModalVisible(false)}
        onOk={() => typeForm.submit()}
        width={500}
      >
        <Form form={typeForm} onFinish={handleTypeSubmit} layout="vertical">
          <Form.Item
            name="name"
            label="类型名称"
            rules={[{ required: true, message: "请输入类型名称" }]}
          >
            <Input placeholder="请输入类型名称" />
          </Form.Item>
          <Form.Item
            name="code"
            label="类型代码"
            rules={[{ required: true, message: "请选择类型代码" }]}
          >
            <Select
              placeholder="请选择类型代码"
              options={resourceTypeOptions}
              disabled={!!currentType}
            />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea rows={3} placeholder="请输入描述" />
          </Form.Item>
        </Form>
      </Modal>

      {/* 自定义字段配置弹窗 */}
      <Modal
        title={`字段配置 - ${currentType?.name || ""}`}
        open={fieldModalVisible}
        onCancel={() => setFieldModalVisible(false)}
        onOk={handleSaveFields}
        width={800}
      >
        <div style={{ marginBottom: 16 }}>
          <Button
            type="dashed"
            icon={<PlusOutlined />}
            onClick={handleAddField}
          >
            添加字段
          </Button>
        </div>
        <Table
          columns={fieldColumns}
          dataSource={currentFields}
          rowKey="id"
          pagination={false}
          size="small"
        />
      </Modal>
    </Card>
  );
};

export default ResourceTypePage;
