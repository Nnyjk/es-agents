import React, { useRef, useState } from "react";
import {
  HistoryOutlined,
  EyeOutlined,
  DeleteOutlined,
  ClockCircleOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  ExclamationCircleOutlined,
} from "@ant-design/icons";
import type { ActionType, ProColumns } from "@ant-design/pro-components";
import { ProTable } from "@ant-design/pro-components";
import {
  Button,
  message,
  Popconfirm,
  Tag,
  Space,
  Modal,
  Descriptions,
  Typography,
  DatePicker,
  Select,
  Tooltip,
} from "antd";
import {
  listNotificationHistory,
  getNotificationHistory,
  deleteNotificationHistory,
} from "../../../services/notification";
import type {
  NotificationHistory,
  NotificationStatus,
  NotificationHistoryQuery,
} from "../NotificationChannels/types";
import { NotificationStatusLabels } from "./types";
import dayjs from "dayjs";

const { Text } = Typography;
const { RangePicker } = DatePicker;

const statusColors: Record<NotificationStatus, string> = {
  PENDING: "warning",
  SENT: "success",
  FAILED: "error",
};

const statusIcons: Record<NotificationStatus, React.ReactNode> = {
  PENDING: <ClockCircleOutlined />,
  SENT: <CheckCircleOutlined />,
  FAILED: <CloseCircleOutlined />,
};

