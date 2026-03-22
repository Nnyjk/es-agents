import React, { useRef, useState, useEffect } from "react";
import {
  Card,
  Space,
  Button,
  Tag,
  Modal,
  Form,
  Input,
  Select,
  message,
  Popconfirm,
  DatePicker,
  InputNumber,
  Descriptions,
  Tabs,
  Table,
  Timeline,
  Upload,
  Dropdown,
  Tooltip,
} from "antd";
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  ExportOutlined,
  ImportOutlined,
  DownloadOutlined,
  EyeOutlined,
  HistoryOutlined,
  ThunderboltOutlined,
  ApartmentOutlined,
  TagOutlined,
} from "@ant-design/icons";
import type { ActionType, ProColumns } from "@ant-design/pro-components";
import { ProTable } from "@ant-design/pro-components";
import {
  queryResources,
  getResource,
  createResource,
  updateResource,
  deleteResource,
  batchDeleteResources,
  batchUpdateStatus,
  importResources,
  exportResources,
  downloadImportTemplate,
  getResourceRelations,
  getResourceChangeHistory,
  getResourceTypes,
  getEnvironments,
  getDepartments,
} from "@/services/cmdb";
import type {
  Resource,
  ResourceType,
  ResourceStatus,
  ResourceRelation,
  ResourceChangeHistory,
  Environment,
  Department,
} from "@/types/cmdb";

const { Dragger } = Upload;

const resourceTypeOptions = [
  { value: "server", label: "服务器", color: "blue" },
  { value: "network_device", label: "网络设备", color: "cyan" },
  { value: "database", label: "数据库", color: "orange" },
  { value: "middleware", label: "中间件", color: "purple" },
  { value: "application", label: "应用服务", color: "green" },
  { value: "storage", label: "存储设备", color: "magenta" },
  { value: "other", label: "其他", color: "default" },
];

const statusOptions = [
  { value: "planning", label: "规划中", color: "default" },
  { value: "purchasing", label: "采购中", color: "processing" },
  { value: "online", label: "运行中", color: "success" },
  { value: "maintaining", label: "维护中", color: "warning" },
  { value: "offline", label: "已下线", color: "error" },
  { value: "scrapped", label: "已报废", color: "default" },
];

