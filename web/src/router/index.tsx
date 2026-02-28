import {
    DashboardOutlined, UserOutlined, TeamOutlined, SafetyOutlined, KeyOutlined, UserSwitchOutlined, SettingOutlined
} from '@ant-design/icons';
import {DashboardPage} from "../pages/manager/dashboard/DashboardPage.tsx";
import {UserPermissionManagerPage} from "../pages/manager/rbac/UserPermissionManagerPage.tsx";
import {UserRoleManagerPage} from "../pages/manager/rbac/UserRoleManagerPage.tsx";
import {UserManagerPage} from "../pages/manager/rbac/UserManagerPage.tsx";
import {RolePermissionManagerPage} from "../pages/manager/rbac/RolePermissionManagerPage.tsx";
import {UserRoleRelationManagerPage} from "../pages/manager/rbac/UserRoleRelationManagerPage.tsx";
import type {MenuItemType} from "antd/es/menu/interface";
import type {MenuItem} from "../types/menu.types.ts";
import {SystemSettingsManagerPage} from "../pages/manager/settings/SystemSettingsManagerPage.tsx";
import {UserProfilePage} from "../pages/manager/profile/UserProfilePage.tsx";

export const menuPathDashboard = "/manager/dashboard";
export const menuPathProfile = "/manager/profile"
export const menuPathLogin = "/auth/login"
export const menuPathRegister = "/auth/register"
export const menuPathResetPassword = "/auth/reset-password"

export type RouteItem = MenuItem & MenuItemType;

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
        key: '/manager/user-roles',
        path: '/manager/user-roles',
        icon: <TeamOutlined />,
        label: "用户角色管理",
        page: <UserRoleManagerPage />
    },
    {
        key: '/manager/user-permissions',
        path: '/manager/user-permissions',
        icon: <SafetyOutlined />,
        label: "用户权限管理",
        page: <UserPermissionManagerPage />
    },
    {
        key: '/manager/role-permissions',
        path: '/manager/role-permissions',
        icon: <KeyOutlined />,
        label: "角色权限管理",
        page: <RolePermissionManagerPage />
    },
    {
        key: '/manager/user-roles-relation',
        path: '/manager/user-roles-relation',
        icon: <UserSwitchOutlined />,
        label: "用户角色分配",
        page: <UserRoleRelationManagerPage />
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