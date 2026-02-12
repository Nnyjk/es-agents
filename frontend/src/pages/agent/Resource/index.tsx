import React, { useRef, useState, useEffect } from 'react';
import { PlusOutlined } from '@ant-design/icons';
import type { ActionType, ProColumns } from '@ant-design/pro-components';
import { ProTable } from '@ant-design/pro-components';
import { Button, message, Popconfirm, Form, Select, Input, Typography } from 'antd';
import { DrawerForm } from '../../../components/DrawerForm';
import { queryAgentResources, saveAgentResource, removeAgentResource, queryAgentCredentials, queryAgentRepositories } from '../../../services/agent';
import type { AgentResource, AgentCredential, AgentRepository } from '../../../types';

const AgentResourceList: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [drawerVisible, setDrawerVisible] = useState(false);
  const [editingItem, setEditingItem] = useState<Partial<AgentResource> | null>(null);
  const [credentials, setCredentials] = useState<AgentCredential[]>([]);
  const [repositories, setRepositories] = useState<AgentRepository[]>([]);

  useEffect(() => {
    queryAgentCredentials().then(setCredentials).catch(console.error);
    queryAgentRepositories().then(setRepositories).catch(console.error);
  }, []);

  const columns: ProColumns<AgentResource>[] = [
    {
      title: '资源名称',
      dataIndex: 'name',
      ellipsis: true,
      copyable: true,
    },
    {
      title: '类型',
      dataIndex: 'type',
      valueEnum: {
        GITLAB: { text: 'GitLab 仓库', status: 'Processing' },
        MAVEN: { text: 'Maven 仓库', status: 'Success' },
        NEXTCLOUD: { text: 'Nextcloud 仓库', status: 'Default' },
        GIT: { text: 'Git 仓库', status: 'Processing' },
        DOCKER: { text: 'Docker 仓库', status: 'Success' },
        HTTPS: { text: 'HTTPS 资源', status: 'Success' },
        HTTP: { text: 'HTTP 资源', status: 'Success' },
        LOCAL: { text: '本地文件', status: 'Default' },
        ALIYUN: { text: '阿里云制品库', status: 'Warning' },
      },
    },
    {
      title: '仓库',
      dataIndex: 'repository',
      render: (_value, record) => record.repository?.name || '-',
    },
    {
      title: '凭证',
      dataIndex: 'credential',
      render: (_value, record) => record.credential?.name || '-',
    },
    {
      title: '配置',
      dataIndex: 'config',
      search: false,
      render: (_value, record) => (
        <Typography.Text
          ellipsis={{ tooltip: record.config }}
          style={{ maxWidth: 360 }}
          copyable
        >
          {record.config}
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
          title="确定删除该资源吗？"
          onConfirm={async () => {
            await removeAgentResource(record.id);
            message.success('删除成功');
            action?.reload();
          }}
        >
          <a style={{ color: 'red' }}>删除</a>
        </Popconfirm>,
      ],
    },
  ];

  const handleSave = async (data: Partial<AgentResource>) => {
    try {
        const resolvedType = data.type || editingItem?.type;
        const repositoryId = data.repositoryId || editingItem?.repositoryId || editingItem?.repository?.id;
        const credentialId = data.credentialId || editingItem?.credentialId || editingItem?.credential?.id;
        let config = data.config;

        if (resolvedType === 'GITLAB') {
          const configValue = {
            ref: (data as any).ref,
            filePath: (data as any).filePath,
          };
          config = JSON.stringify(configValue);
        } else if (resolvedType === 'MAVEN') {
          const configValue = {
            downloadUrl: (data as any).downloadUrl,
            groupId: (data as any).groupId,
            artifactId: (data as any).artifactId,
            version: (data as any).version,
            packaging: (data as any).packaging,
            classifier: (data as any).classifier,
          };
          config = JSON.stringify(configValue);
        } else if (resolvedType === 'NEXTCLOUD') {
          const configValue = {
            downloadUrl: (data as any).downloadUrl,
            filePath: (data as any).filePath,
          };
          config = JSON.stringify(configValue);
        } else if (resolvedType === 'HTTP' || resolvedType === 'HTTPS') {
          const configValue = {
            url: (data as any).url,
            fileName: (data as any).fileName,
          };
          config = JSON.stringify(configValue);
        } else if (resolvedType === 'LOCAL') {
          const configValue = {
            file: (data as any).file,
          };
          config = JSON.stringify(configValue);
        } else {
          config = (data as any).rawConfig || '{}';
        }

        const payload = {
          id: editingItem?.id,
          name: data.name,
          type: resolvedType,
          config,
          repositoryId,
          credentialId,
        };

        await saveAgentResource(payload);
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

  const buildInitialValues = () => {
    if (!editingItem) {
      return { type: 'GITLAB' };
    }
    const configValue = parseConfig(editingItem.config);
    const repositoryId = editingItem.repository?.id || editingItem.repositoryId;
    const credentialId = editingItem.credential?.id || editingItem.credentialId;
    if (editingItem.type === 'GITLAB') {
      return {
        ...editingItem,
        repositoryId,
        credentialId,
        ref: (configValue as any).ref,
        filePath: (configValue as any).filePath,
      };
    }
    if (editingItem.type === 'MAVEN') {
      return {
        ...editingItem,
        repositoryId,
        credentialId,
        downloadUrl: (configValue as any).downloadUrl,
        groupId: (configValue as any).groupId,
        artifactId: (configValue as any).artifactId,
        version: (configValue as any).version,
        packaging: (configValue as any).packaging,
        classifier: (configValue as any).classifier,
      };
    }
    if (editingItem.type === 'NEXTCLOUD') {
      return {
        ...editingItem,
        repositoryId,
        credentialId,
        downloadUrl: (configValue as any).downloadUrl,
        filePath: (configValue as any).filePath,
      };
    }
    if (editingItem.type === 'HTTP' || editingItem.type === 'HTTPS') {
      return {
        ...editingItem,
        credentialId,
        url: (configValue as any).url,
        fileName: (configValue as any).fileName,
      };
    }
    if (editingItem.type === 'LOCAL') {
      return {
        ...editingItem,
        file: (configValue as any).file,
      };
    }
    return {
      ...editingItem,
      rawConfig: editingItem.config,
    };
  };

  return (
    <>
      <ProTable<AgentResource>
        headerTitle="资源列表"
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
            新建资源
          </Button>,
        ]}
        request={async () => {
          const res = await queryAgentResources();
          return {
            data: res,
            success: true,
          };
        }}
        columns={columns}
      />

      <DrawerForm
        visible={drawerVisible}
        title={editingItem ? '编辑资源' : '新建资源'}
        width={600}
        onClose={handleClose}
        onSave={handleSave}
        initialValues={buildInitialValues()}
      >
        <Form.Item name="name" label="资源名称" rules={[{ required: true }]}>
          <Input placeholder="请输入资源名称" />
        </Form.Item>
        <Form.Item name="type" label="资源类型" rules={[{ required: true }]}>
            <Select>
                <Select.Option value="GITLAB">GitLab 仓库</Select.Option>
                <Select.Option value="MAVEN">Maven 仓库</Select.Option>
                <Select.Option value="NEXTCLOUD">Nextcloud 仓库</Select.Option>
                <Select.Option value="GIT">Git 仓库</Select.Option>
                <Select.Option value="DOCKER">Docker 仓库</Select.Option>
                <Select.Option value="HTTPS">HTTPS 资源</Select.Option>
                <Select.Option value="HTTP">HTTP 资源</Select.Option>
                <Select.Option value="LOCAL">本地文件</Select.Option>
                <Select.Option value="ALIYUN">阿里云制品库</Select.Option>
            </Select>
        </Form.Item>
        <Form.Item noStyle shouldUpdate>
          {({ getFieldValue }) => {
            const type = getFieldValue('type');
            if (type === 'GITLAB') {
              return (
                <>
                  <Form.Item name="repositoryId" label="仓库" rules={[{ required: true }]}>
                    <Select placeholder="请选择仓库">
                      {repositories.map(repository => (
                        <Select.Option key={repository.id} value={repository.id}>
                          {repository.name} ({repository.projectPath})
                        </Select.Option>
                      ))}
                    </Select>
                  </Form.Item>
                  <Form.Item name="credentialId" label="凭证">
                    <Select placeholder="可选：覆盖仓库凭证" allowClear>
                      {credentials.map(credential => (
                        <Select.Option key={credential.id} value={credential.id}>
                          {credential.name} ({credential.type})
                        </Select.Option>
                      ))}
                    </Select>
                  </Form.Item>
                  <Form.Item name="ref" label="分支或标签">
                    <Input placeholder="例如：main 或 v1.0.0" />
                  </Form.Item>
                  <Form.Item name="filePath" label="文件路径">
                    <Input placeholder="例如：dist/agent.tar.gz" />
                  </Form.Item>
                </>
              );
            }
            if (type === 'MAVEN') {
              return (
                <>
                  <Form.Item name="repositoryId" label="仓库" rules={[{ required: true }]}>
                    <Select placeholder="请选择仓库">
                      {repositories.map(repository => (
                        <Select.Option key={repository.id} value={repository.id}>
                          {repository.name} ({repository.projectPath})
                        </Select.Option>
                      ))}
                    </Select>
                  </Form.Item>
                  <Form.Item name="credentialId" label="凭证">
                    <Select placeholder="可选：覆盖仓库凭证" allowClear>
                      {credentials.map(credential => (
                        <Select.Option key={credential.id} value={credential.id}>
                          {credential.name} ({credential.type})
                        </Select.Option>
                      ))}
                    </Select>
                  </Form.Item>
                  <Form.Item name="downloadUrl" label="直接地址">
                    <Input placeholder="可选：直接填写制品下载地址" />
                  </Form.Item>
                  <Form.Item name="groupId" label="GroupId">
                    <Input placeholder="例如：com.easystation" />
                  </Form.Item>
                  <Form.Item name="artifactId" label="ArtifactId">
                    <Input placeholder="例如：agent-package" />
                  </Form.Item>
                  <Form.Item name="version" label="版本">
                    <Input placeholder="例如：1.0.0" />
                  </Form.Item>
                  <Form.Item name="packaging" label="打包类型">
                    <Input placeholder="例如：jar" />
                  </Form.Item>
                  <Form.Item name="classifier" label="Classifier">
                    <Input placeholder="可选：例如：linux-amd64" />
                  </Form.Item>
                </>
              );
            }
            if (type === 'NEXTCLOUD') {
              return (
                <>
                  <Form.Item name="repositoryId" label="仓库" rules={[{ required: true }]}>
                    <Select placeholder="请选择仓库">
                      {repositories.map(repository => (
                        <Select.Option key={repository.id} value={repository.id}>
                          {repository.name} ({repository.projectPath})
                        </Select.Option>
                      ))}
                    </Select>
                  </Form.Item>
                  <Form.Item name="credentialId" label="凭证">
                    <Select placeholder="可选：覆盖仓库凭证" allowClear>
                      {credentials.map(credential => (
                        <Select.Option key={credential.id} value={credential.id}>
                          {credential.name} ({credential.type})
                        </Select.Option>
                      ))}
                    </Select>
                  </Form.Item>
                  <Form.Item name="downloadUrl" label="直接地址">
                    <Input placeholder="可选：直接填写下载地址" />
                  </Form.Item>
                  <Form.Item name="filePath" label="文件路径">
                    <Input placeholder="例如：/downloads/agent.zip" />
                  </Form.Item>
                </>
              );
            }
            if (type === 'HTTP' || type === 'HTTPS') {
              return (
                <>
                  <Form.Item name="url" label="下载地址" rules={[{ required: true }]}>
                    <Input placeholder="请输入下载地址" />
                  </Form.Item>
                  <Form.Item name="fileName" label="文件名">
                    <Input placeholder="可选：自定义下载文件名" />
                  </Form.Item>
                  <Form.Item name="credentialId" label="凭证">
                    <Select placeholder="可选：关联凭证" allowClear>
                      {credentials.map(credential => (
                        <Select.Option key={credential.id} value={credential.id}>
                          {credential.name} ({credential.type})
                        </Select.Option>
                      ))}
                    </Select>
                  </Form.Item>
                </>
              );
            }
            if (type === 'LOCAL') {
              return (
                <Form.Item name="file" label="文件名" rules={[{ required: true }]}>
                  <Input placeholder="请输入文件名" />
                </Form.Item>
              );
            }
            return (
              <Form.Item name="rawConfig" label="配置 (JSON)" rules={[{ required: true }]}>
                <Input.TextArea rows={6} placeholder='{"key": "value"}' />
              </Form.Item>
            );
          }}
        </Form.Item>
      </DrawerForm>
    </>
  );
};

export default AgentResourceList;
