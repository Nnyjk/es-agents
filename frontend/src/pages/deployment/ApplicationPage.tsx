import React, { useState } from "react";
import {
  ProTable,
  ModalForm,
  ProFormText,
  ProFormSelect,
  ProFormTextArea,
} from "@ant-design/pro-components";
import {
  Button,
  Space,
  Tag,
  message,
  Popconfirm,
  Descriptions,
  Drawer,
  Form,
  Tooltip,
} from "antd";
import type { ProColumns } from "@ant-design/pro-components";
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  InboxOutlined,
  SettingOutlined,
  EyeOutlined,
} from "@ant-design/icons";
import {
  getApplications,
  createApplication,
  updateApplication,
  deleteApplication,
  archiveApplication,
  getApplicationEnvironments,
} from "@/services/deployment";
import type {
  Application,
  ApplicationStatus,
  ApplicationEnvironment,
} from "@/types/deployment";

const statusColors: Record<ApplicationStatus, string> = {
  active: "green",
  inactive: "orange",
  archived: "default",
};

const statusLabels: Record<ApplicationStatus, string> = {
  active: "活跃",
  inactive: "未激活",
  archived: "已归档",
};

const ApplicationPage: React.FC = () => {
  const [modalVisible, setModalVisible] = useState(false);
  const [configModalVisible, setConfigModalVisible] = useState(false);
  const [detailDrawerVisible, setDetailDrawerVisible] = useState(false);
  const [currentApplication, setCurrentApplication] =
    useState<Application | null>(null);
  const [environments, setEnvironments] = useState<ApplicationEnvironment[]>(
    [],
  );
  const [configForm] = Form.useForm();

  const handleAdd = () => {
    setCurrentApplication(null);
    setModalVisible(true);
  };

  const handleEdit = (record: Application) => {
    setCurrentApplication(record);
    setModalVisible(true);
  };

  const handleConfig = async (record: Application) => {
    setCurrentApplication(record);
    configForm.setFieldsValue(record.config);
    const envs = await getApplicationEnvironments(record.id);
    setEnvironments(envs);
    setConfigModalVisible(true);
  };

  const handleDetail = async (record: Application) => {
    setCurrentApplication(record);
    const envs = await getApplicationEnvironments(record.id);
    setEnvironments(envs);
    setDetailDrawerVisible(true);
  };

  const handleDelete = async (id: string) => {
    await deleteApplication(id);
    message.success("删除成功");
  };

  const handleArchive = async (id: string) => {
    await archiveApplication(id);
    message.success("归档成功");
  };

  const handleSubmit = async (values: Record<string, unknown>) => {
    const techStack = values.techStack as string[];
    const data = {
      ...values,
      techStack: techStack || [],
      config: {
        repositoryUrl: "",
        branch: "main",
        buildScript: "",
        deployPath: "",
        healthCheckUrl: "",
        buildCommand: "",
        startCommand: "",
        stopCommand: "",
      },
    };

    if (currentApplication) {
      await updateApplication(currentApplication.id, data);
      message.success("更新成功");
    } else {
      await createApplication(data as Parameters<typeof createApplication>[0]);
      message.success("创建成功");
    }
    setModalVisible(false);
    return true;
  };

  const handleConfigSubmit = async (values: Record<string, unknown>) => {
    if (currentApplication) {
      await updateApplication(currentApplication.id, { config: values as Application["config"] });
      message.success("配置更新成功");
    }
    setConfigModalVisible(false);
    return true;
  };

  const columns: ProColumns<Application>[] = [
    {
      title: "应用名称",
      dataIndex: "name",
      width: 150,
      render: (_, record) => (
        <a onClick={() => handleDetail(record)}>{record.name}</a>
      ),
    },
    {
      title: "所属项目",
      dataIndex: "project",
      width: 120,
      search: false,
    },
    {
      title: "负责人",
      dataIndex: "owner",
      width: 100,
    },
    {
      title: "技术栈",
      dataIndex: "techStack",
      width: 200,
      search: false,
      render: (_, record) => (
        <Space size="small" wrap>
          {record.techStack?.map((tech) => (
            <Tag key={tech} color="blue">
              {tech}
            </Tag>
          ))}
        </Space>
      ),
    },
    {
      title: "当前版本",
      dataIndex: "currentVersion",
      width: 120,
      search: false,
      render: (_, record) => record.currentVersion || "-",
    },
    {
      title: "状态",
      dataIndex: "status",
      width: 100,
      valueType: "select",
      valueEnum: {
        active: { text: "活跃", status: "Success" },
        inactive: { text: "未激活", status: "Warning" },
        archived: { text: "已归档", status: "Default" },
      },
      render: (_, record) => (
        <Tag color={statusColors[record.status]}>
          {statusLabels[record.status]}
        </Tag>
      ),
    },
    {
      title: "环境状态",
      dataIndex: "environments",
      width: 200,
      search: false,
      render: (_, record) => (
        <Space size="small">
          {record.environments?.slice(0, 3).map((env) => (
            <Tooltip
              key={env.environmentId}
              title={`${env.environmentName}: ${env.version}`}
            >
              <Tag
                color={
                  env.healthStatus === "healthy"
                    ? "green"
                    : env.healthStatus === "unhealthy"
                      ? "red"
                      : "default"
                }
              >
                {env.environmentName.slice(0, 3).toUpperCase()}
              </Tag>
            </Tooltip>
          ))}
        </Space>
      ),
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
            icon={<SettingOutlined />}
            onClick={() => handleConfig(record)}
          >
            配置
          </Button>
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确定要归档此应用吗？"
            onConfirm={() => handleArchive(record.id)}
          >
            <Button type="link" size="small" icon={<InboxOutlined />}>
              归档
            </Button>
          </Popconfirm>
          <Popconfirm
            title="确定要删除此应用吗？"
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
      <ProTable<Application>
        columns={columns}
        request={async (params) => {
          const result = await getApplications({
            current: params.current || 1,
            pageSize: params.pageSize || 10,
            name: params.name,
            owner: params.owner,
            status: params.status as ApplicationStatus,
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
            新建应用
          </Button>,
        ]}
        pagination={{
          defaultPageSize: 10,
          showSizeChanger: true,
        }}
      />

      <ModalForm
        title={currentApplication ? "编辑应用" : "新建应用"}
        open={modalVisible}
        onFinish={handleSubmit}
        onOpenChange={setModalVisible}
        initialValues={currentApplication || {}}
        modalProps={{
          destroyOnClose: true,
        }}
      >
        <ProFormText
          name="name"
          label="应用名称"
          rules={[{ required: true, message: "请输入应用名称" }]}
          placeholder="请输入应用名称"
        />
        <ProFormText
          name="project"
          label="所属项目"
          rules={[{ required: true, message: "请输入所属项目" }]}
          placeholder="请输入所属项目"
        />
        <ProFormText
          name="owner"
          label="负责人"
          rules={[{ required: true, message: "请输入负责人" }]}
          placeholder="请输入负责人"
        />
        <ProFormSelect
          name="techStack"
          label="技术栈"
          mode="multiple"
          placeholder="请选择技术栈"
          options={[
            { label: "Java", value: "Java" },
            { label: "Python", value: "Python" },
            { label: "Node.js", value: "Node.js" },
            { label: "Go", value: "Go" },
            { label: "React", value: "React" },
            { label: "Vue", value: "Vue" },
            { label: "Spring Boot", value: "Spring Boot" },
            { label: "Django", value: "Django" },
            { label: "Flask", value: "Flask" },
            { label: "Express", value: "Express" },
            { label: "Gin", value: "Gin" },
          ]}
        />
        <ProFormSelect
          name="status"
          label="状态"
          options={[
            { label: "活跃", value: "active" },
            { label: "未激活", value: "inactive" },
          ]}
          initialValue="active"
        />
      </ModalForm>

      <ModalForm
        title="应用配置"
        open={configModalVisible}
        onFinish={handleConfigSubmit}
        onOpenChange={setConfigModalVisible}
        form={configForm}
        modalProps={{
          destroyOnClose: true,
          width: 600,
        }}
      >
        <ProFormText
          name="repositoryUrl"
          label="代码仓库地址"
          placeholder="git@github.com:org/repo.git"
        />
        <ProFormText
          name="branch"
          label="分支"
          placeholder="main"
          initialValue="main"
        />
        <ProFormTextArea
          name="buildScript"
          label="构建脚本"
          placeholder="构建脚本命令"
          fieldProps={{ rows: 3 }}
        />
        <ProFormText
          name="buildCommand"
          label="构建命令"
          placeholder="mvn clean package"
        />
        <ProFormText
          name="deployPath"
          label="部署路径"
          placeholder="/opt/app"
        />
        <ProFormText
          name="startCommand"
          label="启动命令"
          placeholder="java -jar app.jar"
        />
        <ProFormText
          name="stopCommand"
          label="停止命令"
          placeholder="kill $PID"
        />
        <ProFormText
          name="healthCheckUrl"
          label="健康检查地址"
          placeholder="http://localhost:8080/health"
        />
      </ModalForm>

      <Drawer
        title="应用详情"
        open={detailDrawerVisible}
        onClose={() => setDetailDrawerVisible(false)}
        width={600}
      >
        {currentApplication && (
          <>
            <Descriptions column={2} bordered size="small">
              <Descriptions.Item label="应用名称" span={2}>
                {currentApplication.name}
              </Descriptions.Item>
              <Descriptions.Item label="所属项目">
                {currentApplication.project}
              </Descriptions.Item>
              <Descriptions.Item label="负责人">
                {currentApplication.owner}
              </Descriptions.Item>
              <Descriptions.Item label="当前版本">
                {currentApplication.currentVersion || "-"}
              </Descriptions.Item>
              <Descriptions.Item label="状态">
                <Tag color={statusColors[currentApplication.status]}>
                  {statusLabels[currentApplication.status]}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="技术栈" span={2}>
                <Space size="small" wrap>
                  {currentApplication.techStack?.map((tech) => (
                    <Tag key={tech} color="blue">
                      {tech}
                    </Tag>
                  ))}
                </Space>
              </Descriptions.Item>
            </Descriptions>

            <h4 style={{ marginTop: 16, marginBottom: 8 }}>环境状态</h4>
            <Descriptions column={1} bordered size="small">
              {environments.map((env) => (
                <Descriptions.Item
                  key={env.environmentId}
                  label={env.environmentName}
                >
                  <Space>
                    <span>版本: {env.version || "-"}</span>
                    <Tag
                      color={
                        env.status === "running"
                          ? "green"
                          : env.status === "stopped"
                            ? "default"
                            : env.status === "deploying"
                              ? "blue"
                              : "red"
                      }
                    >
                      {env.status}
                    </Tag>
                    <Tag
                      color={
                        env.healthStatus === "healthy"
                          ? "green"
                          : env.healthStatus === "unhealthy"
                            ? "red"
                            : "default"
                      }
                    >
                      {env.healthStatus}
                    </Tag>
                  </Space>
                </Descriptions.Item>
              ))}
            </Descriptions>
          </>
        )}
      </Drawer>
    </>
  );
};

export default ApplicationPage;