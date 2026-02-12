import React, { useRef, useState } from 'react';
import { Modal, Button, message, Popconfirm, Form, Input } from 'antd';
import { ProTable, ActionType, ProColumns } from '@ant-design/pro-components';
import { PlusOutlined, PlayCircleOutlined } from '@ant-design/icons';
import { AgentCommand } from '@/types';
import { queryAgentTemplates, saveAgentCommand, removeAgentCommand } from '@/services/agent';
import { DrawerForm } from '@/components/DrawerForm';

interface TerminalCommandModalProps {
    visible: boolean;
    onClose: () => void;
    onExecute: (command: string) => void;
    hostOs?: string;
}

export const TerminalCommandModal: React.FC<TerminalCommandModalProps> = ({ visible, onClose, onExecute, hostOs }) => {
    const actionRef = useRef<ActionType>();
    const [editVisible, setEditVisible] = useState(false);
    const [editingItem, setEditingItem] = useState<Partial<AgentCommand> | null>(null);
    const [templateId, setTemplateId] = useState<string>();

    const handleSave = async (data: any) => {
        try {
            if (!templateId) {
                message.error('未找到对应环境的命令模板，无法保存');
                return;
            }
            await saveAgentCommand({ ...editingItem, ...data, templateId });
            message.success('保存成功');
            setEditVisible(false);
            setEditingItem(null);
            actionRef.current?.reload();
        } catch (error) {
            console.error(error);
            message.error('保存失败');
        }
    };

    const handleDelete = async (id: string) => {
        try {
            await removeAgentCommand(id);
            message.success('删除成功');
            actionRef.current?.reload();
        } catch (error) {
            console.error(error);
            message.error('删除失败');
        }
    };

    const columns: ProColumns<AgentCommand>[] = [
        { title: '名称', dataIndex: 'name', width: 150 },
        { 
            title: '脚本', 
            dataIndex: 'script', 
            ellipsis: true,
            copyable: true 
        },
        {
            title: '超时(秒)',
            dataIndex: 'timeout',
            width: 100,
            render: (text) => text || 60
        },
        {
            title: '操作',
            valueType: 'option',
            width: 180,
            render: (_, record) => [
                <a key="exec" onClick={() => {
                    onExecute(record.script);
                    onClose();
                }}>
                    <PlayCircleOutlined /> 执行
                </a>,
                <a key="edit" onClick={() => { setEditingItem(record); setEditVisible(true); }}>编辑</a>,
                <Popconfirm key="del" title="确定删除?" onConfirm={() => handleDelete(record.id)}>
                    <a style={{ color: 'red' }}>删除</a>
                </Popconfirm>
            ]
        }
    ];

    return (
        <>
            <Modal
                title="终端命令管理"
                open={visible}
                onCancel={onClose}
                footer={null}
                width={800}
            >
                <ProTable<AgentCommand>
                    actionRef={actionRef}
                    rowKey="id"
                    search={false}
                    options={false}
                    request={async () => {
                        if (!hostOs) return { data: [], success: true };
                        // Map hostOs (lowercase) to OsType (uppercase)
                        const osType = hostOs.toUpperCase();
                        // Query templates for LOCAL source and matching OS
                        const templates = await queryAgentTemplates({ sourceType: 'LOCAL', osType });
                        
                        // Use the first matching template (should be only one per OS for LOCAL)
                        const template = templates[0];
                        if (template) {
                            setTemplateId(template.id);
                            return { data: template.commands || [], success: true };
                        }
                        return { data: [], success: true };
                    }}
                    toolBarRender={() => [
                        <Button 
                            key="add" 
                            type="primary" 
                            icon={<PlusOutlined />} 
                            onClick={() => { setEditingItem(null); setEditVisible(true); }}
                            disabled={!templateId}
                        >
                            新建命令
                        </Button>
                    ]}
                    columns={columns}
                    pagination={{ pageSize: 5 }}
                />
            </Modal>

            <DrawerForm
                visible={editVisible}
                title={editingItem ? "编辑命令" : "新建命令"}
                onClose={() => { setEditVisible(false); setEditingItem(null); }}
                onSave={handleSave}
                initialValues={editingItem || undefined}
            >
                <Form.Item name="name" label="名称" rules={[{ required: true }]}>
                    <Input />
                </Form.Item>
                <Form.Item name="script" label="脚本" rules={[{ required: true }]}>
                    <Input.TextArea rows={4} />
                </Form.Item>
                <Form.Item name="timeout" label="超时时间(秒)" initialValue={60}>
                    <Input type="number" />
                </Form.Item>
                <Form.Item name="defaultArgs" label="默认参数">
                    <Input />
                </Form.Item>
            </DrawerForm>
        </>
    );
};
