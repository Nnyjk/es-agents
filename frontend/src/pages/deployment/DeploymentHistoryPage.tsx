import React, { useState, useRef } from "react";
import {
  ProTable,
  ModalForm,

  ProFormTextArea,
} from "@ant-design/pro-components";
import {
  Button,
  Space,
  Tag,
  message,
  Popconfirm,
  Drawer,
  Descriptions,
  Typography,
  Card,
  Statistic,
  Row,
  Col,
} from "antd";
import type { ProColumns, ActionType } from "@ant-design/pro-components";
import {
  RollbackOutlined,
  EyeOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  HistoryOutlined,
} from "@ant-design/icons";
import {
  getDeploymentHistory,
  getDeploymentHistoryDetail,
  rollbackDeployment,

} from "@/services/deployment";
import type {
  DeploymentHistory,
  DeploymentHistoryStatus,
} from "@/types/deployment";

interface DeploymentHistoryQueryParams {
  timeRange?: any[];
  pageNum?: number;
  pageSize?: number;
  applicationId?: string;
  environmentId?: string;
  status?: string;
  version?: string;
  triggeredBy?: string;
  startTime?: string;
  endTime?: string;
  keyword?: string;
}

const statusColors: Record<DeploymentHistoryStatus, string> = {
  pending: "default",
  deploying: "processing",
  success: "success",
  failed: "error",
  rolled_back: "warning",
};

const statusLabels: Record<DeploymentHistoryStatus, string> = {
  pending: "待部署",
  deploying: "部署中",
  success: "成功",
  failed: "失败",
  rolled_back: "已回滚",
};

