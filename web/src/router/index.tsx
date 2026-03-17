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
import {MyTenantDepartmentManagerPage} from "@/pages/manager/tenant/MyTenantDepartmentManagerPage.tsx";
import {ProtectedControllerWarningWrapper} from "@/components/ProtectedControllerWarningWrapper.tsx";
import {UserPermissionManagerController} from "@/api/user-permission.api.ts";
import {TenantPermissionManagerController} from "@/api/tenant-permission.api.ts";
import {MailTemplateTypeManagerController} from "@/api/mail-template-type.api.ts";
import {MailTemplateCategoryManagerController} from "@/api/mail-template-category.api.ts";
import type {TFunction} from "i18next";

export const menuPathDashboard = "/manager/dashboard";
export const menuPathProfile = "/manager/profile"
export const menuPathLogin = "/auth/login"
export const menuPathRegister = "/auth/register"
export const menuPathResetPassword = "/auth/reset-password"
export const menuPathOAuthCode = "/auth/oauth2-code"

export type RouteItem = MenuItem & MenuItemType;

export function getMenuGroups(t: TFunction): MenuGroup[] {
    return [
        {
            name: 'rbac',
            icon: <KeyOutlined />,
            label: t('menu.groups.rbac'),
        },
        {
            name: 'system_storage',
            icon: <FileOutlined />,
            label: t('menu.groups.system_storage'),
        },
        {
            name: 'mail_template',
            icon: <MailOutlined />,
            label: t('menu.groups.mail_template'),
        },
        {
            name: 'tenant',
            icon: <ShopOutlined />,
            label: t('menu.groups.tenant'),
        },
        {
            name: 'i_tenant',
            icon: <ShopOutlined />,
            label: t('menu.groups.i_tenant'),
        }
    ];
}

export function getPublicMenus(t: TFunction): RouteItem[] {
    return [
        {
            key: menuPathDashboard,
            path: menuPathDashboard,
            icon: <DashboardOutlined />,
            label: t('menu.pub.dashboard'),
            page: <DashboardPage />
        },
        {
            key: menuPathProfile,
            path: menuPathProfile,
            icon: <UserOutlined />,
            label: t('menu.pub.profile'),
            page: <UserProfilePage />
        }
    ];
}

export function getTenantMenus(t: TFunction): RouteItem[] {
    return [
        {
            key: '/manager/tenant/dashboard',
            path: '/manager/tenant/dashboard',
            icon: <ShopOutlined />,
            label: t('menu.myTenant.dashboard'),
            page: <MyTenantDashboard />,
            group: 'i_tenant',
        },
        {
            key: '/manager/tenant/members',
            path: '/manager/tenant/members',
            icon: <TeamOutlined />,
            label: t('menu.myTenant.members'),
            page: <MyTenantMemberManagerPage />,
            group: 'i_tenant',
        },
        {
            key: '/manager/tenant/invitations',
            path: '/manager/tenant/invitations',
            icon: <TagsOutlined />,
            label: t('menu.myTenant.invitations'),
            page: <MyTenantInvitationManagerPage />,
            group: 'i_tenant',
        },
        {
            key: '/manager/tenant/roles',
            path: '/manager/tenant/roles',
            icon: <KeyOutlined />,
            label: t('menu.myTenant.roles'),
            page: <MyTenantRoleManagerPage />,
            group: 'i_tenant',
        },
        {
            key: '/manager/tenant/member-roles',
            path: '/manager/tenant/member-roles',
            icon: <UserSwitchOutlined />,
            label: t('menu.myTenant.memberRoles'),
            page: <MyTenantMemberRoleManagerPage />,
            group: 'i_tenant',
        },
        {
            key: '/manager/tenant/departments',
            path: '/manager/tenant/departments',
            icon: <ApartmentOutlined />,
            label: t('menu.myTenant.departments'),
            page: <MyTenantDepartmentManagerPage />,
            group: 'i_tenant',
        },
        {
            key: '/manager/tenant/profile',
            path: '/manager/tenant/profile',
            icon: <SettingOutlined />,
            label: t('menu.myTenant.profile'),
            page: <MyTenantProfilePage />,
            group: 'i_tenant',
        },
    ];
}

