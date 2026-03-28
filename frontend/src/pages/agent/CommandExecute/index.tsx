import React, { useState, useEffect, useRef } from "react";
import {
  Card,
  Form,
  Select,
  Input,
  Button,
  Space,
  message,
  Divider,
  Spin,
  Tag,
  Row,
  Col,
  Typography,
} from "antd";

const { Paragraph } = Typography;
import {
  PlayCircleOutlined,
  StopOutlined,
  ClearOutlined,
  CodeOutlined,
} from "@ant-design/icons";
import { PageContainer } from "@ant-design/pro-components";
import dayjs from "dayjs";
import { queryAgentInstances, queryAgentCommands } from "@/services/agent";
import {
  executeCommand,
  getCommandExecutionStatus,
  cancelCommandExecution,
} from "@/services/commandExecution";
import { queryHosts } from "@/services/infra";
import type { AgentInstance, AgentCommand, Host, ListResponse } from "@/types";
import type { CommandExecution, ExecutionStatus } from "@/types/command";

const { Option } = Select;
const { TextArea } = Input;

const statusConfig: Record<ExecutionStatus, { color: string; text: string }> = {
  PENDING: { color: "default", text: "等待中" },
  RUNNING: { color: "processing", text: "执行中" },
  SUCCESS: { color: "success", text: "成功" },
  FAILED: { color: "error", text: "失败" },
  CANCELLED: { color: "warning", text: "已取消" },
};

