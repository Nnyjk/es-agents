import React, { useRef, useState, useEffect } from 'react';
import { PlusOutlined } from '@ant-design/icons';
import type { ActionType, ProColumns } from '@ant-design/pro-components';
import { ProTable } from '@ant-design/pro-components';
import { Button, message, Popconfirm, Form, Select, Input, InputNumber, Typography } from 'antd';
import { DrawerForm } from '../../../components/DrawerForm';
import { queryAgentCommands, saveAgentCommand, removeAgentCommand, queryAgentTemplates } from '../../../services/agent';
import type { AgentCommand, AgentTemplate } from '../../../types';

const AgentCommandList: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [drawerVisible, setDrawerVisible] = useState(false);
  const [editingItem, setEditingItem] = useState<Partial<AgentCommand> | null>(null);
  const [templates, setTemplates] = useState<AgentTemplate[]>([]);

  useEffect(() => {
    queryAgentTemplates().then(setTemplates);
  }, []);

  const columns: ProColumns<AgentCommand>[] = [
    {
      title: '命令名称',
      dataIndex: 'name',
      ellipsis: true,
      copyable: true,
    },
    {
      title: '所属模板',
      dataIndex: 'templateId',
      valueType: 'select',
      valueEnum: templates.reduce((acc, t) => ({ ...acc, [t.id]: t.name }), {}),
      render: (_, record) => templates.find(t => t.id === record.templateId)?.name || '-',
    },
    {
        title: '超时(秒)',
        dataIndex: 'timeout',
        search: false,
    },
    {
        title: '脚本预览',
        dataIndex: 'script',
        search: false,
        render: (_value, record) => (
          <Typography.Text
            ellipsis={{ tooltip: record.script }}
            style={{ maxWidth: 360 }}
            copyable
          >
            {record.script}
          </Typography.Text>
        ),
    },
    {
        title: '默认参数',
        dataIndex: 'defaultArgs',
        search: false,
        render: (_value, record) => {
          if (!record.defaultArgs) return '-';
          return (
            <Typography.Text
              ellipsis={{ tooltip: record.defaultArgs }}
              style={{ maxWidth: 320 }}
              copyable
            >
              {record.defaultArgs}
            </Typography.Text>
          );
        },
    },
    {
      title: '操作',
      valueType: 'option',
      render: (_text, record, _, action) => [
        <a
          key="edit"
          onClick={() => {
            setEditingItem(record);
            setDrawerVisible(true);
          }}
        >
          编辑
        </a>,
        <Popconfirm
          key="delete"
          title="确定删除该命令吗？"
          onConfirm={async () => {
            await removeAgentCommand(record.id);
            message.success('删除成功');
            action?.reload();
          }}
        >
          <a style={{ color: 'red' }}>删除</a>
        </Popconfirm>,
      ],
    },
  ];

  const handleSave = async (data: Partial<AgentCommand>) => {
    try {
        await saveAgentCommand({ ...editingItem, ...data });
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
      <ProTable<AgentCommand>
        headerTitle="命令列表"
        actionRef={actionRef}
        rowKey="id"
        search={{ labelWidth: 120 }}
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
            新建命令
          </Button>,
        ]}
        request={async (params) => {
          const res = await queryAgentCommands(params.templateId);
          return {
            data: res,
            success: true,
          };
        }}
        columns={columns}
      />

      <DrawerForm
        visible={drawerVisible}
        title={editingItem ? '编辑命令' : '新建命令'}
        width={600}
        onClose={handleClose}
        onSave={handleSave}
        initialValues={editingItem || { timeout: 60, defaultArgs: '{}' }}
      >
        <Form.Item name="name" label="命令名称" rules={[{ required: true }]}>
          <Input placeholder="请输入命令名称" />
        </Form.Item>
        <Form.Item name="templateId" label="所属模板" rules={[{ required: true }]}>
            <Select placeholder="请选择所属模板" disabled={!!editingItem}> 
                {templates.map(t => (
                    <Select.Option key={t.id} value={t.id}>{t.name}</Select.Option>
                ))}
            </Select>
        </Form.Item>
        <Form.Item name="timeout" label="超时时间(秒)" rules={[{ required: true }]}>
            <InputNumber min={1} />
        </Form.Item>
        <Form.Item name="script" label="脚本内容" rules={[{ required: true }]}>
            <Input.TextArea rows={8} style={{ fontFamily: 'monospace' }} />
        </Form.Item>
        <Form.Item name="defaultArgs" label="默认参数 (JSON)">
            <Input.TextArea rows={4} placeholder="{}" style={{ fontFamily: 'monospace' }} />
        </Form.Item>
      </DrawerForm>
    </>
  );
};

export default AgentCommandList;
