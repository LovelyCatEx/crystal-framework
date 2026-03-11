import {
    DashboardOutlined, UserOutlined, TeamOutlined, SafetyOutlined, KeyOutlined, UserSwitchOutlined, SettingOutlined,
    CloudOutlined, FileOutlined, DatabaseOutlined, MailOutlined, FolderOutlined, TagsOutlined, ShopOutlined
} from '@ant-design/icons';
import {DashboardPage} from "../pages/manager/dashboard/DashboardPage.tsx";
import {UserPermissionManagerPage} from "../pages/manager/rbac/UserPermissionManagerPage.tsx";
import {UserRoleManagerPage} from "../pages/manager/rbac/UserRoleManagerPage.tsx";
import {UserManagerPage} from "../pages/manager/user/UserManagerPage.tsx";
import {RolePermissionManagerPage} from "../pages/manager/rbac/RolePermissionManagerPage.tsx";
import {UserRoleRelationManagerPage} from "../pages/manager/rbac/UserRoleRelationManagerPage.tsx";
import type {MenuItemType} from "antd/es/menu/interface";
import type {MenuGroup, MenuItem} from "../types/menu.types.ts";
import {SystemSettingsManagerPage} from "../pages/manager/settings/SystemSettingsManagerPage.tsx";
import {UserProfilePage} from "../pages/manager/profile/UserProfilePage.tsx";
import {OAuthAccountManagerPage} from "../pages/manager/user/OAuthAccountManagerPage.tsx";
import {FileResourceManagerPage} from "../pages/manager/resource/FileResourceManagerPage.tsx";
import {StorageProviderManagerPage} from "../pages/manager/resource/StorageProviderManagerPage.tsx";
import {MailTemplateCategoryManagerPage} from "../pages/manager/mail/MailTemplateCategoryManagerPage.tsx";
import {MailTemplateTypeManagerPage} from "../pages/manager/mail/MailTemplateTypeManagerPage.tsx";
import {MailTemplateManagerPage} from "../pages/manager/mail/MailTemplateManagerPage.tsx";
import {TenantManagerPage} from "../pages/manager/tenant/TenantManagerPage.tsx";
import {TenantTireTypeManagerPage} from "../pages/manager/tenant/TenantTireTypeManagerPage.tsx";
import {TenantMemberManagerPage} from "../pages/manager/tenant/TenantMemberManagerPage.tsx";
import {TenantPermissionManagerPage} from "../pages/manager/tenant/TenantPermissionManagerPage.tsx";
import {TenantRoleManagerPage} from "../pages/manager/tenant/TenantRoleManagerPage.tsx";

export const menuPathDashboard = "/manager/dashboard";
export const menuPathProfile = "/manager/profile"
export const menuPathLogin = "/auth/login"
export const menuPathRegister = "/auth/register"
export const menuPathResetPassword = "/auth/reset-password"
export const menuPathOAuthCode = "/auth/oauth2-code"

export type RouteItem = MenuItem & MenuItemType;

export const menuGroups: MenuGroup[] = [
    {
        name: 'rbac',
        icon: <KeyOutlined />,
        label: '用户权限',
    },
    {
        name: 'system_storage',
        icon: <FileOutlined />,
        label: '系统储存',
    },
    {
        name: 'mail_template',
        icon: <MailOutlined />,
        label: '邮件模板',
    },
    {
        name: 'tenant',
        icon: <ShopOutlined />,
        label: '租户管理',
    }
]

export const publicMenus: RouteItem[] = [
    {
        key: menuPathDashboard,
        path: menuPathDashboard,
        icon: <DashboardOutlined />,
        label: "仪表盘",
        page: <DashboardPage />
    },
    {
        key: menuPathProfile,
        path: menuPathProfile,
        icon: <UserOutlined />,
        label: "个人中心",
        page: <UserProfilePage />
    }
]

