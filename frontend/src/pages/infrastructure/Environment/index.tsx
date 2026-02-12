import React, { useRef, useState } from 'react';
import { PlusOutlined } from '@ant-design/icons';
import type { ActionType, ProColumns } from '@ant-design/pro-components';
import { ProTable } from '@ant-design/pro-components';
import { Button, message, Popconfirm, Form, Input } from 'antd';
import { DrawerForm } from '../../../components/DrawerForm';
import { queryEnvironments, saveEnvironment, removeEnvironment } from '../../../services/infra';
import type { Environment } from '../../../types';

const EnvironmentList: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [drawerVisible, setDrawerVisible] = useState(false);
  const [editingItem, setEditingItem] = useState<Partial<Environment> | null>(null);

  const handleSave = async (data: any) => {
    try {
      await saveEnvironment({ ...editingItem, ...data });
      message.success('保存成功');
      setDrawerVisible(false);
      setEditingItem(null);
      actionRef.current?.reload();
    } catch (error) {
      console.error(error);
      message.error('保存失败');
    }
  };

  const handleDelete = async (id: string) => {
    try {
      await removeEnvironment(id);
      message.success('删除成功');
      actionRef.current?.reload();
    } catch (error) {
      console.error(error);
      message.error('删除失败');
    }
  };

  const columns: ProColumns<Environment>[] = [
    { title: '环境名称', dataIndex: 'name' },
    { title: '编码', dataIndex: 'code' },
    { title: '描述', dataIndex: 'description' },
    {
        title: '操作',
        valueType: 'option',
        render: (_, record) => [
            <a key="edit" onClick={() => { setEditingItem(record); setDrawerVisible(true); }}>编辑</a>,
            <Popconfirm key="delete" title="确定删除?" onConfirm={() => handleDelete(record.id)}>
              <a style={{ color: 'red' }}>删除</a>
            </Popconfirm>
        ]
    }
  ];

  return (
    <>
      <ProTable<Environment>
        headerTitle="环境列表"
        columns={columns}
        actionRef={actionRef}
        rowKey="id"
        request={async (params) => {
          const res = await queryEnvironments(params);
          const list = Array.isArray(res) ? res : res.data;
          return {
            data: list,
            success: true,
            total: Array.isArray(res) ? list.length : res.total
          };
        }}
        toolBarRender={() => [
            <Button key="add" type="primary" icon={<PlusOutlined />} onClick={() => { setEditingItem(null); setDrawerVisible(true); }}>
                新建环境
            </Button>
        ]}
      />
      <DrawerForm
        visible={drawerVisible}
        title={editingItem?.id ? "编辑环境" : "新建环境"}
        onClose={() => { setDrawerVisible(false); setEditingItem(null); }}
        onSave={handleSave}
        initialValues={editingItem || undefined}
      >
        <Form.Item name="name" label="环境名称" rules={[{ required: true }]}>
            <Input placeholder="请输入环境名称" />
        </Form.Item>
        <Form.Item name="code" label="编码" rules={[{ required: true }]}>
            <Input placeholder="请输入编码" />
        </Form.Item>
        <Form.Item name="description" label="描述">
            <Input.TextArea placeholder="请输入描述" />
        </Form.Item>
      </DrawerForm>
    </>
  );
};

export default EnvironmentList;
