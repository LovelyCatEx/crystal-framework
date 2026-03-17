import {Avatar, Button, Divider, Dropdown, Layout, Menu, message, Space, Spin} from "antd";
import {
    DownOutlined,
    LoadingOutlined,
    LogoutOutlined,
    MenuFoldOutlined,
    MenuUnfoldOutlined,
    ShopOutlined,
    UserOutlined, UserSwitchOutlined, BgColorsOutlined
} from "@ant-design/icons";
import {ThemeColorPickerModal} from "@/components/ThemeColorPickerModal.tsx";
import {ThemeModeSelector} from "@/components/ThemeModeSelector.tsx";
import {
    getStoredThemeKey,
    setStoredThemeKey,
    updateThemeCSSVariables,
    getStoredThemeMode,
    setStoredThemeMode,
    THEME_MODE_STORAGE_KEY
} from "@/global/theme-config.ts";
import type {ThemeColor, ThemeMode} from "@/types/theme.types.ts";
import {Route, Routes, useLocation, useNavigate} from "react-router-dom";
import {useTranslation} from "react-i18next";
import {Content, Header} from "antd/es/layout/layout";
import Sider from "antd/es/layout/Sider";
import {clearUserAuthentication, setUserAuthentication} from "@/utils/token.utils.ts";
import {useEffect, useMemo, useState} from "react";
import {buildDocumentTitle, ProjectDisplayName} from "@/global/global-settings.ts";
import {useLoggedUser} from "@/compositions/use-logged-user.ts";
import {computeAccessibleMenus, getMenuGroups, menuPathLogin, menuPathProfile, type RouteItem} from "@/router";
import './ManagerContainerPageStyles.css';
import type {ItemType} from "antd/es/menu/interface";
import type {UserTenantVO} from "@/types/tenant.types.ts";
import {switchTenant} from "@/api/auth.api.ts";
import {useUserTenants} from "@/compositions/use-tenant.ts";
import {TenantMemberStatus} from "@/types/tenant-member.types.ts";
import {theme} from "antd";
import {LanguageSwitcher} from "@/components/LanguageSwitcher.tsx";
const { useToken } = theme;

function TenantSwitcher() {
    const loggedUser = useLoggedUser();
    const userTenants = useUserTenants();
    const [switchingTenantId, setSwitchingTenantId] = useState<string | null>(null);
    const { t } = useTranslation();

    const handleTenantSwitch = async (tenant: UserTenantVO) => {
        setSwitchingTenantId(tenant.tenantId);
        try {
            const result = await switchTenant({ tenantId: tenant.tenantId });
            if (result.data) {
                setUserAuthentication(result.data.token, result.data.expiresIn);
                void message.success(t('pages.managerContainer.switchSuccess', { tenantName: tenant.tenantName }));
                window.location.reload();
            }
        } catch (error) {
            void message.error(t('pages.managerContainer.switchFailed', { tenantName: tenant.tenantName }));
        } finally {
            setSwitchingTenantId(null);
        }
    };

    const tenants = userTenants.joinedTenants || [];
    if (tenants.length === 0) {
        return null;
    }

    const isNonTenantAuthentication = tenants.all((it) => !it.authenticated)

    const allOptions: UserTenantVO[] = [
        {
            tenantId: '0',
            tenantName: loggedUser.userProfile?.nickname ?? t('pages.managerContainer.notOrganizationIdentity'),
            tenantAvatar: loggedUser.userProfile?.avatar ?? null,
            memberStatus: TenantMemberStatus.ACTIVE,
            authenticated: isNonTenantAuthentication
        },
        ...tenants
    ];

    const dropdownItems = allOptions
        .filter((it) => it.memberStatus === TenantMemberStatus.ACTIVE)
        .map(tenant => ({
            key: tenant.tenantId,
            label: (
                <div className={`flex items-center gap-2 py-1 px-2 rounded`}>
                    <Avatar
                        size="small"
                        icon={<ShopOutlined />}
                        src={tenant.tenantAvatar}
                    />
                    <span className={tenant.authenticated ? 'font-medium text-blue-500' : ''}>{tenant.tenantName}</span>
                    {tenant.authenticated && <span className="text-xs text-blue-500 ml-auto">{t('pages.managerContainer.current')}</span>}
                    {switchingTenantId === tenant.tenantId && <Spin size="small" />}
                </div>
            ),
            onClick: () => handleTenantSwitch(tenant),
            disabled: switchingTenantId !== null || tenant.authenticated,
        }));

    return !userTenants.isJoinedTenantsLoading ? (
        <Dropdown
            menu={{ items: dropdownItems }}
            placement="bottomLeft"
            arrow
        >
            <Space orientation="horizontal" size={6} className="cursor-pointer">
                {isNonTenantAuthentication ? <UserSwitchOutlined /> : <ShopOutlined />}

                <span className="hidden sm:inline">{userTenants.currentTenant?.tenantName ?? loggedUser.userProfile?.username}</span>
                <DownOutlined className="text-xs" />
            </Space>
        </Dropdown>
    ) : (
        <Space orientation="horizontal" size={8}>
            <LoadingOutlined />
            <span className="hidden sm:inline">{t('pages.managerContainer.switching')}</span>
        </Space>
    );
}

