import type {MenuItem} from "../types/menu.types.ts";
import type {MenuItemType} from "antd/es/menu/interface";
import {
    DashboardOutlined, UserOutlined, TeamOutlined, SafetyOutlined, KeyOutlined, UserSwitchOutlined
} from '@ant-design/icons';
import {DashboardPage} from "../pages/manager/dashboard/DashboardPage.tsx";
import {UserPermissionManagerPage} from "../pages/manager/rbac/UserPermissionManagerPage.tsx";
import {UserRoleManagerPage} from "../pages/manager/rbac/UserRoleManagerPage.tsx";
import {UserManagerPage} from "../pages/manager/rbac/UserManagerPage.tsx";
import {RolePermissionManagerPage} from "../pages/manager/rbac/RolePermissionManagerPage.tsx";
import {UserRoleRelationManagerPage} from "../pages/manager/rbac/UserRoleRelationManagerPage.tsx";

export const menuPathDashboard = "/manager/dashboard";
export const menuPathProfile = "/manager/profile"
export const menuPathLogin = "/auth/login"
export const menuPathRegister = "/auth/register"
export const menuPathResetPassword = "/auth/reset-password"

export const publicMenus: (MenuItem & MenuItemType)[] = [
    {
        key: menuPathDashboard,
        path: menuPathDashboard,
        icon: <DashboardOutlined />,
        label: "仪表盘",
        page: <DashboardPage />
    }
]

export const adminMenus: (MenuItem & MenuItemType)[] = [
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
    }
]

export const menus: (MenuItem & MenuItemType)[] = [...publicMenus, ...adminMenus]