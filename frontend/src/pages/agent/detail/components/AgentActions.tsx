import React, { useState } from "react";
import {
  Card,
  Button,
  Space,
  Popconfirm,
  Modal,
  Form,
  Input,
  message,
  Tooltip,
} from "antd";
import {
  RedoOutlined,
  StopOutlined,
  DeleteOutlined,
  CloudUploadOutlined,
  ExclamationCircleOutlined,
} from "@ant-design/icons";
import type { AgentActionType } from "../types";
import { AgentStatusConfig } from "../types";
import type { AgentStatus } from "@/types/agentMonitoring";
import styles from "../AgentDetail.module.css";

interface AgentActionsProps {
  agentId: string;
  status: AgentStatus;
  onActionComplete?: (
    action: AgentActionType,
    result: { success: boolean; message?: string; taskId?: string },
  ) => void;
}

/**
 * 根据状态判断操作是否可用
 */
const getActionAvailability = (status: AgentStatus) => {
  return {
    DEPLOY: {
      enabled:
        status === "UNCONFIGURED" || status === "OFFLINE" || status === "ERROR",
      disabledReason:
        status === "DEPLOYING"
          ? "正在部署中"
          : status === "ONLINE"
            ? "Agent 已在线"
            : undefined,
    },
    RESTART: {
      enabled: status === "ONLINE",
      disabledReason: status !== "ONLINE" ? "Agent 需在线才能重启" : undefined,
    },
    STOP: {
      enabled: status === "ONLINE",
      disabledReason: status !== "ONLINE" ? "Agent 需在线才能停止" : undefined,
    },
    DELETE: {
      enabled: true,
      disabledReason: undefined,
    },
  };
};

/**
 * Agent 快捷操作组件
 * 提供部署、重启、停止、删除等操作按钮
 */
