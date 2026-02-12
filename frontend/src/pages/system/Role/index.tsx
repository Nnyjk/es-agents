import React, { useEffect, useRef, useState } from 'react';
import { PlusOutlined } from '@ant-design/icons';
import type { ActionType, ProColumns } from '@ant-design/pro-components';
import { ProTable } from '@ant-design/pro-components';
import { Button, message, Popconfirm, Form, Input, Drawer, Tree, Spin, Space } from 'antd';
import { useNavigate, useParams } from 'react-router-dom';
import { DrawerForm } from '../../../components/DrawerForm';
import { getRoles, createRole, updateRole, deleteRole, getRole } from '../../../services/role';
import { getModules } from '../../../services/module';
import type { Role, Module } from '../../../types';

interface TreeNode {
  key: string;
  title: string;
  children: TreeNode[];
}

const RoleList: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [drawerVisible, setDrawerVisible] = useState(false);
  const [editingItem, setEditingItem] = useState<Role | null>(null);
  const [authDrawerVisible, setAuthDrawerVisible] = useState(false);
  const [authTreeData, setAuthTreeData] = useState<TreeNode[]>([]);
  const [authCheckedKeys, setAuthCheckedKeys] = useState<React.Key[]>([]);
  const [authLoading, setAuthLoading] = useState(false);
  const navigate = useNavigate();
  const { id } = useParams<{ id?: string }>();

  const columns: ProColumns<Role>[] = [
    {
      title: '角色编码',
      dataIndex: 'code',
      copyable: true,
    },
    {
      title: '角色名称',
      dataIndex: 'name',
    },
    {
      title: '描述',
      dataIndex: 'description',
      hideInSearch: true,
    },
    {
      title: '操作',
      valueType: 'option',
      key: 'option',
      render: (_text, record, _, action) => [
        <a
          key="auth"
          onClick={() => navigate(`/roles/auth/${record.id}`)}
        >
          授权
        </a>,
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
            await deleteRole(record.id);
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
    if (editingItem) {
      await updateRole(editingItem.id, data);
      message.success('更新成功');
    } else {
      await createRole(data);
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

  const convertToTree = (list: Module[]): TreeNode[] => {
    const map: Record<string, number> = {};
    const roots: TreeNode[] = [];
    const nodeList: TreeNode[] = list.map(item => ({
      key: item.id,
      title: item.name,
      children: [],
    }));

    nodeList.forEach((item, i) => {
      map[item.key] = i;
    });

    list.forEach((item, i) => {
      const node = nodeList[i];
      if (item.parentId && map[item.parentId] !== undefined) {
        nodeList[map[item.parentId]].children.push(node);
      } else {
        roots.push(node);
      }
    });
    return roots;
  };

  const loadAuthorization = async (roleId: string) => {
    setAuthLoading(true);
    try {
      const [modules, roleData] = await Promise.all([getModules(), getRole(roleId)]);
      setAuthTreeData(convertToTree(modules));
      setAuthCheckedKeys(roleData.moduleIds || []);
    } catch (error) {
      console.error(error);
      message.error('加载数据失败');
    } finally {
      setAuthLoading(false);
    }
  };

  useEffect(() => {
    if (id) {
      setAuthDrawerVisible(true);
      loadAuthorization(id);
      return;
    }
    setAuthDrawerVisible(false);
    setAuthTreeData([]);
    setAuthCheckedKeys([]);
  }, [id]);

  const handleAuthClose = () => {
    setAuthDrawerVisible(false);
    navigate('/roles');
  };

  const handleAuthSave = async () => {
    if (!id) return;
    setAuthLoading(true);
    try {
      await updateRole(id, { moduleIds: authCheckedKeys as string[] });
      message.success('权限保存成功');
      handleAuthClose();
      actionRef.current?.reload();
    } catch (error) {
      console.error(error);
      message.error('保存失败');
      setAuthLoading(false);
    }
  };

  return (
    <>
      <ProTable<Role>
        columns={columns}
        actionRef={actionRef}
        cardBordered
        request={async () => {
          const data = await getRoles();
          return {
            data,
            success: true,
          };
        }}
        editable={{
          type: 'multiple',
          onSave: async (_key, row) => {
             const { id, ...rest } = row;
             await updateRole(id, rest);
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
            新建角色
          </Button>,
        ]}
      />

      <DrawerForm
        visible={drawerVisible}
        title={editingItem ? '编辑角色' : '新建角色'}
        width={600}
        onClose={handleClose}
        onSave={handleSave}
        initialValues={editingItem || undefined}
      >
        <Form.Item name="code" label="角色编码" rules={[{ required: true }]}>
          <Input placeholder="请输入角色编码" />
        </Form.Item>
        <Form.Item name="name" label="角色名称" rules={[{ required: true }]}>
          <Input placeholder="请输入角色名称" />
        </Form.Item>
        <Form.Item name="description" label="描述">
          <Input.TextArea rows={3} placeholder="请输入描述" />
        </Form.Item>
      </DrawerForm>

      <Drawer
        title="角色授权"
        open={authDrawerVisible}
        placement="right"
        width={560}
        onClose={handleAuthClose}
        destroyOnClose
        extra={(
          <Space>
            <Button onClick={handleAuthClose}>取消</Button>
            <Button type="primary" onClick={handleAuthSave} loading={authLoading}>
              保存
            </Button>
          </Space>
        )}
      >
        {authLoading ? <Spin /> : (
          <Tree
            checkable
            treeData={authTreeData}
            checkedKeys={authCheckedKeys}
            onCheck={(keys) => setAuthCheckedKeys(keys as React.Key[])}
          />
        )}
      </Drawer>
    </>
  );
};

export default RoleList;
