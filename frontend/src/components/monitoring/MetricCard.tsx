import React from "react";
import { Card, Statistic } from "antd";
import { ArrowUpOutlined, ArrowDownOutlined } from "@ant-design/icons";
import type { TrendDirection } from "../../types/monitoring";
import styles from "./MetricCard.module.css";

interface MetricCardProps {
  /** 标题 */
  title: string;
  /** 数值 */
  value: number;
  /** 单位 */
  unit?: string;
  /** 趋势方向 */
  trend?: TrendDirection;
  /** 趋势百分比 */
  trendValue?: number;
  /** 精度 */
  precision?: number;
  /** 自定义样式 */
  style?: React.CSSProperties;
  /** 自定义类名 */
  className?: string;
  /** 加载状态 */
  loading?: boolean;
}

/**
 * 获取趋势图标
 */
const getTrendIcon = (trend: TrendDirection): React.ReactNode => {
  if (trend === "up") {
    return <ArrowUpOutlined />;
  }
  if (trend === "down") {
    return <ArrowDownOutlined />;
  }
  return null;
};

/**
 * 获取趋势颜色
 */
const getTrendColor = (trend: TrendDirection): string => {
  if (trend === "up") {
    return "#52c41a";
  }
  if (trend === "down") {
    return "#ff4d4f";
  }
  return "rgba(0, 0, 0, 0.45)";
};

/**
 * 监控指标卡片组件
 * 用于展示单个监控指标的数值和趋势
 */
const MetricCard: React.FC<MetricCardProps> = ({
  title,
  value,
  unit,
  trend,
  trendValue,
  precision = 2,
  style,
  className,
  loading = false,
}) => {
  const trendIcon = trend ? getTrendIcon(trend) : null;
  const trendColor = trend ? getTrendColor(trend) : undefined;

  return (
    <Card
      className={`${styles.metricCard} ${className || ""}`}
      style={style}
      loading={loading}
    >
      <Statistic
        title={title}
        value={value}
        precision={precision}
        suffix={unit}
        valueStyle={{ fontWeight: 600 }}
      />
      {trend && trendValue !== undefined && (
        <div className={styles.trendContainer}>
          <Statistic
            value={trendValue}
            precision={1}
            prefix={trendIcon}
            suffix="%"
            valueStyle={{
              fontSize: 12,
              color: trendColor,
            }}
          />
          <span className={styles.trendLabel}>
            {trend === "up" ? "较上一周期上升" : "较上一周期下降"}
          </span>
        </div>
      )}
    </Card>
  );
};

export { MetricCard };
export default MetricCard;