const AgentActions: React.FC<AgentActionsProps> = ({
  agentId,
  status,
  onActionComplete,
}) => {
  const [deployModalVisible, setDeployModalVisible] = useState(false);
  const [deployForm] = Form.useForm();
  const [loading, setLoading] = useState<string | null>(null);

  const availability = getActionAvailability(status);

  // 执行部署
  const handleDeploy = async (values: {
    version: string;
    remarks?: string;
  }) => {
    setLoading("DEPLOY");
    try {
      const response = await fetch(`/api/agents/instances/${agentId}/deploy`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(values),
      });
      const result = await response.json();
      if (response.ok) {
        message.success("部署任务已创建");
        setDeployModalVisible(false);
        deployForm.resetFields();
        onActionComplete?.("DEPLOY", { success: true, taskId: result.taskId });
      } else {
        message.error(result.message || "部署失败");
        onActionComplete?.("DEPLOY", {
          success: false,
          message: result.message,
        });
      }
    } catch (e) {
      const errorMessage = e instanceof Error ? e.message : "部署请求失败";
      message.error(errorMessage);
      onActionComplete?.("DEPLOY", { success: false, message: errorMessage });
    } finally {
      setLoading(null);
    }
  };

  // 执行重启
  const handleRestart = async () => {
    setLoading("RESTART");
    try {
      const response = await fetch(`/api/agents/instances/${agentId}/restart`, {
        method: "POST",
      });
      if (response.ok) {
        message.success("重启命令已发送");
        onActionComplete?.("RESTART", { success: true });
      } else {
        const result = await response.json();
        message.error(result.message || "重启失败");
        onActionComplete?.("RESTART", {
          success: false,
          message: result.message,
        });
      }
    } catch (e) {
      const errorMessage = e instanceof Error ? e.message : "重启请求失败";
      message.error(errorMessage);
      onActionComplete?.("RESTART", { success: false, message: errorMessage });
    } finally {
      setLoading(null);
    }
  };

  // 执行停止
  const handleStop = async () => {
    setLoading("STOP");
    try {
      const response = await fetch(`/api/agents/instances/${agentId}/stop`, {
        method: "POST",
      });
      if (response.ok) {
        message.success("停止命令已发送");
        onActionComplete?.("STOP", { success: true });
      } else {
        const result = await response.json();
        message.error(result.message || "停止失败");
        onActionComplete?.("STOP", { success: false, message: result.message });
      }
    } catch (e) {
      const errorMessage = e instanceof Error ? e.message : "停止请求失败";
      message.error(errorMessage);
      onActionComplete?.("STOP", { success: false, message: errorMessage });
    } finally {
      setLoading(null);
    }
  };

  // 执行删除
  const handleDelete = async () => {
    setLoading("DELETE");
    try {
      const response = await fetch(`/api/agents/instances/${agentId}`, {
        method: "DELETE",
      });
      if (response.ok) {
        message.success("Agent 已删除");
        onActionComplete?.("DELETE", { success: true });
      } else {
        const result = await response.json();
        message.error(result.message || "删除失败");
        onActionComplete?.("DELETE", {
          success: false,
          message: result.message,
        });
      }
    } catch (e) {
      const errorMessage = e instanceof Error ? e.message : "删除请求失败";
      message.error(errorMessage);
      onActionComplete?.("DELETE", { success: false, message: errorMessage });
    } finally {
      setLoading(null);
    }
  };

  const statusConfig = AgentStatusConfig[status];

  return (
    <Card className={styles.actionsCard} title="快捷操作">
      <div style={{ marginBottom: 12 }}>
        <Space>
          <span>当前状态：</span>
          <Tooltip title={statusConfig?.description}>
            <span>{statusConfig?.text || status}</span>
          </Tooltip>
        </Space>
      </div>

      <Space className={styles.actionsButtons} wrap>
        {/* 部署按钮 */}
        <Tooltip title={availability.DEPLOY.disabledReason}>
          <Button
            type="primary"
            icon={<CloudUploadOutlined />}
            disabled={!availability.DEPLOY.enabled}
            loading={loading === "DEPLOY"}
            onClick={() => setDeployModalVisible(true)}
          >
            部署
          </Button>
        </Tooltip>

        {/* 重启按钮 */}
        <Tooltip title={availability.RESTART.disabledReason}>
          <Button
            icon={<RedoOutlined />}
            disabled={!availability.RESTART.enabled}
            loading={loading === "RESTART"}
            onClick={() => {
              Modal.confirm({
                title: "确认重启？",
                icon: <ExclamationCircleOutlined />,
                content: "重启操作将重新启动 Agent 服务",
                okText: "确认",
                cancelText: "取消",
                onOk: handleRestart,
              });
            }}
          >
            重启
          </Button>
        </Tooltip>

        {/* 停止按钮 */}
        <Tooltip title={availability.STOP.disabledReason}>
          <Button
            icon={<StopOutlined />}
            disabled={!availability.STOP.enabled}
            loading={loading === "STOP"}
            onClick={() => {
              Modal.confirm({
                title: "确认停止？",
                icon: <ExclamationCircleOutlined />,
                content: "停止操作将停止 Agent 服务",
                okText: "确认",
                cancelText: "取消",
                onOk: handleStop,
              });
            }}
          >
            停止
          </Button>
        </Tooltip>

        {/* 删除按钮 */}
        <Popconfirm
          title="确认删除该 Agent？"
          description="此操作不可恢复"
          onConfirm={handleDelete}
          okText="确认"
          cancelText="取消"
          okButtonProps={{ danger: true }}
        >
          <Button
            danger
            icon={<DeleteOutlined />}
            loading={loading === "DELETE"}
          >
            删除
          </Button>
        </Popconfirm>
      </Space>

      {/* 部署参数模态框 */}
      <Modal
        title="部署 Agent"
        open={deployModalVisible}
        onCancel={() => {
          setDeployModalVisible(false);
          deployForm.resetFields();
        }}
        footer={null}
      >
        <Form
          form={deployForm}
          layout="vertical"
          onFinish={handleDeploy}
          initialValues={{ version: "" }}
        >
          <Form.Item
            name="version"
            label="版本号"
            rules={[{ required: true, message: "请输入版本号" }]}
          >
            <Input placeholder="如: 1.0.0" />
          </Form.Item>
          <Form.Item name="remarks" label="备注">
            <Input.TextArea rows={3} placeholder="部署说明（可选）" />
          </Form.Item>
          <Form.Item style={{ marginBottom: 0 }}>
            <Space>
              <Button
                type="primary"
                htmlType="submit"
                loading={loading === "DEPLOY"}
              >
                开始部署
              </Button>
              <Button onClick={() => setDeployModalVisible(false)}>取消</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </Card>
  );
};

export default AgentActions;
