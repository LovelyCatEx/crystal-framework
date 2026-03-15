import {
    ApartmentOutlined,
    CloudOutlined,
    DashboardOutlined,
    DatabaseOutlined,
    FileOutlined,
    FolderOutlined,
    KeyOutlined,
    MailOutlined,
    SafetyOutlined,
    SettingOutlined,
    ShopOutlined,
    TagsOutlined,
    TeamOutlined,
    UserOutlined,
    UserSwitchOutlined
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
import {TenantRolePermissionManagerPage} from "../pages/manager/tenant/TenantRolePermissionManagerPage.tsx";
import {TenantMemberRoleManagerPage} from "../pages/manager/tenant/TenantMemberRoleManagerPage.tsx";
import {TenantDepartmentManagerPage} from "@/pages/manager/tenant/TenantDepartmentManagerPage.tsx";
import {TenantInvitationManagerPage} from "@/pages/manager/tenant/TenantInvitationManagerPage.tsx";
import {MyTenantProfilePage} from "@/pages/manager/tenant/MyTenantProfilePage.tsx";
import {MyTenantDashboard} from "@/pages/manager/tenant/MyTenantDashboard.tsx";
import {MyTenantMemberManagerPage} from "@/pages/manager/tenant/MyTenantMemberManagerPage.tsx";
import {MyTenantInvitationManagerPage} from "@/pages/manager/tenant/MyTenantInvitationManagerPage.tsx";
import {MyTenantRoleManagerPage} from "@/pages/manager/tenant/MyTenantRoleManagerPage.tsx";
import {MyTenantMemberRoleManagerPage} from "@/pages/manager/tenant/MyTenantMemberRoleManagerPage.tsx";
import {MyTenantRolePermissionManagerPage} from "@/pages/manager/tenant/MyTenantRolePermissionManagerPage.tsx";
import {MyTenantDepartmentManagerPage} from "@/pages/manager/tenant/MyTenantDepartmentManagerPage.tsx";

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
    },
    {
        name: 'i_tenant',
        icon: <ShopOutlined />,
        label: '组织管理',
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

export const tenantMenus: RouteItem[] = [
    {
        key: '/manager/tenant/dashboard',
        path: '/manager/tenant/dashboard',
        icon: <ShopOutlined />,
        label: "我的组织",
        page: <MyTenantDashboard />,
        group: 'i_tenant',
    },
    {
        key: '/manager/tenant/members',
        path: '/manager/tenant/members',
        icon: <TeamOutlined />,
        label: "成员管理",
        page: <MyTenantMemberManagerPage />,
        group: 'i_tenant',
    },
    {
        key: '/manager/tenant/invitations',
        path: '/manager/tenant/invitations',
        icon: <TagsOutlined />,
        label: "邀请码管理",
        page: <MyTenantInvitationManagerPage />,
        group: 'i_tenant',
    },
    {
        key: '/manager/tenant/roles',
        path: '/manager/tenant/roles',
        icon: <KeyOutlined />,
        label: "角色管理",
        page: <MyTenantRoleManagerPage />,
        group: 'i_tenant',
    },
    {
        key: '/manager/tenant/member-roles',
        path: '/manager/tenant/member-roles',
        icon: <UserSwitchOutlined />,
        label: "成员角色管理",
        page: <MyTenantMemberRoleManagerPage />,
        group: 'i_tenant',
    },
    {
        key: '/manager/tenant/role-permissions',
        path: '/manager/tenant/role-permissions',
        icon: <SafetyOutlined />,
        label: "角色权限管理",
        page: <MyTenantRolePermissionManagerPage />,
        group: 'i_tenant',
    },
    {
        key: '/manager/tenant/departments',
        path: '/manager/tenant/departments',
        icon: <ApartmentOutlined />,
        label: "部门管理",
        page: <MyTenantDepartmentManagerPage />,
        group: 'i_tenant',
    },
    {
        key: '/manager/tenant/profile',
        path: '/manager/tenant/profile',
        icon: <SettingOutlined />,
        label: "组织设置",
        page: <MyTenantProfilePage />,
        group: 'i_tenant',
    },
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
        key: '/manager/tenants',
        path: '/manager/tenants',
        icon: <ShopOutlined />,
        label: "租户管理",
        page: <TenantManagerPage />,
        group: 'tenant'
    },
    {
        key: '/manager/tenant-members',
        path: '/manager/tenant-members',
        icon: <TeamOutlined />,
        label: "成员管理",
        page: <TenantMemberManagerPage />,
        group: 'tenant'
    },
    {
        key: '/manager/tenant-roles',
        path: '/manager/tenant-roles',
        icon: <KeyOutlined />,
        label: "角色管理",
        page: <TenantRoleManagerPage />,
        group: 'tenant'
    },
    {
        key: '/manager/tenant-permissions',
        path: '/manager/tenant-permissions',
        icon: <SafetyOutlined />,
        label: "权限管理",
        page: <TenantPermissionManagerPage />,
        group: 'tenant'
    },
    {
        key: '/manager/tenant-role-permissions',
        path: '/manager/tenant-role-permissions',
        icon: <KeyOutlined />,
        label: "角色权限管理",
        page: <TenantRolePermissionManagerPage />,
        group: 'tenant'
    },
    {
        key: '/manager/tenant-member-roles',
        path: '/manager/tenant-member-roles',
        icon: <UserSwitchOutlined />,
        label: "成员角色管理",
        page: <TenantMemberRoleManagerPage />,
        group: 'tenant'
    },
    {
        key: '/manager/tenant-departments',
        path: '/manager/tenant-departments',
        icon: <ApartmentOutlined />,
        label: "部门管理",
        page: <TenantDepartmentManagerPage />,
        group: 'tenant'
    },
    {
        key: '/manager/tenant-invitations',
        path: '/manager/tenant-invitations',
        icon: <TagsOutlined />,
        label: "邀请码管理",
        page: <TenantInvitationManagerPage />,
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
        ...tenantMenus
            .filter((menu) => accessiblePathList.includes(menu.path)),
        ...adminMenus
            .filter((menu) => accessiblePathList.includes(menu.path)),
    ];
}