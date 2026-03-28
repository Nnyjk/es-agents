import React, { useState, useEffect } from "react";
import {
  Modal,
  Form,
  Select,
  Input,
  Transfer,
  Progress,
  Tag,
  Table,
  Space,
  Button,
  message,
  Tabs,
  Spin,
} from "antd";
import {
  CodeOutlined,
  CloudServerOutlined,
  UploadOutlined,
} from "@ant-design/icons";
import {
  batchExecuteCommand,
  batchDeploy,
  batchUpgrade,
  getBatchOperation,
  getBatchOperationItems,
} from "@/services/batch";
import { queryHosts } from "@/services/infra";
import { queryAgentInstances } from "@/services/agent";
import type { Host, AgentInstance, ListResponse } from "@/types";
import type {
  BatchOperation,
  BatchOperationItem,
  BatchOperationType,
  BatchOperationStatus,
  BatchOperationItemStatus,
} from "@/types/batch";
import styles from "./BatchOperationModal.module.css";

const { TextArea } = Input;

interface TargetItem {
  key: string;
  title: string;
  disabled?: boolean;
}

interface BatchOperationModalProps {
  open: boolean;
  onCancel: () => void;
  onSuccess?: (operation: BatchOperation) => void;
}

const operationTypes: { value: BatchOperationType; label: string; icon: React.ReactNode }[] = [
  { value: "BATCH_COMMAND", label: "批量命令执行", icon: <CodeOutlined /> },
  { value: "BATCH_DEPLOY", label: "批量部署", icon: <CloudServerOutlined /> },
  { value: "BATCH_UPGRADE", label: "批量升级", icon: <UploadOutlined /> },
];

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

