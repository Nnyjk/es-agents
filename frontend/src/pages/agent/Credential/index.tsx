import React, { useRef, useState } from 'react';
import { PlusOutlined } from '@ant-design/icons';
import type { ActionType, ProColumns } from '@ant-design/pro-components';
import { ProTable } from '@ant-design/pro-components';
import { Button, message, Popconfirm, Form, Select, Input, Typography } from 'antd';
import { DrawerForm } from '../../../components/DrawerForm';
import { queryAgentCredentials, saveAgentCredential, removeAgentCredential } from '../../../services/agent';
import type { AgentCredential } from '../../../types';

const AgentCredentialList: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [drawerVisible, setDrawerVisible] = useState(false);
  const [editingItem, setEditingItem] = useState<Partial<AgentCredential> | null>(null);

  const parseConfig = (value?: string) => {
    if (!value) {
      return {};
    }
    try {
      return JSON.parse(value);
    } catch (error) {
      return {};
    }
  };

  const maskValue = (value?: string) => {
    if (!value) {
      return '-';
    }
    if (value.length <= 4) {
      return '****';
    }
    return `${value.slice(0, 2)}****${value.slice(-2)}`;
  };

  const buildSummary = (record: AgentCredential) => {
    const configValue = parseConfig(record.config);
    if (record.type === 'STATIC_TOKEN') {
      return `token: ${maskValue((configValue as any).token)}`;
    }
    if (record.type === 'API_TOKEN') {
      return `tokenUrl: ${(configValue as any).tokenUrl || '-'}`;
    }
    if (record.type === 'SCRIPT_TOKEN') {
      return `script: ${(configValue as any).script || '-'}`;
    }
    if (record.type === 'SSO_TOKEN') {
      return `issuerUrl: ${(configValue as any).issuerUrl || '-'}`;
    }
    return '-';
  };

  const columns: ProColumns<AgentCredential>[] = [
    {
      title: '凭证名称',
      dataIndex: 'name',
      ellipsis: true,
      copyable: true,
    },
    {
      title: '类型',
      dataIndex: 'type',
      valueEnum: {
        STATIC_TOKEN: { text: '静态令牌', status: 'Success' },
        API_TOKEN: { text: '接口令牌', status: 'Processing' },
        SCRIPT_TOKEN: { text: '脚本令牌', status: 'Warning' },
        SSO_TOKEN: { text: 'SSO令牌', status: 'Default' },
      },
    },
    {
      title: '配置摘要',
      dataIndex: 'config',
      search: false,
      render: (_value, record) => (
        <Typography.Text ellipsis={{ tooltip: record.config }} style={{ maxWidth: 360 }}>
          {buildSummary(record)}
        </Typography.Text>
      ),
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      valueType: 'dateTime',
      search: false,
    },
    {
      title: '更新时间',
      dataIndex: 'updatedAt',
      valueType: 'dateTime',
      search: false,
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
          title="确定删除该凭证吗？"
          onConfirm={async () => {
            await removeAgentCredential(record.id);
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
      return { type: 'STATIC_TOKEN' };
    }
    const configValue = parseConfig(editingItem.config);
    if (editingItem.type === 'STATIC_TOKEN') {
      return {
        ...editingItem,
        token: (configValue as any).token,
      };
    }
    if (editingItem.type === 'API_TOKEN') {
      return {
        ...editingItem,
        tokenUrl: (configValue as any).tokenUrl,
        clientId: (configValue as any).clientId,
        clientSecret: (configValue as any).clientSecret,
        scope: (configValue as any).scope,
      };
    }
    if (editingItem.type === 'SCRIPT_TOKEN') {
      return {
        ...editingItem,
        baseToken: (configValue as any).baseToken,
        script: (configValue as any).script,
      };
    }
    if (editingItem.type === 'SSO_TOKEN') {
      return {
        ...editingItem,
        issuerUrl: (configValue as any).issuerUrl,
        clientId: (configValue as any).clientId,
        redirectUri: (configValue as any).redirectUri,
        scope: (configValue as any).scope,
      };
    }
    return editingItem;
  };

  const handleSave = async (data: Partial<AgentCredential>) => {
    try {
      const resolvedType = data.type || editingItem?.type;
      let config = data.config;
      if (resolvedType === 'STATIC_TOKEN') {
        config = JSON.stringify({ token: (data as any).token });
      } else if (resolvedType === 'API_TOKEN') {
        config = JSON.stringify({
          tokenUrl: (data as any).tokenUrl,
          clientId: (data as any).clientId,
          clientSecret: (data as any).clientSecret,
          scope: (data as any).scope,
        });
      } else if (resolvedType === 'SCRIPT_TOKEN') {
        config = JSON.stringify({
          baseToken: (data as any).baseToken,
          script: (data as any).script,
        });
      } else if (resolvedType === 'SSO_TOKEN') {
        config = JSON.stringify({
          issuerUrl: (data as any).issuerUrl,
          clientId: (data as any).clientId,
          redirectUri: (data as any).redirectUri,
          scope: (data as any).scope,
        });
      }

      const payload = {
        id: editingItem?.id,
        name: data.name,
        type: resolvedType,
        config,
      };

      await saveAgentCredential(payload);
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
      <ProTable<AgentCredential>
        headerTitle="凭证列表"
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
            新建凭证
          </Button>,
        ]}
        request={async () => {
          const res = await queryAgentCredentials();
          return {
            data: res,
            success: true,
          };
        }}
        columns={columns}
      />

      <DrawerForm
        visible={drawerVisible}
        title={editingItem ? '编辑凭证' : '新建凭证'}
        width={600}
        onClose={handleClose}
        onSave={handleSave}
        initialValues={buildInitialValues()}
      >
        <Form.Item name="name" label="凭证名称" rules={[{ required: true }]}>
          <Input placeholder="请输入凭证名称" />
        </Form.Item>
        <Form.Item name="type" label="凭证类型" rules={[{ required: true }]}>
          <Select>
            <Select.Option value="STATIC_TOKEN">静态令牌</Select.Option>
            <Select.Option value="API_TOKEN">接口令牌</Select.Option>
            <Select.Option value="SCRIPT_TOKEN">脚本令牌</Select.Option>
            <Select.Option value="SSO_TOKEN">SSO令牌</Select.Option>
          </Select>
        </Form.Item>
        <Form.Item noStyle shouldUpdate>
          {({ getFieldValue }) => {
            const type = getFieldValue('type');
            if (type === 'STATIC_TOKEN') {
              return (
                <Form.Item name="token" label="静态令牌" rules={[{ required: true }]}>
                  <Input.Password placeholder="请输入令牌" />
                </Form.Item>
              );
            }
            if (type === 'API_TOKEN') {
              return (
                <>
                  <Form.Item name="tokenUrl" label="令牌接口" rules={[{ required: true }]}>
                    <Input placeholder="请输入令牌接口" />
                  </Form.Item>
                  <Form.Item name="clientId" label="客户端ID">
                    <Input placeholder="请输入客户端ID" />
                  </Form.Item>
                  <Form.Item name="clientSecret" label="客户端密钥">
                    <Input.Password placeholder="请输入客户端密钥" />
                  </Form.Item>
                  <Form.Item name="scope" label="Scope">
                    <Input placeholder="例如：read write" />
                  </Form.Item>
                </>
              );
            }
            if (type === 'SCRIPT_TOKEN') {
              return (
                <>
                  <Form.Item name="baseToken" label="静态令牌">
                    <Input.Password placeholder="可选：基础令牌" />
                  </Form.Item>
                  <Form.Item name="script" label="脚本" rules={[{ required: true }]}>
                    <Input.TextArea rows={4} placeholder="例如：echo $BASE_TOKEN" />
                  </Form.Item>
                </>
              );
            }
            if (type === 'SSO_TOKEN') {
              return (
                <>
                  <Form.Item name="issuerUrl" label="SSO地址" rules={[{ required: true }]}>
                    <Input placeholder="请输入SSO地址" />
                  </Form.Item>
                  <Form.Item name="clientId" label="客户端ID">
                    <Input placeholder="请输入客户端ID" />
                  </Form.Item>
                  <Form.Item name="redirectUri" label="回调地址">
                    <Input placeholder="请输入回调地址" />
                  </Form.Item>
                  <Form.Item name="scope" label="Scope">
                    <Input placeholder="例如：openid profile" />
                  </Form.Item>
                </>
              );
            }
            return null;
          }}
        </Form.Item>
      </DrawerForm>
    </>
  );
};

export default AgentCredentialList;
