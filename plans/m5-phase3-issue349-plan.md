# M5 Phase 3 - Issue #349: 深色模式支持

## 目标
为 ESA 前端添加深色模式支持，提升用户体验和视觉舒适度。

## 技术方案

### 1. CSS 变量主题系统
- 定义 CSS 变量用于主题颜色
- 创建 `:root` (浅色) 和 `[data-theme="dark"]` (深色) 两套变量
- 覆盖所有颜色相关的 CSS 属性

### 2. ThemeProvider
- 创建 React Context 管理主题状态
- 持久化主题选择到 localStorage
- 提供 `useTheme` hook 供组件使用

### 3. ThemeToggle 组件
- 添加主题切换按钮（太阳/月亮图标）
- 放置在导航栏或设置区域
- 支持平滑过渡动画

## 实现步骤

### Step 1: CSS 变量定义
**文件**: `frontend/src/styles/theme.css`
```css
:root {
  --bg-primary: #ffffff;
  --bg-secondary: #f5f5f5;
  --text-primary: #1a1a1a;
  --text-secondary: #666666;
  --border-color: #e0e0e0;
  --accent-color: #1890ff;
  /* ... 更多变量 */
}

[data-theme="dark"] {
  --bg-primary: #1a1a1a;
  --bg-secondary: #2d2d2d;
  --text-primary: #ffffff;
  --text-secondary: #b0b0b0;
  --border-color: #404040;
  --accent-color: #40a9ff;
  /* ... 更多变量 */
}
```

### Step 2: ThemeProvider 实现
**文件**: `frontend/src/contexts/ThemeContext.tsx`
- 创建 ThemeContext
- 实现 ThemeProvider 组件
- 导出 useTheme hook

### Step 3: ThemeToggle 组件
**文件**: `frontend/src/components/ThemeToggle.tsx`
- 切换按钮 UI
- 点击事件处理
- 图标切换（太阳/月亮）

### Step 4: 应用主题
- 在 `frontend/src/App.tsx` 中包裹 ThemeProvider
- 在 `frontend/src/index.css` 中导入 theme.css
- 确保所有组件使用 CSS 变量

### Step 5: 测试与优化
- 测试主题切换功能
- 检查所有页面在深色模式下的显示
- 优化过渡动画

## 验收标准
- [ ] 主题切换按钮可见且可用
- [ ] 深色模式下所有页面正常显示
- [ ] 主题选择持久化（刷新后保持）
- [ ] 切换动画流畅
- [ ] 无控制台错误

## 相关文件
- `frontend/src/styles/theme.css`
- `frontend/src/contexts/ThemeContext.tsx`
- `frontend/src/components/ThemeToggle.tsx`
- `frontend/src/App.tsx`
- `frontend/src/index.css`

## 估计工时
- 开发：4-6 小时
- 测试：1-2 小时
