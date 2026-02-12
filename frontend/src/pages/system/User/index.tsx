import React, { useRef, useState, useEffect } from 'react';
import { PlusOutlined } from '@ant-design/icons';
import type { ActionType, ProColumns } from '@ant-design/pro-components';
import { ProTable } from '@ant-design/pro-components';
import { Button, message, Popconfirm, Tag, Form, Input, Select } from 'antd';
import { DrawerForm } from '../../../components/DrawerForm';
import { getUsers, createUser, updateUser, deleteUser, changeUserStatus } from '../../../services/user';
import { getRoles } from '../../../services/role';
import type { User, Role } from '../../../types';

const UserList: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [drawerVisible, setDrawerVisible] = useState(false);
  const [editingItem, setEditingItem] = useState<User | null>(null);
  const [roles, setRoles] = useState<Role[]>([]);

  useEffect(() => {
    fetchRoles();
  }, []);

  const fetchRoles = async () => {
    try {
      const data = await getRoles();
      setRoles(data);
    } catch (e) {
      console.error(e);
    }
  };

  const columns: ProColumns<User>[] = [
    {
      title: '用户名',
      dataIndex: 'username',
      copyable: true,
      ellipsis: true,
      formItemProps: {
        rules: [{ required: true, message: '此项为必填项' }],
      },
    },
    {
      title: '密码',
      dataIndex: 'password',
      hideInTable: true,
      hideInSearch: true,
      valueType: 'password',
      formItemProps: {
        rules: [{ required: true, message: '此项为必填项' }],
      },
      // Only show in create
      hideInForm: false, 
    },
    {
      title: '状态',
      dataIndex: 'status',
      valueEnum: {
        ACTIVE: { text: '正常', status: 'Success' },
        INACTIVE: { text: '停用', status: 'Error' },
        LOCKED: { text: '锁定', status: 'Warning' },
      },
    },
    {
      title: '角色',
      dataIndex: 'roles',
      hideInForm: true,
      hideInSearch: true,
      render: (_, record) => (
        <>
          {record.roles?.map((role: Role) => (
            <Tag key={role.id}>{role.name}</Tag>
          ))}
        </>
      ),
    },
    {
      title: '角色设置',
      dataIndex: 'roleIds',
      hideInTable: true,
      hideInSearch: true,
      valueType: 'select',
      fieldProps: { mode: 'multiple' },
      request: async () => {
        const roles = await getRoles();
        return roles.map((r: Role) => ({ label: r.name, value: r.id }));
      },
    },
    {
      title: '操作',
      valueType: 'option',
      key: 'option',
      render: (_text, record, _, action) => [
        <a
          key="editable"
          onClick={() => {
            setEditingItem(record);
            setDrawerVisible(true);
          }}
        >
          编辑
        </a>,
        <Popconfirm
          key="delete"
          title="确定删除吗?"
          onConfirm={async () => {
            await deleteUser(record.id);
            message.success('删除成功');
            action?.reload();
          }}
        >
          <a style={{ color: 'red' }}>删除</a>
        </Popconfirm>,
        record.status === 'ACTIVE' ? (
           <a key="stop" onClick={async () => {
             await changeUserStatus(record.id, 'INACTIVE');
             action?.reload();
           }}>停用</a>
        ) : (
           <a key="start" onClick={async () => {
             await changeUserStatus(record.id, 'ACTIVE');
             action?.reload();
           }}>启用</a>
        )
      ],
    },
  ];

  const handleSave = async (data: any) => {
    if (editingItem) {
      await updateUser(editingItem.id, data);
      message.success('更新成功');
    } else {
      await createUser(data);
      message.success('创建成功');
    }
    setDrawerVisible(false);
    setEditingItem(null);
    actionRef.current?.reload();
  };

  const handleClose = () => {
    setDrawerVisible(false);
    setEditingItem(null);
  };

  return (
    <>
      <ProTable<User>
        columns={columns}
        actionRef={actionRef}
        cardBordered
        request={async () => {
          const data = await getUsers();
          return {
            data,
            success: true,
          };
        }}
        editable={{
          type: 'multiple',
          onSave: async (_key, row) => {
             const { id, ...rest } = row;
             await updateUser(id, rest);
             message.success('更新成功');
          },
        }}
        rowKey="id"
        search={{
          labelWidth: 'auto',
        }}
        toolBarRender={() => [
          <Button
            key="button"
            icon={<PlusOutlined />}
            onClick={() => {
              setEditingItem(null);
              setDrawerVisible(true);
            }}
            type="primary"
          >
            新建
          </Button>,
        ]}
      />

      <DrawerForm
        visible={drawerVisible}
        title={editingItem ? '编辑用户' : '新建用户'}
        width={600}
        onClose={handleClose}
        onSave={handleSave}
        initialValues={editingItem || { status: 'ACTIVE' }}
      >
        <Form.Item name="username" label="用户名" rules={[{ required: true }]}>
          <Input placeholder="请输入用户名" />
        </Form.Item>
        {!editingItem && (
          <Form.Item name="password" label="密码" rules={[{ required: true }]}>
            <Input.Password placeholder="请输入密码" />
          </Form.Item>
        )}
        <Form.Item name="status" label="状态" rules={[{ required: true }]}>
          <Select>
            <Select.Option value="ACTIVE">正常</Select.Option>
            <Select.Option value="INACTIVE">停用</Select.Option>
            <Select.Option value="LOCKED">锁定</Select.Option>
          </Select>
        </Form.Item>
        <Form.Item name="roleIds" label="角色">
          <Select mode="multiple" placeholder="请选择角色">
            {roles.map(role => (
              <Select.Option key={role.id} value={role.id}>{role.name}</Select.Option>
            ))}
          </Select>
        </Form.Item>
      </DrawerForm>
    </>
  );
};

export default UserList;
