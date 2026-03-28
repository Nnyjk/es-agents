# Frontend 模块测试规范

## 测试框架

Frontend 模块使用 Vitest 作为测试框架：

- **Vitest**：快速、Vite 原生测试框架
- **Testing Library**：React 组件测试工具
  - `@testing-library/react`：组件渲染和查询
  - `@testing-library/jest-dom`：DOM 断言扩展
  - `@testing-library/user-event`：用户交互模拟
- **jsdom**：DOM 环境模拟

## 测试命名规范

### 测试文件命名

测试文件命名遵循 `{ComponentName}.test.tsx` 格式：

```
frontend/src/
├── components/
│   ├── monitoring/
│   │   ├── MetricCard.tsx
│   │   ├── MetricCard.test.tsx
│   ├── DrawerForm/
│   │   ├── index.tsx
│   │   ├── index.test.tsx
├── pages/
│   ├── infrastructure/Host/components/
│   │   ├── HostInstallGuideModal.tsx
│   │   ├── HostInstallGuideModal.test.tsx
```

### 测试结构命名

使用 `describe` 组织测试套件，测试用例使用清晰的描述：

```tsx
describe("MetricCard", () => {
  describe("rendering", () => {
    it("renders title and value correctly", () => { ... });
    it("shows loading state", () => { ... });
  });

  describe("interactions", () => {
    it("handles trend display", () => { ... });
  });
});
```

## 配置说明

### Vitest 配置

测试配置集成在 `vite.config.ts` 中：

```typescript
export default defineConfig({
  test: {
    environment: "jsdom",
    setupFiles: "./src/test/setup.ts",
    globals: true,
  },
});
```

### 测试设置文件

`src/test/setup.ts` 配置测试环境：

```typescript
import "@testing-library/jest-dom/vitest";

// Mock window.matchMedia for responsive components
Object.defineProperty(window, "matchMedia", { ... });
```

## 运行测试

### 运行所有测试

```bash
cd frontend
npm run test
```

### 运行特定文件测试

```bash
npm run test -- src/components/monitoring/MetricCard.test.tsx
```

### 运行监视模式

```bash
npm run test -- --watch
```

### 查看覆盖率

```bash
npm run test -- --coverage
```

## 测试编写指南

### 1. 基础渲染测试

```tsx
import { describe, expect, it } from "vitest";
import { render, screen } from "@testing-library/react";
import { MetricCard } from "./MetricCard";

describe("MetricCard", () => {
  it("renders title and value", () => {
    render(<MetricCard title="CPU" value={75.5} />);

    expect(screen.getByText("CPU")).toBeInTheDocument();
    expect(screen.getByText("75.5")).toBeInTheDocument();
  });
});
```

### 2. Props 测试

```tsx
it("displays unit suffix", () => {
  render(<MetricCard title="Memory" value={1024} unit="MB" />);

  expect(screen.getByText("MB")).toBeInTheDocument();
});
```

### 3. 条件渲染测试

```tsx
it("shows trend when provided", () => {
  render(<MetricCard title="CPU" value={50} trend="up" trendValue={10} />);

  expect(screen.getByText("10")).toBeInTheDocument();
  expect(screen.getByText(/上升/)).toBeInTheDocument();
});

it("hides trend when not provided", () => {
  render(<MetricCard title="CPU" value={50} />);

  expect(screen.queryByText(/上升/)).not.toBeInTheDocument();
});
```

### 4. 加载状态测试

```tsx
it("shows loading state", () => {
  render(<MetricCard title="CPU" value={50} loading />);

  // Ant Design Card loading shows skeleton
  expect(screen.getByRole("img", { hidden: true })).toBeInTheDocument();
});
```

### 5. 用户交互测试

```tsx
import { userEvent } from "@testing-library/user-event";

it("calls onChange when clicked", async () => {
  const user = userEvent.setup();
  const handleChange = vi.fn();

  render(<SelectButton onChange={handleChange} />);

  await user.click(screen.getByRole("button"));

  expect(handleChange).toHaveBeenCalled();
});
```

### 6. Mock API 测试

```tsx
import { vi } from "vitest";

vi.mock("@/services/api", () => ({
  fetchData: vi.fn().mockResolvedValue({ data: [] }),
}));

it("loads data on mount", async () => {
  render(<DataComponent />);

  await waitFor(() => {
    expect(screen.getByText("Loaded")).toBeInTheDocument();
  });
});
```

## 测试最佳实践

### 1. 使用 getByRole 查询

优先使用语义化查询：

```tsx
// Good
expect(screen.getByRole("button")).toBeInTheDocument();
expect(screen.getByRole("heading", { name: "Title" })).toBeInTheDocument();

// Avoid
expect(screen.getByTestId("button")).toBeInTheDocument();
```

### 2. 测试用户可见行为

测试用户能看到和操作的：

```tsx
// Good - 测试渲染结果
it("shows success message", () => {
  render(<StatusMessage status="success" />);
  expect(screen.getByText("操作成功")).toBeInTheDocument();
});

// Avoid - 测试内部状态
it("sets state to success", () => {
  // ...
  expect(component.state.status).toBe("success");
});
```

### 3. 使用 userEvent 模拟交互

```tsx
// Good - 模拟真实用户行为
const user = userEvent.setup();
await user.click(button);
await user.type(input, "text");

// Avoid - 直接触发事件
fireEvent.click(button);
```

### 4. 异步测试

```tsx
import { waitFor } from "@testing-library/react";

it("shows data after loading", async () => {
  render(<AsyncComponent />);

  await waitFor(() => {
    expect(screen.getByText("Data loaded")).toBeInTheDocument();
  });
});
```

### 5. Mock 外部依赖

```tsx
// Mock hooks
vi.mock("@/hooks/useData", () => ({
  useData: vi.fn().mockReturnValue({ data: [], loading: false }),
}));

// Mock components
vi.mock("./ChildComponent", () => ({
  ChildComponent: () => <div>Mocked Child</div>,
}));
```

## 覆盖率要求

- 组件渲染测试覆盖率：≥ 80%
- 用户交互测试覆盖率：≥ 70%
- 工具函数测试覆盖率：≥ 90%

## CI 集成

测试通过 CI 自动运行：

- 每次提交触发测试
- PR 合并前必须通过测试
- 覆盖率阈值检查