const DeploymentHistoryPage: React.FC = () => {
  const [detailDrawerVisible, setDetailDrawerVisible] = useState(false);
  const [rollbackModalVisible, setRollbackModalVisible] = useState(false);
  const [currentRecord, setCurrentRecord] = useState<DeploymentHistory | null>(null);
  const actionRef = useRef<ActionType>();

  const handleDetail = async (record: DeploymentHistory) => {
    const detail = await getDeploymentHistoryDetail(record.id);
    setCurrentRecord(detail);
    setDetailDrawerVisible(true);
  };

  const handleRollback = async (record: DeploymentHistory) => {
    setCurrentRecord(record);
    setRollbackModalVisible(true);
  };

  const handleRollbackSubmit = async (values: Record<string, unknown>) => {
    if (!currentRecord) return false;
    try {
      await rollbackDeployment(currentRecord.id, values.reason as string);
      message.success("回滚成功");
      setRollbackModalVisible(false);
      actionRef.current?.reload();
      return true;
    } catch (error: any) {
      message.error(`回滚失败：${error.message}`);
      return false;
    }
  };

  const canRollback = (record: DeploymentHistory) =>
    record.releaseStatus === "success" || record.releaseStatus === "failed";

  const columns: ProColumns<DeploymentHistory>[] = [
    {
      title: "版本",
      dataIndex: "version",
      width: 120,
      render: (text) => <Typography.Text code>{text}</Typography.Text>,
    },
    {
      title: "状态",
      dataIndex: "releaseStatus",
      width: 100,
      valueType: "select",
      valueEnum: {
        pending: { text: "待部署", status: "Default" },
        deploying: { text: "部署中", status: "Processing" },
        success: { text: "成功", status: "Success" },
        failed: { text: "失败", status: "Error" },
        rolled_back: { text: "已回滚", status: "Warning" },
      },
      render: (_, record) => (
        <Tag color={statusColors[record.releaseStatus]}>{statusLabels[record.releaseStatus]}</Tag>
      ),
    },
    {
      title: "触发类型",
      dataIndex: "triggerType",
      width: 100,
      hideInSearch: true,
      render: (_, record) => {
        const labels: Record<string, string> = {
          manual: "手动",
          auto: "自动",
          webhook: "Webhook",
        };
        return labels[record.triggerType] || record.triggerType;
      },
    },
    {
      title: "操作人",
      dataIndex: "triggeredBy",
      width: 120,
    },
    {
      title: "部署时间",
      dataIndex: "createdAt",
      width: 180,
      valueType: "dateTime",
      hideInSearch: true,
    },
    {
      title: "耗时",
      dataIndex: "duration",
      width: 100,
      hideInSearch: true,
      render: (_, record) => {
        if (record.duration) {
          const seconds = record.duration;
          if (seconds < 60) return `${seconds}秒`;
          const minutes = Math.floor(seconds / 60);
          const remainingSeconds = seconds % 60;
          return `${minutes}分${remainingSeconds}秒`;
        }
        return "-";
      },
    },
    {
      title: "时间范围",
      valueType: "dateTimeRange",
      dataIndex: "timeRange",
      hideInTable: true,
    },
    {
      title: "版本",
      dataIndex: "version",
      hideInTable: true,
    },
    {
      title: "操作人",
      dataIndex: "triggeredBy",
      hideInTable: true,
    },
    {
      title: "操作",
      valueType: "option",
      width: 180,
      render: (_, record) => (
        <Space>
          <Button
            type="link"
            size="small"
            icon={<EyeOutlined />}
            onClick={() => handleDetail(record)}
          >
            详情
          </Button>
          <Popconfirm
            title="确认回滚？"
            onConfirm={() => handleRollback(record)}
            okText="确认"
            cancelText="取消"
            disabled={!canRollback(record)}
          >
            <Button
              type="link"
              size="small"
              danger
              icon={<RollbackOutlined />}
              disabled={!canRollback(record)}
            >
              回滚
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <>
      <Card style={{ marginBottom: 16 }}>
        <Row gutter={16}>
          <Col span={6}>
            <Statistic title="总部署次数" value={0} prefix={<HistoryOutlined />} />
          </Col>
          <Col span={6}>
            <Statistic title="成功" value={0} valueStyle={{ color: "#52c41a" }} prefix={<CheckCircleOutlined />} />
          </Col>
          <Col span={6}>
            <Statistic title="失败" value={0} valueStyle={{ color: "#ff4d4f" }} prefix={<CloseCircleOutlined />} />
          </Col>
          <Col span={6}>
            <Statistic title="回滚次数" value={0} valueStyle={{ color: "#faad14" }} prefix={<RollbackOutlined />} />
          </Col>
        </Row>
      </Card>

      <ProTable<DeploymentHistory, DeploymentHistoryQueryParams>
        headerTitle="部署历史记录"
        actionRef={actionRef}
        rowKey="id"
        search={{ labelWidth: "auto" }}
        request={async (params) => {
          const { timeRange, ...restParams } = params;
          const queryParams: DeploymentHistoryQueryParams = {
            pageNum: params.current,
            pageSize: params.pageSize,
            ...restParams,
          };
          if (timeRange && timeRange.length === 2) {
            queryParams.startTime = timeRange[0]?.toString();
            queryParams.endTime = timeRange[1]?.toString();
          }
          try {
            const result = await getDeploymentHistory(queryParams);
            return { data: result.list, success: true, total: result.total };
          } catch (error: any) {
            message.error(`获取部署历史失败：${error.message}`);
            return { data: [], success: false, total: 0 };
          }
        }}
        columns={columns}
        pagination={{ defaultPageSize: 10, showSizeChanger: true, showQuickJumper: true }}
        dateFormatter="string"
      />

      <Drawer
        title="部署详情"
        placement="right"
        width={720}
        onClose={() => { setDetailDrawerVisible(false); setCurrentRecord(null); }}
        open={detailDrawerVisible}
      >
        {currentRecord && (
          <Descriptions column={1} bordered size="small">
            <Descriptions.Item label="发布ID">{currentRecord.releaseId}</Descriptions.Item>
            <Descriptions.Item label="版本"><Typography.Text code>{currentRecord.version}</Typography.Text></Descriptions.Item>
            <Descriptions.Item label="状态">
              <Tag color={statusColors[currentRecord.releaseStatus]}>{statusLabels[currentRecord.releaseStatus]}</Tag>
            </Descriptions.Item>
            <Descriptions.Item label="触发类型">{currentRecord.triggerType}</Descriptions.Item>
            <Descriptions.Item label="操作人">{currentRecord.triggeredBy}</Descriptions.Item>
            <Descriptions.Item label="开始时间">{currentRecord.startedAt}</Descriptions.Item>
            <Descriptions.Item label="结束时间">{currentRecord.finishedAt}</Descriptions.Item>
            {currentRecord.duration && (
              <Descriptions.Item label="耗时">{currentRecord.duration}秒</Descriptions.Item>
            )}
            {currentRecord.changeLog && (
              <Descriptions.Item label="变更日志" span={2}>{currentRecord.changeLog}</Descriptions.Item>
            )}
          </Descriptions>
        )}
      </Drawer>

      <ModalForm
        title="确认回滚"
        open={rollbackModalVisible}
        onOpenChange={setRollbackModalVisible}
        modalProps={{ destroyOnClose: true, onCancel: () => setRollbackModalVisible(false) }}
        onFinish={handleRollbackSubmit}
        submitter={{ searchConfig: { submitText: "确认回滚", resetText: "取消" } }}
      >
        <ProFormTextArea
          name="reason"
          label="回滚原因"
          placeholder="请输入回滚原因（必填）"
          rules={[{ required: true, message: "请输入回滚原因" }]}
          fieldProps={{ rows: 4 }}
        />
      </ModalForm>
    </>
  );
};

export default DeploymentHistoryPage;
