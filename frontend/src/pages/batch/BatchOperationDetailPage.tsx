import React, { useState, useEffect } from "react";
import {
  Card,
  Descriptions,
  Tag,
  Table,
  Button,
  Space,
  Spin,
  message,
  Progress,
  Empty,
} from "antd";
import {
  CodeOutlined,
  CloudServerOutlined,
  UploadOutlined,
  ReloadOutlined,
} from "@ant-design/icons";
import { PageContainer } from "@ant-design/pro-components";
import dayjs from "dayjs";
import { useParams, useNavigate } from "react-router-dom";
import { getBatchOperation, getBatchOperationItems } from "@/services/batch";
import type {
  BatchOperation,
  BatchOperationItem,
  BatchOperationType,
  BatchOperationStatus,
  BatchOperationItemStatus,
} from "@/types/batch";
import styles from "./BatchOperationDetailPage.module.css";

const typeLabels: Record<BatchOperationType, string> = {
  BATCH_COMMAND: "批量命令执行",
  BATCH_DEPLOY: "批量部署",
  BATCH_UPGRADE: "批量升级",
};

const typeIcons: Record<BatchOperationType, React.ReactNode> = {
  BATCH_COMMAND: <CodeOutlined />,
  BATCH_DEPLOY: <CloudServerOutlined />,
  BATCH_UPGRADE: <UploadOutlined />,
};

const statusLabels: Record<BatchOperationStatus, string> = {
  PENDING: "待执行",
  RUNNING: "执行中",
  PARTIAL_SUCCESS: "部分成功",
  SUCCESS: "成功",
  FAILED: "失败",
};

const statusColors: Record<BatchOperationStatus, string> = {
  PENDING: "default",
  RUNNING: "processing",
  PARTIAL_SUCCESS: "warning",
  SUCCESS: "success",
  FAILED: "error",
};

const itemStatusLabels: Record<BatchOperationItemStatus, string> = {
  PENDING: "待执行",
  RUNNING: "执行中",
  SUCCESS: "成功",
  FAILED: "失败",
};

const itemStatusColors: Record<BatchOperationItemStatus, string> = {
  PENDING: "default",
  RUNNING: "processing",
  SUCCESS: "success",
  FAILED: "error",
};

const BatchOperationDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [operation, setOperation] = useState<BatchOperation | null>(null);
  const [items, setItems] = useState<BatchOperationItem[]>([]);
  const [polling, setPolling] = useState(false);

  const fetchData = async () => {
    if (!id) return;
    setLoading(true);
    try {
      const operationData = await getBatchOperation(id);
      setOperation(operationData);

      const itemsData = await getBatchOperationItems(id);
      setItems(itemsData);

      // Start polling if operation is running
      if (operationData.status === "PENDING" || operationData.status === "RUNNING") {
        setPolling(true);
      }
    } catch (error) {
      console.error("Fetch batch operation failed", error);
      message.error("获取批量操作详情失败");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [id]);

  // Polling for running operations
  useEffect(() => {
    if (!polling || !id) return;

    const interval = setInterval(async () => {
      try {
        const operationData = await getBatchOperation(id);
        setOperation(operationData);

        const itemsData = await getBatchOperationItems(id);
        setItems(itemsData);

        if (operationData.status !== "PENDING" && operationData.status !== "RUNNING") {
          setPolling(false);
          clearInterval(interval);
        }
      } catch (error) {
        console.error("Poll operation status failed", error);
      }
    }, 2000);

    return () => clearInterval(interval);
  }, [polling, id]);

  const handleBack = () => {
    navigate("/batch");
  };

  const handleRefresh = () => {
    fetchData();
  };

  const getProgressPercent = () => {
    if (!operation) return 0;
    const completed = operation.successCount + operation.failedCount;
    return Math.round((completed / operation.totalItems) * 100);
  };

  const columns = [
    {
      title: "目标ID",
      dataIndex: "targetId",
      key: "targetId",
      width: 200,
      ellipsis: true,
    },
    {
      title: "目标类型",
      dataIndex: "targetType",
      key: "targetType",
      width: 100,
      render: (type: string) => type === "HOST" ? "主机" : "Agent",
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 100,
      render: (status: BatchOperationItemStatus) => (
        <Tag color={itemStatusColors[status]}>
          {itemStatusLabels[status]}
        </Tag>
      ),
    },
    {
      title: "开始时间",
      dataIndex: "startedAt",
      key: "startedAt",
      width: 180,
      render: (time: string) => time ? dayjs(time).format("YYYY-MM-DD HH:mm:ss") : "-",
    },
    {
      title: "完成时间",
      dataIndex: "completedAt",
      key: "completedAt",
      width: 180,
      render: (time: string) => time ? dayjs(time).format("YYYY-MM-DD HH:mm:ss") : "-",
    },
    {
      title: "错误信息",
      dataIndex: "errorMessage",
      key: "errorMessage",
      ellipsis: true,
      render: (msg: string) => msg || "-",
    },
  ];

  if (loading) {
    return (
      <PageContainer>
        <div className={styles.loadingContainer}>
          <Spin size="large" />
        </div>
      </PageContainer>
    );
  }

  if (!operation) {
    return (
      <PageContainer>
        <Card>
          <Empty description="批量操作不存在" />
          <div className={styles.emptyAction}>
            <Button type="primary" onClick={handleBack}>
              返回列表
            </Button>
          </div>
        </Card>
      </PageContainer>
    );
  }

  return (
    <PageContainer
      header={{
        title: typeLabels[operation.operationType],
        subTitle: `批量操作详情`,
        onBack: handleBack,
      }}
      extra={[
        <Button
          key="refresh"
          icon={<ReloadOutlined />}
          onClick={handleRefresh}
          loading={loading}
        >
          刷新
        </Button>,
        polling && <Spin key="polling-indicator" />,
      ]}
    >
      <Card className={styles.infoCard}>
        <Descriptions bordered column={3}>
          <Descriptions.Item label="操作类型">
            <Space>
              {typeIcons[operation.operationType]}
              {typeLabels[operation.operationType]}
            </Space>
          </Descriptions.Item>
          <Descriptions.Item label="状态">
            <Tag color={statusColors[operation.status]}>
              {statusLabels[operation.status]}
            </Tag>
          </Descriptions.Item>
          <Descriptions.Item label="操作ID">
            {operation.id}
          </Descriptions.Item>
          <Descriptions.Item label="目标总数">
            {operation.totalItems} 个
          </Descriptions.Item>
          <Descriptions.Item label="成功数量">
            <span style={{ color: "#52c41a" }}>{operation.successCount}</span>
          </Descriptions.Item>
          <Descriptions.Item label="失败数量">
            <span style={{ color: "#ff4d4f" }}>{operation.failedCount}</span>
          </Descriptions.Item>
          <Descriptions.Item label="创建时间">
            {dayjs(operation.createdAt).format("YYYY-MM-DD HH:mm:ss")}
          </Descriptions.Item>
          <Descriptions.Item label="完成时间">
            {operation.completedAt
              ? dayjs(operation.completedAt).format("YYYY-MM-DD HH:mm:ss")
              : "-"}
          </Descriptions.Item>
          <Descriptions.Item label="执行进度">
            {operation.status === "PENDING" || operation.status === "RUNNING"
              ? "执行中..."
              : "已完成"}
          </Descriptions.Item>
        </Descriptions>

        {(operation.status === "PENDING" || operation.status === "RUNNING") && (
          <div className={styles.progressSection}>
            <Progress
              percent={getProgressPercent()}
              status="active"
              strokeColor={{
                "0%": "#108ee9",
                "100%": "#87d068",
              }}
            />
          </div>
        )}
      </Card>

      <Card className={styles.itemsCard} title="执行项列表">
        <Table
          dataSource={items}
          columns={columns}
          rowKey="id"
          pagination={{
            pageSize: 10,
            showSizeChanger: true,
            showTotal: (total) => `共 ${total} 条`,
          }}
          scroll={{ x: 900 }}
        />
      </Card>
    </PageContainer>
  );
};

export default BatchOperationDetailPage;