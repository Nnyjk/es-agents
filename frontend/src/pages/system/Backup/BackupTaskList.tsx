import React, { useRef, useState } from "react";
import {
  PlusOutlined,
  PlayCircleOutlined,
  PauseCircleOutlined,
} from "@ant-design/icons";
import type { ActionType, ProColumns } from "@ant-design/pro-components";
import { ProTable, ProForm } from "@ant-design/pro-components";
import {
  Button,
  message,
  Popconfirm,
  Tag,
  Space,
  Tooltip,
  Badge,
  Input,
  Select,
  InputNumber,
} from "antd";
import { DrawerForm } from "../../../components/DrawerForm";
import {
  getBackupTasks,
  createBackupTask,
  updateBackupTask,
  deleteBackupTask,
  enableBackupTask,
  disableBackupTask,
  triggerBackupTask,
} from "../../../services/backup";
import type {
  BackupTask,
  BackupTaskRequest,
  BackupTaskStatus,
  BackupType,
  BackupContent,
  StorageType,
} from "../../../types/backup";

const statusColors: Record<BackupTaskStatus, string> = {
  ENABLED: "success",
  DISABLED: "default",
  RUNNING: "processing",
  ERROR: "error",
};

const statusTexts: Record<BackupTaskStatus, string> = {
  ENABLED: "已启用",
  DISABLED: "已禁用",
  RUNNING: "运行中",
  ERROR: "错误",
};

const backupTypeTexts: Record<BackupType, string> = {
  FULL: "全量备份",
  INCREMENTAL: "增量备份",
  DIFFERENTIAL: "差异备份",
};

const backupContentTexts: Record<BackupContent, string> = {
  DATABASE: "数据库",
  CONFIG: "配置文件",
  LOGS: "日志文件",
  ALL: "全部数据",
};

const storageTypeTexts: Record<StorageType, string> = {
  LOCAL: "本地存储",
  S3: "对象存储(S3)",
  NFS: "NFS存储",
  FTP: "FTP存储",
};