const BatchOperationModal: React.FC<BatchOperationModalProps> = ({
  open,
  onCancel,
  onSuccess,
}) => {
  const [form] = Form.useForm();
  const [operationType, setOperationType] = useState<BatchOperationType>("BATCH_COMMAND");
  const [hosts, setHosts] = useState<Host[]>([]);
  const [agents, setAgents] = useState<AgentInstance[]>([]);
  const [selectedTargets, setSelectedTargets] = useState<string[]>([]);
  const [loading, setLoading] = useState(false);
  const [currentOperation, setCurrentOperation] = useState<BatchOperation | null>(null);
  const [operationItems, setOperationItems] = useState<BatchOperationItem[]>([]);
  const [polling, setPolling] = useState(false);
  const [activeTab, setActiveTab] = useState<"config" | "progress">("config");

  // Load hosts and agents
  useEffect(() => {
    if (open) {
      queryHosts().then((res) => {
        const hostList = Array.isArray(res) ? res : (res as ListResponse<Host>).data || [];
        setHosts(hostList);
      });
      queryAgentInstances().then((res) => {
        const agentList = Array.isArray(res) ? res : (res as ListResponse<AgentInstance>).data || [];
        setAgents(agentList);
      });
    }
  }, [open]);

  // Poll for operation status
  useEffect(() => {
    if (!polling || !currentOperation) return;

    const interval = setInterval(async () => {
      try {
        const operation = await getBatchOperation(currentOperation.id);
        setCurrentOperation(operation);

        const items = await getBatchOperationItems(currentOperation.id);
        setOperationItems(items);

        if (operation.status !== "PENDING" && operation.status !== "RUNNING") {
          setPolling(false);
          clearInterval(interval);
          if (operation.status === "SUCCESS" || operation.status === "PARTIAL_SUCCESS") {
            message.success(`批量操作完成: ${operation.successCount} 成功, ${operation.failedCount} 失败`);
          } else {
            message.error("批量操作失败");
          }
        }
      } catch (error) {
        console.error("Poll operation status failed", error);
      }
    }, 2000);

    return () => clearInterval(interval);
  }, [polling, currentOperation]);

  const getTargetOptions = (): TargetItem[] => {
    if (operationType === "BATCH_COMMAND") {
      return hosts.map((h) => ({
        key: h.id,
        title: `${h.name} (${h.hostname})`,
        disabled: h.status !== "ONLINE",
      }));
    } else {
      return agents.map((a) => ({
        key: a.id,
        title: `${a.template?.name || a.id} - ${a.host?.name || "未知主机"}`,
        disabled: a.status !== "ONLINE",
      }));
    }
  };

  const handleOperationTypeChange = (value: BatchOperationType) => {
    setOperationType(value);
    setSelectedTargets([]);
    form.resetFields(["targets", "command", "version"]);
  };

  const handleExecute = async () => {
    try {
      await form.validateFields();
      setLoading(true);

      let operation: BatchOperation;

      if (operationType === "BATCH_COMMAND") {
        operation = await batchExecuteCommand({
          hostIds: selectedTargets,
          command: form.getFieldValue("command"),
        });
      } else if (operationType === "BATCH_DEPLOY") {
        operation = await batchDeploy({
          agentIds: selectedTargets,
        });
      } else {
        operation = await batchUpgrade({
          agentIds: selectedTargets,
          version: form.getFieldValue("version"),
        });
      }

      setCurrentOperation(operation);
      setPolling(true);
      setActiveTab("progress");

      // Fetch initial items
      const items = await getBatchOperationItems(operation.id);
      setOperationItems(items);

      message.success("批量操作已创建");
      onSuccess?.(operation);
    } catch (error: any) {
      message.error(error.message || "创建批量操作失败");
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    form.resetFields();
    setSelectedTargets([]);
    setCurrentOperation(null);
    setOperationItems([]);
    setPolling(false);
    setActiveTab("config");
    onCancel();
  };

  const getProgressPercent = () => {
    if (!currentOperation) return 0;
    const completed = currentOperation.successCount + currentOperation.failedCount;
    return Math.round((completed / currentOperation.totalItems) * 100);
  };

  const renderConfigTab = () => (
    <div className={styles.configContent}>
      <Form form={form} layout="vertical">
        <Form.Item
          name="operationType"
          label="操作类型"
          initialValue="BATCH_COMMAND"
        >
          <Select onChange={handleOperationTypeChange} value={operationType}>
            {operationTypes.map((t) => (
              <Select.Option key={t.value} value={t.value}>
                <Space>
                  {t.icon}
                  {t.label}
                </Space>
              </Select.Option>
            ))}
          </Select>
        </Form.Item>

        <Form.Item
          name="targets"
          label={
            operationType === "BATCH_COMMAND"
              ? "目标主机"
              : "目标 Agent"
          }
          rules={[{ required: true, message: "请选择目标" }]}
        >
          <Transfer
            dataSource={getTargetOptions()}
            titles={["可选", "已选"]}
            targetKeys={selectedTargets}
            onChange={(targetKeys) => setSelectedTargets(targetKeys as string[])}
            render={(item) => item.title}
            listStyle={{
              width: 250,
              height: 300,
            }}
            showSearch
            filterOption={(input, option) =>
              option.title.toLowerCase().includes(input.toLowerCase())
            }
          />
        </Form.Item>

        {operationType === "BATCH_COMMAND" && (
          <Form.Item
            name="command"
            label="命令内容"
            rules={[{ required: true, message: "请输入命令内容" }]}
          >
            <TextArea
              rows={4}
              placeholder="输入要执行的命令，如: ls -la"
            />
          </Form.Item>
        )}

        {operationType === "BATCH_UPGRADE" && (
          <Form.Item
            name="version"
            label="升级版本"
            rules={[{ required: true, message: "请输入升级版本" }]}
          >
            <Input placeholder="输入目标版本号，如: v2.0.0" />
          </Form.Item>
        )}
      </Form>
    </div>
  );

  const renderProgressTab = () => (
    <div className={styles.progressContent}>
      {currentOperation && (
        <div className={styles.progressHeader}>
          <div className={styles.progressInfo}>
            <span>操作状态: </span>
            <Tag color={statusColors[currentOperation.status]}>
              {statusLabels[currentOperation.status]}
            </Tag>
          </div>
          <div className={styles.progressStats}>
            <span>总数: {currentOperation.totalItems}</span>
            <span className={styles.successCount}>
              成功: {currentOperation.successCount}
            </span>
            <span className={styles.failedCount}>
              失败: {currentOperation.failedCount}
            </span>
          </div>
        </div>
      )}

      {polling && (
        <div className={styles.progressBar}>
          <Progress percent={getProgressPercent()} status="active" />
        </div>
      )}

      <Table
        dataSource={operationItems}
        rowKey="id"
        loading={polling && operationItems.length === 0}
        pagination={false}
        size="small"
        columns={[
          {
            title: "目标",
            dataIndex: "targetId",
            key: "targetId",
            width: 200,
          },
          {
            title: "类型",
            dataIndex: "targetType",
            key: "targetType",
            width: 80,
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
            title: "错误信息",
            dataIndex: "errorMessage",
            key: "errorMessage",
            ellipsis: true,
            render: (msg: string) => msg || "-",
          },
        ]}
      />
    </div>
  );

  return (
    <Modal
      title="批量操作"
      open={open}
      onCancel={handleClose}
      width={800}
      footer={[
        activeTab === "config" && (
          <Button key="cancel" onClick={handleClose}>
            取消
          </Button>
        ),
        activeTab === "config" && (
          <Button
            key="execute"
            type="primary"
            loading={loading}
            onClick={handleExecute}
            disabled={selectedTargets.length === 0}
          >
            执行
          </Button>
        ),
        activeTab === "progress" && polling && (
          <Button key="close" onClick={handleClose}>
            关闭
          </Button>
        ),
        activeTab === "progress" && !polling && (
          <Button key="close" type="primary" onClick={handleClose}>
            完成
          </Button>
        ),
      ]}
    >
      <Tabs
        activeKey={activeTab}
        onChange={(key) => setActiveTab(key as "config" | "progress")}
        items={[
          {
            key: "config",
            label: "配置",
            children: renderConfigTab(),
          },
          {
            key: "progress",
            label: (
              <Space>
                进度
                {polling && <Spin size="small" />}
              </Space>
            ),
            children: renderProgressTab(),
            disabled: !currentOperation,
          },
        ]}
      />
    </Modal>
  );
};

export default BatchOperationModal;