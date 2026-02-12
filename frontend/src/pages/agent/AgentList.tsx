import React, { useRef, useState, useEffect } from 'react';
import { PlusOutlined } from '@ant-design/icons';
import type { ActionType, ProColumns } from '@ant-design/pro-components';
import { ProTable } from '@ant-design/pro-components';
import { Button, message, Popconfirm, Form, Select } from 'antd';
import { DrawerForm } from '../../components/DrawerForm';
import { queryAgentInstances, removeAgentInstance, saveAgentInstance, queryAgentTemplates } from '../../services/agent';
import { queryHosts } from '../../services/infra';
import type { AgentInstance, Host, AgentTemplate } from '../../types';

const AgentList: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [drawerVisible, setDrawerVisible] = useState(false);
  const [editingItem, setEditingItem] = useState<Partial<AgentInstance> | null>(null);
  
  const [hosts, setHosts] = useState<Host[]>([]);
  const [templates, setTemplates] = useState<AgentTemplate[]>([]);

  // Load dependency data
  useEffect(() => {
    queryHosts().then((res: any) => {
        const list = Array.isArray(res) ? res : res.data;
        setHosts(list);
    });
    queryAgentTemplates().then((res) => {
        setTemplates(res);
    }).catch(console.error);
  }, []);

  const columns: ProColumns<AgentInstance>[] = [
    { 
      title: 'Agent ID', 
      dataIndex: 'id',
      hideInTable: true,
      search: false
    },
    {
      title: '所在主机',
      dataIndex: 'host',
      render: (_, record) => record.host?.name || record.host?.hostname || '-',
    },
    {
        title: '使用模板',
        dataIndex: 'template',
        render: (_, record) => record.template?.name || '-',
        search: false
    },
    {
      title: '状态',
      dataIndex: 'status',
      valueEnum: {
        OFFLINE: { text: '离线', status: 'Default' },
        ONLINE: { text: '在线', status: 'Success' },
        BUSY: { text: '繁忙', status: 'Processing' },
        UNCONFIGURED: { text: '未配置', status: 'Warning' }
      },
    },
    {
      title: '版本',
      dataIndex: 'version',
      search: false,
    },
    {
      title: '最后心跳时间',
      dataIndex: 'lastHeartbeatTime',
      valueType: 'dateTime',
      search: false,
    },
    {
      title: '操作',
      valueType: 'option',
      render: (_text, record, _, action) => [
        <Popconfirm
          key="delete"
          title="确定删除该代理吗？"
          onConfirm={async () => {
            await removeAgentInstance(record.id);
            message.success('删除成功');
            action?.reload();
          }}
        >
          <a style={{ color: 'red' }}>删除</a>
        </Popconfirm>,
      ],
    },
  ];

  const handleSave = async (data: any) => {
    try {
        await saveAgentInstance({ ...editingItem, ...data });
        message.success('保存成功');
        setDrawerVisible(false);
        setEditingItem(null);
        actionRef.current?.reload();
    } catch (error) {
        console.error(error);
        message.error('保存失败');
    }
  };

  const handleClose = () => {
    setDrawerVisible(false);
    setEditingItem(null);
  };

  return (
    <>
      <ProTable<AgentInstance>
        headerTitle="Agent列表"
        actionRef={actionRef}
        rowKey="id"
        search={{
          labelWidth: 120,
        }}
        toolBarRender={() => [
          <Button
            key="button"
            icon={<PlusOutlined />}
            type="primary"
            onClick={() => {
              setEditingItem(null);
              setDrawerVisible(true);
            }}
          >
            部署新Agent
          </Button>,
        ]}
        request={async (params) => {
          const res = await queryAgentInstances(params);
          const list = Array.isArray(res) ? res : res.data;
          return {
            data: list,
            success: true,
            total: Array.isArray(res) ? list.length : res.total
          };
        }}
        columns={columns}
      />

      <DrawerForm
        visible={drawerVisible}
        title="部署Agent"
        width={600}
        onClose={handleClose}
        onSave={handleSave}
        initialValues={editingItem || undefined}
      >
        <Form.Item name="hostId" label="选择主机" rules={[{ required: true }]}>
          <Select placeholder="请选择目标主机">
            {hosts.map(host => (
                <Select.Option key={host.id} value={host.id}>
                    {host.name} ({host.hostname}) - {host.environmentName || host.environment?.name}
                </Select.Option>
            ))}
          </Select>
        </Form.Item>
        <Form.Item name="templateId" label="选择模板" rules={[{ required: true }]}>
            <Select placeholder="请选择Agent模板">
                {templates.map(tpl => (
                    <Select.Option key={tpl.id} value={tpl.id}>{tpl.name}</Select.Option>
                ))}
            </Select>
        </Form.Item>
      </DrawerForm>
    </>
  );
};

export default AgentList;
