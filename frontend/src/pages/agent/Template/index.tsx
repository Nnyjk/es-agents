import React, { useRef, useState, useEffect } from 'react';
import { PlusOutlined } from '@ant-design/icons';
import type { ActionType, ProColumns } from '@ant-design/pro-components';
import { ProTable } from '@ant-design/pro-components';
import { Button, message, Popconfirm, Form, Select, Input } from 'antd';
import { DrawerForm } from '../../../components/DrawerForm';
import { queryAgentTemplates, saveAgentTemplate, removeAgentTemplate, queryAgentResources } from '../../../services/agent';
import type { AgentTemplate, AgentResource } from '../../../types';

const AgentTemplateList: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [drawerVisible, setDrawerVisible] = useState(false);
  const [editingItem, setEditingItem] = useState<Partial<AgentTemplate> | null>(null);
  const [resources, setResources] = useState<AgentResource[]>([]);

  useEffect(() => {
    queryAgentResources().then(setResources);
  }, []);

  const columns: ProColumns<AgentTemplate>[] = [
    {
      title: '模板名称',
      dataIndex: 'name',
      ellipsis: true,
      copyable: true,
    },
    {
      title: '描述',
      dataIndex: 'description',
      ellipsis: true,
    },
    {
      title: '来源资源',
      dataIndex: 'source',
      render: (_, record) => record.source?.name || '-',
    },
    {
      title: '来源类型',
      dataIndex: 'sourceType',
      valueEnum: {
        GITLAB: { text: 'GitLab 仓库' },
        MAVEN: { text: 'Maven 仓库' },
        NEXTCLOUD: { text: 'Nextcloud 仓库' },
        GIT: { text: 'Git 仓库' },
        DOCKER: { text: 'Docker 仓库' },
        HTTPS: { text: 'HTTPS 资源' },
        HTTP: { text: 'HTTP 资源' },
        LOCAL: { text: '本地文件' },
        ALIYUN: { text: '阿里云制品库' },
      },
      render: (_, record) => record.source?.type || '-',
    },
    {
        title: '包含命令数',
        render: (_, record) => record.commands?.length || 0,
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
          title="确定删除该模板吗？"
          onConfirm={async () => {
            await removeAgentTemplate(record.id);
            message.success('删除成功');
            action?.reload();
          }}
        >
          <a style={{ color: 'red' }}>删除</a>
        </Popconfirm>,
      ],
    },
  ];

  const handleSave = async (data: Partial<AgentTemplate>) => {
    try {
        await saveAgentTemplate({ ...editingItem, ...data });
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
      <ProTable<AgentTemplate>
        headerTitle="模板列表"
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
            新建模板
          </Button>,
        ]}
        request={async () => {
          const res = await queryAgentTemplates();
          return {
            data: res,
            success: true,
          };
        }}
        columns={columns}
      />

      <DrawerForm
        visible={drawerVisible}
        title={editingItem ? '编辑模板' : '新建模板'}
        width={600}
        onClose={handleClose}
        onSave={handleSave}
        initialValues={editingItem || undefined}
      >
        <Form.Item name="name" label="模板名称" rules={[{ required: true }]}>
          <Input placeholder="请输入模板名称" />
        </Form.Item>
        <Form.Item name="description" label="描述">
          <Input.TextArea placeholder="请输入描述" />
        </Form.Item>
        <Form.Item name="sourceId" label="来源资源" rules={[{ required: true }]}>
            <Select placeholder="请选择资源">
                {resources.map(r => (
                    <Select.Option key={r.id} value={r.id}>{r.name} ({r.type})</Select.Option>
                ))}
            </Select>
        </Form.Item>
      </DrawerForm>
    </>
  );
};

export default AgentTemplateList;
