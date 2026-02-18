import type {MenuItem} from "../types/menu.types.ts";
import type {MenuItemType} from "antd/es/menu/interface";
import {
    DashboardOutlined,
} from '@ant-design/icons';
import {DashboardPage} from "../pages/manager/dashboard/DashboardPage.tsx";

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

export const adminMenus: (MenuItem & MenuItemType)[] = []

export const menus: (MenuItem & MenuItemType)[] = [...publicMenus, ...adminMenus]