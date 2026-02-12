import React, { useRef, useState } from 'react';
import { PlusOutlined } from '@ant-design/icons';
import type { ActionType, ProColumns } from '@ant-design/pro-components';
import { ProTable } from '@ant-design/pro-components';
import { Button, message, Popconfirm, Form, Input, Select, InputNumber, TreeSelect } from 'antd';
import { DrawerForm } from '../../../components/DrawerForm';
import { getModules, createModule, updateModule, deleteModule } from '../../../services/module';
import type { Module } from '../../../types';

const ModuleList: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [drawerVisible, setDrawerVisible] = useState(false);
  const [editingItem, setEditingItem] = useState<Module | null>(null);
  const [treeData, setTreeData] = useState<any[]>([]);

  const columns: ProColumns<Module>[] = [
    {
      title: '名称',
      dataIndex: 'name',
    },
    {
      title: '编码',
      dataIndex: 'code',
    },
    {
      title: '类型',
      dataIndex: 'type',
      valueEnum: {
        MENU: { text: '菜单', status: 'Processing' },
        BUTTON: { text: '按钮', status: 'Default' },
        DIR: { text: '目录', status: 'Success' },
      },
    },
    {
      title: '路径',
      dataIndex: 'path',
    },
    {
      title: '排序',
      dataIndex: 'sortOrder',
      hideInSearch: true,
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
            fetchTreeData();
            setDrawerVisible(true);
          }}
        >
          编辑
        </a>,
        <Popconfirm
          key="delete"
          title="确定删除吗?"
          onConfirm={async () => {
            await deleteModule(record.id);
            message.success('删除成功');
            action?.reload();
          }}
        >
          <a style={{ color: 'red' }}>删除</a>
        </Popconfirm>,
      ],
    },
  ];

  const convertToTree = (list: Module[]) => {
      const map: any = {};
      const roots: any[] = [];
      // Deep clone to avoid mutating original list if it's used elsewhere
      const nodeList = list.map(item => ({ 
          ...item, 
          key: item.id, 
          value: item.id,
          title: item.name,
          children: [] as any[] 
      }));

      nodeList.forEach((item, i) => {
        map[item.id] = i;
      });
      
      nodeList.forEach(item => {
        if (item.parentId && map[item.parentId] !== undefined) {
           nodeList[map[item.parentId]].children?.push(item);
        } else {
           roots.push(item);
        }
      });
      return roots;
  }

  const fetchTreeData = async () => {
      try {
          const data = await getModules();
          setTreeData(convertToTree(data));
      } catch (e) {
          console.error(e);
      }
  };

  const handleSave = async (data: any) => {
    if (editingItem) {
      await updateModule(editingItem.id, data);
      message.success('更新成功');
    } else {
      await createModule(data);
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
      <ProTable<Module>
        columns={columns}
        actionRef={actionRef}
        cardBordered
        request={async () => {
          const data = await getModules();
          // Convert flat list to tree for table display
          // ProTable handles tree data if children exist
          const tree = convertToTree(data);
          return {
            data: tree,
            success: true,
          };
        }}
        editable={{
          type: 'multiple',
          onSave: async (_key, row) => {
             const { id, children, ...rest } = row;
             await updateModule(id, rest);
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
              fetchTreeData();
              setDrawerVisible(true);
            }}
            type="primary"
          >
            新建模块
          </Button>,
        ]}
      />

      <DrawerForm
        visible={drawerVisible}
        title={editingItem ? '编辑模块' : '新建模块'}
        width={600}
        onClose={handleClose}
        onSave={handleSave}
        initialValues={editingItem || undefined}
      >
        <Form.Item name="parentId" label="上级菜单">
            <TreeSelect
                style={{ width: '100%' }}
                dropdownStyle={{ maxHeight: 400, overflow: 'auto' }}
                treeData={treeData}
                placeholder="请选择上级菜单"
                treeDefaultExpandAll
                allowClear
            />
        </Form.Item>
        <Form.Item name="name" label="名称" rules={[{ required: true }]}>
          <Input placeholder="请输入名称" />
        </Form.Item>
        <Form.Item name="code" label="编码" rules={[{ required: true }]}>
          <Input placeholder="请输入编码" />
        </Form.Item>
        <Form.Item name="type" label="类型" rules={[{ required: true }]}>
          <Select>
            <Select.Option value="DIR">目录</Select.Option>
            <Select.Option value="MENU">菜单</Select.Option>
            <Select.Option value="BUTTON">按钮</Select.Option>
          </Select>
        </Form.Item>
        <Form.Item name="path" label="路径">
          <Input placeholder="请输入路由路径" />
        </Form.Item>
        <Form.Item name="sortOrder" label="排序">
          <InputNumber style={{ width: '100%' }} placeholder="请输入排序号" />
        </Form.Item>
      </DrawerForm>
    </>
  );
};

export default ModuleList;
