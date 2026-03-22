import React, { useState, useRef } from "react";
import {
  ProTable,
  ModalForm,
  ProFormText,
  ProFormSelect,
  ProFormTextArea,
  ProFormDateTimePicker,
} from "@ant-design/pro-components";
import {
  Button,
  Space,
  Tag,
  message,
  Popconfirm,
  Drawer,
  Descriptions,
  Timeline,
  Typography,
  Steps,
  Progress,
  Card,
  Divider,
  Alert,
} from "antd";
import type { ProColumns, ActionType } from "@ant-design/pro-components";
import {
  PlusOutlined,
  EyeOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  SyncOutlined,
  RollbackOutlined,
  HistoryOutlined,
} from "@ant-design/icons";
import {
  getReleases,
  createRelease,
  getReleaseDetail,
  approveRelease,
  rejectRelease,
  startRelease,
  rollbackRelease,
  getReleaseHistory,
} from "@/services/deployment";
import type { Release, ReleaseStatus, ReleaseType } from "@/types/deployment";

const statusColors: Record<ReleaseStatus, string> = {
  draft: "default",
  pending: "blue",
  approved: "cyan",
  deploying: "processing",
  success: "green",
  failed: "red",
  cancelled: "orange",
  rolled_back: "purple",
};

const statusLabels: Record<ReleaseStatus, string> = {
  draft: "草稿",
  pending: "待审批",
  approved: "已审批",
  deploying: "部署中",
  success: "成功",
  failed: "失败",
  cancelled: "已取消",
  rolled_back: "已回滚",
};

const typeLabels: Record<ReleaseType, string> = {
  major: "主版本",
  minor: "次版本",
  patch: "补丁版本",
  hotfix: "热修复",
};