const NotificationHistoryList: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [detailVisible, setDetailVisible] = useState(false);
  const [selectedItem, setSelectedItem] = useState<NotificationHistory | null>(
    null
  );
  const [detailLoading, setDetailLoading] = useState(false);

  // Filter state
  const [filters, setFilters] = useState<NotificationHistoryQuery>({});

  const handleDelete = async (id: string) => {
    try {
      await deleteNotificationHistory(id);
      message.success("删除成功");
      actionRef.current?.reload();
    } catch (error: any) {
      message.error(error.message || "删除失败");
    }
  };

  const handleViewDetail = async (id: string) => {
    setDetailLoading(true);
    setDetailVisible(true);
    try {
      const detail = await getNotificationHistory(id);
      setSelectedItem(detail);
    } catch (error: any) {
      message.error(error.message || "获取详情失败");
      setSelectedItem(null);
    } finally {
      setDetailLoading(false);
    }
  };

  const columns: ProColumns<NotificationHistory>[] = [
    {
      title: "接收人",
      dataIndex: "recipient",
      key: "recipient",
      width: 150,
      render: (text) => (
        <Space>
          <HistoryOutlined />
          <Text>{text}</Text>
        </Space>
      ),
    },
    {
      title: "通知标题",
      dataIndex: "title",
      key: "title",
      ellipsis: true,
      width: 200,
      render: (title) => (title ? <Text ellipsis>{title}</Text> : "-"),
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 100,
      render: (status) => (
        <Tag
          icon={statusIcons[status as NotificationStatus]}
          color={statusColors[status as NotificationStatus] || "default"}
        >
          {NotificationStatusLabels[status as NotificationStatus] || status}
        </Tag>
      ),
    },
    {
      title: "发送时间",
      dataIndex: "sentAt",
      key: "sentAt",
      valueType: "dateTime",
      width: 170,
      render: (time) =>
        time ? dayjs(time as string).format("YYYY-MM-DD HH:mm:ss") : "-",
    },
    {
      title: "重试次数",
      dataIndex: "retryCount",
      key: "retryCount",
      width: 80,
      render: (count) =>
        count && Number(count) > 0 ? (
          <Tag color="orange">
            <ExclamationCircleOutlined /> {count}
          </Tag>
        ) : (
          "-"
        ),
    },
    {
      title: "错误信息",
      dataIndex: "errorMessage",
      key: "errorMessage",
      ellipsis: true,
      width: 150,
      render: (msg) =>
        msg ? (
          <Tooltip title={msg}>
            <Text type="danger" ellipsis>
              {msg}
            </Text>
          </Tooltip>
        ) : (
          "-"
        ),
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
      width: 150,
      render: (_, record) => [
        <Button
          key="view"
          type="link"
          size="small"
          icon={<EyeOutlined />}
          onClick={() => handleViewDetail(record.id)}
        >
          详情
        </Button>,
        <Popconfirm
          key="delete"
          title="确定删除该通知记录吗？"
          onConfirm={() => handleDelete(record.id)}
        >
          <Button type="link" size="small" danger icon={<DeleteOutlined />}>
            删除
          </Button>
        </Popconfirm>,
      ],
    },
  ];

  return (
    <>
      <ProTable<NotificationHistory>
        actionRef={actionRef}
        rowKey="id"
        columns={columns}
        request={async (_params) => {
          const query: NotificationHistoryQuery = {
            ...filters,
            limit: 100,
          };
          const res = await listNotificationHistory(query);
          return {
            data: res || [],
            total: res?.length || 0,
            success: true,
          };
        }}
        toolBarRender={() => [
          <RangePicker
            key="timeRange"
            placeholder={["开始时间", "结束时间"] as [string, string]}
            onChange={(dates) => {
              if (dates && dates[0] && dates[1]) {
                setFilters({
                  ...filters,
                  startTime: dates[0].toISOString(),
                  endTime: dates[1].toISOString(),
                });
              } else {
                setFilters({
                  ...filters,
                  startTime: undefined,
                  endTime: undefined,
                });
              }
              actionRef.current?.reload();
            }}
          />,
          <Select
            key="statusFilter"
            placeholder="状态筛选"
            allowClear
            style={{ width: 120 }}
            onChange={(value) => {
              setFilters({ ...filters, status: value });
              actionRef.current?.reload();
            }}
          >
            <Select.Option value="PENDING">待发送</Select.Option>
            <Select.Option value="SENT">已发送</Select.Option>
            <Select.Option value="FAILED">发送失败</Select.Option>
          </Select>,
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

      <Modal
        title={
          <Space>
            <EyeOutlined />
            <span>通知详情</span>
          </Space>
        }
        open={detailVisible}
        onCancel={() => setDetailVisible(false)}
        footer={null}
        width={700}
        loading={detailLoading}
      >
        {selectedItem && (
          <Descriptions column={2} bordered size="small">
            <Descriptions.Item label="接收人">
              {selectedItem.recipient}
            </Descriptions.Item>
            <Descriptions.Item label="状态">
              <Tag
                icon={statusIcons[selectedItem.status]}
                color={statusColors[selectedItem.status]}
              >
                {NotificationStatusLabels[selectedItem.status]}
              </Tag>
            </Descriptions.Item>
            <Descriptions.Item label="通知标题" span={2}>
              {selectedItem.title || "-"}
            </Descriptions.Item>
            <Descriptions.Item label="通知内容" span={2}>
              <div style={{ whiteSpace: "pre-wrap" }}>
                {selectedItem.content || "-"}
              </div>
            </Descriptions.Item>
            <Descriptions.Item label="渠道ID">
              {selectedItem.channelId}
            </Descriptions.Item>
            <Descriptions.Item label="模板ID">
              {selectedItem.templateId || "-"}
            </Descriptions.Item>
            <Descriptions.Item label="发送时间">
              {selectedItem.sentAt
                ? dayjs(selectedItem.sentAt).format("YYYY-MM-DD HH:mm:ss")
                : "-"}
            </Descriptions.Item>
            <Descriptions.Item label="创建时间">
              {selectedItem.createdAt
                ? dayjs(selectedItem.createdAt).format("YYYY-MM-DD HH:mm:ss")
                : "-"}
            </Descriptions.Item>
            <Descriptions.Item label="重试次数">
              {selectedItem.retryCount || 0}
            </Descriptions.Item>
            {selectedItem.errorMessage && (
              <Descriptions.Item label="错误信息" span={2}>
                <Text type="danger">{selectedItem.errorMessage}</Text>
              </Descriptions.Item>
            )}
          </Descriptions>
        )}
      </Modal>
    </>
  );
};

export default NotificationHistoryList;