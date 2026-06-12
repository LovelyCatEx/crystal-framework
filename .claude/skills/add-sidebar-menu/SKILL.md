---
name: add-sidebar-menu
description: 在管理后台侧边栏中添加新的菜单项或菜单分组，包括路由注册、页面组件引入和 i18n 配置。
---

# 添加侧边栏菜单

## 触发条件

当用户需要在管理后台侧边栏新增一个页面入口时使用，包括：
- 添加新的菜单项到已有分组
- 创建新的菜单分组并在其下添加菜单项

## 核心文件

| 文件 | 职责 |
|------|------|
| `web/src/router/index.tsx` | 路由定义、menu group 注册、menu item 注册 |
| `web/src/i18n/locales/zh-CN.ts` | 中文翻译 |
| `web/src/i18n/locales/en-US.ts` | 英文翻译 |
| `web/src/types/menu.types.ts` | MenuGroup / MenuItem 类型定义 |

## 路由文件结构

`web/src/router/index.tsx` 包含以下关键部分：

### 1. Lazy Import 区域（文件顶部）

所有页面组件必须在顶部以 `lazy(() => import(...))` 方式引入：

```tsx
const MyNewPage = lazy(() => import("@/pages/manager/xxx/MyNewPage.tsx"));
```

### 2. getMenuGroups() — 菜单分组注册

返回所有侧边栏分组定义。每个 group 包含 `name`、`icon`、`label`：

```tsx
export function getMenuGroups(t: TFunction): MenuGroup[] {
    return [
        { name: 'rbac', icon: <KeyOutlined />, label: t('menu.groups.rbac') },
        { name: 'tenant', icon: <ShopOutlined />, label: t('menu.groups.tenant') },
        // ...
        ...toTranslatedMenuGroups(t),
    ];
}
```

**新增分组必须在此数组中注册**，否则侧边栏不会显示该分组。

### 3. 三个菜单函数 — 菜单项注册

| 函数 | 适用场景 | 路由前缀 |
|------|---------|---------|
| `getPublicMenus(t)` | 登录后无需权限即可访问 | `/manager/` |
| `getTenantMenus(t)` | 以租户身份登录后可访问 | `/manager/tenant/` |
| `getAdminMenus(t)` | 登录后需要系统权限可访问 | `/manager/` |

每个菜单项结构为 `RouteItem`：

```tsx
{
    key: '/manager/my-page',        // 唯一标识（通常等于 path）
    path: '/manager/my-page',       // URL 路径
    icon: <BookOutlined />,         // ant-design 图标
    label: t('menu.admin.myPage'),  // 翻译后的菜单名
    page: <MyNewPage />,            // 对应的页面组件
    group: 'approval'               // 所属分组（对应 getMenuGroups 中的 name）
}
```

### 4. 常用图标

从 `@ant-design/icons` 引入。当前已导入的图标包括：
`ApartmentOutlined`, `AuditOutlined`, `BookOutlined`, `CloudOutlined`, `DashboardOutlined`, `DatabaseOutlined`, `FileOutlined`, `FolderOutlined`, `KeyOutlined`, `LineChartOutlined`, `MailOutlined`, `MonitorOutlined`, `NotificationOutlined`, `SafetyOutlined`, `SettingOutlined`, `ShopOutlined`, `TagsOutlined`, `TeamOutlined`, `UserOutlined`, `UserSwitchOutlined`

需要其他图标时在顶部 import 中追加。

## 执行步骤

### 仅添加菜单项（使用已有分组）

**1. 页面组件**：确认页面组件已创建在 `web/src/pages/manager/` 对应子目录下

**2. Lazy Import**：在 `router/index.tsx` 顶部添加 lazy import

**3. 菜单项注册**：在对应的菜单函数中添加 RouteItem 对象
- 系统管理员页面 → `getAdminMenus()`
- 租户成员页面 → `getTenantMenus()`
- 无权限页面 → `getPublicMenus()`

**4. i18n**：在 `menu.admin` / `menu.myTenant` / `menu.pub` 中添加翻译 key（zh-CN + en-US 同步）

### 添加新的菜单分组 + 菜单项

**1-4 同上**

**5. 分组注册**：在 `getMenuGroups()` 数组中添加新的 `{ name, icon, label }` 对象

**6. 分组 i18n**：在 `menu.groups` 中添加分组名翻译（zh-CN + en-US 同步）

## 注意事项

- `group` 字段的值必须与 `getMenuGroups()` 中某个对象的 `name` 严格一致，否则菜单项不会显示
- 不指定 `group` 的菜单项会显示在侧边栏顶层（无分组折叠）
- 菜单项的 `key` 和 `path` 通常相同，必须唯一
- i18n 必须 zh-CN / en-US 双语同步，条目数量一一对应
- 页面文件命名必须保留完整模块前缀（即使在子文件夹中）

## 输出格式

完成后说明：
1. 新增的菜单项（path、所属函数、所属 group）
2. 是否新增了分组（name、icon）
3. 修改的文件列表
4. 新增的 i18n key
