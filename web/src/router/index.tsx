import {
    ApartmentOutlined,
    AuditOutlined,
    BookOutlined,
    CloudOutlined,
    DashboardOutlined,
    DatabaseOutlined,
    FileOutlined,
    FolderOutlined,
    FormOutlined,
    KeyOutlined,
    LineChartOutlined,
    MailOutlined,
    MonitorOutlined,
    NotificationOutlined,
    SafetyOutlined,
    SettingOutlined,
    ShopOutlined,
    TagsOutlined,
    TeamOutlined,
    UserOutlined,
    UserSwitchOutlined
} from '@ant-design/icons';
import { lazy } from 'react';

const DashboardPage = lazy(() => import("../pages/manager/dashboard/DashboardPage.tsx"));
const UserPermissionManagerPage = lazy(() => import("../pages/manager/rbac/UserPermissionManagerPage.tsx"));
const UserRoleManagerPage = lazy(() => import("../pages/manager/rbac/UserRoleManagerPage.tsx"));
const UserManagerPage = lazy(() => import("../pages/manager/user/UserManagerPage.tsx"));
const UserRoleRelationManagerPage = lazy(() => import("../pages/manager/rbac/UserRoleRelationManagerPage.tsx"));
const SystemSettingsManagerPage = lazy(() => import("../pages/manager/settings/SystemSettingsManagerPage.tsx"));
const UserProfilePage = lazy(() => import("../pages/manager/profile/UserProfilePage.tsx"));
const OAuthAccountManagerPage = lazy(() => import("../pages/manager/user/OAuthAccountManagerPage.tsx"));
const FileResourceManagerPage = lazy(() => import("../pages/manager/resource/FileResourceManagerPage.tsx"));
const StorageProviderManagerPage = lazy(() => import("../pages/manager/resource/StorageProviderManagerPage.tsx"));
const MailTemplateCategoryManagerPage = lazy(() => import("../pages/manager/mail/MailTemplateCategoryManagerPage.tsx"));
const MailTemplateTypeManagerPage = lazy(() => import("../pages/manager/mail/MailTemplateTypeManagerPage.tsx"));
const MailTemplateManagerPage = lazy(() => import("../pages/manager/mail/MailTemplateManagerPage.tsx"));
const TenantManagerPage = lazy(() => import("../pages/manager/tenant/TenantManagerPage.tsx"));
const TenantTireTypeManagerPage = lazy(() => import("../pages/manager/tenant/TenantTireTypeManagerPage.tsx"));
const TenantTireBenefitFeatureManagerPage = lazy(() => import("../pages/manager/tenant/TenantTireBenefitFeatureManagerPage.tsx"));
const TenantTireBenefitValueContainer = lazy(() => import("../pages/manager/tenant/benefit/TenantTireBenefitValueContainer.tsx"));
const TenantMemberManagerPage = lazy(() => import("../pages/manager/tenant/TenantMemberManagerPage.tsx"));
const TenantPermissionManagerPage = lazy(() => import("../pages/manager/tenant/TenantPermissionManagerPage.tsx"));
const TenantRoleManagerPage = lazy(() => import("../pages/manager/tenant/TenantRoleManagerPage.tsx"));
const TenantRolePermissionManagerPage = lazy(() => import("../pages/manager/tenant/TenantRolePermissionManagerPage.tsx"));
const TenantMemberRoleManagerPage = lazy(() => import("../pages/manager/tenant/TenantMemberRoleManagerPage.tsx"));
const TenantDepartmentManagerPage = lazy(() => import("@/pages/manager/tenant/TenantDepartmentManagerPage.tsx"));
const TenantInvitationManagerPage = lazy(() => import("@/pages/manager/tenant/TenantInvitationManagerPage.tsx"));
const TenantMessageChannelManagerPage = lazy(() => import("@/pages/manager/tenant/TenantMessageChannelManagerPage.tsx"));
const AuditLogManagerPage = lazy(() => import("@/pages/manager/audit/AuditLogManagerPage.tsx"));
const MyTenantProfilePage = lazy(() => import("@/pages/manager/tenant/MyTenantProfilePage.tsx"));
const MyTenantPersonalProfilePage = lazy(() => import("@/pages/manager/tenant/MyTenantPersonalProfilePage.tsx"));
const MyTenantDashboard = lazy(() => import("@/pages/manager/tenant/MyTenantDashboard.tsx"));
const MailSendLogManagerPage = lazy(() => import("@/pages/manager/mail/MailSendLogManagerPage.tsx"));
const MyTenantMemberManagerPage = lazy(() => import("@/pages/manager/tenant/MyTenantMemberManagerPage.tsx"));
const MyTenantInvitationManagerPage = lazy(() => import("@/pages/manager/tenant/MyTenantInvitationManagerPage.tsx"));
const MyTenantMessageChannelManagerPage = lazy(() => import("@/pages/manager/tenant/MyTenantMessageChannelManagerPage.tsx"));
const MyTenantRoleManagerPage = lazy(() => import("@/pages/manager/tenant/MyTenantRoleManagerPage.tsx"));
const MyTenantMemberRoleManagerPage = lazy(() => import("@/pages/manager/tenant/MyTenantMemberRoleManagerPage.tsx"));
const MyTenantDepartmentManagerPage = lazy(() => import("@/pages/manager/tenant/MyTenantDepartmentManagerPage.tsx"));
const UserLoginLogManagerPage = lazy(() => import("@/pages/manager/auth/UserLoginLogManagerPage.tsx"));
const SessionMonitorPage = lazy(() => import("@/pages/manager/monitor/SessionMonitorPage.tsx"));
const SystemMonitorPage = lazy(() => import("@/pages/manager/monitor/SystemMonitorPage.tsx"));
const AnnouncementManagerPage = lazy(() => import("@/pages/manager/system/AnnouncementManagerPage.tsx"));
const TenantDictTypeManagerPage = lazy(() => import("@/pages/manager/tenant/dict/TenantDictTypeManagerPage.tsx"));
const TenantDictItemManagerPage = lazy(() => import("@/pages/manager/tenant/dict/TenantDictItemManagerPage.tsx"));
const MyTenantDictTypeManagerPage = lazy(() => import("@/pages/manager/tenant/dict/MyTenantDictTypeManagerPage.tsx"));
const MyTenantDictItemManagerPage = lazy(() => import("@/pages/manager/tenant/dict/MyTenantDictItemManagerPage.tsx"));
const SystemDictTypeManagerPage = lazy(() => import("@/pages/manager/tenant/dict/SystemDictTypeManagerPage.tsx"));
const SystemDictItemManagerPage = lazy(() => import("@/pages/manager/tenant/dict/SystemDictItemManagerPage.tsx"));
const ApprovalFlowDefinitionManagerPage = lazy(() => import("@/pages/manager/approval/ApprovalFlowDefinitionManagerPage.tsx"));
const MyApprovalFlowDefinitionManagerPage = lazy(() => import("@/pages/manager/approval/MyApprovalFlowDefinitionManagerPage.tsx"));
const TenantApprovalFlowDefinitionManagerPage = lazy(() => import("@/pages/manager/approval/TenantApprovalFlowDefinitionManagerPage.tsx"));
const InitiableApprovalFlowsPage = lazy(() => import("@/pages/manager/approval/InitiableApprovalFlowsPage.tsx"));
const MyApprovalFlowsPage = lazy(() => import("@/pages/manager/approval/MyApprovalFlowsPage.tsx"));
const MyTenantApprovalFlowInstanceManagerPage = lazy(() => import("@/pages/manager/approval/MyTenantApprovalFlowInstanceManagerPage.tsx"));
const TenantApprovalFlowInstanceManagerPage = lazy(() => import("@/pages/manager/approval/TenantApprovalFlowInstanceManagerPage.tsx"));
const ApprovalFlowInstanceManagerPage = lazy(() => import("@/pages/manager/approval/ApprovalFlowInstanceManagerPage.tsx"));