export function ManagerContainerPage({ parentPath }: { parentPath: string }) {
    const { token } = useToken();
    const { t } = useTranslation();

    const loggedUser = useLoggedUser();
    const [collapsed, setCollapsed] = useState(false);
    const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
    const [openKeys, setOpenKeys] = useState<string[]>([]);
    const [themeModalOpen, setThemeModalOpen] = useState(false);
    const [currentThemeKey, setCurrentThemeKey] = useState<string>(getStoredThemeKey);
    const [themeMode, setThemeMode] = useState<ThemeMode>(getStoredThemeMode);
    const navigate = useNavigate();
    const location = useLocation();

    const handleThemeChange = (theme: ThemeColor) => {
        setCurrentThemeKey(theme.key);
        setStoredThemeKey(theme.key);
        updateThemeCSSVariables(theme, themeMode);
        window.dispatchEvent(new StorageEvent('storage', {
            key: 'app-theme-color-key',
            newValue: theme.key,
        }));
    };

    const handleThemeModeChange = (mode: ThemeMode) => {
        setThemeMode(mode);
        setStoredThemeMode(mode);
        window.dispatchEvent(new StorageEvent('storage', {
            key: THEME_MODE_STORAGE_KEY,
            newValue: mode,
        }));
    };

    const availableMenus = useMemo(() => {
        return computeAccessibleMenus(loggedUser.accessibleMenuPaths ?? [], t);
    }, [loggedUser.accessibleMenuPaths, t]);

    const menuGroups = useMemo(() => getMenuGroups(t), [t]);

    const { menuItems } = useMemo(() => {
        const groupMap = new Map<string, RouteItem[]>();
        const processedGroups = new Set<string>();

        availableMenus.forEach(menu => {
            if (menu.group) {
                const groupName = menu.group;
                if (!groupMap.has(groupName)) {
                    groupMap.set(groupName, []);
                }
                groupMap.get(groupName)!.push(menu);
            }
        });

        const result: ItemType[] = [];

        availableMenus.forEach(menu => {
            if (menu.group) {
                const groupName = menu.group;
                if (processedGroups.has(groupName)) {
                    return;
                }
                processedGroups.add(groupName);

                const group = menuGroups.find(g => g.name === groupName);
                const items = groupMap.get(groupName) || [];
                if (group && items.length > 0) {
                    result.push({
                        key: groupName,
                        label: group.label,
                        icon: group.icon,
                        children: items.map(item => ({
                            key: item.key,
                            label: item.label,
                            icon: item.icon,
                        })),
                    });
                }
            } else {
                result.push(menu);
            }
        });

        return { menuItems: result };
    }, [availableMenus, menuGroups]);

    const handleMenuClick = (e: unknown) => {
        navigate((e as { key: string }).key);
    };

    const selectedKeys = useMemo(() => {
        const currentPath = location.pathname;
        const matchedKey = Array.from(availableMenus)
            .sort((a, b) => b.key.toString().length - a.key.toString().length)
            .find((item) => currentPath.startsWith(item.key as string));

        return matchedKey ? [matchedKey] : [{ key: '/', path: '/', icon: <></>, label: '' }];
    }, [availableMenus, location.pathname]);

    useEffect(() => {
        const matchedKey = selectedKeys.length > 0 ? selectedKeys[0] : null;
        if (matchedKey) {
            document.title = buildDocumentTitle(matchedKey.label)
        } else {
            document.title = ProjectDisplayName
        }
    }, [selectedKeys]);

    useEffect(() => {
        const currentPath = location.pathname;
        const matchedMenu = availableMenus.find((item) => currentPath.startsWith(item.key as string));
        if (matchedMenu?.group) {
            setOpenKeys([matchedMenu.group]);
        }
    }, [availableMenus, location.pathname]);

    return (
        <Layout className="min-h-screen">
            <Header
                className="fixed top-0 left-0 w-full h-16 px-6 flex items-center justify-between z-50 backdrop-blur-md border-b shadow-sm"
                style={{ borderColor: token.colorBorder }}
            >
                <div className="flex items-center gap-4">
                    <div className="flex items-center gap-2 cursor-pointer" onClick={() => navigate('/')}>
                        <img src="/logo.svg" alt="Logo" className="w-8 h-8"/>
                        <span className="text-2xl font-bold tracking-tight" style={{ color: token.colorTextHeading }}>
                            {ProjectDisplayName}
                        </span>
                    </div>
                    <TenantSwitcher />
                </div>

                <div className="flex flex-row items-center gap-4">
                    {/* Mobile Menu Button */}
                    <Button
                        type="text"
                        size="large"
                        icon={mobileMenuOpen ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
                        onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
                        className="md:hidden"
                    />

                    <ThemeModeSelector
                        value={themeMode}
                        onChange={handleThemeModeChange}
                        size="middle"
                        shape="round"
                    />

                    <LanguageSwitcher />

                    <Dropdown
                        menu={{
                            items: [
                                { key: 'profile', label: t('pages.managerContainer.userProfile'), icon: <UserOutlined /> },
                                { key: 'theme', label: t('pages.managerContainer.customTheme'), icon: <BgColorsOutlined /> },
                                { type: 'divider' },
                                {
                                    key: 'logout',
                                    label: t('pages.managerContainer.logout'),
                                    icon: <LogoutOutlined />,
                                    danger: true,
                                },
                            ],
                            onClick: (e) => {
                                if (e.key === 'profile') {
                                    navigate(menuPathProfile);
                                } else if (e.key === 'theme') {
                                    setThemeModalOpen(true);
                                } else if (e.key === 'logout') {
                                    clearUserAuthentication();
                                    navigate(`${menuPathLogin}?redirectTo=${window.location.pathname}`);
                                }
                            },
                        }}
                    >
                        <Space className="cursor-pointer">
                            <Avatar
                                icon={<UserOutlined />}
                                src={loggedUser.userProfile?.avatar}
                            />
                            <span className="hidden sm:inline font-medium" style={{ color: token.colorTextHeading }}>
                                {loggedUser?.userProfile?.nickname}
                            </span>
                        </Space>
                    </Dropdown>
                </div>
            </Header>

            <Layout className="mt-16">
                {/* Left-Side Menu */}
                <Sider
                    width={260}
                    trigger={null}
                    collapsible
                    collapsed={collapsed}
                    className="hidden md:block overflow-auto h-[calc(100vh-64px)] fixed left-0 border-r"
                    style={{ borderColor: token.colorBorder }}
                >
                    <div className="flex items-center justify-between px-4 py-2">
                        {!collapsed && <span className="text-sm text-gray-800"></span>}

                        {/* Left-Side Menu Collapse Button */}
                        <Button
                            type="text"
                            size="small"
                            onClick={() => setCollapsed(!collapsed)}
                            className={`w-8 h-8 flex items-center justify-center rounded-lg hover:bg-gray-100 transition-colors ${collapsed ? 'mx-auto' : ''}`}
                        >
                            {collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
                        </Button>
                    </div>

                    <Divider className="mt-0 mb-0" plain />

                    <Menu
                        mode="inline"
                        selectedKeys={selectedKeys.map((e) => e.key.toString())}
                        openKeys={collapsed ? [] : openKeys}
                        onOpenChange={setOpenKeys}
                        items={menuItems}
                        onClick={handleMenuClick}
                        className="py-4 px-2 border-none"
                    />
                </Sider>

                {/* Main Router View */}
                <Content
                    className={`p-6 transition-all duration-300 ${collapsed ? 'md:ml-20' : 'md:ml-[260px]'} relative z-10`}
                >
                    <Routes>
                        {availableMenus.map((menu) => (
                            <Route path={menu.path.replace(parentPath, "")} element={
                                menu.page ? menu.page : <>NO IMPLEMENTATIONS</>
                            } />
                        ))}
                    </Routes>
                </Content>

                {/* Mobile Menu Overlay */}
                {mobileMenuOpen && (
                    <div className="fixed inset-0 md:hidden mobile-menu-overlay" style={{ zIndex: 999999 }}>
                        {/* Background Overlay */}
                        <div
                            className="absolute inset-0 bg-black bg-opacity-50"
                            onClick={() => setMobileMenuOpen(false)}
                        />

                        {/* Mobile Menu */}
                        <div className="absolute left-0 top-0 bottom-0 w-64 bg-white shadow-xl overflow-auto mobile-menu-panel z-70">
                            <div className="flex items-center justify-between px-4 py-4 border-b">
                                <span className="text-lg font-semibold text-gray-900">{t('pages.managerContainer.menu')}</span>
                                <Button
                                    type="text"
                                    size="small"
                                    onClick={() => setMobileMenuOpen(false)}
                                    className="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-gray-100"
                                >
                                    <MenuFoldOutlined />
                                </Button>
                            </div>

                            <Menu
                                mode="inline"
                                items={menuItems}
                                selectedKeys={selectedKeys.map((e) => e.key.toString())}
                                openKeys={openKeys}
                                onOpenChange={setOpenKeys}
                                onClick={(e) => {
                                    handleMenuClick(e);
                                    setMobileMenuOpen(false);
                                }}
                                className="py-4 px-2 border-none"
                            />
                        </div>
                    </div>
                )}
            </Layout>

            <ThemeColorPickerModal
                open={themeModalOpen}
                currentThemeKey={currentThemeKey}
                onClose={() => setThemeModalOpen(false)}
                onThemeChange={handleThemeChange}
            />
        </Layout>
    );
}
