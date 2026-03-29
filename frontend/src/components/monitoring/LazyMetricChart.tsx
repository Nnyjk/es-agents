import React, { lazy, Suspense } from 'react';
import { Spin, Card } from 'antd';
import type { TimeSeriesData, TimeRangePreset } from '../../types/monitoring';

// 懒加载 Recharts
const MetricChart = lazy(() => import('./MetricChart'));

interface LazyMetricChartProps {
  title: string;
  data: TimeSeriesData[];
  loading?: boolean;
  unit?: string;
  color?: string;
  showTimeRangeSelector?: boolean;
  timeRange?: TimeRangePreset;
  onTimeRangeChange?: (range: TimeRangePreset) => void;
  style?: React.CSSProperties;
  className?: string;
  height?: number;
}

/**
 * 懒加载的 MetricChart 组件
 * 用于减少初始包体积，仅在需要时加载 Recharts
 */
export const LazyMetricChart: React.FC<LazyMetricChartProps> = (props) => {
  const defaultLoading = (
    <Card title={props.title} style={{ height: props.height || 300 }}>
      <div style={{ 
        display: 'flex', 
        alignItems: 'center', 
        justifyContent: 'center',
        height: '100%'
      }}>
        <Spin tip="加载图表..." />
      </div>
    </Card>
  );

  return (
    <Suspense fallback={defaultLoading}>
      <MetricChart {...props} />
    </Suspense>
  );
};

export default LazyMetricChart;