export function getAdminMenus(t: TFunction): RouteItem[] {
    return [
        {
            key: '/manager/users',
            path: '/manager/users',
            icon: <UserOutlined />,
            label: t('menu.admin.users'),
            page: <UserManagerPage />
        },
        {
            key: '/manager/oauth-accounts',
            path: '/manager/oauth-accounts',
            icon: <CloudOutlined />,
            label: t('menu.admin.oauthAccounts'),
            page: <OAuthAccountManagerPage />
        },
        {
            key: '/manager/user-roles',
            path: '/manager/user-roles',
            icon: <TeamOutlined />,
            label: t('menu.admin.userRoles'),
            page: <UserRoleManagerPage />,
            group: 'rbac'
        },
        {
            key: '/manager/user-permissions',
            path: '/manager/user-permissions',
            icon: <SafetyOutlined />,
            label: t('menu.admin.userPermissions'),
            page: (
                <ProtectedControllerWarningWrapper controller={UserPermissionManagerController}>
                    <UserPermissionManagerPage />
                </ProtectedControllerWarningWrapper>
            ),
            group: 'rbac'
        },
        {
            key: '/manager/user-roles-relation',
            path: '/manager/user-roles-relation',
            icon: <UserSwitchOutlined />,
            label: t('menu.admin.userRolesRelation'),
            page: <UserRoleRelationManagerPage />,
            group: 'rbac'
        },
        {
            key: '/manager/tenants',
            path: '/manager/tenants',
            icon: <ShopOutlined />,
            label: t('menu.admin.tenants'),
            page: <TenantManagerPage />,
            group: 'tenant'
        },
        {
            key: '/manager/tenant-members',
            path: '/manager/tenant-members',
            icon: <TeamOutlined />,
            label: t('menu.admin.tenantMembers'),
            page: <TenantMemberManagerPage />,
            group: 'tenant'
        },
        {
            key: '/manager/tenant-roles',
            path: '/manager/tenant-roles',
            icon: <KeyOutlined />,
            label: t('menu.admin.tenantRoles'),
            page: <TenantRoleManagerPage />,
            group: 'tenant'
        },
        {
            key: '/manager/tenant-permissions',
            path: '/manager/tenant-permissions',
            icon: <SafetyOutlined />,
            label: t('menu.admin.tenantPermissions'),
            page: (
                <ProtectedControllerWarningWrapper controller={TenantPermissionManagerController}>
                    <TenantPermissionManagerPage />
                </ProtectedControllerWarningWrapper>
            ),
            group: 'tenant'
        },
        {
            key: '/manager/tenant-role-permissions',
            path: '/manager/tenant-role-permissions',
            icon: <KeyOutlined />,
            label: t('menu.admin.tenantRolePermissions'),
            page: <TenantRolePermissionManagerPage />,
            group: 'tenant'
        },
        {
            key: '/manager/tenant-member-roles',
            path: '/manager/tenant-member-roles',
            icon: <UserSwitchOutlined />,
            label: t('menu.admin.tenantMemberRoles'),
            page: <TenantMemberRoleManagerPage />,
            group: 'tenant'
        },
        {
            key: '/manager/tenant-departments',
            path: '/manager/tenant-departments',
            icon: <ApartmentOutlined />,
            label: t('menu.admin.tenantDepartments'),
            page: <TenantDepartmentManagerPage />,
            group: 'tenant'
        },
        {
            key: '/manager/tenant-invitations',
            path: '/manager/tenant-invitations',
            icon: <TagsOutlined />,
            label: t('menu.admin.tenantInvitations'),
            page: <TenantInvitationManagerPage />,
            group: 'tenant'
        },
        {
            key: '/manager/tenant-tire-types',
            path: '/manager/tenant-tire-types',
            icon: <ShopOutlined />,
            label: t('menu.admin.tenantTireTypes'),
            page: <TenantTireTypeManagerPage />,
            group: 'tenant'
        },
        {
            key: '/manager/file-resources',
            path: '/manager/file-resources',
            icon: <FileOutlined />,
            label: t('menu.admin.fileResources'),
            page: <FileResourceManagerPage />,
            group: 'system_storage'
        },
        {
            key: '/manager/storage-providers',
            path: '/manager/storage-providers',
            icon: <DatabaseOutlined />,
            label: t('menu.admin.storageProviders'),
            page: <StorageProviderManagerPage />,
            group: 'system_storage'
        },
        {
            key: '/manager/mail-templates',
            path: '/manager/mail-templates',
            icon: <MailOutlined />,
            label: t('menu.admin.mailTemplates'),
            page: <MailTemplateManagerPage />,
            group: 'mail_template'
        },
        {
            key: '/manager/mail-template-types',
            path: '/manager/mail-template-types',
            icon: <TagsOutlined />,
            label: t('menu.admin.mailTemplateTypes'),
            page: (
                <ProtectedControllerWarningWrapper controller={MailTemplateTypeManagerController}>
                    <MailTemplateTypeManagerPage />
                </ProtectedControllerWarningWrapper>
            ),
            group: 'mail_template'
        },
        {
            key: '/manager/mail-template-categories',
            path: '/manager/mail-template-categories',
            icon: <FolderOutlined />,
            label: t('menu.admin.mailTemplateCategories'),
            page: (
                <ProtectedControllerWarningWrapper controller={MailTemplateCategoryManagerController}>
                    <MailTemplateCategoryManagerPage />
                </ProtectedControllerWarningWrapper>
            ),
            group: 'mail_template'
        },
        {
            key: '/manager/settings',
            path: '/manager/settings',
            icon: <SettingOutlined />,
            label: t('menu.admin.settings'),
            page: <SystemSettingsManagerPage />
        }
    ];
}

export function computeAccessibleMenus(accessiblePathList: string[], t: TFunction): RouteItem[] {
    const publicMenus = getPublicMenus(t);
    const tenantMenus = getTenantMenus(t);
    const adminMenus = getAdminMenus(t);

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

// 为了向后兼容，保留旧的导出（使用默认翻译函数）
import i18n from "@/i18n";
export const menuGroups = getMenuGroups(i18n.t.bind(i18n));
export const publicMenus = getPublicMenus(i18n.t.bind(i18n));
export const tenantMenus = getTenantMenus(i18n.t.bind(i18n));
export const adminMenus = getAdminMenus(i18n.t.bind(i18n));
