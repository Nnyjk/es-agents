import React, { useRef, useState, useEffect } from 'react';
import { PlusOutlined, DownloadOutlined, SettingOutlined, CodeOutlined, LinkOutlined, ApiOutlined } from '@ant-design/icons';
import type { ActionType, ProColumns } from '@ant-design/pro-components';
import { ProTable } from '@ant-design/pro-components';
import { Button, message, Popconfirm, Form, Input, Select, Drawer, InputNumber, Modal } from 'antd';
import Editor from '@monaco-editor/react';
import { DrawerForm } from '@/components/DrawerForm';
import { TerminalCommandModal } from './components/TerminalCommandModal';
import { queryHosts, saveHost, removeHost, queryEnvironments, connectHost } from '@/services/infra';
import type { Host, Environment } from '@/types';
import { XTerm } from 'xterm-for-react';
import { FitAddon } from 'xterm-addon-fit';

const HostList: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [drawerVisible, setDrawerVisible] = useState(false);
  const [configDrawerVisible, setConfigDrawerVisible] = useState(false);
  const [terminalVisible, setTerminalVisible] = useState(false);
  const [installGuideVisible, setInstallGuideVisible] = useState(false);
  const [cmdModalVisible, setCmdModalVisible] = useState(false);
  const [editingItem, setEditingItem] = useState<Partial<Host> | null>(null);
  const [environments, setEnvironments] = useState<Environment[]>([]);
  const [configContent, setConfigContent] = useState('');
  const [heartbeatInterval, setHeartbeatInterval] = useState<number>(30);
  const [currentHost, setCurrentHost] = useState<Host | null>(null);
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

  // Handle Terminal Socket
  useEffect(() => {
    if (terminalVisible && currentHost) {
        // Connect to Console WebSocket
        // Assuming development env for now: localhost:8080. In production, use window.location.host
        const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        const host = 'localhost:8080'; // Should be dynamic
        const wsUrl = `${protocol}//${host}/ws/console/${currentHost.id}`;
        
        const ws = new WebSocket(wsUrl);
        let onDataDisposable: any = null;

        ws.onopen = () => {
            xtermRef.current?.terminal.writeln('\r\n*** Connected to Agent Console ***\r\n');
            // Fetch logs history
            ws.send(JSON.stringify({ type: 'FETCH_LOGS' }));
            
            // Setup outgoing data handler once connected
            if (xtermRef.current) {
                onDataDisposable = xtermRef.current.terminal.onData(data => {
                    if (ws.readyState === WebSocket.OPEN) {
                        // Send as INPUT type
                        ws.send(JSON.stringify({
                            type: 'INPUT',
                            content: { content: data }
                        }));
                    }
                });
            }
        };
        ws.onmessage = (event) => {
            // Check if it's a JSON message (our protocol) or raw string
            try {
                const data = JSON.parse(event.data);
                if (data.type === 'LOG') {
                     xtermRef.current?.terminal.writeln(data.content || JSON.stringify(data));
                } else if (data.type === 'LOG_HISTORY') {
                     if (Array.isArray(data.content)) {
                         data.content.forEach((line: string) => {
                              xtermRef.current?.terminal.writeln(line);
                         });
                     }
                } else if (data.type === 'HEARTBEAT') {
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
            xtermRef.current?.terminal.writeln('\r\n*** Disconnected ***\r\n');
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
        terminalSocket.send(JSON.stringify({
            type: 'EXEC_CMD',
            content: { command: script }
        }));
        xtermRef.current?.terminal.writeln(`\r\n> ${script}\r\n`);
    } else {
        message.error('终端未连接');
    }
  };

  const handleSave = async (data: any) => {
    try {
      await saveHost({ ...editingItem, ...data });
      message.success('保存成功');
      setDrawerVisible(false);
      setEditingItem(null);
      actionRef.current?.reload();
    } catch (error) {
      console.error(error);
      message.error('保存失败');
    }
  };

  const handleSaveConfig = async () => {
    if (!currentHost) return;
    try {
      await saveHost({ 
        ...currentHost, 
        config: configContent,
        heartbeatInterval: heartbeatInterval
      });
      message.success('配置保存成功');
      setConfigDrawerVisible(false);
      setCurrentHost(null);
      actionRef.current?.reload();
    } catch (error) {
      console.error(error);
      message.error('配置保存失败');
    }
  };

  const handleDelete = async (id: string) => {
    try {
      await removeHost(id);
      message.success('删除成功');
      actionRef.current?.reload();
    } catch (error) {
      console.error(error);
      message.error('删除失败');
    }
  };

  const handleDownloadPackage = async (host: Host) => {
    try {
        const os = host.os?.toUpperCase();
        let assetName = '';
        
        // Determine package name based on OS
        if (os === 'LINUX' || os === 'LINUX_DOCKER') {
            assetName = 'host-agent-linux.tar.gz';
        } else if (os === 'WINDOWS') {
            assetName = 'host-agent-windows.tar.gz';
        } else if (os === 'MACOS') {
            assetName = 'host-agent-macos.tar.gz';
        } else {
            message.error(`不支持的操作系统：${host.os}`);
            return;
        }

        // Fetch latest release from GitHub Releases API
        const repoOwner = 'Nnyjk';
        const repoName = 'es-agents';
        const releaseUrl = `https://api.github.com/repos/${repoOwner}/${repoName}/releases/latest`;
        
        const response = await fetch(releaseUrl);
        if (!response.ok) {
            throw new Error('Failed to fetch latest release from GitHub');
        }
        
        const release = await response.json();
        
        // Find the matching asset
        const asset = release.assets?.find((a: any) => a.name === assetName);
        
        if (asset && asset.browser_download_url) {
            // Trigger download
            const link = document.createElement('a');
            link.href = asset.browser_download_url;
            link.target = '_blank';
            link.download = assetName;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            message.success(`开始下载 ${assetName}`);
        } else {
            message.error(`未找到对应的 Host Agent 安装包 (${assetName})`);
            console.warn('Available assets:', release.assets?.map((a: any) => a.name));
        }
    } catch (error: any) {
        console.error(error);
        message.error(`下载失败：${error.message || '请检查网络连接'}`);
    }
  };

  const handleConnect = async (id: string) => {
    setConnecting(true);
    try {
        await connectHost(id);
        message.success('连接成功，Host 状态已更新为在线');
        actionRef.current?.reload();
        if (installGuideVisible) setInstallGuideVisible(false);
    } catch (error: any) {
        console.error(error);
        message.error(`连接失败: ${error.message || '请检查网关地址和 Host Agent 状态'}`);
    } finally {
        setConnecting(false);
    }
  };

  const columns: ProColumns<Host>[] = [
    { title: '主机名称', dataIndex: 'name' },
    { title: '地址', dataIndex: 'hostname' },
    { title: '系统', dataIndex: 'os', hideInForm: true },
    { 
      title: '所属环境', 
      dataIndex: 'environment',
      render: (_, record) => record.environmentName || record.environment?.name || '-',
      hideInForm: true
    },
    { 
      title: '状态', 
      dataIndex: 'status',
      valueEnum: {
        UNCONNECTED: { text: '未接入', status: 'Default' },
        OFFLINE: { text: '离线', status: 'Error' },
        ONLINE: { text: '在线', status: 'Success' },
        EXCEPTION: { text: '异常', status: 'Error' },
        MAINTENANCE: { text: '维护中', status: 'Warning' },
      }
    },
    {
        title: '操作',
        valueType: 'option',
        width: 350,
        render: (_, record) => {
            const isOnline = record.status === 'ONLINE';
            return [
                isOnline ? (
                    <a key="terminal" onClick={() => {
                        setCurrentHost(record);
                        setTerminalVisible(true);
                    }}>
                        <CodeOutlined /> 终端
                    </a>
                ) : (
                    <a key="access" onClick={() => {
                        setCurrentHost(record);
                        setInstallGuideVisible(true);
                    }}>
                        <LinkOutlined /> 接入
                    </a>
                ),
                <a key="config" onClick={() => {
                  setCurrentHost(record);
                  setConfigContent(record.config || '');
                  setHeartbeatInterval(record.heartbeatInterval || 30);
                  setConfigDrawerVisible(true);
                }}>
                  <SettingOutlined /> 配置
                </a>,
                <a key="edit" onClick={() => { 
                  setEditingItem({
                    ...record,
                    environmentId: record.environmentId || record.environment?.id // Flatten for form
                  }); 
                  setDrawerVisible(true); 
                }}>编辑</a>,
                <Popconfirm key="delete" title="确定删除?" onConfirm={() => handleDelete(record.id)}>
                  <a style={{ color: 'red' }}>删除</a>
                </Popconfirm>
            ];
        }
    }
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
            total: Array.isArray(res) ? list.length : res.total
          };
        }}
        toolBarRender={() => [
            <Button key="add" type="primary" icon={<PlusOutlined />} onClick={() => { setEditingItem(null); setDrawerVisible(true); }}>
                新建主机
            </Button>
        ]}
      />
      <DrawerForm
        visible={drawerVisible}
        title={editingItem?.id ? "编辑主机" : "新建主机"}
        onClose={() => { setDrawerVisible(false); setEditingItem(null); }}
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
        <Form.Item name="gatewayUrl" label="网关地址" tooltip="Server 连接此 Host Agent 的网关地址 (例如 http://192.168.1.100:9090 或通过中间件的地址)">
            <Input placeholder="请输入网关地址 (Server 将主动连接此地址)" />
        </Form.Item>
        <Form.Item name="environmentId" label="所属环境" rules={[{ required: true }]}>
            <Select placeholder="请选择环境">
              {environments.map(env => (
                <Select.Option key={env.id} value={env.id}>{env.name}</Select.Option>
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
        <div style={{ height: 'calc(100vh - 150px)', border: '1px solid #d9d9d9' }}>
          <Editor
            height="100%"
            defaultLanguage="yaml"
            value={configContent}
            onChange={(value) => setConfigContent(value || '')}
            options={{
              minimap: { enabled: false },
              scrollBeyondLastLine: false,
              fontSize: 14,
            }}
          />
        </div>
      </Drawer>
      
      {/* Access Guide Modal */}
      <Modal
        title="Host Agent 接入指南"
        open={installGuideVisible}
        onCancel={() => setInstallGuideVisible(false)}
        footer={null}
        width={600}
      >
          <div style={{ padding: 20 }}>
            <h3>1. 下载部署包</h3>
            <p>点击下方按钮下载包含配置文件和安装脚本的 Host Agent 部署包。</p>
            <Button type="primary" icon={<DownloadOutlined />} onClick={() => currentHost && handleDownloadPackage(currentHost)}>
                下载部署包
            </Button>
            
            <h3 style={{ marginTop: 20 }}>2. 上传至服务器</h3>
            <p>将下载的 tar.gz 包上传到目标服务器的目标目录。</p>
            
            <h3 style={{ marginTop: 20 }}>3. 安装并启动</h3>
            <div style={{ background: '#f5f5f5', padding: 10, borderRadius: 4 }}>
                <code>
                    tar -zxvf host-agent-install.tar.gz<br/>
                    chmod +x install.sh<br/>
                    ./install.sh
                </code>
            </div>
            
            <h3 style={{ marginTop: 20 }}>4. 验证连接</h3>
            <p>Host Agent 启动后，请点击下方按钮由 Server 主动尝试连接。</p>
            <Button 
                type="primary" 
                icon={<ApiOutlined />} 
                loading={connecting}
                onClick={() => currentHost && handleConnect(currentHost.id)}
            >
                连接 Host Agent
            </Button>
          </div>
      </Modal>

      {/* Terminal Drawer */}
      <Drawer
        title={`终端 - ${currentHost?.name}`}
        width="80%"
        onClose={() => setTerminalVisible(false)}
        open={terminalVisible}
        extra={
            <Button icon={<ApiOutlined />} onClick={() => setCmdModalVisible(true)}>
                命令面板
            </Button>
        }
        bodyStyle={{ padding: 0, backgroundColor: '#000' }}
      >
        <div style={{ height: '100%', width: '100%' }}>
            <XTerm 
                ref={xtermRef} 
                addons={[fitAddonRef.current]}
                options={{ theme: { background: '#000' } }}
            />
        </div>
      </Drawer>
      
      <TerminalCommandModal 
        visible={cmdModalVisible}
        onClose={() => setCmdModalVisible(false)}
        onExecute={handleExecuteCommand}
        hostOs={currentHost?.os}
      />
    </>
  );
};

export default HostList;
