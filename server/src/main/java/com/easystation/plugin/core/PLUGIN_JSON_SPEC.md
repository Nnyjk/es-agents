# Plugin.json 元数据格式规范

**版本**: 1.0.0  
**模块**: plugin-api  
**Issue**: #384

---

## 概述

`plugin.json` 是 ESA 插件的元数据描述文件，定义插件的基本信息、依赖关系、扩展点等。

---

## 文件位置

```
<plugin-root>/
├── plugin.json          # 必需：插件元数据
├── META-INF/
│   └── plugin/
│       └── ...          # 可选：插件资源
├── lib/
│   └── ...              # 可选：依赖库
└── classes/
    └── ...              # 编译后的类文件
```

---

## 完整示例

```json
{
  "$schema": "https://es-agents.io/schemas/plugin/v1/plugin.schema.json",
  "id": "github-integration",
  "name": "GitHub Integration",
  "version": "1.2.0",
  "description": "GitHub 集成插件，支持 Issue、PR、Webhook 等功能",
  "author": "ESA Team",
  "email": "team@es-agents.io",
  "license": "MIT",
  "main": "com.easystation.plugin.github.GitHubPlugin",
  "icon": "github.svg",
  "homepage": "https://github.com/Nnyjk/es-agents",
  "repository": {
    "type": "git",
    "url": "https://github.com/Nnyjk/es-agents.git"
  },
  "keywords": ["github", "integration", "git"],
  "categories": ["integration", "devops"],
  
  "engines": {
    "esa": ">=1.0.0"
  },
  
  "dependencies": [
    {
      "id": "core-plugin",
      "version": ">=1.0.0",
      "optional": false
    },
    {
      "id": "http-client",
      "version": ">=2.0.0",
      "optional": true
    }
  ],
  
  "provides": [
    "github-issue-tracker",
    "github-pr-manager",
    "github-webhook-handler"
  ],
  
  "requires": [
    "http-client",
    "json-parser"
  ],
  
  "extensionPoints": [
    {
      "name": "github-webhook-handler",
      "description": "GitHub Webhook 处理器",
      "type": "com.easystation.plugin.github.WebhookHandler"
    }
  ],
  
  "config": {
    "schema": {
      "type": "object",
      "properties": {
        "apiUrl": {
          "type": "string",
          "default": "https://api.github.com",
          "description": "GitHub API 地址"
        },
        "token": {
          "type": "string",
          "format": "password",
          "description": "GitHub Personal Access Token"
        },
        "webhookSecret": {
          "type": "string",
          "format": "password",
          "description": "Webhook 验证密钥"
        }
      },
      "required": ["token"]
    },
    "defaults": {
      "apiUrl": "https://api.github.com"
    }
  },
  
  "permissions": [
    "network:outbound",
    "storage:persistent",
    "config:read-write"
  ],
  
  "lifecycle": {
    "initTimeout": 30000,
    "startTimeout": 60000,
    "stopTimeout": 30000
  }
}
```

---

## 字段说明

### 必填字段

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | string | 插件唯一标识，建议使用 kebab-case 格式 |
| `name` | string | 插件显示名称 |
| `version` | string | 语义化版本号 (major.minor.patch) |
| `main` | string | 插件主类全限定名 |

### 可选字段

| 字段 | 类型 | 说明 | 默认值 |
|------|------|------|--------|
| `description` | string | 插件描述 | - |
| `author` | string | 作者/组织名称 | - |
| `email` | string | 联系邮箱 | - |
| `license` | string | 许可证名称 | - |
| `icon` | string | 图标文件名或 URL | - |
| `homepage` | string | 主页 URL | - |
| `repository` | object | 代码仓库信息 | - |
| `keywords` | array | 关键词列表 | [] |
| `categories` | array | 分类列表 | [] |
| `engines` | object | 引擎版本要求 | - |
| `dependencies` | array | 依赖列表 | [] |
| `provides` | array | 提供的扩展点列表 | [] |
| `requires` | array | 需要的扩展点列表 | [] |
| `extensionPoints` | array | 定义的扩展点 | [] |
| `config` | object | 配置 Schema 和默认值 | - |
| `permissions` | array | 权限列表 | [] |
| `lifecycle` | object | 生命周期超时设置 | - |

---

## 依赖管理

### 依赖格式

```json
{
  "dependencies": [
    {
      "id": "plugin-id",
      "version": ">=1.0.0",
      "optional": false
    }
  ]
}
```

### 版本约束

| 格式 | 说明 |
|------|------|
| `1.0.0` | 精确匹配 |
| `>=1.0.0` | 大于等于 |
| `>1.0.0` | 大于 |
| `<=1.0.0` | 小于等于 |
| `<1.0.0` | 小于 |
| `^1.0.0` | 兼容版本 (1.x.x) |
| `~1.0.0` | 近似版本 (1.0.x) |

---

## 扩展点

### 提供扩展点

```json
{
  "provides": ["extension-point-1", "extension-point-2"]
}
```

### 需要扩展点

```json
{
  "requires": ["extension-point-1"]
}
```

### 定义扩展点

```json
{
  "extensionPoints": [
    {
      "name": "my-extension",
      "description": "我的扩展点",
      "type": "com.example.MyExtension"
    }
  ]
}
```

---

## 配置 Schema

使用 JSON Schema 定义插件配置：

```json
{
  "config": {
    "schema": {
      "type": "object",
      "properties": {
        "apiKey": {
          "type": "string",
          "format": "password"
        },
        "timeout": {
          "type": "integer",
          "minimum": 1000,
          "maximum": 60000
        }
      },
      "required": ["apiKey"]
    },
    "defaults": {
      "timeout": 5000
    }
  }
}
```

---

## 权限

| 权限 | 说明 |
|------|------|
| `network:outbound` | 允许出站网络请求 |
| `network:inbound` | 允许入站网络连接 |
| `storage:persistent` | 允许持久化存储 |
| `storage:temp` | 允许临时存储 |
| `config:read` | 允许读取配置 |
| `config:read-write` | 允许读写配置 |
| `event:publish` | 允许发布事件 |
| `event:subscribe` | 允许订阅事件 |

---

## 生命周期超时

```json
{
  "lifecycle": {
    "initTimeout": 30000,
    "startTimeout": 60000,
    "stopTimeout": 30000
  }
}
```

| 字段 | 类型 | 说明 | 默认值 |
|------|------|------|--------|
| `initTimeout` | integer | 初始化超时 (毫秒) | 30000 |
| `startTimeout` | integer | 启动超时 (毫秒) | 60000 |
| `stopTimeout` | integer | 停止超时 (毫秒) | 30000 |

---

## 验证规则

1. **ID 格式**: 必须是小写字母、数字、连字符，不能以数字开头
2. **版本格式**: 必须符合语义化版本规范 (SemVer)
3. **主类存在**: `main` 指定的类必须存在且实现 `Plugin` 接口
4. **依赖完整**: 非可选依赖必须可满足
5. **扩展点一致**: `requires` 中的扩展点必须有插件提供

---

## 相关文件

- [Plugin 接口](./Plugin.java)
- [PluginDescriptor 接口](./PluginDescriptor.java)
- [PluginContext 接口](./PluginContext.java)
- [ExtensionPoint 接口](./ExtensionPoint.java)