export const adminMenus: RouteItem[] = [
    {
        key: '/manager/users',
        path: '/manager/users',
        icon: <UserOutlined />,
        label: "用户管理",
        page: <UserManagerPage />
    },
    {
        key: '/manager/oauth-accounts',
        path: '/manager/oauth-accounts',
        icon: <CloudOutlined />,
        label: "OAuth账号管理",
        page: <OAuthAccountManagerPage />
    },
    {
        key: '/manager/user-roles',
        path: '/manager/user-roles',
        icon: <TeamOutlined />,
        label: "用户角色管理",
        page: <UserRoleManagerPage />,
        group: 'rbac'
    },
    {
        key: '/manager/user-permissions',
        path: '/manager/user-permissions',
        icon: <SafetyOutlined />,
        label: "用户权限管理",
        page: <UserPermissionManagerPage />,
        group: 'rbac'
    },
    {
        key: '/manager/role-permissions',
        path: '/manager/role-permissions',
        icon: <KeyOutlined />,
        label: "角色权限管理",
        page: <RolePermissionManagerPage />,
        group: 'rbac'
    },
    {
        key: '/manager/user-roles-relation',
        path: '/manager/user-roles-relation',
        icon: <UserSwitchOutlined />,
        label: "用户角色分配",
        page: <UserRoleRelationManagerPage />,
        group: 'rbac'
    },
    {
        key: '/manager/file-resources',
        path: '/manager/file-resources',
        icon: <FileOutlined />,
        label: "文件资源管理",
        page: <FileResourceManagerPage />,
        group: 'system_storage'
    },
    {
        key: '/manager/storage-providers',
        path: '/manager/storage-providers',
        icon: <DatabaseOutlined />,
        label: "存储提供商管理",
        page: <StorageProviderManagerPage />,
        group: 'system_storage'
    },
    {
        key: '/manager/mail-templates',
        path: '/manager/mail-templates',
        icon: <MailOutlined />,
        label: "邮件模板管理",
        page: <MailTemplateManagerPage />,
        group: 'mail_template'
    },
    {
        key: '/manager/mail-template-types',
        path: '/manager/mail-template-types',
        icon: <TagsOutlined />,
        label: "邮件模板类型",
        page: <MailTemplateTypeManagerPage />,
        group: 'mail_template'
    },
    {
        key: '/manager/mail-template-categories',
        path: '/manager/mail-template-categories',
        icon: <FolderOutlined />,
        label: "邮件模板分类",
        page: <MailTemplateCategoryManagerPage />,
        group: 'mail_template'
    },
    {
        key: '/manager/tenants',
        path: '/manager/tenants',
        icon: <ShopOutlined />,
        label: "租户管理",
        page: <TenantManagerPage />,
        group: 'tenant'
    },
    {
        key: '/manager/tenant-tire-types',
        path: '/manager/tenant-tire-types',
        icon: <ShopOutlined />,
        label: "套餐类型管理",
        page: <TenantTireTypeManagerPage />,
        group: 'tenant'
    },
    {
        key: '/manager/tenant-members',
        path: '/manager/tenant-members',
        icon: <TeamOutlined />,
        label: "租户成员管理",
        page: <TenantMemberManagerPage />,
        group: 'tenant'
    },
    {
        key: '/manager/tenant-roles',
        path: '/manager/tenant-roles',
        icon: <KeyOutlined />,
        label: "租户角色管理",
        page: <TenantRoleManagerPage />,
        group: 'tenant'
    },
    {
        key: '/manager/tenant-permissions',
        path: '/manager/tenant-permissions',
        icon: <SafetyOutlined />,
        label: "租户权限管理",
        page: <TenantPermissionManagerPage />,
        group: 'tenant'
    },
    {
        key: '/manager/settings',
        path: '/manager/settings',
        icon: <SettingOutlined />,
        label: "系统设置",
        page: <SystemSettingsManagerPage />
    }
]

export function computeAccessibleMenus(accessiblePathList: string[]): RouteItem[] {
    if (!accessiblePathList || accessiblePathList.length == 0) {
        return [
            ...publicMenus,
        ];
    }

    return [
        ...publicMenus,
        ...adminMenus
            .filter((menu) => accessiblePathList.includes(menu.path)),
    ];
}