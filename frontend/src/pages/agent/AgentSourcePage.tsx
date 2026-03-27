/**
 * Agent 资源来源管理页面
 * 用于管理 Agent 模板部署时获取软件包的来源配置
 */

import React, { useRef, useState, useEffect } from "react";
import { PlusOutlined } from "@ant-design/icons";
import type { ActionType, ProColumns } from "@ant-design/pro-components";
import { ProTable } from "@ant-design/pro-components";
import {
  Button,
  message,
  Popconfirm,
  Form,
  Select,
  Input,
  Typography,
  Space,
  Tag,
} from "antd";
import { DrawerForm } from "../../components/DrawerForm";
import {
  queryAgentSources,
  saveAgentSource,
  removeAgentSource,
} from "../../services/agentSource";
import {
  queryAgentCredentials,
  queryAgentRepositories,
} from "../../services/agent";
import type {
  AgentSource,
  AgentSourceType,
} from "../../types/agent";
import type { AgentCredential, AgentRepository } from "../../types";
import { AgentSourceTypeLabels as sourceTypeLabels } from "../../types/agent";

const AgentSourcePage: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [drawerVisible, setDrawerVisible] = useState(false);
  const [editingItem, setEditingItem] = useState<Partial<AgentSource> | null>(
    null
  );
  const [credentials, setCredentials] = useState<AgentCredential[]>([]);
  const [repositories, setRepositories] = useState<AgentRepository[]>([]);
  const [searchKeyword, setSearchKeyword] = useState<string>("");
  const [filterType, setFilterType] = useState<AgentSourceType | undefined>();

  useEffect(() => {
    queryAgentCredentials().then(setCredentials).catch(console.error);
    queryAgentRepositories().then(setRepositories).catch(console.error);
  }, []);

  const typeValueEnum: Record<AgentSourceType, { text: string; status: string }> = {
    GITLAB: { text: "GitLab 仓库", status: "Processing" },
    MAVEN: { text: "Maven 仓库", status: "Success" },
    NEXTCLOUD: { text: "Nextcloud 仓库", status: "Default" },
    GIT: { text: "Git 仓库", status: "Processing" },
    DOCKER: { text: "Docker 仓库", status: "Success" },
    HTTPS: { text: "HTTPS 资源", status: "Success" },
    HTTP: { text: "HTTP 资源", status: "Success" },
    LOCAL: { text: "本地文件", status: "Default" },
    ALIYUN: { text: "阿里云制品库", status: "Warning" },
  };

  const columns: ProColumns<AgentSource>[] = [
    {
      title: "资源名称",
      dataIndex: "name",
      ellipsis: true,
      copyable: true,
      sorter: true,
    },
    {
      title: "类型",
      dataIndex: "type",
      valueEnum: typeValueEnum,
      render: (_, record) => (
        <Tag color={getTypeTagColor(record.type)}>
          {sourceTypeLabels[record.type]}
        </Tag>
      ),
    },
    {
      title: "仓库",
      dataIndex: "repository",
      render: (_value, record) => record.repository?.name || "-",
      search: false,
    },
    {
      title: "凭证",
      dataIndex: "credential",
      render: (_value, record) => record.credential?.name || "-",
      search: false,
    },
    {
      title: "配置",
      dataIndex: "config",
      search: false,
      render: (_value, record) =>
        record.config ? (
          <Typography.Text
            ellipsis={{ tooltip: record.config }}
            style={{ maxWidth: 360 }}
            copyable
          >
            {record.config}
          </Typography.Text>
        ) : "-",
    },
    {
      title: "创建时间",
      dataIndex: "createdAt",
      valueType: "dateTime",
      search: false,
      sorter: true,
    },
    {
      title: "更新时间",
      dataIndex: "updatedAt",
      valueType: "dateTime",
      search: false,
      sorter: true,
    },
    {
      title: "操作",
      valueType: "option",
      width: 150,
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
          title="确定删除该资源来源吗？"
          description="删除后无法恢复，请确认操作"
          onConfirm={async () => {
            await removeAgentSource(record.id);
            message.success("删除成功");
            action?.reload();
          }}
        >
          <a style={{ color: "red" }}>删除</a>
        </Popconfirm>,
      ],
    },
  ];

  const getTypeTagColor = (type: AgentSourceType): string => {
    const colorMap: Record<AgentSourceType, string> = {
      GITLAB: "blue",
      MAVEN: "green",
      NEXTCLOUD: "default",
      GIT: "blue",
      DOCKER: "cyan",
      HTTPS: "green",
      HTTP: "lime",
      LOCAL: "orange",
      ALIYUN: "purple",
    };
    return colorMap[type] || "default";
  };

  const parseConfig = (value?: string): Record<string, unknown> => {
    if (!value) return {};
    try {
      return JSON.parse(value);
    } catch {
      return {};
    }
  };

  const buildInitialValues = (): Record<string, unknown> => {
    if (!editingItem) {
      return { type: "GITLAB" };
    }
    const configValue = parseConfig(editingItem.config);
    const repositoryId = editingItem.repository?.id;
    const credentialId = editingItem.credential?.id;

    switch (editingItem.type) {
      case "GITLAB":
        return {
          ...editingItem,
          repositoryId,
          credentialId,
          ref: configValue.ref as string,
          filePath: configValue.filePath as string,
        };
      case "MAVEN":
        return {
          ...editingItem,
          repositoryId,
          credentialId,
          downloadUrl: configValue.downloadUrl as string,
          groupId: configValue.groupId as string,
          artifactId: configValue.artifactId as string,
          version: configValue.version as string,
          packaging: configValue.packaging as string,
          classifier: configValue.classifier as string,
        };
      case "NEXTCLOUD":
        return {
          ...editingItem,
          repositoryId,
          credentialId,
          downloadUrl: configValue.downloadUrl as string,
          filePath: configValue.filePath as string,
        };
      case "HTTP":
      case "HTTPS":
        return {
          ...editingItem,
          credentialId,
          url: configValue.url as string,
          fileName: configValue.fileName as string,
        };
      case "LOCAL":
        return {
          ...editingItem,
          file: configValue.file as string,
        };
      default:
        return {
          ...editingItem,
          rawConfig: editingItem.config,
        };
    }
  };

  const handleSave = async (data: Record<string, unknown>): Promise<void> => {
    try {
      const resolvedType = (data.type as AgentSourceType) || editingItem?.type;
      const repositoryId =
        data.repositoryId ||
        editingItem?.repository?.id;
      const credentialId =
        data.credentialId ||
        editingItem?.credential?.id;
      let config = data.config as string | undefined;

      switch (resolvedType) {
        case "GITLAB":
          config = JSON.stringify({
            ref: data.ref,
            filePath: data.filePath,
          });
          break;
        case "MAVEN":
          config = JSON.stringify({
            downloadUrl: data.downloadUrl,
            groupId: data.groupId,
            artifactId: data.artifactId,
            version: data.version,
            packaging: data.packaging,
            classifier: data.classifier,
          });
          break;
        case "NEXTCLOUD":
          config = JSON.stringify({
            downloadUrl: data.downloadUrl,
            filePath: data.filePath,
          });
          break;
        case "HTTP":
        case "HTTPS":
          config = JSON.stringify({
            url: data.url,
            fileName: data.fileName,
          });
          break;
        case "LOCAL":
          config = JSON.stringify({
            file: data.file,
          });
          break;
        default:
          config = (data.rawConfig as string) || "{}";
          break;
      }

      const payload: Partial<AgentSource> = {
        id: editingItem?.id,
        name: data.name as string,
        type: resolvedType,
        config,
        repositoryId: repositoryId as string,
        credentialId: credentialId as string,
      };

      await saveAgentSource(payload);
      message.success("保存成功");
      setDrawerVisible(false);
      setEditingItem(null);
      actionRef.current?.reload();
    } catch (error) {
      console.error(error);
      message.error("保存失败");
    }
  };

  const handleClose = (): void => {
    setDrawerVisible(false);
    setEditingItem(null);
  };

  const renderConfigForm = (type: AgentSourceType): React.ReactNode => {
    switch (type) {
      case "GITLAB":
        return (
          <>
            <Form.Item
              name="repositoryId"
              label="仓库"
              rules={[{ required: true, message: "请选择仓库" }]}
            >
              <Select placeholder="请选择仓库">
                {repositories.map((repo) => (
                  <Select.Option key={repo.id} value={repo.id}>
                    {repo.name} ({repo.projectPath})
                  </Select.Option>
                ))}
              </Select>
            </Form.Item>
            <Form.Item name="credentialId" label="凭证">
              <Select placeholder="可选：覆盖仓库凭证" allowClear>
                {credentials.map((cred) => (
                  <Select.Option key={cred.id} value={cred.id}>
                    {cred.name} ({cred.type})
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
      case "MAVEN":
        return (
          <>
            <Form.Item
              name="repositoryId"
              label="仓库"
              rules={[{ required: true, message: "请选择仓库" }]}
            >
              <Select placeholder="请选择仓库">
                {repositories.map((repo) => (
                  <Select.Option key={repo.id} value={repo.id}>
                    {repo.name} ({repo.projectPath})
                  </Select.Option>
                ))}
              </Select>
            </Form.Item>
            <Form.Item name="credentialId" label="凭证">
              <Select placeholder="可选：覆盖仓库凭证" allowClear>
                {credentials.map((cred) => (
                  <Select.Option key={cred.id} value={cred.id}>
                    {cred.name} ({cred.type})
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
      case "NEXTCLOUD":
        return (
          <>
            <Form.Item
              name="repositoryId"
              label="仓库"
              rules={[{ required: true, message: "请选择仓库" }]}
            >
              <Select placeholder="请选择仓库">
                {repositories.map((repo) => (
                  <Select.Option key={repo.id} value={repo.id}>
                    {repo.name} ({repo.projectPath})
                  </Select.Option>
                ))}
              </Select>
            </Form.Item>
            <Form.Item name="credentialId" label="凭证">
              <Select placeholder="可选：覆盖仓库凭证" allowClear>
                {credentials.map((cred) => (
                  <Select.Option key={cred.id} value={cred.id}>
                    {cred.name} ({cred.type})
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
      case "HTTP":
      case "HTTPS":
        return (
          <>
            <Form.Item
              name="url"
              label="下载地址"
              rules={[{ required: true, message: "请输入下载地址" }]}
            >
              <Input placeholder="请输入下载地址" />
            </Form.Item>
            <Form.Item name="fileName" label="文件名">
              <Input placeholder="可选：自定义下载文件名" />
            </Form.Item>
            <Form.Item name="credentialId" label="凭证">
              <Select placeholder="可选：关联凭证" allowClear>
                {credentials.map((cred) => (
                  <Select.Option key={cred.id} value={cred.id}>
                    {cred.name} ({cred.type})
                  </Select.Option>
                ))}
              </Select>
            </Form.Item>
          </>
        );
      case "LOCAL":
        return (
          <Form.Item
            name="file"
            label="文件名"
            rules={[{ required: true, message: "请输入文件名" }]}
          >
            <Input placeholder="请输入文件名" />
          </Form.Item>
        );
      default:
        return (
          <Form.Item
            name="rawConfig"
            label="配置 (JSON)"
            rules={[{ required: true, message: "请输入配置" }]}
          >
            <Input.TextArea rows={6} placeholder='{"key": "value"}' />
          </Form.Item>
        );
    }
  };

  return (
    <>
      <ProTable<AgentSource>
        headerTitle="资源来源列表"
        actionRef={actionRef}
        rowKey="id"
        search={{
          labelWidth: 120,
          optionRender: (_searchConfig, _formProps, dom) => [
            ...dom.reverse(),
            <Button
              key="reset"
              onClick={() => {
                setSearchKeyword("");
                setFilterType(undefined);
                actionRef.current?.reload();
              }}
            >
              重置
            </Button>,
          ],
        }}
        toolBarRender={() => [
          <Space key="filters">
            <Select
              style={{ width: 150 }}
              placeholder="类型筛选"
              allowClear
              value={filterType}
              onChange={(value) => {
                setFilterType(value);
                actionRef.current?.reload();
              }}
            >
              {Object.entries(sourceTypeLabels).map(([key, label]) => (
                <Select.Option key={key} value={key}>
                  {label}
                </Select.Option>
              ))}
            </Select>
            <Input.Search
              style={{ width: 200 }}
              placeholder="搜索名称"
              allowClear
              value={searchKeyword}
              onChange={(e) => setSearchKeyword(e.target.value)}
              onSearch={() => actionRef.current?.reload()}
            />
          </Space>,
          <Button
            key="button"
            icon={<PlusOutlined />}
            type="primary"
            onClick={() => {
              setEditingItem(null);
              setDrawerVisible(true);
            }}
          >
            新建资源来源
          </Button>,
        ]}
        request={async (params) => {
          const res = await queryAgentSources();
          let filteredData = res;

          // 类型筛选
          if (filterType) {
            filteredData = filteredData.filter(
              (item) => item.type === filterType
            );
          }

          // 关键字搜索
          if (params.name || searchKeyword) {
            const keyword = params.name || searchKeyword;
            filteredData = filteredData.filter((item) =>
              item.name.toLowerCase().includes(keyword.toLowerCase())
            );
          }

          return {
            data: filteredData,
            success: true,
            total: filteredData.length,
          };
        }}
        columns={columns}
        pagination={{
          defaultPageSize: 10,
          showSizeChanger: true,
          showQuickJumper: true,
        }}
      />

      <DrawerForm
        visible={drawerVisible}
        title={editingItem ? "编辑资源来源" : "新建资源来源"}
        width={600}
        onClose={handleClose}
        onSave={handleSave}
        initialValues={buildInitialValues()}
      >
        <Form.Item
          name="name"
          label="资源名称"
          rules={[{ required: true, message: "请输入资源名称" }]}
        >
          <Input placeholder="请输入资源名称" />
        </Form.Item>
        <Form.Item
          name="type"
          label="资源类型"
          rules={[{ required: true, message: "请选择资源类型" }]}
        >
          <Select>
            {Object.entries(sourceTypeLabels).map(([key, label]) => (
              <Select.Option key={key} value={key}>
                {label}
              </Select.Option>
            ))}
          </Select>
        </Form.Item>
        <Form.Item noStyle shouldUpdate>
          {({ getFieldValue }) => {
            const type = getFieldValue("type") as AgentSourceType;
            return renderConfigForm(type || "GITLAB");
          }}
        </Form.Item>
      </DrawerForm>
    </>
  );
};

export default AgentSourcePage;