import React, { useRef, useState, useEffect } from "react";
import { PlusOutlined, EyeOutlined, DeleteOutlined } from "@ant-design/icons";
import type { ActionType, ProColumns } from "@ant-design/pro-components";
import { ProTable } from "@ant-design/pro-components";
import { Button, message, Popconfirm, Form, Select, Space, Modal } from "antd";
import { useNavigate } from "react-router-dom";
import { DrawerForm } from "../../components/DrawerForm";
import {
  queryAgentInstances,
  removeAgentInstance,
  saveAgentInstance,
  queryAgentTemplates,
  batchRemoveAgentInstances,
} from "../../services/agent";
import { queryHosts } from "../../services/infra";
import type { AgentInstance, Host, AgentTemplate } from "../../types";
import { AgentStatusDisplay } from "../../components/agent";

const AgentList: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const navigate = useNavigate();
  const [drawerVisible, setDrawerVisible] = useState(false);
  const [editingItem, setEditingItem] = useState<Partial<AgentInstance> | null>(
    null,
  );

  const [hosts, setHosts] = useState<Host[]>([]);
  const [templates, setTemplates] = useState<AgentTemplate[]>([]);
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);

  // Load dependency data
  useEffect(() => {
    queryHosts().then((res: any) => {
      const list = Array.isArray(res) ? res : res.data;
      setHosts(list);
    });
    queryAgentTemplates()
      .then((res) => {
        setTemplates(res);
      })
      .catch(console.error);
  }, []);

  // 批量删除
  const handleBatchDelete = async () => {
    if (selectedRowKeys.length === 0) {
      message.warning("请选择要删除的 Agent");
      return;
    }

    Modal.confirm({
      title: `确定要删除选中的 ${selectedRowKeys.length} 个 Agent 吗？`,
      content: "此操作不可恢复，请谨慎操作",
      okText: "确定删除",
      cancelText: "取消",
      okType: "danger",
      onOk: async () => {
        try {
          await batchRemoveAgentInstances(selectedRowKeys as string[]);
          message.success(`成功删除 ${selectedRowKeys.length} 个 Agent`);
          setSelectedRowKeys([]);
          actionRef.current?.reload();
        } catch (error: any) {
          console.error(error);
          message.error(error.message || "批量删除失败");
        }
      },
    });
  };

  const columns: ProColumns<AgentInstance>[] = [
    {
      title: "Agent ID",
      dataIndex: "id",
      hideInTable: true,
      search: false,
    },
    {
      title: "所在主机",
      dataIndex: "host",
      render: (_, record) => record.host?.name || record.host?.hostname || "-",
    },
    {
      title: "使用模板",
      dataIndex: "template",
      render: (_, record) => record.template?.name || "-",
      search: false,
    },
    {
      title: "状态",
      dataIndex: "status",
      render: (_, record) => {
        // 映射旧状态到新状态枚举
        const statusMap: Record<string, string> = {
          EXCEPTION: "ERROR",
          BUSY: "ONLINE",
        };
        const mappedStatus = statusMap[record.status] || record.status;
        return (
          <AgentStatusDisplay
            status={mappedStatus as any}
            mode="tag"
            showIcon={false}
            size="small"
          />
        );
      },
    },
    {
      title: "版本",
      dataIndex: "version",
      search: false,
    },
    {
      title: "最后心跳时间",
      dataIndex: "lastHeartbeatTime",
      valueType: "dateTime",
      search: false,
    },
    {
      title: "操作",
      valueType: "option",
      render: (_text, record, _, action) => [
        <Button
          key="detail"
          type="link"
          size="small"
          icon={<EyeOutlined />}
          onClick={() => navigate(`/agents/${record.id}`)}
        >
          详情
        </Button>,
        <Popconfirm
          key="delete"
          title="确定删除该代理吗？"
          onConfirm={async () => {
            await removeAgentInstance(record.id);
            message.success("删除成功");
            action?.reload();
          }}
        >
          <a style={{ color: "red" }}>删除</a>
        </Popconfirm>,
      ],
    },
  ];

  const handleSave = async (data: any) => {
    try {
      await saveAgentInstance({ ...editingItem, ...data });
      message.success("保存成功");
      setDrawerVisible(false);
      setEditingItem(null);
      actionRef.current?.reload();
    } catch (error) {
      console.error(error);
      message.error("保存失败");
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
        rowSelection={{
          selectedRowKeys,
          onChange: setSelectedRowKeys,
        }}
        tableAlertRender={({
          selectedRowKeys,
          selectedRows,
          onCleanSelected,
        }) => (
          <Space>
            <span>已选择 {selectedRowKeys.length} 项</span>
            <Button type="link" onClick={onCleanSelected}>
              取消选择
            </Button>
          </Space>
        )}
        tableAlertOptionRender={() => (
          <Space>
            <Button
              danger
              icon={<DeleteOutlined />}
              onClick={handleBatchDelete}
              disabled={selectedRowKeys.length === 0}
            >
              批量删除
            </Button>
          </Space>
        )}
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
            total: Array.isArray(res) ? list.length : res.total,
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
            {hosts.map((host) => (
              <Select.Option key={host.id} value={host.id}>
                {host.name} ({host.hostname}) -{" "}
                {host.environmentName || host.environment?.name}
              </Select.Option>
            ))}
          </Select>
        </Form.Item>
        <Form.Item
          name="templateId"
          label="选择模板"
          rules={[{ required: true }]}
        >
          <Select placeholder="请选择Agent模板">
            {templates.map((tpl) => (
              <Select.Option key={tpl.id} value={tpl.id}>
                {tpl.name}
              </Select.Option>
            ))}
          </Select>
        </Form.Item>
      </DrawerForm>
    </>
  );
};

export default AgentList;
