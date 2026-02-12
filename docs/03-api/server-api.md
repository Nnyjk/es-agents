# 前后端接口交互规范

## 1. 概述
本文档定义前端 (React) 与服务端 (Quarkus) 之间的 HTTP REST API 交互规范。

## 2. 协议基础
- **通信协议**：HTTP/1.1 或 HTTP/2
- **数据格式**：JSON (`Content-Type: application/json`)
- **字符编码**：UTF-8

## 3. URL 命名规范
- 采用 RESTful 风格。
- 资源名词使用复数，连字符分隔（kebab-case）。
- 示例：
  - `GET /api/v1/environments` - 获取环境列表
  - `POST /api/v1/environments` - 创建环境
  - `GET /api/v1/hosts/{id}` - 获取特定主机

## 4. 请求头 (Request Headers)
| Header | 说明 | 示例 |
|:---|:---|:---|
| `Authorization` | JWT 认证令牌 | `Bearer <token>` |
| `Content-Type` | 请求体格式 | `application/json` |
| `X-Request-ID` | 请求追踪 ID (可选) | `req-123456` |

## 5. 响应格式 (Response Format)

### 5.1 成功响应
对于 `GET`, `PUT`, `POST` 请求，直接返回资源对象或对象列表。
Quarkus 推荐直接返回实体/DTO，不强制包裹 `data` 字段，以减少层级。

**HTTP 200 OK / 201 Created**
```json
{
  "id": "123",
  "name": "Production",
  "description": "生产环境",
  "createdAt": "2023-10-01T12:00:00Z"
}
```

### 5.2 分页响应
对于列表查询，使用 HTTP Header 或 包裹结构返回分页信息。本项目采用 **包裹结构** 以便前端统一处理。

```json
{
  "items": [
    { "id": "1", "name": "A" },
    { "id": "2", "name": "B" }
  ],
  "total": 100,
  "page": 1,
  "size": 20
}
```

### 5.3 错误响应
发生错误时，返回对应的 HTTP 状态码（4xx, 5xx），Body 中包含标准错误结构。

```json
{
  "code": "RESOURCE_NOT_FOUND",
  "message": "Environment with id '123' not found",
  "details": {
    "entityId": "123"
  }
}
```

## 6. HTTP 状态码使用
- `200 OK`: 同步请求成功。
- `201 Created`: 资源创建成功。
- `202 Accepted`: 异步任务已接受（如触发部署）。
- `204 No Content`: 删除成功，无返回内容。
- `400 Bad Request`: 参数校验失败。
- `401 Unauthorized`: 未登录或 Token 过期。
- `403 Forbidden`: 无权限访问。
- `404 Not Found`: 资源不存在。
- `422 Unprocessable Entity`: 业务逻辑校验失败。
- `500 Internal Server Error`: 服务端内部错误。

## 7. 数据类型约定
- **时间**：ISO 8601 格式字符串 (`yyyy-MM-dd'T'HH:mm:ss'Z'`)。
- **ID**：字符串格式（推荐 UUID 或 String 形式的 Long）。
- **枚举**：字符串格式（如 `status: "RUNNING"`）。
