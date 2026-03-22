import React, { useRef, useState, useEffect } from "react";
import { ReloadOutlined, WarningOutlined, CloudSyncOutlined } from "@ant-design/icons";
import type { ActionType, ProColumns } from "@ant-design/pro-components";
import { ProTable } from "@ant-design/pro-components";
import {
  Button,
  message,
  Tag,
  Modal,
  Steps,
  Select,
  Checkbox,
  Alert,
  Descriptions,
  Progress,
  Typography,
  Card,
  Popconfirm,
} from "antd";
import {
  getBackupRecords,
  getRestoreTasks,
  createRestoreTask,
  cancelRestoreTask,
  getRestoreTask,
} from "../../../services/backup";
import type {
  BackupRecord,
  RestoreTask,
  RestoreTaskStatus,
  RestoreScope,
} from "../../../types/backup";

const { Text, Title } = Typography;

const statusColors: Record<RestoreTaskStatus, string> = {
  PENDING: "default",
  RUNNING: "processing",
  SUCCESS: "success",
  FAILED: "error",
};

const statusTexts: Record<RestoreTaskStatus, string> = {
  PENDING: "等待中",
  RUNNING: "运行中",
  SUCCESS: "成功",
  FAILED: "失败",
};

const RestorePage: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [wizardVisible, setWizardVisible] = useState(false);
  const [currentStep, setCurrentStep] = useState(0);
  const [backupRecords, setBackupRecords] = useState<BackupRecord[]>([]);
  const [selectedBackup, setSelectedBackup] = useState<BackupRecord | null>(null);
  const [restoreScopes, setRestoreScopes] = useState<RestoreScope[]>([]);
  const [restoreLoading, setRestoreLoading] = useState(false);
  const [currentRestoreTask, setCurrentRestoreTask] = useState<RestoreTask | null>(null);

  useEffect(() => {
    if (wizardVisible) {
      loadBackupRecords();
    }
  }, [wizardVisible]);

  useEffect(() => {
    if (currentRestoreTask && currentRestoreTask.status === "RUNNING") {
      const timer = setInterval(async () => {
        try {
          const task = await getRestoreTask(currentRestoreTask.id);
          setCurrentRestoreTask(task);
          if (task.status !== "RUNNING") {
            clearInterval(timer);
            actionRef.current?.reload();
          }
        } catch (error) {
          clearInterval(timer);
        }
      }, 2000);
      return () => clearInterval(timer);
    }
  }, [currentRestoreTask]);

  const loadBackupRecords = async () => {
    try {
      const data = await getBackupRecords();
      setBackupRecords(data.filter((r) => r.status === "SUCCESS"));
    } catch (error) {
      message.error("加载备份记录失败");
    }
  };

  const handleStartWizard = () => {
    setCurrentStep(0);
    setSelectedBackup(null);
    setRestoreScopes([]);
    setCurrentRestoreTask(null);
    setWizardVisible(true);
  };

  const handleSelectBackup = (backupId: number) => {
    const backup = backupRecords.find((r) => r.id === backupId);
    setSelectedBackup(backup || null);
  };

  const handleRestore = async () => {
    if (!selectedBackup || restoreScopes.length === 0) {
      message.error("请选择备份文件和恢复范围");
      return;
    }

    setRestoreLoading(true);
    try {
      const task = await createRestoreTask({
        backupRecordId: selectedBackup.id,
        restoreScope: restoreScopes[0], // TODO: 支持多选
      });
      setCurrentRestoreTask(task);
      setCurrentStep(2);
      message.success("恢复任务已创建");
    } catch (error) {
      message.error("创建恢复任务失败");
    } finally {
      setRestoreLoading(false);
    }
  };

  const handleCancelRestore = async () => {
    if (!currentRestoreTask) return;
    try {
      await cancelRestoreTask(currentRestoreTask.id);
      message.success("已取消恢复任务");
      setWizardVisible(false);
      actionRef.current?.reload();
    } catch (error) {
      message.error("取消失败");
    }
  };

  const columns: ProColumns<RestoreTask>[] = [
    {
      title: "任务ID",
      dataIndex: "id",
      key: "id",
      width: 80,
    },
    {
      title: "备份名称",
      dataIndex: "backupName",
      key: "backupName",
      width: 200,
      ellipsis: true,
    },
    {
      title: "恢复范围",
      dataIndex: "restoreScope",
      key: "restoreScope",
      width: 100,
      render: (_, record) => {
        const scopeTexts: Record<RestoreScope, string> = {
          DATABASE: "数据库",
          CONFIG: "配置文件",
          LOGS: "日志文件",
          ALL: "全部数据",
        };
        return scopeTexts[record.restoreScope];
      },
    },
    {
      title: "进度",
      dataIndex: "progress",
      key: "progress",
      width: 150,
      render: (_, record) => (
        <Progress
          percent={record.progress}
          size="small"
          status={
            record.status === "FAILED"
              ? "exception"
              : record.status === "SUCCESS"
              ? "success"
              : "active"
          }
        />
      ),
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 100,
      render: (_, record) => (
        <Tag color={statusColors[record.status]}>
          {statusTexts[record.status]}
        </Tag>
      ),
    },
    {
      title: "操作人",
      dataIndex: "operator",
      key: "operator",
      width: 100,
    },
    {
      title: "开始时间",
      dataIndex: "startTime",
      key: "startTime",
      width: 180,
    },
    {
      title: "结束时间",
      dataIndex: "endTime",
      key: "endTime",
      width: 180,
    },
    {
      title: "错误信息",
      dataIndex: "errorMessage",
      key: "errorMessage",
      width: 200,
      ellipsis: true,
      render: (_, record) =>
        record.errorMessage ? (
          <Text type="danger">{record.errorMessage}</Text>
        ) : (
          "-"
        ),
    },
  ];

  const backupOptions = backupRecords.map((r) => ({
    label: `${r.backupName} (${r.startTime})`,
    value: r.id,
  }));

  const scopeOptions = [
    { label: "数据库", value: "DATABASE" as RestoreScope },
    { label: "配置文件", value: "CONFIG" as RestoreScope },
    { label: "日志文件", value: "LOGS" as RestoreScope },
    { label: "全部数据", value: "ALL" as RestoreScope },
  ];

  return (
    <>
      <ProTable<RestoreTask>
        columns={columns}
        actionRef={actionRef}
        rowKey="id"
        request={async () => {
          try {
            const data = await getRestoreTasks();
            return {
              data,
              success: true,
            };
          } catch (error) {
            return {
              data: [],
              success: false,
            };
          }
        }}
        toolBarRender={() => [
          <Button
            key="restore"
            type="primary"
            icon={<CloudSyncOutlined />}
            onClick={handleStartWizard}
          >
            新建恢复任务
          </Button>,
          <Button
            key="refresh"
            icon={<ReloadOutlined />}
            onClick={() => actionRef.current?.reload()}
          >
            刷新
          </Button>,
        ]}
        search={false}
        pagination={{
          pageSize: 10,
        }}
        scroll={{ x: 1300 }}
      />

      <Modal
        title="数据恢复向导"
        open={wizardVisible}
        onCancel={() => setWizardVisible(false)}
        footer={null}
        width={700}
      >
        <Steps current={currentStep} style={{ marginBottom: 24 }}>
          <Steps.Step title="选择备份" description="选择要恢复的备份文件" />
          <Steps.Step title="确认恢复" description="选择恢复范围并确认" />
          <Steps.Step title="执行恢复" description="查看恢复进度" />
        </Steps>

        {currentStep === 0 && (
          <Card>
            <Title level={5}>选择备份文件</Title>
            <Select
              style={{ width: "100%", marginTop: 16 }}
              placeholder="请选择要恢复的备份文件"
              options={backupOptions}
              value={selectedBackup?.id}
              onChange={handleSelectBackup}
              showSearch
              filterOption={(input, option) =>
                (option?.label ?? "").toLowerCase().includes(input.toLowerCase())
              }
            />
            {selectedBackup && (
              <Descriptions column={2} size="small" style={{ marginTop: 16 }} bordered>
                <Descriptions.Item label="备份名称">
                  {selectedBackup.backupName}
                </Descriptions.Item>
                <Descriptions.Item label="备份时间">
                  {selectedBackup.startTime}
                </Descriptions.Item>
                <Descriptions.Item label="文件大小">
                  {selectedBackup.fileSizeFormatted}
                </Descriptions.Item>
                <Descriptions.Item label="备份类型">
                  {selectedBackup.backupType}
                </Descriptions.Item>
              </Descriptions>
            )}
            <div style={{ marginTop: 24, textAlign: "right" }}>
              <Button
                type="primary"
                onClick={() => setCurrentStep(1)}
                disabled={!selectedBackup}
              >
                下一步
              </Button>
            </div>
          </Card>
        )}

        {currentStep === 1 && (
          <Card>
            <Alert
              message="警告"
              description="恢复操作将覆盖当前系统数据，此操作不可撤销！请确保已做好数据备份。"
              type="warning"
              showIcon
              icon={<WarningOutlined />}
              style={{ marginBottom: 16 }}
            />
            <Title level={5}>选择恢复范围</Title>
            <Checkbox.Group
              options={scopeOptions}
              value={restoreScopes}
              onChange={(values) => setRestoreScopes(values as RestoreScope[])}
              style={{ marginTop: 16 }}
            />
            <Descriptions column={2} size="small" style={{ marginTop: 24 }} bordered>
              <Descriptions.Item label="备份文件">
                {selectedBackup?.backupName}
              </Descriptions.Item>
              <Descriptions.Item label="备份时间">
                {selectedBackup?.startTime}
              </Descriptions.Item>
            </Descriptions>
            <div style={{ marginTop: 24, textAlign: "right" }}>
              <Button style={{ marginRight: 8 }} onClick={() => setCurrentStep(0)}>
                上一步
              </Button>
              <Popconfirm
                title="确定要执行恢复操作吗？此操作不可撤销！"
                onConfirm={handleRestore}
              >
                <Button
                  type="primary"
                  danger
                  loading={restoreLoading}
                  disabled={restoreScopes.length === 0}
                >
                  确认恢复
                </Button>
              </Popconfirm>
            </div>
          </Card>
        )}

        {currentStep === 2 && (
          <Card>
            <Title level={5}>恢复进度</Title>
            {currentRestoreTask && (
              <>
                <Progress
                  percent={currentRestoreTask.progress}
                  status={
                    currentRestoreTask.status === "FAILED"
                      ? "exception"
                      : currentRestoreTask.status === "SUCCESS"
                      ? "success"
                      : "active"
                  }
                  style={{ marginTop: 16 }}
                />
                <Descriptions column={2} size="small" style={{ marginTop: 24 }} bordered>
                  <Descriptions.Item label="备份文件">
                    {currentRestoreTask.backupName}
                  </Descriptions.Item>
                  <Descriptions.Item label="恢复范围">
                    {currentRestoreTask.restoreScope}
                  </Descriptions.Item>
                  <Descriptions.Item label="状态">
                    <Tag color={statusColors[currentRestoreTask.status]}>
                      {statusTexts[currentRestoreTask.status]}
                    </Tag>
                  </Descriptions.Item>
                  <Descriptions.Item label="开始时间">
                    {currentRestoreTask.startTime}
                  </Descriptions.Item>
                </Descriptions>
                {currentRestoreTask.errorMessage && (
                  <Alert
                    message="恢复失败"
                    description={currentRestoreTask.errorMessage}
                    type="error"
                    style={{ marginTop: 16 }}
                  />
                )}
              </>
            )}
            <div style={{ marginTop: 24, textAlign: "right" }}>
              {currentRestoreTask?.status === "RUNNING" && (
                <Button danger onClick={handleCancelRestore} style={{ marginRight: 8 }}>
                  取消恢复
                </Button>
              )}
              <Button
                type="primary"
                onClick={() => {
                  setWizardVisible(false);
                  actionRef.current?.reload();
                }}
                disabled={currentRestoreTask?.status === "RUNNING"}
              >
                完成
              </Button>
            </div>
          </Card>
        )}
      </Modal>
    </>
  );
};

export default RestorePage;