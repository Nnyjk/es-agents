import React, { useState, useRef } from "react";
import {
  ProTable,
  ModalForm,
  ProFormText,
  ProFormSelect,
  ProFormTextArea,
  ProFormSwitch,
} from "@ant-design/pro-components";
import {
  Button,
  Space,
  Tag,
  message,
  Popconfirm,
  Drawer,
  Descriptions,
  Card,
  Row,
  Col,
  Statistic,
  Progress,
  Divider,
  Badge,
} from "antd";
import type { ProColumns, ActionType } from "@ant-design/pro-components";
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  SettingOutlined,
  EyeOutlined,
  CloudServerOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  SyncOutlined,
} from "@ant-design/icons";
import {
  getEnvironments,
  createEnvironment,
  updateEnvironment,
  deleteEnvironment,
  getEnvironmentResources,
  getEnvironmentApplications,
} from "@/services/deployment";
import type {
  Environment,
  EnvironmentResource,
  EnvironmentApplication,
} from "@/types/deployment";

const typeColors: Record<string, string> = {
  dev: "blue",
  test: "cyan",
  staging: "orange",
  prod: "red",
};

const typeLabels: Record<string, string> = {
  dev: "开发环境",
  test: "测试环境",
  staging: "预发环境",
  prod: "生产环境",
};

