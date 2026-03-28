import React from "react";
import { Card, Select, Spin } from "antd";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Legend,
} from "recharts";
import type { TimeSeriesData, TimeRangePreset } from "../../types/monitoring";
import styles from "./MetricChart.module.css";

interface MetricChartProps {
  /** 图表标题 */
  title: string;
  /** 时序数据 */
  data: TimeSeriesData[];
  /** 数据加载状态 */
  loading?: boolean;
  /** Y 轴单位 */
  unit?: string;
  /** 颜色 */
  color?: string;
  /** 时间范围选择器 */
  showTimeRangeSelector?: boolean;
  /** 当前时间范围 */
  timeRange?: TimeRangePreset;
  /** 时间范围变更回调 */
  onTimeRangeChange?: (range: TimeRangePreset) => void;
  /** 自定义样式 */
  style?: React.CSSProperties;
  /** 自定义类名 */
  className?: string;
  /** 图表高度 */
  height?: number;
}

/**
 * 时间范围选项
 */
const TIME_RANGE_OPTIONS: { value: TimeRangePreset; label: string }[] = [
  { value: "5m", label: "5 分钟" },
  { value: "15m", label: "15 分钟" },
  { value: "30m", label: "30 分钟" },
  { value: "1h", label: "1 小时" },
  { value: "3h", label: "3 小时" },
  { value: "6h", label: "6 小时" },
  { value: "12h", label: "12 小时" },
  { value: "24h", label: "24 小时" },
  { value: "7d", label: "7 天" },
  { value: "30d", label: "30 天" },
];

/**
 * 格式化时间戳
 */
const formatTimestamp = (timestamp: string): string => {
  const date = new Date(timestamp);
  return date.toLocaleTimeString("zh-CN", {
    hour: "2-digit",
    minute: "2-digit",
  });
};

/**
 * 格式化数值
 */
const formatValue = (value: number, unit?: string): string => {
  if (unit === "%") {
    return `${value.toFixed(1)}%`;
  }
  if (value >= 1000) {
    return `${(value / 1000).toFixed(1)}K`;
  }
  return value.toFixed(2);
};

/**
 * 监控指标图表组件
 * 使用 Recharts 折线图展示时序数据
 */
const MetricChart: React.FC<MetricChartProps> = ({
  title,
  data,
  loading = false,
  unit,
  color = "#1890ff",
  showTimeRangeSelector = false,
  timeRange = "1h",
  onTimeRangeChange,
  style,
  className,
  height = 300,
}) => {
  // 转换数据格式供 Recharts 使用
  const chartData = data.map((item) => ({
    time: formatTimestamp(item.timestamp),
    value: item.value,
    originalTime: item.timestamp,
  }));

  return (
    <Card
      title={title}
      className={`${styles.metricChart} ${className || ""}`}
      style={style}
      extra={
        showTimeRangeSelector ? (
          <Select
            value={timeRange}
            onChange={onTimeRangeChange}
            options={TIME_RANGE_OPTIONS}
            style={{ width: 120 }}
            size="small"
          />
        ) : null
      }
    >
      <Spin spinning={loading}>
        <ResponsiveContainer width="100%" height={height}>
          <LineChart
            data={chartData}
            margin={{ top: 5, right: 20, left: 10, bottom: 5 }}
          >
            <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
            <XAxis
              dataKey="time"
              tick={{ fontSize: 12 }}
              tickLine={{ stroke: "#d9d9d9" }}
              axisLine={{ stroke: "#d9d9d9" }}
            />
            <YAxis
              tick={{ fontSize: 12 }}
              tickLine={{ stroke: "#d9d9d9" }}
              axisLine={{ stroke: "#d9d9d9" }}
              tickFormatter={(value) => formatValue(value, unit)}
            />
            <Tooltip
              contentStyle={{
                borderRadius: 4,
                border: "1px solid #d9d9d9",
                boxShadow: "0 2px 8px rgba(0, 0, 0, 0.1)",
              }}
              formatter={(value) => [
                formatValue(Number(value) || 0, unit),
                "值",
              ]}
              labelFormatter={(label) => `时间: ${label}`}
            />
            <Legend />
            <Line
              type="monotone"
              dataKey="value"
              stroke={color}
              strokeWidth={2}
              dot={{ fill: color, r: 2 }}
              activeDot={{ r: 4 }}
              name={title}
            />
          </LineChart>
        </ResponsiveContainer>
      </Spin>
    </Card>
  );
};

export { MetricChart };
export default MetricChart;