import {ProtectedControllerWarningWrapper} from "@/components/base/ProtectedControllerWarningWrapper.tsx";
import {UserPermissionManagerController} from "@/api/user/rbac/user-permission.api.ts";
import {TenantPermissionManagerController} from "@/api/tenant/rbac/tenant-permission.api.ts";
import {TenantTireBenefitFeatureManagerController} from "@/api/tenant/tenant-benefit.api.ts";
import {MailTemplateTypeManagerController} from "@/api/mail/mail-template-type.api.ts";
import {MailTemplateCategoryManagerController} from "@/api/mail/mail-template-category.api.ts";
import type {TFunction} from "i18next";
import {pluginRegistry} from "@/plugin/registry.ts";
import type {PluginRouteItem} from "@/plugin/types.ts";
import i18n from "@/i18n";
import type {MenuGroup, MenuItem} from "@/types/menu.types.ts";
import type {MenuItemType} from "antd/lib/menu/interface";
import {menuPathDashboard, menuPathProfile} from "@/router/paths.ts";



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
        },
        {
            name: 'approval',
            icon: <ApartmentOutlined />,
            label: t('menu.groups.approval'),
        },
        {
            name: 'logs',
            icon: <AuditOutlined />,
            label: t('menu.groups.logs'),
        },
        {
            name: 'monitor',
            icon: <LineChartOutlined />,
            label: t('menu.groups.monitor'),
        },
        ...toTranslatedMenuGroups(t),
    ];
}

