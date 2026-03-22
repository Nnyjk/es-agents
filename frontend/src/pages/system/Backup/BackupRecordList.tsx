import React, { useRef, useState } from "react";
import { DownloadOutlined, DeleteOutlined, SafetyOutlined, ReloadOutlined } from "@ant-design/icons";
import type { ActionType, ProColumns } from "@ant-design/pro-components";
import { ProTable } from "@ant-design/pro-components";
import {
  Button,
  message,
  Popconfirm,
  Tag,
  Space,
  Tooltip,
  Modal,
  Descriptions,
} from "antd";
import {
  getBackupRecords,
  deleteBackupRecord,
  validateBackupRecord,
  downloadBackupRecord,
} from "../../../services/backup";
import type { BackupRecord, BackupRecordStatus, BackupType, BackupContent } from "../../../types/backup";

const statusColors: Record<BackupRecordStatus, string> = {
  RUNNING: "processing",
  SUCCESS: "success",
  FAILED: "error",
  VALIDATING: "warning",
  INVALID: "error",
};

const statusTexts: Record<BackupRecordStatus, string> = {
  RUNNING: "运行中",
  SUCCESS: "成功",
  FAILED: "失败",
  VALIDATING: "校验中",
  INVALID: "已损坏",
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

const BackupRecordList: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [detailVisible, setDetailVisible] = useState(false);
  const [currentRecord, setCurrentRecord] = useState<BackupRecord | null>(null);
  const [validating, setValidating] = useState(false);

  const handleDelete = async (id: number) => {
    try {
      await deleteBackupRecord(id);
      message.success("删除成功");
      actionRef.current?.reload();
    } catch (error) {
      message.error("删除失败");
    }
  };

  const handleValidate = async (id: number) => {
    setValidating(true);
    try {
      const result = await validateBackupRecord(id);
      if (result.valid) {
        message.success("校验通过：备份文件完整有效");
      } else {
        message.warning(`校验失败：${result.message}`);
      }
      actionRef.current?.reload();
    } catch (error) {
      message.error("校验失败");
    } finally {
      setValidating(false);
    }
  };

  const handleDownload = (id: number, name: string) => {
    const url = downloadBackupRecord(id);
    const link = document.createElement("a");
    link.href = url;
    link.download = name;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    message.success("开始下载");
  };

  const showDetail = (record: BackupRecord) => {
    setCurrentRecord(record);
    setDetailVisible(true);
  };

  const columns: ProColumns<BackupRecord>[] = [
    {
      title: "备份名称",
      dataIndex: "backupName",
      key: "backupName",
      width: 200,
      ellipsis: true,
      render: (_, record) => (
        <a onClick={() => showDetail(record)}>{record.backupName}</a>
      ),
    },
    {
      title: "关联任务",
      dataIndex: "taskName",
      key: "taskName",
      width: 150,
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
      title: "文件大小",
      dataIndex: "fileSizeFormatted",
      key: "fileSize",
      width: 100,
      sorter: (a, b) => a.fileSize - b.fileSize,
    },
    {
      title: "备份时间",
      dataIndex: "startTime",
      key: "startTime",
      width: 180,
      sorter: true,
    },
    {
      title: "耗时",
      dataIndex: "duration",
      key: "duration",
      width: 80,
      render: (_, record) => {
        const seconds = record.duration;
        if (seconds < 60) return `${seconds}秒`;
        const minutes = Math.floor(seconds / 60);
        const secs = seconds % 60;
        return `${minutes}分${secs}秒`;
      },
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
      title: "操作",
      key: "action",
      width: 180,
      fixed: "right",
      render: (_, record) => (
        <Space size="small">
          <Tooltip title="下载备份文件">
            <Button
              type="link"
              size="small"
              icon={<DownloadOutlined />}
              onClick={() => handleDownload(record.id, record.backupName)}
              disabled={record.status !== "SUCCESS"}
            >
              下载
            </Button>
          </Tooltip>
          <Tooltip title="校验备份完整性">
            <Button
              type="link"
              size="small"
              icon={<SafetyOutlined />}
              onClick={() => handleValidate(record.id)}
              loading={validating}
              disabled={record.status !== "SUCCESS"}
            >
              校验
            </Button>
          </Tooltip>
          <Popconfirm
            title="确定要删除此备份记录吗？删除后无法恢复。"
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
      <ProTable<BackupRecord>
        columns={columns}
        actionRef={actionRef}
        rowKey="id"
        request={async () => {
          try {
            const data = await getBackupRecords();
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
        title="备份详情"
        open={detailVisible}
        onCancel={() => setDetailVisible(false)}
        footer={null}
        width={700}
      >
        {currentRecord && (
          <Descriptions column={2} bordered size="small">
            <Descriptions.Item label="备份名称" span={2}>
              {currentRecord.backupName}
            </Descriptions.Item>
            <Descriptions.Item label="关联任务">
              {currentRecord.taskName}
            </Descriptions.Item>
            <Descriptions.Item label="备份类型">
              {backupTypeTexts[currentRecord.backupType]}
            </Descriptions.Item>
            <Descriptions.Item label="备份内容">
              {backupContentTexts[currentRecord.backupContent]}
            </Descriptions.Item>
            <Descriptions.Item label="存储类型">
              {currentRecord.storageType}
            </Descriptions.Item>
            <Descriptions.Item label="文件大小">
              {currentRecord.fileSizeFormatted}
            </Descriptions.Item>
            <Descriptions.Item label="存储路径" span={2}>
              {currentRecord.storagePath}
            </Descriptions.Item>
            <Descriptions.Item label="备份状态">
              <Tag color={statusColors[currentRecord.status]}>
                {statusTexts[currentRecord.status]}
              </Tag>
            </Descriptions.Item>
            <Descriptions.Item label="操作人">
              {currentRecord.operator}
            </Descriptions.Item>
            <Descriptions.Item label="开始时间">
              {currentRecord.startTime}
            </Descriptions.Item>
            <Descriptions.Item label="结束时间">
              {currentRecord.endTime || "-"}
            </Descriptions.Item>
            <Descriptions.Item label="耗时">
              {currentRecord.duration}秒
            </Descriptions.Item>
            <Descriptions.Item label="校验和">
              {currentRecord.checksum || "-"}
            </Descriptions.Item>
            {currentRecord.errorMessage && (
              <Descriptions.Item label="错误信息" span={2}>
                <span style={{ color: "#ff4d4f" }}>{currentRecord.errorMessage}</span>
              </Descriptions.Item>
            )}
          </Descriptions>
        )}
      </Modal>
    </>
  );
};

export default BackupRecordList;