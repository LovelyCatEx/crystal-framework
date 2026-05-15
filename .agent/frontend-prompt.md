# Crystal Framework 前端开发 Agent 提示词

## 项目概述

Crystal Framework 前端是一个基于 **React 19 + TypeScript + Vite + Ant Design 6 + TailwindCSS** 的多租户 SaaS 管理后台。使用 pnpm 管理依赖，支持中英文国际化。

---

## 技术栈

- **框架**: React 19.2 (with React Compiler)
- **构建**: Vite 7.2
- **语言**: TypeScript 5.9
- **UI 库**: Ant Design 6.3 (`antd`)
- **图标**: @ant-design/icons 6.x
- **样式**: TailwindCSS 3.4 + PostCSS + Autoprefixer
- **HTTP**: Axios 1.13
- **状态/缓存**: SWR 2.4
- **路由**: react-router-dom 7.13
- **国际化**: i18next + react-i18next + i18next-browser-languagedetector
- **日期**: dayjs
- **包管理**: pnpm 9
- **Lint**: ESLint 9 + typescript-eslint

---

## 目录结构 (`web/src/`)

### `api/` — API 服务层

| 文件 | 用途 |
|------|------|
| `request.ts` | Axios 实例配置（30s 超时，JSON Content-Type） |
| `system-request.ts` | 封装 `doGet`/`doPost`/`doDelete`/`doPut`/`doPatch`，自动添加 Authorization header，统一错误处理（401 跳转登录，403 提示，其他 toast） |
| `BaseManagerController.ts` | **通用管理端 API 基类**。泛型类，提供 `getById`、`list`、`create`、`query`、`update`、`delete` 方法。构造函数接收 `baseUrl`（如 `/manager/user`） |
| `user.api.ts` | 用户相关 API（UserManagerController 实例 + getUserProfile, getUserAccessibleMenus 等） |
| `audit-log.api.ts` | 审计日志 API |
| `tenant-member.api.ts` | 租户成员 API |
| `*.api.ts` | 每个模块一个 API 文件，导出 Controller 实例和 DTO 类型 |

### `components/` — 可复用组件

| 文件/目录 | 用途 |
|-----------|------|
| `ManagerPageContainer.tsx` | **管理页面容器**。封装 EntityTable + 新增/编辑 Modal + 批量操作。Props: `entityName`, `title`, `subtitle`, `columns`, `query`, `create`, `update`, `delete`, `editModalFormChildren`, `tableActions`, `showActionBar` |
| `EntityTable.tsx` | **数据表格**。分页、搜索、列定义、行选择、刷新。通过 `tableActions`/`tablePrefixActions` 添加筛选器（每个 action 有 `label`、`children`、`queryParamsProvider`） |
| `ActionBarComponent.tsx` | 页面标题栏（title + subtitle + actions） |
| `ProtectedControllerWarningWrapper.tsx` | 高危页面保护（只读/编辑模式选择） |
| `MaintenanceGuard.tsx` | 维护模式守卫 |
| `MaintenanceBanner.tsx` | 维护模式横幅 |
| `AvatarResource.tsx` | 头像组件（通过 fileEntityId 加载） |
| `CopyableToolTip.tsx` | 可复制的 Tooltip |
| `columns/` | **表格列定义**。每个实体一个文件，导出 `use{Entity}TableColumns()` hook，返回 `EntityTableColumns<Entity>` |
| `types/entity-table.types.ts` | EntityTableColumn 类型定义 |
| `card/pop/` | Popover 卡片组件（UserCard 等） |
| `selector/` | 实体选择器组件（UserIdSelector 等） |
| `tenant/` | 租户相关组件（TenantSelectorWithDetail 等） |

### `pages/` — 页面

| 目录 | 用途 |
|------|------|
| `pages/auth/` | 认证页面：LoginPage, RegisterPage, ForgotPasswordPage, OAuth2CodePage |
| `pages/manager/dashboard/` | 仪表盘（业务统计、系统监控、我的组织） |
| `pages/manager/user/` | 用户管理、OAuth 账号管理 |
| `pages/manager/rbac/` | 用户角色、权限、角色关联管理 |
| `pages/manager/tenant/` | 租户管理（租户、成员、角色、权限、部门、邀请码、套餐类型）+ 我的组织页面 |
| `pages/manager/resource/` | 文件资源、存储提供商管理 |
| `pages/manager/mail/` | 邮件模板、类型、分类管理 |
| `pages/manager/settings/` | 系统设置 |
| `pages/manager/audit/` | 审计日志管理 |
| `pages/manager/profile/` | 个人中心 |

### `router/` — 路由与导航

| 文件 | 用途 |
|------|------|
| `router/index.tsx` | **核心路由配置**。定义 `getPublicMenus`（仪表盘、个人中心）、`getTenantMenus`（我的组织相关）、`getAdminMenus`（管理端所有页面）。每个菜单项包含 `key`、`path`、`icon`、`label`、`page`、`group`（可选） |

菜单分组（`getMenuGroups`）：
- `rbac` — 用户权限
- `system_storage` — 系统储存
- `mail_template` — 邮件模板
- `tenant` — 租户管理
- `i_tenant` — 组织管理（当前用户的租户）

