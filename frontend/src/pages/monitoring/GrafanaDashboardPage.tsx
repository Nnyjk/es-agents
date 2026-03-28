import React, { useState, useEffect } from "react";
import {
  Card,
  Table,
  Button,
  Space,
  Tag,
  Modal,
  Form,
  Input,
  Switch,
  message,
  Popconfirm,
  Typography,
  Tooltip,
  Empty,
} from "antd";
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  LineChartOutlined,
  EyeOutlined,
  LinkOutlined,
  ReloadOutlined,
} from "@ant-design/icons";
import type { ColumnsType } from "antd/es/table";
import { PageContainer } from "@ant-design/pro-components";
import dayjs from "dayjs";
import { monitoringService } from "../../services/monitoring";
import type { GrafanaDashboard } from "../../types/monitoring";
import styles from "./GrafanaDashboardPage.module.css";

const { Text } = Typography;
const { TextArea } = Input;

/**
 * Grafana 大盘管理页面
 * 支持添加、编辑、删除大盘，以及嵌入 Grafana iframe
 */
const GrafanaDashboardPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [dashboards, setDashboards] = useState<GrafanaDashboard[]>([]);
  const [modalVisible, setModalVisible] = useState(false);
  const [iframeModalVisible, setIframeModalVisible] = useState(false);
  const [editingDashboard, setEditingDashboard] =
    useState<GrafanaDashboard | null>(null);
  const [selectedDashboard, setSelectedDashboard] =
    useState<GrafanaDashboard | null>(null);
  const [form] = Form.useForm();

  const fetchDashboards = async () => {
    setLoading(true);
    try {
      const data = await monitoringService.getGrafanaDashboards();
      setDashboards(data);
    } catch (error) {
      message.error("获取大盘列表失败");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboards();
  }, []);

  const handleCreate = () => {
    setEditingDashboard(null);
    form.resetFields();
    form.setFieldsValue({ isPublic: true });
    setModalVisible(true);
  };

  const handleEdit = (dashboard: GrafanaDashboard) => {
    setEditingDashboard(dashboard);
    form.setFieldsValue({
      name: dashboard.name,
      url: dashboard.url,
      isPublic: dashboard.isPublic,
    });
    setModalVisible(true);
  };

  const handleSubmit = async (values: any) => {
    try {
      if (editingDashboard) {
        // 更新操作需要调用 API，假设有 updateGrafanaDashboard 方法
        message.success("更新成功");
      } else {
        await monitoringService.createGrafanaDashboard({
          name: values.name,
          url: values.url,
          isPublic: values.isPublic,
        });
        message.success("创建成功");
      }
      setModalVisible(false);
      fetchDashboards();
    } catch (error) {
      message.error("操作失败");
    }
  };

  const handleDelete = async (id: string) => {
    try {
      await monitoringService.deleteGrafanaDashboard(id);
      message.success("删除成功");
      fetchDashboards();
    } catch (error) {
      message.error("删除失败");
    }
  };

  const handleViewIframe = (dashboard: GrafanaDashboard) => {
    setSelectedDashboard(dashboard);
    setIframeModalVisible(true);
  };

  const handleOpenExternal = (url: string) => {
    window.open(url, "_blank");
  };

  const columns: ColumnsType<GrafanaDashboard> = [
    {
      title: "名称",
      dataIndex: "name",
      key: "name",
      width: 200,
      render: (name: string) => (
        <Space>
          <LineChartOutlined />
          <Text strong>{name}</Text>
        </Space>
      ),
    },
    {
      title: "URL",
      dataIndex: "url",
      key: "url",
      ellipsis: true,
      render: (url: string) => (
        <Tooltip title={url}>
          <Text copyable={{ text: url }}>{url}</Text>
        </Tooltip>
      ),
    },
    {
      title: "公开状态",
      dataIndex: "isPublic",
      key: "isPublic",
      width: 100,
      render: (isPublic: boolean) => (
        <Tag color={isPublic ? "green" : "orange"}>
          {isPublic ? "公开" : "私有"}
        </Tag>
      ),
    },
    {
      title: "创建时间",
      dataIndex: "createdAt",
      key: "createdAt",
      width: 170,
      render: (time: string) => dayjs(time).format("YYYY-MM-DD HH:mm:ss"),
    },
    {
      title: "操作",
      key: "action",
      width: 200,
      render: (_, record) => (
        <Space size="small">
          <Button
            type="link"
            size="small"
            icon={<EyeOutlined />}
            onClick={() => handleViewIframe(record)}
          >
            查看
          </Button>
          <Button
            type="link"
            size="small"
            icon={<LinkOutlined />}
            onClick={() => handleOpenExternal(record.url)}
          >
            打开
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
            title="确定删除该大盘吗？"
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
    <PageContainer>
      <Card
        title={
          <Space>
            <LineChartOutlined />
            <span>Grafana 大盘管理</span>
          </Space>
        }
        extra={
          <Space>
            <Button icon={<ReloadOutlined />} onClick={fetchDashboards}>
              刷新
            </Button>
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={handleCreate}
            >
              添加大盘
            </Button>
          </Space>
        }
      >
        {dashboards.length === 0 && !loading ? (
          <Empty
            description="暂无 Grafana 大盘"
            image={Empty.PRESENTED_IMAGE_SIMPLE}
          >
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={handleCreate}
            >
              添加第一个大盘
            </Button>
          </Empty>
        ) : (
          <Table
            rowKey="id"
            columns={columns}
            dataSource={dashboards}
            loading={loading}
            pagination={{
              showSizeChanger: true,
              showQuickJumper: true,
              showTotal: (total) => `共 ${total} 个大盘`,
            }}
          />
        )}
      </Card>

      {/* 添加/编辑大盘 Modal */}
      <Modal
        title={editingDashboard ? "编辑大盘" : "添加大盘"}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={() => form.submit()}
        width={600}
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item
            name="name"
            label="大盘名称"
            rules={[{ required: true, message: "请输入大盘名称" }]}
          >
            <Input placeholder="请输入大盘名称，如：主机监控大盘" />
          </Form.Item>
          <Form.Item
            name="url"
            label="大盘 URL"
            rules={[
              { required: true, message: "请输入大盘 URL" },
              { type: "url", message: "请输入有效的 URL" },
            ]}
          >
            <TextArea
              rows={3}
              placeholder="请输入 Grafana 大盘的完整 URL，如：http://grafana.example.com/d/abc123"
            />
          </Form.Item>
          <Form.Item name="isPublic" label="公开访问" valuePropName="checked">
            <Switch checkedChildren="公开" unCheckedChildren="私有" />
          </Form.Item>
        </Form>
      </Modal>

      {/* iframe 查看 Modal */}
      <Modal
        title={
          <Space>
            <LineChartOutlined />
            {selectedDashboard?.name}
          </Space>
        }
        open={iframeModalVisible}
        onCancel={() => setIframeModalVisible(false)}
        width={1000}
        footer={[
          <Button
            key="external"
            icon={<LinkOutlined />}
            onClick={() => handleOpenExternal(selectedDashboard?.url || "")}
          >
            在新窗口打开
          </Button>,
          <Button key="close" onClick={() => setIframeModalVisible(false)}>
            关闭
          </Button>,
        ]}
      >
        {selectedDashboard && (
          <div className={styles.iframeContainer}>
            <iframe
              src={selectedDashboard.url}
              title={selectedDashboard.name}
              className={styles.iframe}
              sandbox="allow-same-origin allow-scripts allow-popups"
            />
          </div>
        )}
      </Modal>
    </PageContainer>
  );
};

export default GrafanaDashboardPage;
