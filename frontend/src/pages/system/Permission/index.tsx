import React, { useRef, useState } from "react";
import { PlusOutlined } from "@ant-design/icons";
import type { ActionType, ProColumns } from "@ant-design/pro-components";
import { ProTable } from "@ant-design/pro-components";
import {
  Button,
  message,
  Popconfirm,
  Form,
  Input,
  Select,
  Tag,
  Space,
  Tooltip,
} from "antd";
import { DrawerForm } from "../../../components/DrawerForm";
import {
  getPermissions,
  createPermission,
  updatePermission,
  deletePermission,
} from "../../../services/permission";
import type { Permission } from "../../../types";

const PermissionList: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [drawerVisible, setDrawerVisible] = useState(false);
  const [editingItem, setEditingItem] = useState<Permission | null>(null);

  const handleAdd = () => {
    setEditingItem(null);
    setDrawerVisible(true);
  };

  const handleEdit = (record: Permission) => {
    setEditingItem(record);
    setDrawerVisible(true);
  };

  const handleDelete = async (id: string) => {
    try {
      await deletePermission(id);
      message.success("删除成功");
      actionRef.current?.reload();
    } catch {
      message.error("删除失败");
    }
  };

  const handleSubmit = async (values: Record<string, any>) => {
    try {
      if (editingItem) {
        await updatePermission(editingItem.id, values);
        message.success("更新成功");
      } else {
        await createPermission(values);
        message.success("创建成功");
      }
      setDrawerVisible(false);
      actionRef.current?.reload();
    } catch {
      message.error("操作失败");
    }
  };

  const columns: ProColumns<Permission>[] = [
    {
      title: "权限名称",
      dataIndex: "name",
      width: 150,
    },
    {
      title: "权限编码",
      dataIndex: "code",
      width: 180,
      render: (_, record) => (
        <Tooltip title={record.code}>
          <code
            style={{
              fontSize: 12,
              background: "#f5f5f5",
              padding: "2px 6px",
              borderRadius: 4,
            }}
          >
            {record.code}
          </code>
        </Tooltip>
      ),
    },
    {
      title: "资源",
      dataIndex: "resource",
      width: 120,
      render: (_, record) => <Tag color="blue">{record.resource}</Tag>,
    },
    {
      title: "操作",
      dataIndex: "action",
      width: 100,
      render: (_, record) => (
        <Tag color={getActionColor(record.action)}>{record.action}</Tag>
      ),
    },
    {
      title: "描述",
      dataIndex: "description",
      ellipsis: true,
      width: 200,
    },
    {
      title: "类型",
      dataIndex: "system",
      width: 80,
      render: (_, record) => (
        <Tag color={record.system ? "purple" : "default"}>
          {record.system ? "系统" : "自定义"}
        </Tag>
      ),
    },
    {
      title: "创建时间",
      dataIndex: "createdAt",
      valueType: "dateTime",
      width: 160,
      search: false,
    },
    {
      title: "操作",
      valueType: "option",
      width: 120,
      render: (_, record) => (
        <Space>
          <Button type="link" size="small" onClick={() => handleEdit(record)}>
            编辑
          </Button>
          <Popconfirm
            title="确定删除此权限?"
            description="删除后不可恢复"
            onConfirm={() => handleDelete(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <Button type="link" danger size="small">
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  const getActionColor = (action: string) => {
    const colors: Record<string, string> = {
      view: "green",
      create: "blue",
      update: "orange",
      delete: "red",
      execute: "purple",
    };
    return colors[action] || "default";
  };

  return (
    <>
      <ProTable<Permission>
        columns={columns}
        actionRef={actionRef}
        request={async (params) => {
          const data = await getPermissions({
            keyword: params.keyword,
            resource: params.resource,
            action: params.action,
          });
          return {
            data,
            success: true,
            total: data.length,
          };
        }}
        rowKey="id"
        search={{
          labelWidth: "auto",
        }}
        options={{
          setting: {
            listsHeight: 400,
          },
        }}
        toolBarRender={() => [
          <Button
            key="add"
            type="primary"
            icon={<PlusOutlined />}
            onClick={handleAdd}
          >
            新增权限
          </Button>,
        ]}
      />
      <DrawerForm
        title={editingItem ? "编辑权限" : "新增权限"}
        visible={drawerVisible}
        onClose={() => setDrawerVisible(false)}
        onSave={handleSubmit}
        initialValues={editingItem || undefined}
      >
        <Form.Item
          name="code"
          label="权限编码"
          rules={[{ required: true, message: "请输入权限编码" }]}
        >
          <Input placeholder="如: user:create" disabled={!!editingItem} />
        </Form.Item>
        <Form.Item
          name="name"
          label="权限名称"
          rules={[{ required: true, message: "请输入权限名称" }]}
        >
          <Input placeholder="请输入权限名称" />
        </Form.Item>
        <Form.Item name="resource" label="资源">
          <Input placeholder="如: user" />
        </Form.Item>
        <Form.Item name="action" label="操作">
          <Select placeholder="请选择操作">
            <Select.Option value="view">查看</Select.Option>
            <Select.Option value="create">创建</Select.Option>
            <Select.Option value="update">更新</Select.Option>
            <Select.Option value="delete">删除</Select.Option>
            <Select.Option value="execute">执行</Select.Option>
          </Select>
        </Form.Item>
        <Form.Item name="description" label="描述">
          <Input.TextArea rows={3} placeholder="请输入权限描述" />
        </Form.Item>
      </DrawerForm>
    </>
  );
};

export default PermissionList;
