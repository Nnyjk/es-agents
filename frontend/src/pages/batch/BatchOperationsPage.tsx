import React, { useState, useEffect } from "react";
import {
  Tag,
  Space,
  Button,
  message,
  Tooltip,
} from "antd";
import {
  PlusOutlined,
  EyeOutlined,
  CodeOutlined,
  CloudServerOutlined,
  UploadOutlined,
} from "@ant-design/icons";
import { PageContainer, ProTable } from "@ant-design/pro-components";
import type { ProColumns } from "@ant-design/pro-components";
import dayjs from "dayjs";
import { useNavigate } from "react-router-dom";
import { listBatchOperations } from "@/services/batch";
import { BatchOperationModal } from "@/components/batch";
import type {
  BatchOperation,
  BatchOperationType,
  BatchOperationStatus,
} from "@/types/batch";

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

const BatchOperationsPage: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState<BatchOperation[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [filters, setFilters] = useState<{
    operationType?: BatchOperationType;
    status?: BatchOperationStatus;
  }>({});
  const [modalOpen, setModalOpen] = useState(false);

  const fetchData = async () => {
    setLoading(true);
    try {
      const result = await listBatchOperations({
        page,
        size,
        ...filters,
      });
      setData(result.data || []);
      setTotal(result.total || 0);
    } catch (error) {
      console.error("Fetch batch operations failed", error);
      message.error("获取批量操作列表失败");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [page, size, filters]);

  const handleViewDetail = (id: string) => {
    navigate(`/batch/${id}`);
  };

  const handleOperationSuccess = () => {
    message.success("批量操作已创建");
    fetchData();
  };

  const columns: ProColumns<BatchOperation>[] = [
    {
      title: "ID",
      dataIndex: "id",
      width: 120,
      ellipsis: true,
      render: (_, record) => (
        <Tooltip title={record.id}>
          <span style={{ cursor: "pointer", color: "#1890ff" }}>
            {record.id.slice(0, 8)}...
          </span>
        </Tooltip>
      ),
    },
    {
      title: "操作类型",
      dataIndex: "operationType",
      width: 150,
      render: (_, record) => (
        <Space>
          {typeIcons[record.operationType]}
          <span>{typeLabels[record.operationType]}</span>
        </Space>
      ),
      valueType: "select",
      valueEnum: {
        BATCH_COMMAND: { text: "批量命令执行" },
        BATCH_DEPLOY: { text: "批量部署" },
        BATCH_UPGRADE: { text: "批量升级" },
      },
    },
    {
      title: "状态",
      dataIndex: "status",
      width: 120,
      render: (_, record) => (
        <Tag color={statusColors[record.status]}>
          {statusLabels[record.status]}
        </Tag>
      ),
      valueType: "select",
      valueEnum: {
        PENDING: { text: "待执行", status: "Default" },
        RUNNING: { text: "执行中", status: "Processing" },
        PARTIAL_SUCCESS: { text: "部分成功", status: "Warning" },
        SUCCESS: { text: "成功", status: "Success" },
        FAILED: { text: "失败", status: "Error" },
      },
    },
    {
      title: "目标数量",
      dataIndex: "totalItems",
      width: 100,
      render: (_, record) => (
        <span>
          {record.totalItems} 个
        </span>
      ),
    },
    {
      title: "成功/失败",
      width: 120,
      render: (_, record) => (
        <Space size="small">
          <span style={{ color: "#52c41a" }}>{record.successCount}</span>
          <span>/</span>
          <span style={{ color: "#ff4d4f" }}>{record.failedCount}</span>
        </Space>
      ),
    },
    {
      title: "创建时间",
      dataIndex: "createdAt",
      width: 180,
      render: (_, record) => dayjs(record.createdAt).format("YYYY-MM-DD HH:mm:ss"),
      sorter: true,
    },
    {
      title: "完成时间",
      dataIndex: "completedAt",
      width: 180,
      render: (_, record) =>
        record.completedAt
          ? dayjs(record.completedAt).format("YYYY-MM-DD HH:mm:ss")
          : "-",
    },
    {
      title: "操作",
      width: 100,
      fixed: "right",
      render: (_, record) => (
        <Button
          type="link"
          icon={<EyeOutlined />}
          onClick={() => handleViewDetail(record.id)}
        >
          详情
        </Button>
      ),
    },
  ];

  return (
    <PageContainer>
      <ProTable<BatchOperation>
        columns={columns}
        dataSource={data}
        loading={loading}
        rowKey="id"
        search={{
          labelWidth: "auto",
          defaultCollapsed: false,
          optionRender: (_, __, dom) => [
            ...dom.reverse(),
            <Button
              key="create"
              type="primary"
              icon={<PlusOutlined />}
              onClick={() => setModalOpen(true)}
            >
              新建批量操作
            </Button>,
          ],
        }}
        toolBarRender={false}
        pagination={{
          current: page + 1,
          pageSize: size,
          total,
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total) => `共 ${total} 条记录`,
          onChange: (p, s) => {
            setPage(p - 1);
            setSize(s);
          },
        }}
        onChange={(_, tableFilters) => {
          const newFilters: { operationType?: BatchOperationType; status?: BatchOperationStatus } = {};
          if (tableFilters.operationType) {
            const values = tableFilters.operationType as string[];
            if (values.length > 0) {
              newFilters.operationType = values[0] as BatchOperationType;
            }
          }
          if (tableFilters.status) {
            const values = tableFilters.status as string[];
            if (values.length > 0) {
              newFilters.status = values[0] as BatchOperationStatus;
            }
          }
          setFilters(newFilters);
        }}
        scroll={{ x: 1200 }}
      />

      <BatchOperationModal
        open={modalOpen}
        onCancel={() => setModalOpen(false)}
        onSuccess={handleOperationSuccess}
      />
    </PageContainer>
  );
};

export default BatchOperationsPage;