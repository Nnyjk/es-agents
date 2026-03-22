declare module "echarts-for-react" {
  import { ComponentType } from "react";

  export interface EChartsOption {
    [key: string]: unknown;
  }

  export interface ReactEChartsProps {
    option: EChartsOption;
    style?: React.CSSProperties;
    className?: string;
    notMerge?: boolean;
    lazyUpdate?: boolean;
    theme?: string | object;
    opts?: {
      devicePixelRatio?: number;
      renderer?: "canvas" | "svg";
      width?: number | "auto";
      height?: number | "auto";
    };
    onChartReady?: (instance: unknown) => void;
    onEvents?: Record<string, Function>;
  }

  const ReactECharts: ComponentType<ReactEChartsProps>;
  export default ReactECharts;
}

declare module "echarts" {
  export interface EChartsOption {
    [key: string]: unknown;
  }
  export function init(dom: HTMLElement, theme?: string | object): unknown;
  export function dispose(dom: HTMLElement): void;
  const echarts: {
    init: typeof init;
    dispose: typeof dispose;
  };
  export default echarts;
}