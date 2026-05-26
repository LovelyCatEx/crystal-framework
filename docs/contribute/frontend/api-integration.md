# API 对接

## 目录规范

源码中前端 API 相关文件按以下结构组织：

```
web/src/
  api/
    system-request.ts          — 底层请求封装（鉴权、加解密、错误处理）
    request.ts                 — axios 实例
    <module>/
      <module>.api.ts          — 模块 API 调用函数，DTO 内联在此
  types/
    <module>/
      <module>.types.ts        — 响应 VO / Entity 类型定义
```

- **DTO**（请求参数接口）内联在对应的 `*.api.ts` 中
- **VO / Entity**（后端返回的数据结构）定义在 `types/<module>/*.types.ts` 中

## system-request.ts

`system-request.ts` 是所有 API 调用的统一入口，封装了以下逻辑：

### Header 预处理

每次请求前，`preProcessHeaders` 会自动处理以下 Header：

- `Authorization: Bearer <token>` — 从本地存储读取 token 并注入
- 各 HTTP 方法自动设置对应的 `Content-Type`：

| 方法 | Content-Type |
|------|-------------|
| GET / DELETE | 不设置（无请求体） |
| POST / PUT / PATCH | `application/x-www-form-urlencoded` |

如需附加自定义 Header，通过第三个参数 `headers` 传入，与默认 Header 合并，重复的 key 以自定义值覆盖。

::: tip Content-Type 与后端注解的对应关系
框架大多数接口使用 `@ModelAttribute` 接收参数，对应 `application/x-www-form-urlencoded`，直接使用 `doPost` / `doPut` / `doPatch` 即可。

少数接口（如批量设置角色权限等需要传递数组或嵌套对象的场景）使用 `@RequestBody`，需要发送 `application/json`，此时通过 `headers` 参数手动覆盖 `Content-Type`：

```typescript
doPost('/api/xxx', payload, { 'Content-Type': 'application/json' });
```
:::

### 错误统一处理

`handleApiResponse` 根据响应 `code` 统一处理：

| code | 行为 |
|------|------|
| `200` | 正常返回 |
| `401` | 弹出 session 过期提示，500ms 后跳转登录页，并携带 `redirectTo` 参数 |
| `403` | 弹出无权限提示 |
| 其他 | 弹出 `message` 字段内容或通用错误提示，并 `throw` 响应对象 |

### 导出函数

```typescript
doGet<T>(url: string, query?: object, headers?: object): Promise<ApiResponse<T>>
doPost<T>(url: string, body?: object, headers?: object): Promise<ApiResponse<T>>
doPut<T>(url: string, body?: object, headers?: object): Promise<ApiResponse<T>>
doPatch<T>(url: string, body?: object, headers?: object): Promise<ApiResponse<T>>
doDelete<T>(url: string, query?: object, headers?: object): Promise<ApiResponse<T>>
```

GET / DELETE 的第二个参数为 URL 查询参数；POST / PUT / PATCH 的第二个参数为表单请求体（`Content-Type: application/x-www-form-urlencoded`）。

### ApiResponse 类型

```typescript
interface ApiResponse<T> {
    code: number;
    message: string;
    data: T | null;
}
```

## 新增模块 API

以 `user-login-log` 模块为例：

```typescript
// types/user/user-login-log.types.ts
export interface UserLoginLogEntity {
    id: string;
    userId: string;
    username: string;
    loginMethod: number;
    success: boolean;
    remoteIp: string;
    createdTime: number;
    modifiedTime: number;
}

// api/user/user-login-log.api.ts
import { doGet, doPost } from "@/api/system-request.ts";
import type { UserLoginLogEntity } from "@/types/user/user-login-log.types.ts";
import type { PaginatedResponseData } from "@/types/api.types.ts";

export interface ManagerReadUserLoginLogDTO {
    page: number;
    pageSize: number;
    userId?: string;
    username?: string;
    loginMethod?: number;
    success?: boolean;
}

export const UserLoginLogManagerController = {
    query: (dto: ManagerReadUserLoginLogDTO) =>
        doGet<PaginatedResponseData<UserLoginLogEntity>>(
            "/api/manager/auth/login-log",
            dto
        ),
};
```

## 路径说明

- 前端 API 路径以 `/api/` 开头
- 开发环境下 Vite proxy 将 `/api/` 代理到后端，配置见 `web/vite.config.ts`
- 后端 Controller 使用 `GlobalConstants.REQUEST_MAPPING_PREFIX` 作为路径前缀，确保前后端路径对齐

## 前端菜单与后端权限的对应关系

```
前端 addAdminMenu path        → 后端 SystemRbacPermissionDeclaration.menu name
  /manager/ext/my-page         →    my.plugin.page:/manager/ext/my-page
```

`SystemRbacPermissionDeclaration.menu` 的 `path` 参数必须与前端 `addAdminMenu` 的 `path` 完全一致，权限系统才能正确识别用户是否有权访问该菜单。

详见[系统权限](../system-permission)。