`computeAccessibleMenus` 根据用户权限过滤可见菜单。

### `compositions/` — React Hooks

| 文件 | 用途 |
|------|------|
| `use-logged-user.ts` | 获取当前登录用户信息、可访问菜单路径、组件路径 |
| `use-maintenance.ts` | 维护模式状态（SWR） |
| `use-user-profile.ts` | 当前用户 profile |
| `use-tenant.ts` | 当前租户信息 |
| `swr.ts` | SWR 辅助 hooks（`useSWRState`, `useSWRComposition`） |

### `types/` — TypeScript 类型

| 文件 | 用途 |
|------|------|
| `BaseEntity.ts` | `{ id: string; createdTime: string; modifiedTime: string }` |
| `api.types.ts` | `PageQuery`, `PaginatedResponseData<T>`, `BaseManagerReadDTO`, `BaseManagerDeleteDTO`, `BaseManagerUpdateDTO` |
| `menu.types.ts` | `MenuItem`, `MenuGroup` |
| `user.types.ts` | User, UserProfileVO, UserAccessibleResourceVO |
| `tenant-member.types.ts` | TenantMemberVO, TenantMemberStatus |
| `*.types.ts` | 每个模块的类型定义 |

### `utils/` — 工具函数

| 文件 | 用途 |
|------|------|
| `token.utils.ts` | JWT token 存储/读取/过期检查 |
| `datetime.utils.ts` | 时间戳格式化（`formatTimestamp`） |
| `file.utils.ts` | 文件下载 |
| `oauth2.utils.ts` | OAuth2 辅助 |
| `url.utils.ts` | URL 处理 |

### `i18n/` — 国际化

| 文件 | 用途 |
|------|------|
| `index.ts` | i18next 初始化配置 |
| `locales/zh-CN.ts` | 中文翻译 |
| `locales/en-US.ts` | 英文翻译 |
| `enum-helpers.ts` | 枚举值翻译辅助函数 |
| `enum-orders.ts` | 枚举排序 |
| `system-settings.tsx` | 系统设置翻译 |

### `config/` — 环境配置

### `global/` — 全局常量与主题

---

## 约定与规范

### 新增管理页面的标准流程

1. **API 文件** (`api/{module}.api.ts`):
   - 定义 Entity 接口
   - 定义 ManagerCreate/Read/Update/Delete DTO 接口
   - 实例化 `BaseManagerController`，传入 baseUrl

2. **表格列** (`components/columns/{Entity}EntityColumns.tsx`):
   - 导出 `use{Entity}TableColumns()` hook
   - 返回 `EntityTableColumns<Entity>` 数组
   - 使用 `useTranslation()` 获取翻译

3. **页面** (`pages/manager/{module}/{Entity}ManagerPage.tsx`):
   - 使用 `ManagerPageContainer` 或 `EntityTable`
   - 传入 `columns`、`query`、`create`、`update`、`delete`
   - 高级筛选通过 `tableActions` 的 `queryParamsProvider` 实现

4. **路由** (`router/index.tsx`):
   - 在 `getAdminMenus` 中添加菜单项
   - 指定 `key`(路径)、`path`、`icon`、`label`(i18n key)、`page`、`group`(可选)

5. **国际化** (`i18n/locales/zh-CN.ts` + `en-US.ts`):
   - `pages.{moduleName}Manager` — 页面翻译
   - `components.columns.{entity}` — 列翻译
   - `entityNames.{entity}` — 实体名称
   - `menu.admin.{menuKey}` — 菜单名称

### API 调用规范

- GET 请求用 `doGet<T>(url, queryParams)`
- POST 请求用 `doPost<T>(url, body)`
- POST 使用 `application/x-www-form-urlencoded`（不是 JSON）
- 所有 API 自动添加 `Authorization: Bearer {token}` header
- 响应格式: `ApiResponse<T> = { code, message, data }`
- 错误处理: 401 跳转登录，403 toast 提示，其他 toast error

### BaseManagerController 使用（仅限 Manager 类型）

**注意：`BaseManagerController` 类仅适用于后端继承了 `StandardManagerController` 的管理端 CRUD 接口。** 对于其他普通 Controller（如用户端 API、系统设置、认证等），直接使用 `doGet`/`doPost` 等函数按需编写即可，不要强行套用 BaseManagerController。

```typescript
// 仅用于 Manager 类型的 CRUD 接口
export const MyController = new BaseManagerController<
    MyEntity,           // 实体类型
    CreateDTO,          // 创建 DTO
    ReadDTO,            // 查询 DTO (extends BaseManagerReadDTO)
    UpdateDTO,          // 更新 DTO (extends BaseManagerUpdateDTO)
    DeleteDTO           // 删除 DTO (extends BaseManagerDeleteDTO)
>('/manager/my-module');
```

对于非 Manager 类型的普通接口，直接调用：
```typescript
// 普通接口示例
export async function getUserProfile(id?: string) {
    return doGet<UserProfileVO>('/api/user/profile', id ? { id } : undefined);
}

export async function updateUserProfile(dto: UpdateUserProfileDTO) {
    return doPost('/api/user/profile', { ...dto });
}
```