const BackupTaskList: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [drawerVisible, setDrawerVisible] = useState(false);
  const [editingItem, setEditingItem] = useState<BackupTask | null>(null);

  const handleCreate = () => {
    setEditingItem(null);
    setDrawerVisible(true);
  };

  const handleEdit = (record: BackupTask) => {
    setEditingItem(record);
    setDrawerVisible(true);
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteBackupTask(id);
      message.success("删除成功");
      actionRef.current?.reload();
    } catch (error) {
      message.error("删除失败");
    }
  };

  const handleToggleStatus = async (record: BackupTask) => {
    try {
      if (record.status === "ENABLED") {
        await disableBackupTask(record.id);
        message.success("已禁用");
      } else {
        await enableBackupTask(record.id);
        message.success("已启用");
      }
      actionRef.current?.reload();
    } catch (error) {
      message.error("操作失败");
    }
  };

  const handleTrigger = async (id: number) => {
    try {
      await triggerBackupTask(id);
      message.success("备份任务已触发");
      actionRef.current?.reload();
    } catch (error) {
      message.error("触发失败");
    }
  };

  const handleSubmit = async (values: BackupTaskRequest) => {
    try {
      if (editingItem) {
        await updateBackupTask(editingItem.id, values);
        message.success("更新成功");
      } else {
        await createBackupTask(values);
        message.success("创建成功");
      }
      setDrawerVisible(false);
      actionRef.current?.reload();
      return true;
    } catch (error) {
      message.error(editingItem ? "更新失败" : "创建失败");
      return false;
    }
  };

  const columns: ProColumns<BackupTask>[] = [
    {
      title: "任务名称",
      dataIndex: "name",
      key: "name",
      width: 180,
      ellipsis: true,
    },
    {
      title: "备份类型",
      dataIndex: "backupType",
      key: "backupType",
      width: 100,
      render: (_, record) => (
        <Tag color="blue">{backupTypeTexts[record.backupType]}</Tag>
      ),
    },
    {
      title: "备份内容",
      dataIndex: "backupContent",
      key: "backupContent",
      width: 100,
      render: (_, record) => backupContentTexts[record.backupContent],
    },
    {
      title: "备份周期",
      dataIndex: "cronExpression",
      key: "cronExpression",
      width: 120,
      copyable: true,
    },
    {
      title: "存储位置",
      dataIndex: "storageType",
      key: "storageType",
      width: 120,
      render: (_, record) => (
        <Tooltip title={record.storagePath}>
          <span>{storageTypeTexts[record.storageType]}</span>
        </Tooltip>
      ),
    },
    {
      title: "保留策略",
      key: "retention",
      width: 100,
      render: (_, record) => (
        <span>
          {record.retentionDays}天 / {record.maxBackups}份
        </span>
      ),
    },
    {
      title: "上次执行",
      dataIndex: "lastRunTime",
      key: "lastRunTime",
      width: 160,
      render: (_, record) => (
        <Space direction="vertical" size={0}>
          <span>{record.lastRunTime || "-"}</span>
          {record.lastRunStatus && (
            <Badge
              status={
                record.lastRunStatus === "SUCCESS"
                  ? "success"
                  : record.lastRunStatus === "FAILED"
                    ? "error"
                    : "processing"
              }
              text={
                record.lastRunStatus === "SUCCESS"
                  ? "成功"
                  : record.lastRunStatus === "FAILED"
                    ? "失败"
                    : "运行中"
              }
            />
          )}
        </Space>
      ),
    },
    {
      title: "下次执行",
      dataIndex: "nextRunTime",
      key: "nextRunTime",
      width: 160,
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 80,
      render: (_, record) => (
        <Tag color={statusColors[record.status]}>
          {statusTexts[record.status]}
        </Tag>
      ),
    },
    {
      title: "操作",
      key: "action",
      width: 200,
      fixed: "right",
      render: (_, record) => (
        <Space size="small">
          <Button type="link" size="small" onClick={() => handleEdit(record)}>
            编辑
          </Button>
          <Popconfirm
            title="确定要手动触发备份吗？"
            onConfirm={() => handleTrigger(record.id)}
          >
            <Button type="link" size="small" icon={<PlayCircleOutlined />}>
              执行
            </Button>
          </Popconfirm>
          <Button
            type="link"
            size="small"
            icon={
              record.status === "ENABLED" ? (
                <PauseCircleOutlined />
              ) : (
                <PlayCircleOutlined />
              )
            }
            onClick={() => handleToggleStatus(record)}
          >
            {record.status === "ENABLED" ? "禁用" : "启用"}
          </Button>
          <Popconfirm
            title="确定要删除此备份任务吗？"
            onConfirm={() => handleDelete(record.id)}
          >
            <Button type="link" size="small" danger>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <>
      <ProTable<BackupTask>
        columns={columns}
        actionRef={actionRef}
        rowKey="id"
        request={async () => {
          try {
            const data = await getBackupTasks();
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
            key="create"
            type="primary"
            icon={<PlusOutlined />}
            onClick={handleCreate}
          >
            新建任务
          </Button>,
        ]}
        search={false}
        pagination={{
          pageSize: 10,
        }}
        scroll={{ x: 1400 }}
      />

      <DrawerForm
        title={editingItem ? "编辑备份任务" : "新建备份任务"}
        open={drawerVisible}
        onClose={() => setDrawerVisible(false)}
        onFinish={handleSubmit}
        initialValues={
          editingItem || {
            backupType: "FULL",
            backupContent: "ALL",
            storageType: "LOCAL",
            status: "DISABLED",
            retentionDays: 30,
            maxBackups: 10,
          }
        }
        width={600}
      >
        <ProForm.Item
          name="name"
          label="任务名称"
          rules={[{ required: true, message: "请输入任务名称" }]}
        >
          <Input placeholder="如：每日数据库备份" />
        </ProForm.Item>
        <ProForm.Item name="description" label="任务描述">
          <Input.TextArea rows={2} placeholder="备份任务描述" />
        </ProForm.Item>
        <ProForm.Item
          name="backupType"
          label="备份类型"
          rules={[{ required: true, message: "请选择备份类型" }]}
        >
          <Select
            options={[
              { label: "全量备份", value: "FULL" },
              { label: "增量备份", value: "INCREMENTAL" },
              { label: "差异备份", value: "DIFFERENTIAL" },
            ]}
          />
        </ProForm.Item>
        <ProForm.Item
          name="backupContent"
          label="备份内容"
          rules={[{ required: true, message: "请选择备份内容" }]}
        >
          <Select
            options={[
              { label: "数据库", value: "DATABASE" },
              { label: "配置文件", value: "CONFIG" },
              { label: "日志文件", value: "LOGS" },
              { label: "全部数据", value: "ALL" },
            ]}
          />
        </ProForm.Item>
        <ProForm.Item
          name="cronExpression"
          label="备份周期"
          rules={[{ required: true, message: "请输入Cron表达式" }]}
          extra="Cron表达式，如：0 0 2 * * ? 表示每天凌晨2点执行"
        >
          <Input placeholder="0 0 2 * * ?" />
        </ProForm.Item>
        <ProForm.Item
          name="storageType"
          label="存储类型"
          rules={[{ required: true, message: "请选择存储类型" }]}
        >
          <Select
            options={[
              { label: "本地存储", value: "LOCAL" },
              { label: "对象存储(S3)", value: "S3" },
              { label: "NFS存储", value: "NFS" },
              { label: "FTP存储", value: "FTP" },
            ]}
          />
        </ProForm.Item>
        <ProForm.Item
          name="storagePath"
          label="存储路径"
          rules={[{ required: true, message: "请输入存储路径" }]}
        >
          <Input placeholder="/data/backups" />
        </ProForm.Item>
        <ProForm.Item
          name="retentionDays"
          label="保留天数"
          rules={[{ required: true, message: "请输入保留天数" }]}
        >
          <InputNumber min={1} style={{ width: "100%" }} addonAfter="天" />
        </ProForm.Item>
        <ProForm.Item
          name="maxBackups"
          label="最大保留份数"
          rules={[{ required: true, message: "请输入最大保留份数" }]}
        >
          <InputNumber min={1} style={{ width: "100%" }} addonAfter="份" />
        </ProForm.Item>
        {editingItem && (
          <ProForm.Item name="status" label="状态">
            <Select
              options={[
                { label: "已启用", value: "ENABLED" },
                { label: "已禁用", value: "DISABLED" },
              ]}
            />
          </ProForm.Item>
        )}
      </DrawerForm>
    </>
  );
};

export default BackupTaskList;