const ResourceListPage: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [detailVisible, setDetailVisible] = useState(false);
  const [formVisible, setFormVisible] = useState(false);
  const [importVisible, setImportVisible] = useState(false);
  const [currentResource, setCurrentResource] = useState<Resource | null>(null);
  const [environments, setEnvironments] = useState<Environment[]>([]);
  const [departments, setDepartments] = useState<Department[]>([]);
  const [relations, setRelations] = useState<ResourceRelation[]>([]);
  const [histories, setHistories] = useState<ResourceChangeHistory[]>([]);
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const [form] = Form.useForm();
  const [importLoading, setImportLoading] = useState(false);

  useEffect(() => {
    loadBaseData();
  }, []);

  const loadBaseData = async () => {
    try {
      const [, envs, depts] = await Promise.all([
        getResourceTypes(),
        getEnvironments(),
        getDepartments(),
      ]);
      setEnvironments(envs);
      setDepartments(depts);
    } catch {
      // Ignore base data loading errors
    }
  };

  const handleCreate = () => {
    setCurrentResource(null);
    form.resetFields();
    setFormVisible(true);
  };

  const handleEdit = async (record: Resource) => {
    try {
      const detail = await getResource(record.id);
      setCurrentResource(detail);
      form.setFieldsValue(detail);
      setFormVisible(true);
    } catch {
      message.error("获取资源详情失败");
    }
  };

  const handleDelete = async (id: string) => {
    try {
      await deleteResource(id);
      message.success("删除成功");
      actionRef.current?.reload();
    } catch {
      message.error("删除失败");
    }
  };

  const handleBatchDelete = async () => {
    if (selectedRowKeys.length === 0) {
      message.warning("请选择要删除的资源");
      return;
    }
    try {
      await batchDeleteResources(selectedRowKeys as string[]);
      message.success("批量删除成功");
      setSelectedRowKeys([]);
      actionRef.current?.reload();
    } catch {
      message.error("批量删除失败");
    }
  };

  const handleBatchUpdateStatus = async (status: ResourceStatus) => {
    if (selectedRowKeys.length === 0) {
      message.warning("请选择要更新的资源");
      return;
    }
    try {
      await batchUpdateStatus(selectedRowKeys as string[], status);
      message.success("批量更新状态成功");
      setSelectedRowKeys([]);
      actionRef.current?.reload();
    } catch {
      message.error("批量更新状态失败");
    }
  };

  const handleSubmit = async (values: Partial<Resource>) => {
    try {
      if (currentResource) {
        await updateResource(currentResource.id, values);
        message.success("更新成功");
      } else {
        await createResource(
          values as Omit<
            Resource,
            "id" | "createdAt" | "updatedAt" | "createdBy" | "updatedBy"
          >,
        );
        message.success("创建成功");
      }
      setFormVisible(false);
      actionRef.current?.reload();
    } catch {
      message.error("操作失败");
    }
  };

  const handleViewDetail = async (record: Resource) => {
    try {
      const [detail, rels, hists] = await Promise.all([
        getResource(record.id),
        getResourceRelations(record.id),
        getResourceChangeHistory(record.id),
      ]);
      setCurrentResource(detail);
      setRelations(rels);
      setHistories(hists);
      setDetailVisible(true);
    } catch {
      message.error("获取资源详情失败");
    }
  };

  const handleExport = async () => {
    try {
      const blob = await exportResources({});
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `resources_${new Date().toISOString().slice(0, 10)}.xlsx`;
      a.click();
      window.URL.revokeObjectURL(url);
      message.success("导出成功");
    } catch {
      message.error("导出失败");
    }
  };

  const handleDownloadTemplate = async () => {
    try {
      const blob = await downloadImportTemplate();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = "resource_import_template.xlsx";
      a.click();
      window.URL.revokeObjectURL(url);
    } catch {
      message.error("下载模板失败");
    }
  };

  const handleImport = async (file: File) => {
    setImportLoading(true);
    try {
      const result = await importResources(file);
      if (result.failed > 0) {
        Modal.warning({
          title: "导入完成",
          content: (
            <div>
              <p>成功: {result.success} 条</p>
              <p>失败: {result.failed} 条</p>
              <div>
                错误信息:
                <ul>
                  {result.errors.slice(0, 5).map((e, i) => (
                    <li key={i}>
                      行 {e.row}: {e.message}
                    </li>
                  ))}
                </ul>
              </div>
            </div>
          ),
        });
      } else {
        message.success(`导入成功 ${result.success} 条`);
      }
      setImportVisible(false);
      actionRef.current?.reload();
    } catch {
      message.error("导入失败");
    } finally {
      setImportLoading(false);
    }
  };

  const columns: ProColumns<Resource>[] = [
    {
      title: "资源名称",
      dataIndex: "name",
      key: "name",
      width: 180,
      ellipsis: true,
      render: (_, record) => (
        <Button
          type="link"
          size="small"
          onClick={() => handleViewDetail(record)}
        >
          {record.name}
        </Button>
      ),
    },
    {
      title: "资源类型",
      dataIndex: "type",
      key: "type",
      width: 100,
      valueType: "select",
      valueEnum: Object.fromEntries(
        resourceTypeOptions.map((o) => [o.value, { text: o.label }]),
      ),
      render: (_, record) => {
        const option = resourceTypeOptions.find((o) => o.value === record.type);
        return <Tag color={option?.color}>{option?.label}</Tag>;
      },
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 100,
      valueType: "select",
      valueEnum: Object.fromEntries(
        statusOptions.map((o) => [o.value, { text: o.label }]),
      ),
      render: (_, record) => {
        const option = statusOptions.find((o) => o.value === record.status);
        return <Tag color={option?.color}>{option?.label}</Tag>;
      },
    },
    {
      title: "环境",
      dataIndex: "environment",
      key: "environment",
      width: 80,
      hideInSearch: true,
    },
    {
      title: "部门",
      dataIndex: "department",
      key: "department",
      width: 120,
      hideInSearch: true,
    },
    {
      title: "负责人",
      dataIndex: "owner",
      key: "owner",
      width: 100,
    },
    {
      title: "IP地址",
      dataIndex: "ip",
      key: "ip",
      width: 140,
      hideInSearch: true,
    },
    {
      title: "品牌/型号",
      dataIndex: "model",
      key: "model",
      width: 150,
      hideInSearch: true,
      render: (_, record) =>
        record.vendor || record.model
          ? `${record.vendor || ""} ${record.model || ""}`.trim()
          : "-",
    },
    {
      title: "创建时间",
      dataIndex: "createdAt",
      key: "createdAt",
      width: 180,
      valueType: "dateTime",
      hideInSearch: true,
    },
    {
      title: "操作",
      key: "action",
      width: 150,
      fixed: "right",
      render: (_, record) => (
        <Space>
          <Tooltip title="查看详情">
            <Button
              type="link"
              size="small"
              icon={<EyeOutlined />}
              onClick={() => handleViewDetail(record)}
            />
          </Tooltip>
          <Tooltip title="编辑">
            <Button
              type="link"
              size="small"
              icon={<EditOutlined />}
              onClick={() => handleEdit(record)}
            />
          </Tooltip>
          <Popconfirm
            title="确定要删除此资源吗？"
            onConfirm={() => handleDelete(record.id)}
          >
            <Tooltip title="删除">
              <Button
                type="link"
                size="small"
                danger
                icon={<DeleteOutlined />}
              />
            </Tooltip>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <Card
      title="资源台账管理"
      extra={
        <Space>
          <Dropdown
            menu={{
              items: [
                {
                  key: "online",
                  label: "运行中",
                  onClick: () =>
                    handleBatchUpdateStatus("online" as ResourceStatus),
                },
                {
                  key: "maintaining",
                  label: "维护中",
                  onClick: () =>
                    handleBatchUpdateStatus("maintaining" as ResourceStatus),
                },
                {
                  key: "offline",
                  label: "已下线",
                  onClick: () =>
                    handleBatchUpdateStatus("offline" as ResourceStatus),
                },
              ],
            }}
          >
            <Button icon={<ThunderboltOutlined />}>批量更新状态</Button>
          </Dropdown>
          <Popconfirm
            title="确定要删除选中的资源吗？"
            onConfirm={handleBatchDelete}
          >
            <Button danger icon={<DeleteOutlined />}>
              批量删除
            </Button>
          </Popconfirm>
          <Button
            icon={<ImportOutlined />}
            onClick={() => setImportVisible(true)}
          >
            导入
          </Button>
          <Button icon={<ExportOutlined />} onClick={handleExport}>
            导出
          </Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
            新建资源
          </Button>
        </Space>
      }
    >
      <ProTable<Resource>
        actionRef={actionRef}
        columns={columns}
        request={async (params) => {
          const result = await queryResources({
            current: params.current || 1,
            pageSize: params.pageSize || 10,
            name: params.name,
            type: params.type as ResourceType,
            status: params.status as ResourceStatus,
          });
          return {
            data: result.list,
            total: result.total,
            success: true,
          };
        }}
        rowKey="id"
        rowSelection={{
          selectedRowKeys,
          onChange: setSelectedRowKeys,
        }}
        scroll={{ x: 1400 }}
        pagination={{ pageSize: 20 }}
      />

      {/* 资源详情弹窗 */}
      <Modal
        title={`资源详情 - ${currentResource?.name || ""}`}
        open={detailVisible}
        onCancel={() => setDetailVisible(false)}
        footer={null}
        width={900}
      >
        <Tabs
          items={[
            {
              key: "basic",
              label: "基本信息",
              icon: <TagOutlined />,
              children: (
                <Descriptions bordered column={2}>
                  <Descriptions.Item label="资源名称">
                    {currentResource?.name}
                  </Descriptions.Item>
                  <Descriptions.Item label="资源类型">
                    {
                      resourceTypeOptions.find(
                        (o) => o.value === currentResource?.type,
                      )?.label
                    }
                  </Descriptions.Item>
                  <Descriptions.Item label="状态">
                    <Tag
                      color={
                        statusOptions.find(
                          (o) => o.value === currentResource?.status,
                        )?.color
                      }
                    >
                      {
                        statusOptions.find(
                          (o) => o.value === currentResource?.status,
                        )?.label
                      }
                    </Tag>
                  </Descriptions.Item>
                  <Descriptions.Item label="环境">
                    {currentResource?.environment}
                  </Descriptions.Item>
                  <Descriptions.Item label="部门">
                    {currentResource?.department}
                  </Descriptions.Item>
                  <Descriptions.Item label="负责人">
                    {currentResource?.owner}
                  </Descriptions.Item>
                  <Descriptions.Item label="IP地址">
                    {currentResource?.ip || "-"}
                  </Descriptions.Item>
                  <Descriptions.Item label="端口">
                    {currentResource?.port || "-"}
                  </Descriptions.Item>
                  <Descriptions.Item label="品牌">
                    {currentResource?.vendor || "-"}
                  </Descriptions.Item>
                  <Descriptions.Item label="型号">
                    {currentResource?.model || "-"}
                  </Descriptions.Item>
                  <Descriptions.Item label="序列号">
                    {currentResource?.serialNumber || "-"}
                  </Descriptions.Item>
                  <Descriptions.Item label="位置">
                    {currentResource?.location || "-"}
                  </Descriptions.Item>
                  <Descriptions.Item label="购买日期">
                    {currentResource?.purchaseDate || "-"}
                  </Descriptions.Item>
                  <Descriptions.Item label="保修到期">
                    {currentResource?.warrantyExpiry || "-"}
                  </Descriptions.Item>
                  <Descriptions.Item label="维保到期">
                    {currentResource?.maintenanceExpiry || "-"}
                  </Descriptions.Item>
                  <Descriptions.Item label="创建时间">
                    {currentResource?.createdAt
                      ? new Date(currentResource.createdAt).toLocaleString()
                      : "-"}
                  </Descriptions.Item>
                  <Descriptions.Item label="描述" span={2}>
                    {currentResource?.description || "-"}
                  </Descriptions.Item>
                </Descriptions>
              ),
            },
            {
              key: "relations",
              label: "关联关系",
              icon: <ApartmentOutlined />,
              children: (
                <Table<ResourceRelation>
                  dataSource={relations}
                  rowKey="id"
                  pagination={false}
                  size="small"
                  columns={[
                    {
                      title: "关联资源",
                      dataIndex: "targetName",
                      key: "targetName",
                    },
                    {
                      title: "关系类型",
                      dataIndex: "relationType",
                      key: "relationType",
                      render: (type) => {
                        const labels: Record<string, string> = {
                          depends_on: "依赖",
                          contains: "包含",
                          connects_to: "连接",
                          runs_on: "运行于",
                          manages: "管理",
                          backup_for: "备份",
                        };
                        return labels[type] || type;
                      },
                    },
                    {
                      title: "描述",
                      dataIndex: "description",
                      key: "description",
                    },
                    {
                      title: "创建时间",
                      dataIndex: "createdAt",
                      key: "createdAt",
                      render: (date) => new Date(date).toLocaleString(),
                    },
                  ]}
                />
              ),
            },
            {
              key: "history",
              label: "变更历史",
              icon: <HistoryOutlined />,
              children: (
                <Timeline
                  items={histories.map((h) => ({
                    children: (
                      <div>
                        <div>
                          <strong>{h.field}</strong>: {h.oldValue} →{" "}
                          {h.newValue}
                        </div>
                        <div style={{ color: "#999" }}>
                          {h.changedBy} 于{" "}
                          {new Date(h.changedAt).toLocaleString()}
                          {h.reason && ` - ${h.reason}`}
                        </div>
                      </div>
                    ),
                  }))}
                />
              ),
            },
          ]}
        />
      </Modal>

      {/* 资源编辑弹窗 */}
      <Modal
        title={currentResource ? "编辑资源" : "新建资源"}
        open={formVisible}
        onCancel={() => setFormVisible(false)}
        onOk={() => form.submit()}
        width={800}
      >
        <Form form={form} onFinish={handleSubmit} layout="vertical">
          <Space direction="vertical" style={{ width: "100%" }} size="middle">
            <Form.Item
              name="name"
              label="资源名称"
              rules={[{ required: true, message: "请输入资源名称" }]}
            >
              <Input placeholder="请输入资源名称" />
            </Form.Item>
            <Space style={{ width: "100%" }}>
              <Form.Item
                name="type"
                label="资源类型"
                rules={[{ required: true, message: "请选择资源类型" }]}
                style={{ width: 200 }}
              >
                <Select
                  placeholder="请选择资源类型"
                  options={resourceTypeOptions}
                />
              </Form.Item>
              <Form.Item
                name="status"
                label="状态"
                rules={[{ required: true, message: "请选择状态" }]}
                style={{ width: 200 }}
              >
                <Select placeholder="请选择状态" options={statusOptions} />
              </Form.Item>
              <Form.Item name="environment" label="环境" style={{ width: 200 }}>
                <Select
                  placeholder="请选择环境"
                  options={environments.map((e) => ({
                    label: e.name,
                    value: e.code,
                  }))}
                />
              </Form.Item>
            </Space>
            <Space style={{ width: "100%" }}>
              <Form.Item name="department" label="部门" style={{ width: 200 }}>
                <Select
                  placeholder="请选择部门"
                  options={departments.map((d) => ({
                    label: d.name,
                    value: d.code,
                  }))}
                />
              </Form.Item>
              <Form.Item
                name="owner"
                label="负责人"
                rules={[{ required: true, message: "请输入负责人" }]}
                style={{ width: 200 }}
              >
                <Input placeholder="请输入负责人" />
              </Form.Item>
              <Form.Item name="users" label="使用人" style={{ width: 200 }}>
                <Input placeholder="多人用逗号分隔" />
              </Form.Item>
            </Space>
            <Space style={{ width: "100%" }}>
              <Form.Item name="ip" label="IP地址" style={{ width: 200 }}>
                <Input placeholder="请输入IP地址" />
              </Form.Item>
              <Form.Item name="port" label="端口" style={{ width: 100 }}>
                <InputNumber min={1} max={65535} placeholder="端口" />
              </Form.Item>
              <Form.Item name="location" label="位置" style={{ width: 200 }}>
                <Input placeholder="请输入位置" />
              </Form.Item>
            </Space>
            <Space style={{ width: "100%" }}>
              <Form.Item name="vendor" label="品牌" style={{ width: 150 }}>
                <Input placeholder="请输入品牌" />
              </Form.Item>
              <Form.Item name="model" label="型号" style={{ width: 150 }}>
                <Input placeholder="请输入型号" />
              </Form.Item>
              <Form.Item
                name="serialNumber"
                label="序列号"
                style={{ width: 150 }}
              >
                <Input placeholder="请输入序列号" />
              </Form.Item>
            </Space>
            <Space style={{ width: "100%" }}>
              <Form.Item
                name="purchaseDate"
                label="购买日期"
                style={{ width: 150 }}
              >
                <DatePicker style={{ width: "100%" }} />
              </Form.Item>
              <Form.Item
                name="warrantyExpiry"
                label="保修到期"
                style={{ width: 150 }}
              >
                <DatePicker style={{ width: "100%" }} />
              </Form.Item>
              <Form.Item
                name="maintenanceExpiry"
                label="维保到期"
                style={{ width: 150 }}
              >
                <DatePicker style={{ width: "100%" }} />
              </Form.Item>
            </Space>
            <Form.Item name="description" label="描述">
              <Input.TextArea rows={3} placeholder="请输入描述" />
            </Form.Item>
          </Space>
        </Form>
      </Modal>

      {/* 导入弹窗 */}
      <Modal
        title="导入资源"
        open={importVisible}
        onCancel={() => setImportVisible(false)}
        footer={null}
        width={500}
      >
        <div style={{ marginBottom: 16 }}>
          <Button icon={<DownloadOutlined />} onClick={handleDownloadTemplate}>
            下载导入模板
          </Button>
        </div>
        <Dragger
          accept=".xlsx,.xls"
          beforeUpload={(file) => {
            handleImport(file);
            return false;
          }}
          showUploadList={false}
          disabled={importLoading}
        >
          <p className="ant-upload-drag-icon">
            <ImportOutlined />
          </p>
          <p className="ant-upload-text">点击或拖拽文件到此区域上传</p>
          <p className="ant-upload-hint">支持 .xlsx 或 .xls 格式</p>
        </Dragger>
      </Modal>
    </Card>
  );
};

export default ResourceListPage;
