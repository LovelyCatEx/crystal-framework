# 添加页面

通过 `configure(registry)` 注册菜单项来添加页面，菜单项会被自动整合到侧边栏布局中。

## 注册菜单项

### addAdminMenu

管理后台菜单，需要对应权限才能看到：

```tsx
registry.addAdminMenu({
    key: '/manager/ext/my-page',
    path: '/manager/ext/my-page',
    icon: <DashboardOutlined />,
    label: 'menu.admin.myPage',
    page: <MyPage />,
    group: 'monitor',
});
```

| 参数 | 类型 | 说明 |
|------|------|------|
| `key` | `string` | 唯一标识，通常与 path 相同 |
| `path` | `string` | 路由路径，须以 `/manager/` 开头 |
| `icon` | `ReactNode` | Ant Design 图标组件 |
| `label` | `string` | i18n 键 |
| `page` | `ReactNode` | 页面组件 |
| `group` | `string` | 所属菜单分组 |

### addPublicMenu

公共菜单项，所有登录用户可见，无需特定权限：

```tsx
registry.addPublicMenu({
    key: '/manager/ext/public-page',
    path: '/manager/ext/public-page',
    icon: <UnlockOutlined />,
    label: 'menu.pub.myPublicPage',
    page: <MyPublicPage />,
    group: 'my_group',
});
```

参数与 `addAdminMenu` 相同。适用于所有用户都能访问的通用页面。

### addTenantMenu

租户菜单项，仅对租户成员可见：

```tsx
registry.addTenantMenu({
    key: '/manager/ext/tenant-page',
    path: '/manager/ext/tenant-page',
    icon: <TeamOutlined />,
    label: 'menu.myTenant.myTenantPage',
    page: <MyTenantPage />,
    group: 'i_tenant',
});
```

## 菜单分组

### 使用内置分组

将 `group` 设置为内置分组名，菜单项会自动归入侧边栏对应分类：

| 分组名 | 标签 | 说明 |
|--------|------|------|
| `monitor` | 监控 | 监控页面 |
| `logs` | 日志 | 日志页面 |
| `rbac` | 权限管理 | RBAC 相关 |
| `system_storage` | 系统存储 | 文件存储 |
| `mail_template` | 邮件模板 | 邮件模板 |
| `tenant` | 租户管理 | 租户管理 |
| `i_tenant` | 我的租户 | 租户侧菜单 |

### 自定义分组

通过 `addMenuGroup()` 注册新的分组，分组会在侧边栏渲染为一个可折叠区块：

```tsx
registry.addMenuGroup({
    name: 'my_group',
    icon: <AppstoreOutlined />,
    label: 'menu.groups.myGroup',
});
```

| 参数 | 类型 | 说明 |
|------|------|------|
| `name` | `string` | 分组唯一标识，与菜单项的 `group` 对应 |
| `icon` | `ReactNode` | 分组图标 |
| `label` | `string` | i18n 键 |

## 页面组件规范

### 基础页面结构

```tsx
import {useTranslation} from "react-i18next";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {StandardCard} from "@/components/StandardCard.tsx";

export function MyPage() {
    const {t} = useTranslation();

    return (
        <>
            <ActionBarComponent
                title={t('pages.myPage.title')}
                subtitle={t('pages.myPage.subtitle')}
            />
            <StandardCard>
                {/* 页面内容 */}
            </StandardCard>
        </>
    );
}
```

### 可用基础组件

| 组件 | 路径 | 用途 |
|------|------|------|
| `ActionBarComponent` | `@/components/ActionBarComponent.tsx` | 页面标题栏，带 title + subtitle |
| `StandardCard` | `@/components/StandardCard.tsx` | 白色卡片容器 |
| `EntityTable` | `@/components/EntityTable.tsx` | 数据表格 |
| Ant Design 系列 | `antd` | 按钮、表单、表格、卡片等 |
| `@ant-design/icons` | 图标库 | 各类图标 |

## 顶层路由

需要脱离 `/manager/*` 布局的独立页面使用 `addTopLevelRoute`：

```tsx
registry.addTopLevelRoute({
    path: '/ext/standalone',
    element: <StandalonePage />,
});
```

顶层路由直接挂在 `<Routes>` 下，与 `/manager/*`、`/auth/*` 等并列，用于登录页、全屏页面等场景。
