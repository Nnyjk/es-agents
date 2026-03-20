import React from "react";
import { Card, Row, Col, Typography, Button } from "antd";
import { useNavigate } from "react-router-dom";
import { RocketOutlined, SettingOutlined, ClusterOutlined, SafetyOutlined, MonitorOutlined } from "@ant-design/icons";

const { Title, Text } = Typography;

interface GoalCardProps {
  title: string;
  description: string;
  icon: React.ReactNode;
  path: string;
  color: string;
}

const GoalCard: React.FC<GoalCardProps> = ({ title, description, icon, path, color }) => {
  const navigate = useNavigate();
  
  return (
    <Card
      hoverable
      style={{ height: "100%", textAlign: "center" }}
      onClick={() => navigate(path)}
    >
      <div style={{ fontSize: 48, color, marginBottom: 16 }}>
        {icon}
      </div>
      <Title level={4} style={{ marginBottom: 8 }}>{title}</Title>
      <Text type="secondary">{description}</Text>
    </Card>
  );
};

const GoalHub: React.FC = () => {
  const navigate = useNavigate();
  
  const goals = [
    {
      title: "部署 Agent",
      description: "选择环境与主机，部署 Agent 到目标服务器",
      icon: <RocketOutlined />,
      path: "/goals/deploy-agent",
      color: "#1890ff",
    },
    {
      title: "执行命令",
      description: "在指定主机上执行预设或自定义命令",
      icon: <TerminalOutlined />,
      path: "/agents/commands",
      color: "#52c41a",
    },
    {
      title: "管理资源",
      description: "管理 Agent 运行时所需的文件和配置资源",
      icon: <FolderOutlined />,
      path: "/agents/resources",
      color: "#faad14",
    },
  ];

  return (
    <div style={{ padding: "0 24px" }}>
      <Title level={2} style={{ marginBottom: 24 }}>目标中心</Title>
      <Text type="secondary" style={{ marginBottom: 24, display: "block" }}>
        选择您要执行的目标任务
      </Text>
      
      <Row gutter={[24, 24]}>
        <Col xs={24} sm={12} lg={8}>
          <GoalCard
            title="部署 Agent"
            description="选择环境与主机，部署 Agent 到目标服务器"
            icon={<RocketOutlined />}
            path="/goals/deploy-agent"
            color="#1890ff"
          />
        </Col>
        <Col xs={24} sm={12} lg={8}>
          <Card
            hoverable
            style={{ height: "100%", textAlign: "center", opacity: 0.6 }}
            onClick={() => {}}
          >
            <div style={{ fontSize: 48, color: "#52c41a", marginBottom: 16 }}>
              <TerminalOutlined />
            </div>
            <Title level={4} style={{ marginBottom: 8 }}>执行命令</Title>
            <Text type="secondary">即将上线</Text>
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={8}>
          <Card
            hoverable
            style={{ height: "100%", textAlign: "center", opacity: 0.6 }}
            onClick={() => {}}
          >
            <div style={{ fontSize: 48, color: "#faad14", marginBottom: 16 }}>
              <FolderOutlined />
            </div>
            <Title level={4} style={{ marginBottom: 8 }}>管理资源</Title>
            <Text type="secondary">即将上线</Text>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

// 临时定义缺失的图标
const TerminalOutlined = () => <span>⌨️</span>;
const FolderOutlined = () => <span>📁</span>;

export default GoalHub;