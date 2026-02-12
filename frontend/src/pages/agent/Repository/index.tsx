import React, { useRef, useState, useEffect } from 'react';
import { PlusOutlined } from '@ant-design/icons';
import type { ActionType, ProColumns } from '@ant-design/pro-components';
import { ProTable } from '@ant-design/pro-components';
import { Button, message, Popconfirm, Form, Select, Input } from 'antd';
import { DrawerForm } from '../../../components/DrawerForm';
import { queryAgentRepositories, saveAgentRepository, removeAgentRepository, queryAgentCredentials } from '../../../services/agent';
import type { AgentRepository, AgentCredential } from '../../../types';

const AgentRepositoryList: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [drawerVisible, setDrawerVisible] = useState(false);
  const [editingItem, setEditingItem] = useState<Partial<AgentRepository> | null>(null);
  const [credentials, setCredentials] = useState<AgentCredential[]>([]);

  useEffect(() => {
    queryAgentCredentials().then(setCredentials).catch(console.error);
  }, []);

  const columns: ProColumns<AgentRepository>[] = [
    {
      title: '仓库名称',
      dataIndex: 'name',
      ellipsis: true,
      copyable: true,
    },
    {
      title: '类型',
      dataIndex: 'type',
      valueEnum: {
        GITLAB: { text: 'GitLab', status: 'Processing' },
        MAVEN: { text: 'Maven 仓库', status: 'Success' },
        NEXTCLOUD: { text: 'Nextcloud', status: 'Default' },
      },
    },
    {
      title: '基础地址',
      dataIndex: 'baseUrl',
      ellipsis: true,
    },
    {
      title: '项目路径',
      dataIndex: 'projectPath',
      ellipsis: true,
    },
    {
      title: '默认分支',
      dataIndex: 'defaultBranch',
      search: false,
    },
    {
      title: '关联凭证',
      dataIndex: 'credential',
      render: (_value, record) => record.credential?.name || '-',
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
          title="确定删除该仓库吗？"
          onConfirm={async () => {
            await removeAgentRepository(record.id);
            message.success('删除成功');
            action?.reload();
          }}
        >
          <a style={{ color: 'red' }}>删除</a>
        </Popconfirm>,
      ],
    },
  ];

  const buildInitialValues = () => {
    if (!editingItem) {
      return { type: 'GITLAB' };
    }
    return {
      ...editingItem,
      credentialId: editingItem.credentialId || editingItem.credential?.id,
    };
  };

  const handleSave = async (data: Partial<AgentRepository>) => {
    try {
      const payload = {
        id: editingItem?.id,
        name: data.name,
        type: data.type || editingItem?.type,
        baseUrl: data.baseUrl,
        projectPath: data.projectPath,
        defaultBranch: data.defaultBranch,
        credentialId: data.credentialId,
      };
      await saveAgentRepository(payload);
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
      <ProTable<AgentRepository>
        headerTitle="仓库列表"
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
            新建仓库
          </Button>,
        ]}
        request={async () => {
          const res = await queryAgentRepositories();
          return {
            data: res,
            success: true,
          };
        }}
        columns={columns}
      />

      <DrawerForm
        visible={drawerVisible}
        title={editingItem ? '编辑仓库' : '新建仓库'}
        width={600}
        onClose={handleClose}
        onSave={handleSave}
        initialValues={buildInitialValues()}
      >
        <Form.Item name="name" label="仓库名称" rules={[{ required: true }]}>
          <Input placeholder="请输入仓库名称" />
        </Form.Item>
        <Form.Item name="type" label="仓库类型" rules={[{ required: true }]}>
          <Select>
            <Select.Option value="GITLAB">GitLab</Select.Option>
            <Select.Option value="MAVEN">Maven</Select.Option>
            <Select.Option value="NEXTCLOUD">Nextcloud</Select.Option>
          </Select>
        </Form.Item>
        <Form.Item name="baseUrl" label="基础地址" rules={[{ required: true }]}>
          <Input placeholder="例如：https://git.dev.yd" />
        </Form.Item>
        <Form.Item name="projectPath" label="项目路径" rules={[{ required: true }]}>
          <Input placeholder="例如：group/project" />
        </Form.Item>
        <Form.Item name="defaultBranch" label="默认分支">
          <Input placeholder="例如：main" />
        </Form.Item>
        <Form.Item name="credentialId" label="关联凭证">
          <Select placeholder="可选：选择凭证" allowClear>
            {credentials.map(credential => (
              <Select.Option key={credential.id} value={credential.id}>
                {credential.name} ({credential.type})
              </Select.Option>
            ))}
          </Select>
        </Form.Item>
      </DrawerForm>
    </>
  );
};

export default AgentRepositoryList;