const ReleasePage: React.FC = () => {
  const [createModalVisible, setCreateModalVisible] = useState(false);
  const [detailDrawerVisible, setDetailDrawerVisible] = useState(false);
  const [historyDrawerVisible, setHistoryDrawerVisible] = useState(false);
  const [currentRelease, setCurrentRelease] = useState<Release | null>(null);
  const [releaseHistory, setReleaseHistory] = useState<Release[]>([]);
  const actionRef = useRef<ActionType>();

  const handleCreate = () => {
    setCreateModalVisible(true);
  };

  const handleDetail = async (record: Release) => {
    const detail = await getReleaseDetail(record.id);
    setCurrentRelease(detail);
    setDetailDrawerVisible(true);
  };

  const handleHistory = async (record: Release) => {
    const history = await getReleaseHistory(record.applicationId);
    setReleaseHistory(history);
    setCurrentRelease(record);
    setHistoryDrawerVisible(true);
  };

  const handleApprove = async (record: Release) => {
    await approveRelease(record.id);
    message.success("审批通过");
    actionRef.current?.reload();
  };

  const handleReject = async (record: Release) => {
    await rejectRelease(record.id);
    message.success("已拒绝");
    actionRef.current?.reload();
  };

  const handleDeploy = async (record: Release) => {
    await startRelease(record.id);
    message.success("开始部署");
    actionRef.current?.reload();
  };

  const handleRollback = async (record: Release) => {
    await rollbackRelease(record.id);
    message.success("已回滚");
    actionRef.current?.reload();
  };

  const handleCreateSubmit = async (values: Record<string, unknown>) => {
    await createRelease(values as Parameters<typeof createRelease>[0]);
    message.success("发布单创建成功");
    setCreateModalVisible(false);
    actionRef.current?.reload();
    return true;
  };

  const canApprove = (record: Release) => record.status === "pending";
  const canReject = (record: Release) => record.status === "pending";
  const canDeploy = (record: Release) => record.status === "approved";
  const canRollback = (record: Release) => record.status === "success" || record.status === "failed";

  const renderDeployProgress = (record: Release) => {
    if (record.status !== "deploying" || !record.deployProgress) return null;
    
    const progress = record.deployProgress;
    const percent = Math.round(
      (progress.deployedInstances / progress.totalInstances) * 100,
    );

    return (
      <div style={{ marginTop: 8 }}>
        <Progress percent={percent} status="active" size="small" />
        <div style={{ fontSize: 12, color: "#666" }}>
          已部署: {progress.deployedInstances}/{progress.totalInstances} 实例
        </div>
      </div>
    );
  };

  const columns: ProColumns<Release>[] = [
    {
      title: "发布单号",
      dataIndex: "releaseId",
      width: 120,
      render: (_, record) => (
        <a onClick={() => handleDetail(record)}>{record.releaseId}</a>
      ),
    },
    {
      title: "应用",
      dataIndex: "applicationName",
      width: 150,
      search: false,
    },
    {
      title: "版本",
      dataIndex: "version",
      width: 120,
    },
    {
      title: "类型",
      dataIndex: "type",
      width: 100,
      valueType: "select",
      valueEnum: {
        major: { text: "主版本" },
        minor: { text: "次版本" },
        patch: { text: "补丁版本" },
        hotfix: { text: "热修复" },
      },
      render: (_, record) => (
        <Tag color="blue">{typeLabels[record.type]}</Tag>
      ),
    },
    {
      title: "环境",
      dataIndex: "environmentName",
      width: 100,
    },
    {
      title: "状态",
      dataIndex: "status",
      width: 100,
      valueType: "select",
      valueEnum: {
        draft: { text: "草稿", status: "Default" },
        pending: { text: "待审批", status: "Processing" },
        approved: { text: "已审批", status: "Success" },
        deploying: { text: "部署中", status: "Processing" },
        success: { text: "成功", status: "Success" },
        failed: { text: "失败", status: "Error" },
        cancelled: { text: "已取消", status: "Warning" },
        rolled_back: { text: "已回滚", status: "Default" },
      },
      render: (_, record) => (
        <Tag color={statusColors[record.status]}>
          {statusLabels[record.status]}
        </Tag>
      ),
    },
    {
      title: "申请人",
      dataIndex: "applicant",
      width: 100,
    },
    {
      title: "创建时间",
      dataIndex: "createdAt",
      width: 160,
      valueType: "dateTime",
      search: false,
    },
    {
      title: "操作",
      key: "action",
      width: 280,
      search: false,
      fixed: "right",
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
            icon={<HistoryOutlined />}
            onClick={() => handleHistory(record)}
          >
            历史
          </Button>
          {canApprove(record) && (
            <Popconfirm
              title="确定通过审批？"
              onConfirm={() => handleApprove(record)}
            >
              <Button
                type="link"
                size="small"
                icon={<CheckCircleOutlined />}
                style={{ color: "#52c41a" }}
              >
                审批
              </Button>
            </Popconfirm>
          )}
          {canReject(record) && (
            <Popconfirm
              title="确定拒绝此发布？"
              onConfirm={() => handleReject(record)}
            >
              <Button
                type="link"
                size="small"
                icon={<CloseCircleOutlined />}
                danger
              >
                拒绝
              </Button>
            </Popconfirm>
          )}
          {canDeploy(record) && (
            <Popconfirm
              title="确定开始部署？"
              onConfirm={() => handleDeploy(record)}
            >
              <Button
                type="link"
                size="small"
                icon={<SyncOutlined />}
                style={{ color: "#1890ff" }}
              >
                部署
              </Button>
            </Popconfirm>
          )}
          {canRollback(record) && (
            <Popconfirm
              title="确定回滚此发布？"
              onConfirm={() => handleRollback(record)}
            >
              <Button
                type="link"
                size="small"
                icon={<RollbackOutlined />}
                danger
              >
                回滚
              </Button>
            </Popconfirm>
          )}
        </Space>
      ),
    },
  ];

  return (
    <>
      <ProTable<Release>
        columns={columns}
        actionRef={actionRef}
        request={async (params) => {
          const result = await getReleases({
            current: params.current || 1,
            pageSize: params.pageSize || 10,
            releaseId: params.releaseId,
            version: params.version,
            type: params.type as ReleaseType,
            status: params.status as ReleaseStatus,
          });
          return {
            data: result.list,
            total: result.total,
            success: true,
          };
        }}
        rowKey="id"
        scroll={{ x: 1400 }}
        search={{
          labelWidth: "auto",
        }}
        toolBarRender={() => [
          <Button
            key="add"
            type="primary"
            icon={<PlusOutlined />}
            onClick={handleCreate}
          >
            创建发布单
          </Button>,
        ]}
        pagination={{
          defaultPageSize: 10,
          showSizeChanger: true,
        }}
      />

      <ModalForm
        title="创建发布单"
        open={createModalVisible}
        onFinish={handleCreateSubmit}
        onOpenChange={setCreateModalVisible}
        modalProps={{
          destroyOnClose: true,
          width: 600,
        }}
      >
        <ProFormSelect
          name="applicationId"
          label="应用"
          rules={[{ required: true, message: "请选择应用" }]}
          placeholder="请选择应用"
          request={async () => {
            // 这里应该调用获取应用列表的接口
            return [];
          }}
        />
        <ProFormSelect
          name="environmentId"
          label="目标环境"
          rules={[{ required: true, message: "请选择目标环境" }]}
          placeholder="请选择目标环境"
          options={[
            { label: "开发环境", value: "dev" },
            { label: "测试环境", value: "test" },
            { label: "预发环境", value: "staging" },
            { label: "生产环境", value: "prod" },
          ]}
        />
        <ProFormText
          name="version"
          label="发布版本"
          rules={[{ required: true, message: "请输入发布版本" }]}
          placeholder="v1.0.0"
        />
        <ProFormSelect
          name="type"
          label="发布类型"
          options={[
            { label: "主版本", value: "major" },
            { label: "次版本", value: "minor" },
            { label: "补丁版本", value: "patch" },
            { label: "热修复", value: "hotfix" },
          ]}
          rules={[{ required: true }]}
          initialValue="patch"
        />
        <ProFormTextArea
          name="releaseNotes"
          label="发布说明"
          placeholder="请输入发布说明"
          rules={[{ required: true, message: "请输入发布说明" }]}
          fieldProps={{ rows: 4 }}
        />
        <ProFormDateTimePicker
          name="scheduledAt"
          label="计划发布时间"
          placeholder="留空则立即发布"
        />
      </ModalForm>

      <Drawer
        title={`发布详情 - ${currentRelease?.releaseId}`}
        open={detailDrawerVisible}
        onClose={() => setDetailDrawerVisible(false)}
        width={700}
      >
        {currentRelease && (
          <>
            {currentRelease.status === "deploying" && (
              <Alert
                message="部署进行中"
                description={renderDeployProgress(currentRelease)}
                type="info"
                showIcon
                style={{ marginBottom: 16 }}
              />
            )}

            <Descriptions column={2} bordered size="small">
              <Descriptions.Item label="发布单号">
                {currentRelease.releaseId}
              </Descriptions.Item>
              <Descriptions.Item label="版本">
                {currentRelease.version}
              </Descriptions.Item>
              <Descriptions.Item label="应用">
                {currentRelease.applicationName}
              </Descriptions.Item>
              <Descriptions.Item label="环境">
                {currentRelease.environmentName}
              </Descriptions.Item>
              <Descriptions.Item label="类型">
                <Tag color="blue">{typeLabels[currentRelease.type]}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label="状态">
                <Tag color={statusColors[currentRelease.status]}>
                  {statusLabels[currentRelease.status]}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="申请人">
                {currentRelease.applicant}
              </Descriptions.Item>
              <Descriptions.Item label="审批人">
                {currentRelease.approver || "-"}
              </Descriptions.Item>
              <Descriptions.Item label="创建时间">
                {new Date(currentRelease.createdAt).toLocaleString()}
              </Descriptions.Item>
              <Descriptions.Item label="计划时间">
                {currentRelease.scheduledAt
                  ? new Date(currentRelease.scheduledAt).toLocaleString()
                  : "-"}
              </Descriptions.Item>
              {currentRelease.deployedAt && (
                <Descriptions.Item label="部署时间">
                  {new Date(currentRelease.deployedAt).toLocaleString()}
                </Descriptions.Item>
              )}
              {currentRelease.completedAt && (
                <Descriptions.Item label="完成时间">
                  {new Date(currentRelease.completedAt).toLocaleString()}
                </Descriptions.Item>
              )}
              <Descriptions.Item label="发布说明" span={2}>
                <Typography.Paragraph
                  ellipsis={{ rows: 3, expandable: true }}
                  style={{ marginBottom: 0 }}
                >
                  {currentRelease.releaseNotes}
                </Typography.Paragraph>
              </Descriptions.Item>
              {currentRelease.rollbackFrom && (
                <Descriptions.Item label="回滚自" span={2}>
                  {currentRelease.rollbackFrom}
                </Descriptions.Item>
              )}
            </Descriptions>

            {currentRelease.changes && (
              <>
                <Divider orientation="left">变更内容</Divider>
                <Card size="small">
                  <Typography.Paragraph>
                    <Typography.Text strong>新增功能：</Typography.Text>
                  </Typography.Paragraph>
                  <ul style={{ paddingLeft: 20, margin: 0 }}>
                    {currentRelease.changes.features?.map((item, i) => (
                      <li key={i}>{item}</li>
                    ))}
                  </ul>
                  <Typography.Paragraph style={{ marginTop: 8 }}>
                    <Typography.Text strong>修复问题：</Typography.Text>
                  </Typography.Paragraph>
                  <ul style={{ paddingLeft: 20, margin: 0 }}>
                    {currentRelease.changes.fixes?.map((item, i) => (
                      <li key={i}>{item}</li>
                    ))}
                  </ul>
                  <Typography.Paragraph style={{ marginTop: 8 }}>
                    <Typography.Text strong>其他变更：</Typography.Text>
                  </Typography.Paragraph>
                  <ul style={{ paddingLeft: 20, margin: 0 }}>
                    {currentRelease.changes.others?.map((item, i) => (
                      <li key={i}>{item}</li>
                    ))}
                  </ul>
                </Card>
              </>
            )}

            {currentRelease.deployProgress && (
              <>
                <Divider orientation="left">部署进度</Divider>
                <Steps
                  current={currentRelease.deployProgress.currentStep}
                  items={currentRelease.deployProgress.steps?.map((step, i) => ({
                    title: step,
                    status:
                      i < currentRelease.deployProgress!.currentStep
                        ? "finish"
                        : i === currentRelease.deployProgress!.currentStep
                          ? "process"
                          : "wait",
                  }))}
                />
              </>
            )}
          </>
        )}
      </Drawer>

      <Drawer
        title="发布历史"
        open={historyDrawerVisible}
        onClose={() => setHistoryDrawerVisible(false)}
        width={600}
      >
        <Timeline
          items={releaseHistory.map((release) => ({
            color: statusColors[release.status],
            children: (
              <div>
                <div
                  style={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                  }}
                >
                  <span>
                    <b>{release.version}</b>
                  </span>
                  <Tag color={statusColors[release.status]}>
                    {statusLabels[release.status]}
                  </Tag>
                </div>
                <div style={{ color: "#666", fontSize: 12 }}>
                  {release.environmentName} -{" "}
                  {new Date(release.createdAt).toLocaleString()}
                </div>
                <div style={{ fontSize: 12 }}>
                  类型: {typeLabels[release.type]}
                </div>
              </div>
            ),
          }))}
        />
      </Drawer>
    </>
  );
};

export default ReleasePage;
