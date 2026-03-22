import React, { useState, useEffect } from "react";
import {
  Card,
  Table,
  Tag,
  Button,
  Space,
  message,
  Modal,
  Descriptions,
  Badge,
} from "antd";
import {
  BellOutlined,
  EyeOutlined,
  ClockCircleOutlined,
  WarningOutlined,
} from "@ant-design/icons";
import type { ColumnsType } from "antd/es/table";
import {
  getExpiryReminders,
  updateReminderNotified,
  getResource,
} from "@/services/cmdb";
import type { ExpiryReminder, Resource } from "@/types/cmdb";

const reminderTypeLabels: Record<string, { label: string; color: string }> = {
  warranty: { label: "保修到期", color: "orange" },
  maintenance: { label: "维保到期", color: "purple" },
  certificate: { label: "证书到期", color: "blue" },
};

const ExpiryReminderPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState<ExpiryReminder[]>([]);
  const [detailVisible, setDetailVisible] = useState(false);
  const [currentResource, setCurrentResource] = useState<Resource | null>(null);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    try {
      const result = await getExpiryReminders();
      setData(result);
    } catch {
      message.error("加载到期提醒失败");
    } finally {
      setLoading(false);
    }
  };

  const handleViewResource = async (resourceId: string) => {
    try {
      const resource = await getResource(resourceId);
      setCurrentResource(resource);
      setDetailVisible(true);
    } catch {
      message.error("获取资源详情失败");
    }
  };

  const handleMarkNotified = async (id: string, notified: boolean) => {
    try {
      await updateReminderNotified(id, notified);
      message.success(notified ? "已标记为已通知" : "已标记为未通知");
      loadData();
    } catch {
      message.error("操作失败");
    }
  };

  const getDaysRemainingColor = (days: number) => {
    if (days <= 7) return "#ff4d4f";
    if (days <= 30) return "#faad14";
    if (days <= 60) return "#1890ff";
    return "#52c41a";
  };

  const columns: ColumnsType<ExpiryReminder> = [
    {
      title: "资源名称",
      dataIndex: "resourceName",
      key: "resourceName",
      width: 200,
      render: (name, record) => (
        <Button
          type="link"
          size="small"
          onClick={() => handleViewResource(record.resourceId)}
        >
          {name}
        </Button>
      ),
    },
    {
      title: "提醒类型",
      dataIndex: "type",
      key: "type",
      width: 120,
      render: (type) => {
        const config = reminderTypeLabels[type] || {
          label: type,
          color: "default",
        };
        return <Tag color={config.color}>{config.label}</Tag>;
      },
    },
    {
      title: "到期日期",
      dataIndex: "expiryDate",
      key: "expiryDate",
      width: 150,
      render: (date) => new Date(date).toLocaleDateString(),
    },
    {
      title: "剩余天数",
      dataIndex: "daysRemaining",
      key: "daysRemaining",
      width: 120,
      render: (days) => (
        <span
          style={{ color: getDaysRemainingColor(days), fontWeight: "bold" }}
        >
          {days > 0 ? `${days} 天` : "已过期"}
        </span>
      ),
    },
    {
      title: "状态",
      dataIndex: "notified",
      key: "notified",
      width: 100,
      render: (notified: boolean) => (
        <Badge
          status={notified ? "success" : "warning"}
          text={notified ? "已通知" : "待通知"}
        />
      ),
    },
    {
      title: "创建时间",
      dataIndex: "createdAt",
      key: "createdAt",
      width: 180,
      render: (date) => new Date(date).toLocaleString(),
    },
    {
      title: "操作",
      key: "action",
      width: 150,
      render: (_, record) => (
        <Space>
          <Button
            type="link"
            size="small"
            icon={<EyeOutlined />}
            onClick={() => handleViewResource(record.resourceId)}
          >
            查看资源
          </Button>
          {record.notified ? (
            <Button
              type="link"
              size="small"
              onClick={() => handleMarkNotified(record.id, false)}
            >
              标记未通知
            </Button>
          ) : (
            <Button
              type="link"
              size="small"
              onClick={() => handleMarkNotified(record.id, true)}
            >
              标记已通知
            </Button>
          )}
        </Space>
      ),
    },
  ];

  // 统计信息
  const expiredCount = data.filter((r) => r.daysRemaining <= 0).length;
  const criticalCount = data.filter(
    (r) => r.daysRemaining > 0 && r.daysRemaining <= 7,
  ).length;
  const warningCount = data.filter(
    (r) => r.daysRemaining > 7 && r.daysRemaining <= 30,
  ).length;

  return (
    <Card
      title={
        <Space>
          <BellOutlined />
          到期提醒
        </Space>
      }
    >
      {/* 统计卡片 */}
      <div style={{ marginBottom: 16, display: "flex", gap: 24 }}>
        <Card size="small" style={{ width: 200 }}>
          <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
            <WarningOutlined style={{ color: "#ff4d4f", fontSize: 24 }} />
            <div>
              <div style={{ fontSize: 12, color: "#999" }}>已过期</div>
              <div
                style={{ fontSize: 24, fontWeight: "bold", color: "#ff4d4f" }}
              >
                {expiredCount}
              </div>
            </div>
          </div>
        </Card>
        <Card size="small" style={{ width: 200 }}>
          <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
            <ClockCircleOutlined style={{ color: "#faad14", fontSize: 24 }} />
            <div>
              <div style={{ fontSize: 12, color: "#999" }}>7天内到期</div>
              <div
                style={{ fontSize: 24, fontWeight: "bold", color: "#faad14" }}
              >
                {criticalCount}
              </div>
            </div>
          </div>
        </Card>
        <Card size="small" style={{ width: 200 }}>
          <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
            <ClockCircleOutlined style={{ color: "#1890ff", fontSize: 24 }} />
            <div>
              <div style={{ fontSize: 12, color: "#999" }}>30天内到期</div>
              <div
                style={{ fontSize: 24, fontWeight: "bold", color: "#1890ff" }}
              >
                {warningCount}
              </div>
            </div>
          </div>
        </Card>
      </div>

      <Table
        columns={columns}
        dataSource={data}
        rowKey="id"
        loading={loading}
        pagination={{ pageSize: 20 }}
      />

      {/* 资源详情弹窗 */}
      <Modal
        title="资源详情"
        open={detailVisible}
        onCancel={() => setDetailVisible(false)}
        footer={null}
        width={700}
      >
        {currentResource && (
          <Descriptions bordered column={2}>
            <Descriptions.Item label="资源名称">
              {currentResource.name}
            </Descriptions.Item>
            <Descriptions.Item label="资源类型">
              {currentResource.type}
            </Descriptions.Item>
            <Descriptions.Item label="状态">
              {currentResource.status}
            </Descriptions.Item>
            <Descriptions.Item label="负责人">
              {currentResource.owner}
            </Descriptions.Item>
            <Descriptions.Item label="部门">
              {currentResource.department}
            </Descriptions.Item>
            <Descriptions.Item label="环境">
              {currentResource.environment}
            </Descriptions.Item>
            <Descriptions.Item label="保修到期">
              {currentResource.warrantyExpiry
                ? new Date(currentResource.warrantyExpiry).toLocaleDateString()
                : "-"}
            </Descriptions.Item>
            <Descriptions.Item label="维保到期">
              {currentResource.maintenanceExpiry
                ? new Date(
                    currentResource.maintenanceExpiry,
                  ).toLocaleDateString()
                : "-"}
            </Descriptions.Item>
          </Descriptions>
        )}
      </Modal>
    </Card>
  );
};

export default ExpiryReminderPage;
