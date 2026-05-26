# API 对接

## 目录规范

插件的前端类型文件建议按以下结构存放：

```
src/
  api/
    my-plugin.api.ts       — API 调用函数，DTO 内联在此
  types/
    my-plugin.types.ts     — 响应 VO 类型定义
```

- **DTO**（请求参数接口）内联在 `*.api.ts` 中，与调用函数放在一起，便于维护
- **VO / Entity**（后端返回的数据结构）定义在 `types/*.types.ts` 中，供页面组件引用

## 使用 system-request 发起请求

框架提供了 `system-request.ts`，封装了鉴权 Header 注入、错误统一处理等逻辑。直接使用其导出的函数即可，无需手动处理 token 或错误弹窗。

```typescript
import { doGet, doPost, doPut, doDelete } from "@/api/system-request.ts";
```

### Header 预处理

每次请求会自动注入以下 Header，无需手动传入：

- `Authorization: Bearer <token>` — 从本地存储读取当前登录 token
- POST / PUT / PATCH 请求自动设置 `Content-Type: application/x-www-form-urlencoded`

如需附加自定义 Header，通过第三个参数 `headers` 传入，与默认 Header 合并，重复的 key 以自定义值覆盖。

::: tip Content-Type 与后端注解的对应关系
框架大多数接口使用 `@ModelAttribute` 接收参数，对应 `application/x-www-form-urlencoded`，直接使用 `doPost` / `doPut` / `doPatch` 即可。

少数接口（如批量设置角色权限等传递数组或嵌套对象的场景）使用 `@RequestBody`，需要发送 `application/json`，此时通过 `headers` 参数手动覆盖 `Content-Type`：

```typescript
doPost('/api/xxx', payload, { 'Content-Type': 'application/json' });
```
:::

### 可用函数

| 函数 | HTTP 方法 | 参数 |
|------|-----------|------|
| `doGet<T>(url, query?, headers?)` | GET | `query` 为 URL 查询参数对象 |
| `doPost<T>(url, body?, headers?)` | POST | `body` 为表单参数对象 |
| `doPut<T>(url, body?, headers?)` | PUT | `body` 为表单参数对象 |
| `doPatch<T>(url, body?, headers?)` | PATCH | `body` 为表单参数对象 |
| `doDelete<T>(url, query?, headers?)` | DELETE | `query` 为 URL 查询参数对象 |

### 返回值

所有函数均返回 `Promise<ApiResponse<T>>`：

```typescript
interface ApiResponse<T> {
    code: number;     // 200 表示成功
    message: string;  // 提示信息
    data: T | null;   // 响应数据
}
```

请求成功（`code === 200`）时直接取 `.data` 使用。

### 错误处理

框架内置了统一的错误处理机制，无需在每个调用处手动处理：

- **401**：自动弹出 session 过期提示，并跳转到登录页
- **403**：自动弹出无权限提示
- **其他错误**：自动弹出响应中的 `message` 或通用错误提示，并抛出异常

### 示例

```typescript
// types/my-plugin.types.ts
export interface MyItem {
    id: string;
    name: string;
    createdTime: number;
}

// api/my-plugin.api.ts
import { doGet, doPost } from "@/api/system-request.ts";
import type { MyItem } from "@/types/my-plugin.types.ts";

// GET 请求
export async function getMyItem(id: string) {
    return doGet<MyItem>("/api/ext/my-plugin/item", { id });
}

// POST 请求
export async function createMyItem(name: string) {
    return doPost<MyItem>("/api/ext/my-plugin/item", { name });
}
```

## 路径说明

- 前端 API 路径以 `/api/` 开头（如 `/api/ext/my-plugin/item`）
- 开发环境下 Vite proxy 自动将 `/api/` 映射到后端
- 后端 Controller 使用 `GlobalConstants.REQUEST_MAPPING_PREFIX` 作为路径前缀

## 前端菜单与后端权限的对应关系

```
前端 addAdminMenu path        → 后端 SystemRbacPermissionDeclaration.menu name
  /manager/ext/my-page         →    my.plugin.page:/manager/ext/my-page
```

`SystemRbacPermissionDeclaration.menu` 的 `path` 参数必须与前端 `addAdminMenu` 的 `path` 一致，权限系统才能正确识别用户是否有权访问该菜单。

详见[系统权限](../sdk/system-permission)。