### ManagerPageContainer 使用

```tsx
<ManagerPageContainer
    ref={pageRef}
    entityName={t('entityNames.xxx')}
    title={t('pages.xxxManager.title')}
    subtitle={t('pages.xxxManager.subtitle')}
    columns={columns}
    editModalFormChildren={(editingItem) => <Form.Item .../>}
    query={async (props) => (await Controller.query(props)).data!}
    create={async (props) => (await Controller.create(props)).data!}
    update={async (props) => (await Controller.update(props)).data!}
    delete={async (props) => (await Controller.delete(props)).data!}
    tableActions={[
        {
            label: <span>筛选标签</span>,
            children: <Select .../>,
            queryParamsProvider() { return { filterField: value }; }
        }
    ]}
/>
```

### 高级筛选模式

- 使用 `tableActions` 数组添加筛选器
- 每个 action 包含 `label`（显示标签）、`children`（筛选 UI）、`queryParamsProvider`（返回查询参数对象）
- 筛选值变化时通过 `useEffect` 调用 `pageRef.current?.refreshData?.({ resetPage: true })`
- 查询参数会自动合并到 `query` 调用中

### 只读页面（无新增/编辑）

- 设置 `showActionBar={false}`
- 单独使用 `<ActionBarComponent>` 显示标题
- `create` 和 `update` 传空函数

### 表格列定义

```tsx
export function useMyEntityTableColumns(): EntityTableColumns<MyEntity> {
    const { t } = useTranslation();
    return [
        {
            title: t('components.columns.myEntity.fieldName'),
            dataIndex: "fieldName",
            key: "fieldName",
            width: 200,
            render: function (_: unknown, row: MyEntity): React.ReactNode {
                return <span>{row.fieldName}</span>;
            }
        }
    ];
}
```

### 国际化结构

```typescript
// zh-CN.ts 结构
{
  pages: {
    xxxManager: {
      title: '...',
      subtitle: '...',
      filter: { ... },
      modal: { ... },
      messages: { ... }
    }
  },
  components: {
    columns: {
      xxx: { field1: '...', field2: '...' }
    }
  },
  entityNames: {
    xxx: '...'
  },
  menu: {
    admin: {
      xxx: '...'
    }
  }
}
```

### ID 处理

- 后端 ID 是 Long（Snowflake），序列化为 String
- 前端所有 ID 类型为 `string`
- `BaseEntity.id` 是 `string`

### 组件库使用

- 表单: Ant Design `Form`, `Form.Item`, `Input`, `Select`, `Row`, `Col`
- 表格: 通过 `EntityTable` 组件（不直接用 antd Table）
- 弹窗: 通过 `ManagerPageContainer` 内置 Modal
- 消息: `message.success/error/warning`
- 图标: `@ant-design/icons`

### 样式

- 使用 TailwindCSS utility classes
- Ant Design 组件通过 `className` 添加 Tailwind 类
- 常用: `rounded-lg`, `h-10`, `flex`, `items-center`, `gap-*`, `text-xs`, `font-mono`

### SWR 数据获取

```typescript
// 简单用法
const { data, isLoading } = useSWRComposition<T>(key, fetcher);

// 带错误处理
const [data] = useSWRState<T>(key, fetcher, onError);
```

### 权限控制

- 后端通过 `@ManagerPermissions` 控制 API 访问
- 前端通过 `computeAccessibleMenus` 过滤菜单可见性
- 权限格式: MENU 权限的 path 部分对应前端路由 path
- 用户登录后获取 `accessibleMenuPaths` 列表
- 高危页面使用 `ProtectedControllerWarningWrapper` 包裹

---

## 构建与开发

- 开发: `pnpm dev`（Vite dev server）
- 构建: `pnpm build`（tsc + vite build）
- Lint: `pnpm lint`
- 类型检查: `npx tsc --noEmit`

---

## 注意事项

1. **不要自创组件**：使用现有的 `ManagerPageContainer`、`EntityTable`、`ActionBarComponent` 等
2. **Manager 类型接口使用 `BaseManagerController` 类**：仅限后端继承了 `StandardManagerController` 的管理端 CRUD 接口。普通 Controller（用户端 API、系统设置、认证等）直接使用 `doGet`/`doPost` 按需编写
3. **高级查询参数通过 `queryParamsProvider` 传递**，不要修改 `EntityTable` 的内部逻辑
4. **所有文本必须国际化**：使用 `useTranslation()` hook，不要硬编码文字
5. **ID 是 string**：前端所有 entity ID 都是 string 类型
6. **POST 请求是 form-urlencoded**：不是 JSON body
7. **路由 path 必须与后端权限 MENU_ 常量的 path 部分一致**
8. **表格列使用 hook 模式**：`use{Entity}TableColumns()`
9. **筛选器变化触发刷新**：通过 `useEffect` + `pageRef.current?.refreshData?.({ resetPage: true })`
10. **翻译 key 命名一致**：pages.{module}Manager, components.columns.{entity}, entityNames.{entity}, menu.admin.{key}
