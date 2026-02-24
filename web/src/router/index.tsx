import type {MenuItem} from "../types/menu.types.ts";
import type {MenuItemType} from "antd/es/menu/interface";
import {
    DashboardOutlined, UserOutlined,
} from '@ant-design/icons';
import {DashboardPage} from "../pages/manager/dashboard/DashboardPage.tsx";
import {UserPermissionManagerPage} from "../pages/manager/rbac/UserPermissionManagerPage.tsx";

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
        key: '/manager/user-permissions',
        path: '/manager/user-permissions',
        icon: <UserOutlined />,
        label: "用户权限管理",
        page: <UserPermissionManagerPage />
    }
]

export const menus: (MenuItem & MenuItemType)[] = [...publicMenus, ...adminMenus]