function toTranslatedMenuGroups(t: TFunction): MenuGroup[] {
    return pluginRegistry.menuGroups.map(g => ({
        ...g,
        label: t(g.label),
    }));
}

function toTranslatedRouteItems(t: TFunction, items: PluginRouteItem[]): RouteItem[] {
    return items.map(item => ({
        ...item,
        label: t(item.label),
    })) as RouteItem[];
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
        },
        {
            key: '/manager/approval/initiate',
            path: '/manager/approval/initiate',
            icon: <FormOutlined />,
            label: t('menu.pub.initiableApprovalFlows'),
            page: <InitiableApprovalFlowsPage />,
            group: 'approval'
        },
        {
            key: '/manager/approval/my-instances',
            path: '/manager/approval/my-instances',
            icon: <AuditOutlined />,
            label: t('menu.pub.myApprovalFlows'),
            page: <MyApprovalFlowsPage />,
            group: 'approval'
        },
        ...toTranslatedRouteItems(t, pluginRegistry.publicMenus),
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
            key: '/manager/tenant/personal-profile',
            path: '/manager/tenant/personal-profile',
            icon: <UserOutlined />,
            label: t('menu.myTenant.personalProfile'),
            page: <MyTenantPersonalProfilePage />,
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
            key: '/manager/tenant/message-channels',
            path: '/manager/tenant/message-channels',
            icon: <NotificationOutlined />,
            label: t('menu.myTenant.messageChannels'),
            page: <MyTenantMessageChannelManagerPage />,
            group: 'i_tenant',
        },
        {
            key: '/manager/tenant/dict-types',
            path: '/manager/tenant/dict-types',
            icon: <BookOutlined />,
            label: t('menu.myTenant.dictTypes'),
            page: <MyTenantDictTypeManagerPage />,
            group: 'i_tenant',
        },
        {
            key: '/manager/tenant/dict-items',
            path: '/manager/tenant/dict-items',
            icon: <BookOutlined />,
            label: t('menu.myTenant.dictTypes'),
            page: <MyTenantDictItemManagerPage />,
            group: 'i_tenant',
        },
        {
            key: '/manager/tenant/approval-flow-definitions',
            path: '/manager/tenant/approval-flow-definitions',
            icon: <ApartmentOutlined />,
            label: t('menu.myTenant.approvalFlowDefinitions'),
            page: <MyApprovalFlowDefinitionManagerPage />,
            group: 'i_tenant',
        },
        {
            key: '/manager/tenant/approval-flow-instances',
            path: '/manager/tenant/approval-flow-instances',
            icon: <AuditOutlined />,
            label: t('menu.myTenant.approvalFlowInstances'),
            page: <MyTenantApprovalFlowInstanceManagerPage />,
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
        ...toTranslatedRouteItems(t, pluginRegistry.tenantMenus),
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
            key: '/manager/tenant-message-channels',
            path: '/manager/tenant-message-channels',
            icon: <NotificationOutlined />,
            label: t('menu.admin.tenantMessageChannels'),
            page: <TenantMessageChannelManagerPage />,
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
            key: '/manager/tenant-tire-benefit-features',
            path: '/manager/tenant-tire-benefit-features',
            icon: <SafetyOutlined />,
            label: t('menu.admin.tenantTireBenefitFeatures'),
            page: <ProtectedControllerWarningWrapper controller={TenantTireBenefitFeatureManagerController}>
                <TenantTireBenefitFeatureManagerPage />
            </ProtectedControllerWarningWrapper>,
            group: 'tenant'
        },
        {
            key: '/manager/tenant-tire-benefit-values',
            path: '/manager/tenant-tire-benefit-values',
            icon: <SafetyOutlined />,
            label: t('menu.admin.tenantTireBenefitValues'),
            page: <TenantTireBenefitValueContainer />,
            group: 'tenant'
        },
        {
            key: '/manager/tenant-dict-types',
            path: '/manager/tenant-dict-types',
            icon: <BookOutlined />,
            label: t('menu.admin.tenantDictTypes'),
            page: <TenantDictTypeManagerPage />,
            group: 'tenant'
        },
        {
            key: '/manager/tenant-dict-items',
            path: '/manager/tenant-dict-items',
            icon: <BookOutlined />,
            label: t('menu.admin.tenantDictItems'),
            page: <TenantDictItemManagerPage />,
            group: 'tenant'
        },
        {
            key: '/manager/tenant-approval-flow-definitions',
            path: '/manager/tenant-approval-flow-definitions',
            icon: <ApartmentOutlined />,
            label: t('menu.admin.tenantApprovalFlowDefinitions'),
            page: <TenantApprovalFlowDefinitionManagerPage />,
            group: 'tenant'
        },
        {
            key: '/manager/tenant-approval-flow-instances',
            path: '/manager/tenant-approval-flow-instances',
            icon: <AuditOutlined />,
            label: t('menu.admin.tenantApprovalFlowInstances'),
            page: <TenantApprovalFlowInstanceManagerPage />,
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
        ...toTranslatedRouteItems(t, pluginRegistry.adminMenus),
        {
            key: '/manager/sessions',
            path: '/manager/sessions',
            icon: <MonitorOutlined />,
            label: t('menu.admin.sessions'),
            page: <SessionMonitorPage />,
            group: 'monitor'
        },
        {
            key: '/manager/monitor/system-metrics',
            path: '/manager/monitor/system-metrics',
            icon: <LineChartOutlined />,
            label: t('menu.admin.systemMonitor'),
            page: <SystemMonitorPage />,
            group: 'monitor'
        },
        {
            key: '/manager/mail-send-logs',
            path: '/manager/mail-send-logs',
            icon: <MailOutlined />,
            label: t('menu.admin.mailSendLogs'),
            page: <MailSendLogManagerPage />,
            group: 'logs'
        },
        {
            key: '/manager/audit-logs',
            path: '/manager/audit-logs',
            icon: <AuditOutlined />,
            label: t('menu.admin.auditLogs'),
            page: <AuditLogManagerPage />,
            group: 'logs'
        },
        {
            key: '/manager/user-login-logs',
            path: '/manager/user-login-logs',
            icon: <KeyOutlined />,
            label: t('menu.admin.userLoginLogs'),
            page: <UserLoginLogManagerPage />,
            group: 'logs'
        },
        {
            key: '/manager/announcements',
            path: '/manager/announcements',
            icon: <NotificationOutlined />,
            label: t('menu.admin.announcements'),
            page: <AnnouncementManagerPage />,
        },
        {
            key: '/manager/approval-flow-definitions',
            path: '/manager/approval-flow-definitions',
            icon: <ApartmentOutlined />,
            label: t('menu.admin.approvalFlowDefinitions'),
            page: <ApprovalFlowDefinitionManagerPage />,
            group: 'approval'
        },
        {
            key: '/manager/approval-flow-instances',
            path: '/manager/approval-flow-instances',
            icon: <AuditOutlined />,
            label: t('menu.admin.approvalFlowInstances'),
            page: <ApprovalFlowInstanceManagerPage />,
            group: 'approval'
        },
        {
            key: '/manager/system-dict-types',
            path: '/manager/system-dict-types',
            icon: <BookOutlined />,
            label: t('menu.admin.systemDictTypes'),
            page: <SystemDictTypeManagerPage />,
            group: 'approval'
        },
        {
            key: '/manager/system-dict-items',
            path: '/manager/system-dict-items',
            icon: <BookOutlined />,
            label: t('menu.admin.systemDictItems'),
            page: <SystemDictItemManagerPage />,
            group: 'approval'
        },
        {
            key: '/manager/settings',
            path: '/manager/settings',
            icon: <SettingOutlined />,
            label: t('menu.admin.settings'),
            page: <SystemSettingsManagerPage />
        },
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

export const menuGroups = getMenuGroups(i18n.t.bind(i18n));
export const publicMenus = getPublicMenus(i18n.t.bind(i18n));
export const tenantMenus = getTenantMenus(i18n.t.bind(i18n));
export const adminMenus = getAdminMenus(i18n.t.bind(i18n));
