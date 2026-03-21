import React, { useRef, useState, useEffect } from "react";
import {
  PlusOutlined,
  SettingOutlined,
  CodeOutlined,
  LinkOutlined,
  ApiOutlined,
  EyeOutlined,
  DesktopOutlined,
} from "@ant-design/icons";
import type { ActionType, ProColumns } from "@ant-design/pro-components";
import { ProTable } from "@ant-design/pro-components";
import {
  Button,
  message,
  Popconfirm,
  Form,
  Input,
  Select,
  Drawer,
  InputNumber,
  Tag,
  Space,
  Modal,
  Descriptions,
  Typography,
  Badge,
} from "antd";
import Editor from "@monaco-editor/react";
import { DrawerForm } from "@/components/DrawerForm";
import { TerminalCommandModal } from "./components/TerminalCommandModal";
import { HostInstallGuideModal } from "./components/HostInstallGuideModal";
import {
  queryHosts,
  saveHost,
  removeHost,
  queryEnvironments,
  connectHost,
  getInstallGuide,
  downloadHostPackage,
} from "@/services/infra";
import type { Host, Environment, HostInstallGuide } from "@/types";
import { XTerm } from "xterm-for-react";
import { FitAddon } from "xterm-addon-fit";

const { Text } = Typography;

const HostList: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [drawerVisible, setDrawerVisible] = useState(false);
  const [configDrawerVisible, setConfigDrawerVisible] = useState(false);
  const [terminalVisible, setTerminalVisible] = useState(false);
  const [installGuideVisible, setInstallGuideVisible] = useState(false);
  const [cmdModalVisible, setCmdModalVisible] = useState(false);
  const [detailVisible, setDetailVisible] = useState(false);
  const [editingItem, setEditingItem] = useState<Partial<Host> | null>(null);
  const [environments, setEnvironments] = useState<Environment[]>([]);
  const [configContent, setConfigContent] = useState("");
  const [heartbeatInterval, setHeartbeatInterval] = useState<number>(30);
  const [currentHost, setCurrentHost] = useState<Host | null>(null);
  const [installGuide, setInstallGuide] = useState<HostInstallGuide | null>(
    null,
  );
  const [installGuideLoading, setInstallGuideLoading] = useState(false);
  const [downloading, setDownloading] = useState(false);
  const [installGuideError, setInstallGuideError] = useState<string | null>(
    null,
  );
  const [downloadError, setDownloadError] = useState<string | null>(null);
  const [connectError, setConnectError] = useState<string | null>(null);
  const [terminalSocket, setTerminalSocket] = useState<WebSocket | null>(null);
  const [connecting, setConnecting] = useState(false);

  const xtermRef = useRef<XTerm>(null);
  const fitAddonRef = useRef<FitAddon>(new FitAddon());

  // Load environments for selection
  useEffect(() => {
    queryEnvironments().then((res: any) => {
      // Handle both array and ListResponse
      const list = Array.isArray(res) ? res : res.data;
      setEnvironments(list);
    });
  }, []);

  // Fetch Install Guide when modal opens
  useEffect(() => {
    if (installGuideVisible && currentHost) {
      setInstallGuideLoading(true);
      setInstallGuideError(null);
      setDownloadError(null);
      setConnectError(null);
      getInstallGuide(currentHost.id)
        .then((data) => setInstallGuide(data))
        .catch((err) => {
          console.error(err);
          setInstallGuideError(
            err?.response?.data?.message ||
              err?.message ||
              "请检查主机绑定的 Agent 资源是否可用",
          );
          message.error("获取接入指南失败");
          setInstallGuide(null);
        })
        .finally(() => {
          setInstallGuideLoading(false);
        });
    } else {
      setInstallGuide(null);
      setInstallGuideLoading(false);
      setDownloading(false);
      setInstallGuideError(null);
      setDownloadError(null);
      setConnectError(null);
    }
  }, [installGuideVisible, currentHost]);

  const handleCloseInstallGuide = () => {
    setInstallGuideVisible(false);
    setDownloading(false);
  };

  // Handle Terminal Socket
  useEffect(() => {
    if (terminalVisible && currentHost) {
      // Connect to Console WebSocket
      // Assuming development env for now: localhost:8080. In production, use window.location.host
      const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
      const host = "localhost:8080"; // Should be dynamic
      const wsUrl = `${protocol}//${host}/ws/console/${currentHost.id}`;

      const ws = new WebSocket(wsUrl);
      let onDataDisposable: any = null;

      ws.onopen = () => {
        xtermRef.current?.terminal.writeln(
          "\r\n*** Connected to Agent Console ***\r\n",
        );
        // Fetch logs history
        ws.send(JSON.stringify({ type: "FETCH_LOGS" }));

        // Setup outgoing data handler once connected
        if (xtermRef.current) {
          onDataDisposable = xtermRef.current.terminal.onData((data) => {
            if (ws.readyState === WebSocket.OPEN) {
              // Send as INPUT type
              ws.send(
                JSON.stringify({
                  type: "INPUT",
                  content: { content: data },
                }),
              );
            }
          });
        }
      };
      ws.onmessage = (event) => {
        // Check if it's a JSON message (our protocol) or raw string
        try {
          const data = JSON.parse(event.data);
          if (data.type === "LOG") {
            xtermRef.current?.terminal.writeln(
              data.content || JSON.stringify(data),
            );
          } else if (data.type === "LOG_HISTORY") {
            if (Array.isArray(data.content)) {
              data.content.forEach((line: string) => {
                xtermRef.current?.terminal.writeln(line);
              });
            }
          } else if (data.type === "HEARTBEAT") {
            // Heartbeat received
          } else {
            xtermRef.current?.terminal.writeln(event.data);
          }
        } catch (e) {
          // Not JSON, just print
          xtermRef.current?.terminal.writeln(event.data);
        }
      };
      ws.onclose = () => {
        xtermRef.current?.terminal.writeln("\r\n*** Disconnected ***\r\n");
      };
      ws.onerror = (err) => {
        xtermRef.current?.terminal.writeln(`\r\n*** Connection Error ***\r\n`);
        console.error(err);
      };
      setTerminalSocket(ws);

      return () => {
        if (onDataDisposable) onDataDisposable.dispose();
        ws.close();
      };
    }
  }, [terminalVisible, currentHost]);

  // Fit terminal on resize/open
  useEffect(() => {
    if (terminalVisible && xtermRef.current) {
      // small delay to let drawer render
      setTimeout(() => {
        fitAddonRef.current.fit();
      }, 100);
    }
  }, [terminalVisible]);

  const handleExecuteCommand = (script: string) => {
    if (terminalSocket && terminalSocket.readyState === WebSocket.OPEN) {
      terminalSocket.send(
        JSON.stringify({
          type: "EXEC_CMD",
          content: { command: script },
        }),
      );
      xtermRef.current?.terminal.writeln(`\r\n> ${script}\r\n`);
    } else {
      message.error("终端未连接");
    }
  };

  const handleSave = async (data: any) => {
    try {
      await saveHost({ ...editingItem, ...data });
      message.success("保存成功");
      setDrawerVisible(false);
      setEditingItem(null);
      actionRef.current?.reload();
    } catch (error) {
      console.error(error);
      message.error("保存失败");
    }
  };

  const handleSaveConfig = async () => {
    if (!currentHost) return;
    try {
      await saveHost({
        ...currentHost,
        config: configContent,
        heartbeatInterval: heartbeatInterval,
      });
      message.success("配置保存成功");
      setConfigDrawerVisible(false);
      setCurrentHost(null);
      actionRef.current?.reload();
    } catch (error) {
      console.error(error);
      message.error("配置保存失败");
    }
  };

  const handleDelete = async (id: string) => {
    try {
      await removeHost(id);
      message.success("删除成功");
      actionRef.current?.reload();
    } catch (error) {
      console.error(error);
      message.error("删除失败");
    }
  };

  const handleDownloadPackage = async (host: Host) => {
    setDownloading(true);
    try {
      setDownloadError(null);
      const guide =
        installGuide && currentHost?.id === host.id
          ? installGuide
          : await getInstallGuide(host.id);
      // Download using GitHub Releases URL if available, otherwise use API endpoint
      await downloadHostPackage(
        guide.downloadUrl,
        guide.githubReleaseUrl,
        guide.packageFileName,
      );
      message.success(`开始下载 ${guide.packageFileName}`);
    } catch (error: any) {
      console.error(error);
      const errorText =
        error?.response?.data?.message ||
        error?.message ||
        "请检查服务端资源配置";
      setDownloadError(errorText);
      message.error(`下载失败：${errorText}`);
    } finally {
      setDownloading(false);
    }
  };

  const handleConnect = async (id: string) => {
    setConnecting(true);
    try {
      setConnectError(null);
      await connectHost(id);
      message.success("连接成功，Host 状态已更新为在线");
      actionRef.current?.reload();
      setInstallGuideVisible(false);
    } catch (error: any) {
      console.error(error);
      const errorText =
        error?.response?.data?.message ||
        error?.message ||
        "请检查网关地址和 Host Agent 状态";
      setConnectError(errorText);
      message.error(`连接失败: ${errorText}`);
    } finally {
      setConnecting(false);
    }
  };

  const columns: ProColumns<Host>[] = [
    { title: "主机名称", dataIndex: "name" },
    { title: "地址", dataIndex: "hostname" },
    { title: "系统", dataIndex: "os", hideInForm: true },
    {
      title: "Agent版本",
      dataIndex: "agentVersion",
      hideInSearch: true,
      hideInForm: true,
      render: (text: string) => (text ? <Tag color="blue">{text}</Tag> : "-"),
    },
    {
      title: "最后心跳",
      dataIndex: "lastHeartbeat",
      valueType: "dateTime",
      hideInForm: true,
    },
    {
      title: "所属环境",
      dataIndex: "environment",
      render: (_, record) =>
        record.environmentName || record.environment?.name || "-",
      hideInForm: true,
    },
    {
      title: "状态",
      dataIndex: "status",
      valueEnum: {
        UNCONNECTED: { text: "未接入", status: "Default" },
        OFFLINE: { text: "离线", status: "Error" },
        ONLINE: { text: "在线", status: "Success" },
        EXCEPTION: { text: "异常", status: "Error" },
        MAINTENANCE: { text: "维护中", status: "Warning" },
      },
    },
    {
      title: "操作",
      valueType: "option",
      width: 400,
      render: (_, record) => {
        const isOnline = record.status === "ONLINE";
        return [
          <Button
            key="detail"
            type="link"
            size="small"
            icon={<EyeOutlined />}
            onClick={() => {
              setCurrentHost(record);
              setDetailVisible(true);
            }}
          >
            详情
          </Button>,
          isOnline ? (
            <Button
              key="terminal"
              type="link"
              size="small"
              icon={<CodeOutlined />}
              onClick={() => {
                setCurrentHost(record);
                setTerminalVisible(true);
              }}
            >
              终端
            </Button>
          ) : (
            <Button
              key="access"
              type="link"
              size="small"
              icon={<LinkOutlined />}
              onClick={() => {
                setCurrentHost(record);
                setInstallGuideVisible(true);
              }}
            >
              接入
            </Button>
          ),
          <Button
            key="config"
            type="link"
            size="small"
            icon={<SettingOutlined />}
            onClick={() => {
              setCurrentHost(record);
              setConfigContent(record.config || "");
              setHeartbeatInterval(record.heartbeatInterval || 30);
              setConfigDrawerVisible(true);
            }}
          >
            配置
          </Button>,
          <Button
            key="edit"
            type="link"
            size="small"
            onClick={() => {
              setEditingItem({
                ...record,
                environmentId: record.environmentId || record.environment?.id,
              });
              setDrawerVisible(true);
            }}
          >
            编辑
          </Button>,
          <Popconfirm
            key="delete"
            title="确定删除?"
            onConfirm={() => handleDelete(record.id)}
          >
            <Button type="link" size="small" danger>
              删除
            </Button>
          </Popconfirm>,
        ];
      },
    },
  ];

  return (
    <>
      <ProTable<Host>
        headerTitle="主机列表"
        columns={columns}
        actionRef={actionRef}
        rowKey="id"
        request={async (params) => {
          const res = await queryHosts(params);
          const list = Array.isArray(res) ? res : res.data;
          return {
            data: list,
            success: true,
            total: Array.isArray(res) ? list.length : res.total,
          };
        }}
        toolBarRender={() => [
          <Button
            key="add"
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => {
              setEditingItem(null);
              setDrawerVisible(true);
            }}
          >
            新建主机
          </Button>,
        ]}
      />
      <DrawerForm
        visible={drawerVisible}
        title={editingItem?.id ? "编辑主机" : "新建主机"}
        onClose={() => {
          setDrawerVisible(false);
          setEditingItem(null);
        }}
        onSave={handleSave}
        initialValues={editingItem || undefined}
      >
        <Form.Item name="name" label="主机名称" rules={[{ required: true }]}>
          <Input placeholder="请输入主机名称" />
        </Form.Item>
        <Form.Item name="hostname" label="IP/域名" rules={[{ required: true }]}>
          <Input placeholder="请输入IP或域名" />
        </Form.Item>
        <Form.Item name="os" label="系统类型" tooltip="主机操作系统类型">
          <Select placeholder="请选择系统类型" allowClear>
            <Select.Option value="LINUX">Linux</Select.Option>
            <Select.Option value="LINUX_DOCKER">Linux (Docker)</Select.Option>
            <Select.Option value="WINDOWS">Windows</Select.Option>
            <Select.Option value="MACOS">macOS</Select.Option>
          </Select>
        </Form.Item>
        <Form.Item
          name="gatewayUrl"
          label="网关地址"
          tooltip="Server 连接此 Host Agent 的网关地址 (例如 http://192.168.1.100:9090 或通过中间件的地址)"
        >
          <Input placeholder="请输入网关地址 (Server 将主动连接此地址)" />
        </Form.Item>
        <Form.Item
          name="environmentId"
          label="所属环境"
          rules={[{ required: true }]}
        >
          <Select placeholder="请选择环境">
            {environments.map((env) => (
              <Select.Option key={env.id} value={env.id}>
                {env.name}
              </Select.Option>
            ))}
          </Select>
        </Form.Item>
        <Form.Item name="description" label="描述">
          <Input.TextArea placeholder="请输入描述" />
        </Form.Item>
      </DrawerForm>
      <Drawer
        title="Host Agent 配置"
        width={720}
        onClose={() => setConfigDrawerVisible(false)}
        open={configDrawerVisible}
        extra={
          <Button type="primary" onClick={handleSaveConfig}>
            保存配置
          </Button>
        }
      >
        <div style={{ marginBottom: 16 }}>
          <span style={{ marginRight: 8 }}>心跳间隔(秒):</span>
          <InputNumber
            min={5}
            max={3600}
            value={heartbeatInterval}
            onChange={(val) => setHeartbeatInterval(val || 30)}
          />
        </div>
        <div
          style={{ height: "calc(100vh - 150px)", border: "1px solid #d9d9d9" }}
        >
          <Editor
            height="100%"
            defaultLanguage="yaml"
            value={configContent}
            onChange={(value) => setConfigContent(value || "")}
            options={{
              minimap: { enabled: false },
              scrollBeyondLastLine: false,
              fontSize: 14,
            }}
          />
        </div>
      </Drawer>

      <HostInstallGuideModal
        visible={installGuideVisible}
        guide={installGuide}
        loading={installGuideLoading}
        guideError={installGuideError}
        downloadError={downloadError}
        connectError={connectError}
        connecting={connecting}
        downloading={downloading}
        onClose={handleCloseInstallGuide}
        onDownload={() => currentHost && handleDownloadPackage(currentHost)}
        onConnect={() => currentHost && handleConnect(currentHost.id)}
      />

      {/* Terminal Drawer */}
      <Drawer
        title={`终端 - ${currentHost?.name}`}
        width="80%"
        onClose={() => setTerminalVisible(false)}
        open={terminalVisible}
        extra={
          <Button
            icon={<ApiOutlined />}
            onClick={() => setCmdModalVisible(true)}
          >
            命令面板
          </Button>
        }
        bodyStyle={{ padding: 0, backgroundColor: "#000" }}
      >
        <div style={{ height: "100%", width: "100%" }}>
          <XTerm
            ref={xtermRef}
            addons={[fitAddonRef.current]}
            options={{ theme: { background: "#000" } }}
          />
        </div>
      </Drawer>

      <TerminalCommandModal
        visible={cmdModalVisible}
        onClose={() => setCmdModalVisible(false)}
        onExecute={handleExecuteCommand}
        hostOs={currentHost?.os}
      />

      {/* Host Detail Modal */}
      <Modal
        title={
          <Space>
            <DesktopOutlined />
            <span>{currentHost?.name} - 主机详情</span>
          </Space>
        }
        open={detailVisible}
        onCancel={() => setDetailVisible(false)}
        footer={null}
        width={700}
      >
        {currentHost && (
          <Descriptions column={2} bordered size="small">
            <Descriptions.Item label="主机名称">
              {currentHost.name}
            </Descriptions.Item>
            <Descriptions.Item label="地址">
              <Text copyable>{currentHost.hostname}</Text>
            </Descriptions.Item>
            <Descriptions.Item label="操作系统">
              {currentHost.os || "-"}
            </Descriptions.Item>
            <Descriptions.Item label="CPU">
              {currentHost.cpuInfo || "-"}
            </Descriptions.Item>
            <Descriptions.Item label="内存">
              {currentHost.memInfo || "-"}
            </Descriptions.Item>
            <Descriptions.Item label="Agent版本">
              {currentHost.agentVersion ? (
                <Tag color="blue">{currentHost.agentVersion}</Tag>
              ) : (
                "-"
              )}
            </Descriptions.Item>
            <Descriptions.Item label="所属环境">
              {currentHost.environmentName ||
                currentHost.environment?.name ||
                "-"}
            </Descriptions.Item>
            <Descriptions.Item label="状态">
              <Tag
                color={
                  currentHost.status === "ONLINE"
                    ? "success"
                    : currentHost.status === "OFFLINE"
                      ? "error"
                      : currentHost.status === "EXCEPTION"
                        ? "warning"
                        : "default"
                }
              >
                {currentHost.status === "ONLINE"
                  ? "在线"
                  : currentHost.status === "OFFLINE"
                    ? "离线"
                    : currentHost.status === "EXCEPTION"
                      ? "异常"
                      : currentHost.status === "MAINTENANCE"
                        ? "维护中"
                        : "未接入"}
              </Tag>
            </Descriptions.Item>
            <Descriptions.Item label="最后心跳">
              {currentHost.lastHeartbeat
                ? new Date(currentHost.lastHeartbeat).toLocaleString()
                : "-"}
            </Descriptions.Item>
            <Descriptions.Item label="心跳间隔">
              {currentHost.heartbeatInterval
                ? `${currentHost.heartbeatInterval}秒`
                : "-"}
            </Descriptions.Item>
            <Descriptions.Item label="网关地址">
              {currentHost.gatewayUrl || "-"}
            </Descriptions.Item>
            <Descriptions.Item label="监听端口">
              {currentHost.listenPort || "-"}
            </Descriptions.Item>
            <Descriptions.Item label="描述" span={2}>
              {currentHost.description || "-"}
            </Descriptions.Item>
            <Descriptions.Item label="创建时间" span={2}>
              {currentHost.createdAt
                ? new Date(currentHost.createdAt).toLocaleString()
                : "-"}
            </Descriptions.Item>
          </Descriptions>
        )}
      </Modal>
    </>
  );
};

export default HostList;
