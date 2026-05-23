# Add a Page

Register menu items via `configure(registry)` to add pages. Menu items are automatically integrated into the sidebar layout.

## Registering Menu Items

### addAdminMenu

Admin menu items, visible only to users with the corresponding permission:

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

| Parameter | Type | Description |
|-----------|------|-------------|
| `key` | `string` | Unique key, usually same as path |
| `path` | `string` | Route path, must start with `/manager/` |
| `icon` | `ReactNode` | Ant Design icon component |
| `label` | `string` | i18n key |
| `page` | `ReactNode` | Page component |
| `group` | `string` | Menu group name |

### addPublicMenu

Public menu items, visible to all authenticated users without specific permissions:

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

Parameters are the same as `addAdminMenu`. Suitable for general-purpose pages accessible to all users.

### addTenantMenu

Tenant menu items, visible only to tenant members:

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

## Menu Groups

### Using Built-in Groups

Set the `group` property to a built-in group name to categorize menu items:

| Group | Label | Description |
|-------|-------|-------------|
| `monitor` | Monitor | Monitoring pages |
| `logs` | Logs | Log pages |
| `rbac` | Permission | RBAC related |
| `system_storage` | Storage | File storage |
| `mail_template` | Mail | Mail templates |
| `tenant` | Tenant | Tenant management |
| `i_tenant` | My Tenant | Tenant-side menu |

### Custom Groups

Use `addMenuGroup()` to register a new group, which renders as a collapsible section in the sidebar:

```tsx
registry.addMenuGroup({
    name: 'my_group',
    icon: <AppstoreOutlined />,
    label: 'menu.groups.myGroup',
});
```

| Parameter | Type | Description |
|-----------|------|-------------|
| `name` | `string` | Unique group identifier, matches the `group` field on menu items |
| `icon` | `ReactNode` | Group icon |
| `label` | `string` | i18n key |

## Page Component Convention

### Basic Structure

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
                {/* Page content */}
            </StandardCard>
        </>
    );
}
```

### Available Components

| Component | Import Path | Usage |
|-----------|-------------|-------|
| `ActionBarComponent` | `@/components/ActionBarComponent.tsx` | Page header with title + subtitle |
| `StandardCard` | `@/components/StandardCard.tsx` | White card container |
| `EntityTable` | `@/components/EntityTable.tsx` | Data table |
| Ant Design | `antd` | Buttons, forms, tables, cards, etc. |
| `@ant-design/icons` | icon library | Various icons |

## Top-Level Routes

Use `addTopLevelRoute` for standalone pages outside the `/manager/*` layout:

```tsx
registry.addTopLevelRoute({
    path: '/ext/standalone',
    element: <StandalonePage />,
});
```

Top-level routes are rendered directly under `<Routes>`, alongside `/manager/*`, `/auth/*`, etc. Suitable for login pages, full-screen pages, etc.