const EnvironmentPage: React.FC = () => {
  const [modalVisible, setModalVisible] = useState(false);
  const [detailDrawerVisible, setDetailDrawerVisible] = useState(false);
  const [currentEnvironment, setCurrentEnvironment] =
    useState<Environment | null>(null);
  const [resources, setResources] = useState<EnvironmentResource | null>(null);
  const [applications, setApplications] = useState<
    EnvironmentApplication[]
  >([]);
  const actionRef = useRef<ActionType>();

  const handleAdd = () => {
    setCurrentEnvironment(null);
    setModalVisible(true);
  };

  const handleEdit = (record: Environment) => {
    setCurrentEnvironment(record);
    setModalVisible(true);
  };

  const handleDetail = async (record: Environment) => {
    setCurrentEnvironment(record);
    const [resourceData, appData] = await Promise.all([
      getEnvironmentResources(record.id),
      getEnvironmentApplications(record.id),
    ]);
    setResources(resourceData);
    setApplications(appData);
    setDetailDrawerVisible(true);
  };

  const handleDelete = async (id: string) => {
    await deleteEnvironment(id);
    message.success("删除成功");
    actionRef.current?.reload();
  };

  const handleSubmit = async (values: Record<string, unknown>) => {
    if (currentEnvironment) {
      await updateEnvironment(
        currentEnvironment.id,
        values as Parameters<typeof updateEnvironment>[1],
      );
      message.success("更新成功");
    } else {
      await createEnvironment(
        values as Parameters<typeof createEnvironment>[0],
      );
      message.success("创建成功");
    }
    setModalVisible(false);
    actionRef.current?.reload();
    return true;
  };

  const renderHealthStatus = (status: string) => {
    const statusConfig: Record<
      string,
      { color: string; icon: React.ReactNode }
    > = {
      healthy: { color: "green", icon: <CheckCircleOutlined /> },
      unhealthy: { color: "red", icon: <CloseCircleOutlined /> },
      unknown: { color: "default", icon: <SyncOutlined /> },
    };
    const config = statusConfig[status] || statusConfig.unknown;
    return (
      <Tag color={config.color} icon={config.icon}>
        {status === "healthy"
          ? "健康"
          : status === "unhealthy"
            ? "异常"
            : "未知"}
      </Tag>
    );
  };

  const columns: ProColumns<Environment>[] = [
    {
      title: "环境名称",
      dataIndex: "name",
      width: 150,
      render: (_, record) => (
        <a onClick={() => handleDetail(record)}>
          <Badge status={record.status === "active" ? "success" : "default"} />
          {record.name}
        </a>
      ),
    },
    {
      title: "环境类型",
      dataIndex: "type",
      width: 120,
      valueType: "select",
      valueEnum: {
        dev: { text: "开发环境", status: "Processing" },
        test: { text: "测试环境", status: "Default" },
        staging: { text: "预发环境", status: "Warning" },
        prod: { text: "生产环境", status: "Success" },
      },
      render: (_, record) => (
        <Tag color={typeColors[record.type]}>{typeLabels[record.type]}</Tag>
      ),
    },
    {
      title: "集群地址",
      dataIndex: "clusterEndpoint",
      width: 200,
      search: false,
      ellipsis: true,
    },
    {
      title: "应用数",
      dataIndex: "applicationCount",
      width: 80,
      search: false,
      align: "center",
    },
    {
      title: "资源使用率",
      dataIndex: "resourceUsage",
      width: 150,
      search: false,
      render: (_, record) => {
        const usage = record.resourceUsage || { cpu: 0, memory: 0 };
        return (
          <Space direction="vertical" size={0} style={{ width: "100%" }}>
            <div style={{ display: "flex", alignItems: "center", gap: 4 }}>
              <span style={{ width: 30 }}>CPU:</span>
              <Progress
                percent={usage.cpu}
                size="small"
                status={usage.cpu > 80 ? "exception" : "normal"}
                style={{ flex: 1 }}
              />
            </div>
            <div style={{ display: "flex", alignItems: "center", gap: 4 }}>
              <span style={{ width: 30 }}>Mem:</span>
              <Progress
                percent={usage.memory}
                size="small"
                status={usage.memory > 80 ? "exception" : "normal"}
                style={{ flex: 1 }}
              />
            </div>
          </Space>
        );
      },
    },
    {
      title: "状态",
      dataIndex: "status",
      width: 100,
      valueType: "select",
      valueEnum: {
        active: { text: "活跃", status: "Success" },
        inactive: { text: "未激活", status: "Default" },
        maintenance: { text: "维护中", status: "Warning" },
      },
      render: (_, record) => (
        <Tag
          color={
            record.status === "active"
              ? "green"
              : record.status === "maintenance"
                ? "orange"
                : "default"
          }
        >
          {record.status === "active"
            ? "活跃"
            : record.status === "maintenance"
              ? "维护中"
              : "未激活"}
        </Tag>
      ),
    },
    {
      title: "健康状态",
      dataIndex: "healthStatus",
      width: 100,
      search: false,
      render: (_, record) =>
        renderHealthStatus(record.healthStatus || "unknown"),
    },
    {
      title: "操作",
      key: "action",
      width: 180,
      search: false,
      render: (_, record) => (
        <Space size="small">
          <Button
            type="link"
            size="small"
            icon={<EyeOutlined />}
            onClick={() => handleDetail(record)}
          >
            详情
          </Button>
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Button
            type="link"
            size="small"
            icon={<SettingOutlined />}
            onClick={() => message.info("配置功能开发中")}
          >
            配置
          </Button>
          <Popconfirm
            title="确定要删除此环境吗？"
            onConfirm={() => handleDelete(record.id)}
          >
            <Button type="link" size="small" danger icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <>
      <ProTable<Environment>
        columns={columns}
        actionRef={actionRef}
        request={async (params) => {
          const result = await getEnvironments({
            current: params.current || 1,
            pageSize: params.pageSize || 10,
            name: params.name,
            type: params.type as Environment["type"],
            status: params.status as Environment["status"],
          });
          return {
            data: result.list,
            total: result.total,
            success: true,
          };
        }}
        rowKey="id"
        search={{
          labelWidth: "auto",
        }}
        toolBarRender={() => [
          <Button
            key="add"
            type="primary"
            icon={<PlusOutlined />}
            onClick={handleAdd}
          >
            新建环境
          </Button>,
        ]}
        pagination={{
          defaultPageSize: 10,
          showSizeChanger: true,
        }}
      />

      <ModalForm
        title={currentEnvironment ? "编辑环境" : "新建环境"}
        open={modalVisible}
        onFinish={handleSubmit}
        onOpenChange={setModalVisible}
        initialValues={
          currentEnvironment || { status: "active", autoDeploy: false }
        }
        modalProps={{
          destroyOnClose: true,
          width: 600,
        }}
      >
        <ProFormText
          name="name"
          label="环境名称"
          rules={[{ required: true, message: "请输入环境名称" }]}
          placeholder="请输入环境名称"
        />
        <ProFormSelect
          name="type"
          label="环境类型"
          options={[
            { label: "开发环境", value: "dev" },
            { label: "测试环境", value: "test" },
            { label: "预发环境", value: "staging" },
            { label: "生产环境", value: "prod" },
          ]}
          rules={[{ required: true, message: "请选择环境类型" }]}
          placeholder="请选择环境类型"
        />
        <ProFormText
          name="clusterEndpoint"
          label="集群地址"
          rules={[{ required: true, message: "请输入集群地址" }]}
          placeholder="https://k8s-api.example.com"
        />
        <ProFormText
          name="namespace"
          label="命名空间"
          placeholder="default"
          initialValue="default"
        />
        <ProFormTextArea
          name="description"
          label="描述"
          placeholder="请输入环境描述"
          fieldProps={{ rows: 3 }}
        />
        <ProFormSelect
          name="status"
          label="状态"
          options={[
            { label: "活跃", value: "active" },
            { label: "未激活", value: "inactive" },
            { label: "维护中", value: "maintenance" },
          ]}
        />
        <ProFormSwitch name="autoDeploy" label="自动部署" />
      </ModalForm>

      <Drawer
        title="环境详情"
        open={detailDrawerVisible}
        onClose={() => setDetailDrawerVisible(false)}
        width={800}
      >
        {currentEnvironment && (
          <>
            <Descriptions column={2} bordered size="small">
              <Descriptions.Item label="环境名称">
                <Badge
                  status={
                    currentEnvironment.status === "active"
                      ? "success"
                      : "default"
                  }
                />
                {currentEnvironment.name}
              </Descriptions.Item>
              <Descriptions.Item label="环境类型">
                <Tag color={typeColors[currentEnvironment.type]}>
                  {typeLabels[currentEnvironment.type]}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="集群地址" span={2}>
                {currentEnvironment.clusterEndpoint}
              </Descriptions.Item>
              <Descriptions.Item label="命名空间">
                {currentEnvironment.namespace}
              </Descriptions.Item>
              <Descriptions.Item label="状态">
                <Tag
                  color={
                    currentEnvironment.status === "active"
                      ? "green"
                      : currentEnvironment.status === "maintenance"
                        ? "orange"
                        : "default"
                  }
                >
                  {currentEnvironment.status === "active"
                    ? "活跃"
                    : currentEnvironment.status === "maintenance"
                      ? "维护中"
                      : "未激活"}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="健康状态">
                {renderHealthStatus(
                  currentEnvironment.healthStatus || "unknown",
                )}
              </Descriptions.Item>
              <Descriptions.Item label="自动部署">
                {currentEnvironment.autoDeploy ? "是" : "否"}
              </Descriptions.Item>
              <Descriptions.Item label="描述" span={2}>
                {currentEnvironment.description || "-"}
              </Descriptions.Item>
              <Descriptions.Item label="创建时间">
                {new Date(currentEnvironment.createdAt).toLocaleString()}
              </Descriptions.Item>
              <Descriptions.Item label="更新时间">
                {new Date(currentEnvironment.updatedAt).toLocaleString()}
              </Descriptions.Item>
            </Descriptions>

            <Divider orientation="left">资源概览</Divider>
            {resources && (
              <Row gutter={16}>
                <Col span={6}>
                  <Card size="small">
                    <Statistic
                      title="节点数"
                      value={resources.nodes.total}
                      suffix={`/ ${resources.nodes.healthy} 健康`}
                      prefix={<CloudServerOutlined />}
                    />
                  </Card>
                </Col>
                <Col span={6}>
                  <Card size="small">
                    <Statistic
                      title="CPU 使用率"
                      value={resources.cpu.used}
                      suffix={`/ ${resources.cpu.total} Core`}
                      valueStyle={{
                        color:
                          (resources.cpu.used / resources.cpu.total) * 100 > 80
                            ? "#cf1322"
                            : "#3f8600",
                      }}
                    />
                  </Card>
                </Col>
                <Col span={6}>
                  <Card size="small">
                    <Statistic
                      title="内存使用率"
                      value={resources.memory.used}
                      suffix={`/ ${resources.memory.total} GB`}
                      valueStyle={{
                        color:
                          (resources.memory.used / resources.memory.total) *
                            100 >
                          80
                            ? "#cf1322"
                            : "#3f8600",
                      }}
                    />
                  </Card>
                </Col>
                <Col span={6}>
                  <Card size="small">
                    <Statistic
                      title="存储使用率"
                      value={resources.storage.used}
                      suffix={`/ ${resources.storage.total} GB`}
                      valueStyle={{
                        color:
                          (resources.storage.used / resources.storage.total) *
                            100 >
                          80
                            ? "#cf1322"
                            : "#3f8600",
                      }}
                    />
                  </Card>
                </Col>
              </Row>
            )}

            <Divider orientation="left">应用列表</Divider>
            <ProTable<EnvironmentApplication>
              columns={[
                {
                  title: "应用名称",
                  dataIndex: "applicationName",
                  width: 150,
                },
                {
                  title: "当前版本",
                  dataIndex: "version",
                  width: 120,
                },
                {
                  title: "状态",
                  dataIndex: "status",
                  width: 100,
                  render: (_, record) => (
                    <Tag
                      color={
                        record.status === "running"
                          ? "green"
                          : record.status === "stopped"
                            ? "default"
                            : record.status === "deploying"
                              ? "blue"
                              : "red"
                      }
                    >
                      {record.status}
                    </Tag>
                  ),
                },
                {
                  title: "实例数",
                  dataIndex: "replicas",
                  width: 100,
                  render: (_, record) =>
                    `${record.readyReplicas}/${record.replicas}`,
                },
                {
                  title: "健康状态",
                  dataIndex: "healthStatus",
                  width: 100,
                  render: (_, record) =>
                    renderHealthStatus(record.healthStatus),
                },
                {
                  title: "最后更新",
                  dataIndex: "updatedAt",
                  width: 160,
                  render: (_, record) =>
                    new Date(record.updatedAt).toLocaleString(),
                },
              ]}
              search={false}
              dataSource={applications}
              rowKey="applicationId"
              pagination={false}
              size="small"
              toolBarRender={false}
            />
          </>
        )}
      </Drawer>
    </>
  );
};

export default EnvironmentPage;
