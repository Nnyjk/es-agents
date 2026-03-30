import react from '@vitejs/plugin-react'
import { defineConfig } from 'vitest/config'
import path from 'path'
import { visualizer } from 'rollup-plugin-visualizer'
import viteCompression from 'vite-plugin-compression'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    react(),
    // Rollup 可视化分析 - 生成 bundle 分析报告
    visualizer({
      open: false,
      gzipSize: true,
      brotliSize: true,
      filename: 'dist/stats.html',
    }),
    // Gzip 压缩
    viteCompression({
      verbose: true,
      disable: false,
      threshold: 10240,
      algorithm: 'gzip',
      ext: '.gz',
    }),
    // Brotli 压缩
    viteCompression({
      verbose: true,
      disable: false,
      threshold: 10240,
      algorithm: 'brotliCompress',
      ext: '.br',
    }),
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  test: {
    environment: 'jsdom',
    setupFiles: './src/test/setup.ts',
    globals: true,
    coverage: {
      provider: 'v8',
      reporter: ['lcov', 'text'],
      directory: 'coverage',
      include: ['src/**/*.{ts,tsx}'],
      exclude: ['src/**/*.test.{ts,tsx}', 'src/test/**/*'],
    },
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/v1': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/agents': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/auth': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/hosts': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/deployments': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/q/openapi': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    }
  },
  // ============================================
  // Build Optimization (M5 Issue #343)
  // ============================================
  build: {
    // Code splitting strategy
    rollupOptions: {
      output: {
        manualChunks: {
          // Vendor chunks for better caching
          'react-vendor': ['react', 'react-dom', 'react-router-dom'],
          'antd-vendor': ['antd', '@ant-design/icons', '@ant-design/pro-components'],
          'chart-vendor': ['recharts'],
          'editor-vendor': ['@monaco-editor/react'],
          'terminal-vendor': ['xterm', 'xterm-addon-fit', 'xterm-for-react'],
        },
      },
    },
    // Chunk size limit for warnings
    chunkSizeWarningLimit: 500,
    // Minification
    minify: 'esbuild',
    // Source maps for production debugging
    sourcemap: false,
    // Target modern browsers
    target: 'esnext',
    // Compress assets
    cssCodeSplit: true,
  },
  // Esbuild optimization
  esbuild: {
    drop: process.env.NODE_ENV === 'production' ? ['console', 'debugger'] : [],
  },
})
