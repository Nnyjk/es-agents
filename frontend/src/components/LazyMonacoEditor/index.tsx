import React, { lazy, Suspense, useState, useEffect } from 'react';
import { Spin } from 'antd';

// 懒加载 Monaco Editor
const MonacoEditor = lazy(() => import('@monaco-editor/react'));

interface LazyMonacoEditorProps {
  height?: string | number;
  defaultLanguage?: string;
  value?: string;
  onChange?: (value: string | undefined) => void;
  options?: any;
  loading?: React.ReactNode;
}

/**
 * 懒加载的 Monaco Editor 组件
 * 用于减少初始包体积，仅在需要时加载
 */
export const LazyMonacoEditor: React.FC<LazyMonacoEditorProps> = ({
  height = '100%',
  defaultLanguage = 'yaml',
  value,
  onChange,
  options = {},
  loading,
}) => {
  const [isLoaded, setIsLoaded] = useState(false);

  // 预加载 Monaco Editor
  useEffect(() => {
    const preload = async () => {
      try {
        await import('@monaco-editor/react');
        setIsLoaded(true);
      } catch (error) {
        console.error('Failed to preload Monaco Editor:', error);
        setIsLoaded(true); // 即使失败也设置为 true，让错误边界处理
      }
    };
    preload();
  }, []);

  const defaultLoading = (
    <div style={{ 
      height, 
      display: 'flex', 
      alignItems: 'center', 
      justifyContent: 'center',
      backgroundColor: '#f5f5f5'
    }}>
      <Spin tip="加载编辑器..." />
    </div>
  );

  return (
    <Suspense fallback={loading || defaultLoading}>
      {isLoaded && (
        <MonacoEditor
          height={height}
          defaultLanguage={defaultLanguage}
          value={value}
          onChange={onChange}
          options={{
            minimap: { enabled: false },
            scrollBeyondLastLine: false,
            fontSize: 14,
            ...options,
          }}
        />
      )}
    </Suspense>
  );
};

export default LazyMonacoEditor;