const CommandExecute: React.FC = () => {
  const [form] = Form.useForm();
  const [hosts, setHosts] = useState<Host[]>([]);
  const [instances, setInstances] = useState<AgentInstance[]>([]);
  const [commands, setCommands] = useState<AgentCommand[]>([]);
  const [selectedHostId, setSelectedHostId] = useState<string>();
  const [executing, setExecuting] = useState(false);
  const [currentExecution, setCurrentExecution] =
    useState<CommandExecution | null>(null);
  const [output, setOutput] = useState<string>("");
  const [pollingInterval, setPollingInterval] = useState<NodeJS.Timeout | null>(
    null,
  );
  const outputRef = useRef<HTMLPreElement>(null);

  // 加载主机列表
  useEffect(() => {
    queryHosts().then((res) => {
      const hostList = Array.isArray(res)
        ? res
        : (res as ListResponse<Host>).data || [];
      setHosts(hostList);
    });
    queryAgentCommands().then((res) => {
      const cmdList = Array.isArray(res)
        ? res
        : (res as ListResponse<AgentCommand>).data || [];
      setCommands(cmdList);
    });
  }, []);

  // 加载 Agent 实例
  useEffect(() => {
    if (selectedHostId) {
      queryAgentInstances({ hostId: selectedHostId }).then((res) => {
        const instanceList = Array.isArray(res)
          ? res
          : (res as ListResponse<AgentInstance>).data || [];
        setInstances(instanceList);
      });
    } else {
      setInstances([]);
    }
  }, [selectedHostId]);

  // 清理轮询
  useEffect(() => {
    return () => {
      if (pollingInterval) {
        clearInterval(pollingInterval);
      }
    };
  }, [pollingInterval]);

  // 滚动到输出底部
  useEffect(() => {
    if (outputRef.current) {
      outputRef.current.scrollTop = outputRef.current.scrollHeight;
    }
  }, [output]);

  const handleExecute = async () => {
    try {
      const values = await form.validateFields();
      setExecuting(true);
      setOutput(`[${dayjs().format("HH:mm:ss")}] 正在执行命令...\n`);
      setCurrentExecution(null);

      // 查找选中的命令模板
      const selectedCommand = commands.find((c) => c.id === values.commandId);

      const result = await executeCommand({
        agentInstanceId: values.agentInstanceId,
        templateId: selectedCommand?.templateId,
        command: selectedCommand?.script || "",
        parameters: values.args ? { args: values.args } : undefined,
        timeout: selectedCommand?.timeout,
      });

      setOutput(
        (prev) =>
          `${prev}[${dayjs().format("HH:mm:ss")}] 任务已创建: ${result.executionId}\n`,
      );

      // 开始轮询执行状态
      const interval = setInterval(async () => {
        try {
          const execution = await getCommandExecutionStatus(result.executionId);
          setCurrentExecution(execution);

          if (execution.status === "RUNNING") {
            setOutput(
              (prev) =>
                `${prev}[${dayjs().format("HH:mm:ss")}] 命令执行中...\n`,
            );
          } else if (execution.status === "SUCCESS") {
            const duration =
              execution.startedAt && execution.finishedAt
                ? dayjs(execution.finishedAt).diff(
                    dayjs(execution.startedAt),
                    "ms",
                  )
                : 0;
            setOutput(
              (prev) =>
                `${prev}[${dayjs().format("HH:mm:ss")}] 执行成功 (${duration}ms)\n`,
            );
            if (execution.output) {
              setOutput(
                (prev) => `${prev}\n--- 输出结果 ---\n${execution.output}\n`,
              );
            }
            clearInterval(interval);
            setPollingInterval(null);
            setExecuting(false);
          } else if (execution.status === "FAILED") {
            setOutput(
              (prev) => `${prev}[${dayjs().format("HH:mm:ss")}] 执行失败\n`,
            );
            if (execution.errorMessage) {
              setOutput(
                (prev) =>
                  `${prev}\n--- 错误信息 ---\n${execution.errorMessage}\n`,
              );
            }
            clearInterval(interval);
            setPollingInterval(null);
            setExecuting(false);
          } else if (execution.status === "CANCELLED") {
            setOutput(
              (prev) => `${prev}[${dayjs().format("HH:mm:ss")}] 已取消\n`,
            );
            clearInterval(interval);
            setPollingInterval(null);
            setExecuting(false);
          }
        } catch (error) {
          console.error("轮询执行状态失败", error);
        }
      }, 2000);

      setPollingInterval(interval);
    } catch (error: any) {
      message.error(error.message || "执行失败");
      setExecuting(false);
    }
  };

  const handleCancel = async () => {
    if (currentExecution && pollingInterval) {
      try {
        await cancelCommandExecution(currentExecution.id);
        clearInterval(pollingInterval);
        setPollingInterval(null);
      } catch (error) {
        console.error("取消执行失败", error);
      }
    }
    setExecuting(false);
    setOutput((prev) => `${prev}[${dayjs().format("HH:mm:ss")}] 已取消\n`);
  };

  const handleClear = () => {
    setOutput("");
    setCurrentExecution(null);
  };

  const getStatusTag = (status?: ExecutionStatus) => {
    if (!status) return null;
    const config = statusConfig[status] || {
      color: "default",
      text: status,
    };
    return <Tag color={config.color}>{config.text}</Tag>;
  };

  return (
    <PageContainer>
      <Row gutter={16}>
        <Col span={8}>
          <Card title="命令执行" bordered={false}>
            <Form form={form} layout="vertical">
              <Form.Item
                name="hostId"
                label="目标主机"
                rules={[{ required: true, message: "请选择目标主机" }]}
              >
                <Select
                  placeholder="选择主机"
                  showSearch
                  optionFilterProp="children"
                  onChange={(value) => {
                    setSelectedHostId(value);
                    form.setFieldsValue({ agentInstanceId: undefined });
                  }}
                >
                  {hosts.map((h) => (
                    <Option key={h.id} value={h.id}>
                      {h.name} ({h.hostname})
                    </Option>
                  ))}
                </Select>
              </Form.Item>

              <Form.Item
                name="agentInstanceId"
                label="Agent 实例"
                rules={[{ required: true, message: "请选择 Agent 实例" }]}
              >
                <Select
                  placeholder={
                    selectedHostId ? "选择 Agent 实例" : "请先选择主机"
                  }
                  disabled={!selectedHostId}
                  showSearch
                  optionFilterProp="children"
                >
                  {instances.map((i) => (
                    <Option key={i.id} value={i.id}>
                      {i.template?.name || i.id} - {i.status}
                    </Option>
                  ))}
                </Select>
              </Form.Item>

              <Form.Item
                name="commandId"
                label="命令模板"
                rules={[{ required: true, message: "请选择命令模板" }]}
              >
                <Select
                  placeholder="选择命令模板"
                  showSearch
                  optionFilterProp="children"
                >
                  {commands.map((c) => (
                    <Option key={c.id} value={c.id}>
                      {c.name}
                    </Option>
                  ))}
                </Select>
              </Form.Item>

              <Form.Item name="args" label="命令参数">
                <TextArea rows={3} placeholder="输入命令参数（可选）" />
              </Form.Item>

              <Form.Item>
                <Space>
                  <Button
                    type="primary"
                    icon={<PlayCircleOutlined />}
                    onClick={handleExecute}
                    loading={executing}
                    disabled={executing}
                  >
                    执行命令
                  </Button>
                  {executing && (
                    <Button
                      icon={<StopOutlined />}
                      onClick={handleCancel}
                      danger
                    >
                      取消
                    </Button>
                  )}
                </Space>
              </Form.Item>
            </Form>
          </Card>
        </Col>

        <Col span={16}>
          <Card
            title={
              <Space>
                <CodeOutlined />
                <span>执行输出</span>
                {currentExecution && getStatusTag(currentExecution.status)}
              </Space>
            }
            extra={
              <Button icon={<ClearOutlined />} onClick={handleClear}>
                清空
              </Button>
            }
            bordered={false}
          >
            <div
              style={{
                background: "#1e1e1e",
                color: "#d4d4d4",
                padding: 16,
                borderRadius: 4,
                minHeight: 400,
                maxHeight: 600,
                overflow: "auto",
                fontFamily: "Consolas, Monaco, monospace",
                fontSize: 13,
              }}
            >
              {executing && (
                <div style={{ marginBottom: 8 }}>
                  <Spin size="small" /> 执行中...
                </div>
              )}
              <pre
                ref={outputRef}
                style={{
                  margin: 0,
                  whiteSpace: "pre-wrap",
                  wordBreak: "break-all",
                }}
              >
                {output || "等待执行..."}
              </pre>
            </div>

            {currentExecution?.output && (
              <>
                <Divider>执行结果</Divider>
                <Paragraph>
                  <pre
                    style={{
                      background: "#f5f5f5",
                      padding: 12,
                      borderRadius: 4,
                    }}
                  >
                    {currentExecution.output}
                  </pre>
                </Paragraph>
              </>
            )}
          </Card>
        </Col>
      </Row>
    </PageContainer>
  );
};

export default CommandExecute;
