import React, { useEffect, useState, useCallback } from "react";
import {
  Row,
  Col,
  Button,
  Space,
  Typography,
  Breadcrumb,
  Result,
  Spin,
  message,
} from "antd";
import {
  ArrowLeftOutlined,
  ReloadOutlined,
  HomeOutlined,
  SettingOutlined,
} from "@ant-design/icons";
import { useParams, useNavigate } from "react-router-dom";
import { PageContainer } from "@ant-design/pro-components";
import AgentStatusCard from "./components/AgentStatusCard";
import AgentLogViewer from "./components/AgentLogViewer";
import AgentHistory from "./components/AgentHistory";
import AgentActions from "./components/AgentActions";
import type { AgentDetailData, AgentActionType } from "./types";
import styles from "./AgentDetail.module.css";

const { Title, Text } = Typography;

/**
 * Agent 实例详情页
 * 展示 Agent 状态、日志、部署历史等信息
 */
const AgentDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [agent, setAgent] = useState<AgentDetailData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [refreshKey, setRefreshKey] = useState(0);

  // 加载 Agent 详情
  const loadAgentDetail = useCallback(async () => {
    if (!id) return;

    setLoading(true);
    setError(null);

    try {
      const response = await fetch(`/api/agents/instances/${id}`);
      if (response.ok) {
        const data = await response.json();
        // 转换数据格式
        const agentData: AgentDetailData = {
          id: data.id,
          hostId: data.host?.id || data.hostId,
          hostName: data.host?.name || data.hostName || "-",
          hostIp: data.host?.hostname || data.hostIp,
          templateId: data.template?.id || data.templateId,
          templateName: data.template?.name || data.templateName || "-",
          status: data.status,
          version: data.version,
          lastHeartbeatTime: data.lastHeartbeatTime,
          heartbeatAgeSeconds: data.heartbeatAgeSeconds,
          isOnline: data.status === "ONLINE",
          createdAt: data.createdAt,
          updatedAt: data.updatedAt,
        };
        setAgent(agentData);
      } else if (response.status === 404) {
        setError("Agent 不存在或已被删除");
      } else {
        const result = await response.json();
        setError(result.message || "加载失败");
      }
    } catch (e) {
      const errorMessage = e instanceof Error ? e.message : "网络请求失败";
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  }, [id]);

  // 初始加载
  useEffect(() => {
    loadAgentDetail();
  }, [loadAgentDetail, refreshKey]);

  // 刷新
  const handleRefresh = () => {
    setRefreshKey((prev) => prev + 1);
  };

  // 返回列表
  const handleBack = () => {
    navigate("/agents");
  };

  // 状态变更处理
  const handleStatusChange = (status: string) => {
    if (agent) {
      setAgent({
        ...agent,
        status: status as AgentDetailData["status"],
        isOnline: status === "ONLINE",
      });
    }
  };

  // 操作完成处理
  const handleActionComplete = (
    action: AgentActionType,
    result: { success: boolean; message?: string; taskId?: string },
  ) => {
    if (action === "DELETE" && result.success) {
      message.success("Agent 已删除，即将返回列表");
      setTimeout(() => navigate("/agents"), 1500);
    } else if (result.success) {
      // 刷新数据
      handleRefresh();
    }
  };

  // 加载中状态
  if (loading && !agent) {
    return (
      <PageContainer>
        <div className={styles.loadingContainer}>
          <Spin size="large" tip="加载中..." />
        </div>
      </PageContainer>
    );
  }

  // 错误状态
  if (error) {
    return (
      <PageContainer>
        <div className={styles.errorContainer}>
          <Result
            status="error"
            title="加载失败"
            subTitle={error}
            extra={[
              <Button type="primary" key="retry" onClick={handleRefresh}>
                重试
              </Button>,
              <Button key="back" onClick={handleBack}>
                返回列表
              </Button>,
            ]}
          />
        </div>
      </PageContainer>
    );
  }

  // Agent 不存在
  if (!agent) {
    return (
      <PageContainer>
        <div className={styles.errorContainer}>
          <Result
            status="404"
            title="Agent 不存在"
            subTitle="该 Agent 可能已被删除或 ID 无效"
            extra={[
              <Button type="primary" key="back" onClick={handleBack}>
                返回列表
              </Button>,
            ]}
          />
        </div>
      </PageContainer>
    );
  }

  return (
    <PageContainer>
      <div className={styles.agentDetail}>
        {/* 头部导航 */}
        <div className={styles.header}>
          <div className={styles.titleRow}>
            <Breadcrumb
              items={[
                { href: "/", title: <HomeOutlined /> },
                { href: "/agents", title: "Agent 管理" },
                { title: agent.templateName },
              ]}
            />
            <Title level={4} style={{ margin: 0 }}>
              <SettingOutlined /> {agent.templateName} - {agent.hostName}
            </Title>
            <Text type="secondary" copyable={{ text: agent.id }}>
              ID: {agent.id.slice(0, 8)}...
            </Text>
          </div>
          <Space>
            <Button icon={<ReloadOutlined />} onClick={handleRefresh}>
              刷新
            </Button>
            <Button icon={<ArrowLeftOutlined />} onClick={handleBack}>
              返回
            </Button>
          </Space>
        </div>

        {/* 主内容区 */}
        <Row gutter={[24, 24]}>
          {/* 左侧 - 状态与操作 */}
          <Col xs={24} lg={8}>
            <Space direction="vertical" size="large" style={{ width: "100%" }}>
              <AgentStatusCard
                agent={agent}
                onStatusChange={handleStatusChange}
              />
              <AgentActions
                agentId={agent.id}
                status={agent.status}
                onActionComplete={handleActionComplete}
              />
            </Space>
          </Col>

          {/* 右侧 - 日志与历史 */}
          <Col xs={24} lg={16}>
            <Space direction="vertical" size="large" style={{ width: "100%" }}>
              <AgentLogViewer agentId={agent.id} wsConnected={agent.isOnline} />
              <AgentHistory agentId={agent.id} />
            </Space>
          </Col>
        </Row>
      </div>
    </PageContainer>
  );
};

export default AgentDetail;