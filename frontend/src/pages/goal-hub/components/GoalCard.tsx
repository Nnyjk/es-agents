import React from "react";
import { Card } from "antd";
import { useNavigate } from "react-router-dom";
import {
  RocketOutlined,
  EyeOutlined,
  CodeOutlined,
  DesktopOutlined,
  EnvironmentOutlined,
  HistoryOutlined,
} from "@ant-design/icons";
import type { GoalCard as GoalCardType } from "../types";
import styles from "../GoalHub.module.css";

const iconMap: Record<string, React.ReactNode> = {
  RocketOutlined: <RocketOutlined />,
  EyeOutlined: <EyeOutlined />,
  CodeOutlined: <CodeOutlined />,
  DesktopOutlined: <DesktopOutlined />,
  EnvironmentOutlined: <EnvironmentOutlined />,
  HistoryOutlined: <HistoryOutlined />,
};

interface GoalCardProps {
  card: GoalCardType;
}

const GoalCard: React.FC<GoalCardProps> = ({ card }) => {
  const navigate = useNavigate();

  return (
    <Card
      hoverable
      className={styles.goalCard}
      onClick={() => navigate(card.route)}
    >
      <div
        className={styles.iconWrapper}
        style={{ color: card.color || "#1890ff" }}
      >
        {iconMap[card.icon] || <RocketOutlined />}
      </div>
      <h3 className={styles.cardTitle}>{card.title}</h3>
      <p className={styles.cardDescription}>{card.description}</p>
      <span className={styles.actionText}>{card.actionText}</span>
    </Card>
  );
};

export default GoalCard;
