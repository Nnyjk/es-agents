import React from "react";
import { Typography } from "antd";
import GoalCard from "./components/GoalCard";
import styles from "./GoalHub.module.css";
import type { GoalCard as GoalCardType } from "./types";

const { Title, Text } = Typography;

const goalCards: GoalCardType[] = [
  {
    id: "deploy-agent",
    title: "部署 Agent",
    description: "从模板创建并部署 Agent 实例到目标主机",
    icon: "RocketOutlined",
    actionText: "开始部署",
    route: "/agents/templates/new",
    color: "#1890ff",
  },
  {
    id: "view-agent-status",
    title: "查看 Agent 状态",
    description: "查看 Agent 实例列表和运行状态",
    icon: "EyeOutlined",
    actionText: "查看列表",
    route: "/agents",
    color: "#52c41a",
  },
  {
    id: "execute-command",
    title: "执行命令",
    description: "向 Agent 发送命令并查看执行结果",
    icon: "CodeOutlined",
    actionText: "执行命令",
    route: "/agents/execute",
    color: "#722ed1",
  },
  {
    id: "manage-hosts",
    title: "管理主机",
    description: "管理目标主机资源和连接配置",
    icon: "DesktopOutlined",
    actionText: "管理主机",
    route: "/infra/hosts",
    color: "#13c2c2",
  },
  {
    id: "manage-environments",
    title: "管理环境",
    description: "管理环境配置和部署目标",
    icon: "EnvironmentOutlined",
    actionText: "管理环境",
    route: "/infra/envs",
    color: "#fa8c16",
  },
  {
    id: "deployment-history",
    title: "查看部署历史",
    description: "查看部署记录和状态详情",
    icon: "HistoryOutlined",
    actionText: "查看历史",
    route: "/deployment",
    color: "#eb2f96",
  },
];

const GoalHub: React.FC = () => {
  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <Title level={2} className={styles.title}>
          目标中心
        </Title>
        <Text type="secondary" className={styles.subtitle}>
          选择您要执行的目标任务，快速开始工作
        </Text>
      </div>

      <div className={styles.grid}>
        {goalCards.map((card) => (
          <GoalCard key={card.id} card={card} />
        ))}
      </div>
    </div>
  );
};

export default GoalHub;