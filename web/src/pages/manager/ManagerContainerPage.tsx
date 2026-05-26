import React, {useEffect, useMemo, useRef, useState} from "react";
import {Avatar, Button, Dropdown, Layout, Menu, message, Space, Spin, Tabs, theme, Watermark} from "antd";
import {
    BgColorsOutlined,
    CheckOutlined,
    CloseCircleOutlined,
    CloseOutlined,
    DownOutlined,
    EditOutlined,
    LeftOutlined,
    LoadingOutlined,
    LogoutOutlined,
    MenuFoldOutlined,
    MenuUnfoldOutlined,
    RightOutlined,
    ShopOutlined,
    UserOutlined,
    UserSwitchOutlined
} from "@ant-design/icons";
import type {DragEndEvent} from '@dnd-kit/core';
import {closestCenter, DndContext, PointerSensor, useSensor} from '@dnd-kit/core';
import {arrayMove, horizontalListSortingStrategy, SortableContext, useSortable,} from '@dnd-kit/sortable';
import {CSS} from '@dnd-kit/utilities';
import {ThemeSettingsModal} from "@/components/ThemeSettingsModal.tsx";
import {ThemeModeSelector} from "@/components/ThemeModeSelector.tsx";
import {
    getStoredPageAnimation,
    getStoredTabEnabled,
    getStoredTabSize,
    getStoredThemeKey,
    getStoredThemeMode,
    setStoredPageAnimation,
    setStoredThemeKey,
    setStoredThemeMode,
    THEME_MODE_STORAGE_KEY,
    type ThemeTabSize,
    updateThemeCSSVariables
} from "@/global/theme-config.ts";
import type {PageAnimationType, ThemeColor, ThemeMode} from "@/types/theme.types.ts";
import {Route, Routes, useLocation, useNavigate} from "react-router-dom";
import {useTranslation} from "react-i18next";
import {Content, Header} from "antd/es/layout/layout";
import Sider from "antd/es/layout/Sider";
import {clearUserAuthentication, setUserAuthentication} from "@/utils/token.utils.ts";
import {buildDocumentTitle, ProjectDisplayName} from "@/global/global-settings.ts";
import {useLoggedUser} from "@/compositions/use-logged-user.ts";
import {computeAccessibleMenus, getMenuGroups, type RouteItem} from "@/router";
import {menuPathLogin, menuPathProfile} from "@/router/paths.ts";
import './ManagerContainerPageStyles.css';
import type {ItemType} from "antd/es/menu/interface";
import type {UserTenantVO} from "@/types/tenant/tenant.types.ts";
import {switchTenant} from "@/api/auth/auth.api.ts";
import {useUserTenants} from "@/compositions/use-tenant.ts";
import {TenantMemberStatus} from "@/types/tenant/tenant-member.types.ts";
import {LanguageSwitcher} from "@/components/LanguageSwitcher.tsx";
import {useSystemIntegrated} from "@/context/SystemIntegratedContext.tsx";
import {MANAGER_PAGE_TABS_EXPIRES_IN, MANAGER_PAGE_TABS_STORAGE_KEY_PREFIX} from "@/global/constants.ts";
import {ContextMenu} from "@/components/contextmenu";
import type {ContextMenuItem} from "@/components/contextmenu";

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

interface TabItem {
    key: string;
    label: string;
    path: string;
}

interface StoredManagerTabs {
    expiresAt: number;
    tabs: TabItem[];
}



interface DraggableTabPaneProps extends React.HTMLAttributes<HTMLDivElement> {
    'data-node-key': string;
}

const DraggableTabNode: React.FC<Readonly<DraggableTabPaneProps>> = ({ className, ...props }) => {
    const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({
        id: props['data-node-key'],
    });

    const style: React.CSSProperties = {
        ...props.style,
        transform: CSS.Translate.toString(transform),
        transition,
        cursor: isDragging ? 'move' : 'pointer',
    };

    return React.cloneElement(props.children as React.ReactElement<any>, {
        ref: setNodeRef,
        style,
        ...attributes,
        ...listeners,
    });
};

function isValidManagerTab(item: unknown): item is TabItem {
    return (
        typeof item === 'object' &&
        item !== null &&
        typeof (item as TabItem).key === 'string' &&
        typeof (item as TabItem).label === 'string' &&
        typeof (item as TabItem).path === 'string'
    );
}

function getStoredManagerTabs(storageKey?: string): TabItem[] {
    if (!storageKey) return [];

    try {
        const stored = localStorage.getItem(storageKey);
        if (!stored) return [];

        const parsed = JSON.parse(stored) as StoredManagerTabs | TabItem[];
        if (Array.isArray(parsed)) {
            return parsed.filter(isValidManagerTab);
        }

        if (typeof parsed !== 'object' || parsed === null || !Array.isArray(parsed.tabs)) {
            return [];
        }

        if (Date.now() > parsed.expiresAt) {
            localStorage.removeItem(storageKey);
            return [];
        }

        return parsed.tabs.filter(isValidManagerTab);
    } catch {
        return [];
    }
}

function setStoredManagerTabs(storageKey: string, tabs: TabItem[]) {
    const stored: StoredManagerTabs = {
        expiresAt: Date.now() + MANAGER_PAGE_TABS_EXPIRES_IN,
        tabs,
    };

    localStorage.setItem(storageKey, JSON.stringify(stored));
}

function getAvailableTab(tab: TabItem, availableMenus: RouteItem[]): TabItem | null {
    const menu = availableMenus.find((item) => item.key === tab.key);
    if (!menu) return null;

    return {
        key: menu.key as string,
        label: menu.label,
        path: tab.path.startsWith(menu.key as string) ? tab.path : menu.path,
    };
}

function ManagerPageTabs({ availableMenus, availableMenusLoading, tabSize, storageKey }: { availableMenus: RouteItem[], availableMenusLoading: boolean, tabSize?: ThemeTabSize, storageKey?: string }) {
    const location = useLocation();
    const navigate = useNavigate();
    const { t } = useTranslation();
    const [tabs, setTabs] = useState<TabItem[]>([]);
    const [editMode, setEditMode] = useState<boolean>(false);
    const [storageReady, setStorageReady] = useState<boolean>(false);
    const [hydratedStorageKey, setHydratedStorageKey] = useState<string | null>(null);

    const sensor = useSensor(PointerSensor, { activationConstraint: { distance: 10 } });

    useEffect(() => {
        if (!storageKey) {
            setHydratedStorageKey(null);
            setStorageReady(true);
            return;
        }

        if (availableMenusLoading) {
            setHydratedStorageKey(null);
            setStorageReady(false);
            return;
        }

        const storedTabs = getStoredManagerTabs(storageKey)
            .map(tab => getAvailableTab(tab, availableMenus))
            .filter((tab): tab is TabItem => tab !== null);

        setStoredManagerTabs(storageKey, storedTabs);
        setTabs(storedTabs);
        setHydratedStorageKey(storageKey);
        setStorageReady(true);
    }, [storageKey, availableMenus, availableMenusLoading]);

    useEffect(() => {
        if (!storageReady || (storageKey && hydratedStorageKey !== storageKey)) return;

        const currentPath = location.pathname;
        const matchedMenu = availableMenus.find((item) => currentPath.startsWith(item.key as string));

        if (matchedMenu) {
            setTabs(prev => {
                const existingIndex = prev.findIndex(tab => tab.key === matchedMenu.key);
                const newTab: TabItem = {
                    key: matchedMenu.key as string,
                    label: matchedMenu.label,
                    path: currentPath
                };

                if (existingIndex >= 0) {
                    const newTabs = [...prev];
                    newTabs[existingIndex] = newTab;
                    return newTabs;
                }

                return [...prev, newTab];
            });
        }
    }, [location.pathname, availableMenus, storageReady, storageKey, hydratedStorageKey]);

    useEffect(() => {
        if (!storageKey || !storageReady || hydratedStorageKey !== storageKey) return;
        setStoredManagerTabs(storageKey, tabs);
    }, [storageKey, storageReady, hydratedStorageKey, tabs]);

    const pendingRemoveRef = useRef<string | null>(null);

    const handleTabChange = (key: string) => {
        // When removing a tab, Tabs internally fires onChange with the removed tab's key
        // before React processes the setTabs state update. handleTabChange reads the stale
        // tabs closure (still containing the removed tab) and navigates to it, which triggers
        // the sync effect that re-adds the tab.
        // Track the pending removal key via ref to skip this spurious onChange.
        if (key === pendingRemoveRef.current) {
            pendingRemoveRef.current = null;
            return;
        }
        const tab = tabs.find(t => t.key === key);
        if (tab) {
            navigate(tab.path);
        }
    };

    const handleTabRemove = (targetKey: string) => {
        pendingRemoveRef.current = targetKey;

        const targetTab = tabs.find(tab => tab.key === targetKey);
        const isRemovingActive = targetTab && location.pathname.startsWith(targetTab.key);

        setTabs(prev => prev.filter(tab => tab.key !== targetKey));

        if (isRemovingActive) {
            const targetIndex = tabs.findIndex(tab => tab.key === targetKey);
            const newTabs = tabs.filter(tab => tab.key !== targetKey);
            if (newTabs.length > 0) {
                const nextTab = newTabs[Math.min(targetIndex, newTabs.length - 1)];
                navigate(nextTab.path);
            }
        }
    };

    const handleCloseLeft = (targetKey: string) => {
        const targetIndex = tabs.findIndex(tab => tab.key === targetKey);
        if (targetIndex <= 0) return; // No tabs to the left

        const newTabs = tabs.slice(targetIndex);

        const currentTabKey = tabs.find(tab => location.pathname.startsWith(tab.key))?.key;
        const currentStillExists = currentTabKey ? newTabs.some(tab => tab.key === currentTabKey) : false;
        if (!currentStillExists) {
            navigate(newTabs[0].path);
        }

        setTabs(newTabs);
    };

    const handleCloseRight = (targetKey: string) => {
        const targetIndex = tabs.findIndex(tab => tab.key === targetKey);
        if (targetIndex >= tabs.length - 1) return; // No tabs to the right

        const newTabs = tabs.slice(0, targetIndex + 1);

        const currentTabKey = tabs.find(tab => location.pathname.startsWith(tab.key))?.key;
        const currentStillExists = currentTabKey ? newTabs.some(tab => tab.key === currentTabKey) : false;
        if (!currentStillExists) {
            navigate(newTabs[newTabs.length - 1].path);
        }

        setTabs(newTabs);
    };

    const handleCloseOthers = (targetKey: string) => {
        if (tabs.length <= 1) return; // No other tabs to close

        const targetTab = tabs.find(tab => tab.key === targetKey);
        if (!targetTab) return;

        setTabs([targetTab]);
        navigate(targetTab.path);
    };
    const onDragEnd = ({ active, over }: DragEndEvent) => {
        if (active.id !== over?.id) {
            setTabs((prev) => {
                const activeIndex = prev.findIndex((i) => i.key === active.id);
                const overIndex = prev.findIndex((i) => i.key === over?.id);
                return arrayMove(prev, activeIndex, overIndex);
            });
        }
    };

    const renderTabBarExtra = () => {
        if (tabs.length === 0) return null;

        return (
            <Button
                type={editMode ? 'primary' : 'text'}
                size="small"
                icon={editMode ? <CheckOutlined /> : <EditOutlined />}
                onClick={() => setEditMode(!editMode)}
                className="mr-2"
            />
        );
    }

    return (
        <Tabs
            type={editMode ? 'editable-card' : 'line'}
            size={tabSize}
            hideAdd
            activeKey={tabs.find(tab => location.pathname.startsWith(tab.key))?.key}
            onChange={handleTabChange}
            onEdit={(targetKey, action) => {
                if (action === 'remove') {
                    handleTabRemove(targetKey as string);
                }
            }}
            items={tabs.map((tab, index) => {
                const menu = availableMenus.find(m => m.key === tab.key);
                const isFirst = index === 0;
                const isLast = index === tabs.length - 1;
                const isOnly = tabs.length === 1;
                const isActiveTab = location.pathname.startsWith(tab.key);

                const contextMenuItems: ContextMenuItem[] = [
                    {
                        key: 'close',
                        label: t('pages.managerContainer.tabClose'),
                        icon: <CloseOutlined />,
                        disabled: isOnly,
                        shortcut: { mac: '⌃W', win: 'Alt+W', binding: 'mod+w' },
                    },
                    { key: 'tab-divider', divider: true },
                    {
                        key: 'close-others',
                        label: t('pages.managerContainer.tabCloseOthers'),
                        icon: <CloseCircleOutlined />,
                        disabled: isOnly,
                        shortcut: { mac: '⌃⇧W', win: 'Alt+Shift+W', binding: 'mod+shift+w' },
                    },
                    {
                        key: 'close-left',
                        label: t('pages.managerContainer.tabCloseLeft'),
                        icon: <LeftOutlined />,
                        disabled: isFirst,
                        shortcut: { mac: '⌃⇧[', win: 'Alt+Shift+[', binding: 'mod+shift+bracketleft' },
                    },
                    {
                        key: 'close-right',
                        label: t('pages.managerContainer.tabCloseRight'),
                        icon: <RightOutlined />,
                        disabled: isLast,
                        shortcut: { mac: '⌃⇧]', win: 'Alt+Shift+]', binding: 'mod+shift+bracketright' },
                    },
                ];

                return {
                    key: tab.key,
                    label: (
                        <ContextMenu
                            isActive={isActiveTab}
                            items={contextMenuItems}
                            onAction={(menuKey) => {
                                switch (menuKey) {
                                    case 'close':
                                        handleTabRemove(tab.key);
                                        break;
                                    case 'close-others':
                                        handleCloseOthers(tab.key);
                                        break;
                                    case 'close-left':
                                        handleCloseLeft(tab.key);
                                        break;
                                    case 'close-right':
                                        handleCloseRight(tab.key);
                                        break;
                                }
                            }}
                        >
                            <span className="flex items-center gap-2">
                                {menu?.icon}
                                {tab.label}
                            </span>
                        </ContextMenu>
                    ),
                };
            })}
            className={`manager-tabs ${!editMode ? 'ml-4' : ''} select-none`}
            renderTabBar={(tabBarProps, DefaultTabBar) =>{
                if (editMode) {
                    return <DefaultTabBar {...tabBarProps} />;
                }

                return (
                    <DndContext sensors={[sensor]} onDragEnd={onDragEnd} collisionDetection={closestCenter}>
                        <SortableContext items={tabs.map((i) => i.key)} strategy={horizontalListSortingStrategy}>
                            <DefaultTabBar {...tabBarProps}>
                                {(node) => (
                                    <DraggableTabNode
                                        {...(node as React.ReactElement<DraggableTabPaneProps>).props}
                                        key={node.key}
                                    >
                                        {node}
                                    </DraggableTabNode>
                                )}
                            </DefaultTabBar>
                        </SortableContext>
                    </DndContext>
                )
            }}
            tabBarExtraContent={renderTabBarExtra()}
        />
    );
}

export function ManagerContainerPage({ parentPath }: { parentPath: string }) {
    const { waterMarkInfo } = useSystemIntegrated()
    const { token } = useToken();
    const { t } = useTranslation();

    const loggedUser = useLoggedUser();
    const userTenants = useUserTenants();
    const [collapsed, setCollapsed] = useState(false);
    const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
    const [openKeys, setOpenKeys] = useState<string[]>([]);
    const [themeModalOpen, setThemeModalOpen] = useState(false);
    const [currentThemeKey, setCurrentThemeKey] = useState<string>(getStoredThemeKey);
    const [themeMode, setThemeMode] = useState<ThemeMode>(getStoredThemeMode);
    const [themeTabsEnabled, setThemeTabsEnabled] = useState<boolean>(getStoredTabEnabled)
    const [themeTabSize, setThemeTabSize] = useState<ThemeTabSize>(getStoredTabSize() as ThemeTabSize)
    const [pageAnimation, setPageAnimation] = useState<PageAnimationType>(getStoredPageAnimation)
    const navigate = useNavigate();
    const location = useLocation();

    const watermarkContent = useMemo(() => {
        if (!waterMarkInfo?.enabled) return '';
        
        switch (waterMarkInfo.type) {
            case 'SYSTEM_NAME':
                return ProjectDisplayName;
            case 'USER_NAME':
                return loggedUser.userProfile?.nickname || loggedUser.userProfile?.username || '';
            case 'CUSTOM':
                return waterMarkInfo.customValue;
            default:
                return ProjectDisplayName;
        }
    }, [waterMarkInfo, loggedUser.userProfile]);

    const watermarkFontColor = useMemo(() => {
        const isDark = themeMode === 'dark';
        const defaultColor = isDark ? '#ffffff26' : '#00000026'; // 暗色模式用白色，亮色模式用黑色
        
        if (!waterMarkInfo?.fontColor) return defaultColor;
        
        let alpha = '18';
        
        const rgbaMatch = waterMarkInfo.fontColor.match(/rgba?\((\d+),\s*(\d+),\s*(\d+)(?:,\s*([\d.]+))?\)/);
        if (rgbaMatch) {
            const [, , , , a] = rgbaMatch;
            alpha = a ? Math.round(parseFloat(a) * 255).toString(16).padStart(2, '0') : 'ff';
        } else if (waterMarkInfo.fontColor.startsWith('#')) {
            const hex = waterMarkInfo.fontColor.replace('#', '');
            if (hex.length === 8) {
                alpha = hex.slice(6, 8);
            } else if (hex.length === 4) {
                alpha = hex.charAt(3).repeat(2);
            }
        }
        
        if (isDark) {
            return `#ffffff${alpha}`;
        }
        
        if (rgbaMatch) {
            const [, r, g, b] = rgbaMatch;
            const red = parseInt(r).toString(16).padStart(2, '0');
            const green = parseInt(g).toString(16).padStart(2, '0');
            const blue = parseInt(b).toString(16).padStart(2, '0');
            return `#${red}${green}${blue}${alpha}`;
        }
        
        if (waterMarkInfo.fontColor.startsWith('#')) {
            return waterMarkInfo.fontColor;
        }
        
        return defaultColor;
    }, [waterMarkInfo?.fontColor, themeMode]);

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

    const handlePageAnimationChange = (animation: PageAnimationType) => {
        setPageAnimation(animation);
        setStoredPageAnimation(animation);
    };

    const pageAnimationClass = pageAnimation === 'none' ? '' : `page-animation-${pageAnimation}`;

    const availableMenus = useMemo(() => {
        return computeAccessibleMenus(loggedUser.accessibleMenuPaths ?? [], t);
    }, [loggedUser.accessibleMenuPaths, t]);

    const managerTabsStorageKey = useMemo(() => {
        const userId = loggedUser.userProfile?.id;
        if (!userId) return undefined;

        const tenantId = userTenants.currentTenant?.tenantId ?? 'personal';
        return `${MANAGER_PAGE_TABS_STORAGE_KEY_PREFIX}:${userId}:${tenantId}`;
    }, [loggedUser.userProfile?.id, userTenants.currentTenant?.tenantId]);

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
        <Layout className="min-h-screen h-[100vh] overflow-auto">
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
                    className="hidden md:block overflow-hidden h-[calc(100vh-64px)] fixed left-0 border-r"
                    style={{ borderColor: token.colorBorder }}
                >
                    <Menu
                        mode="inline"
                        selectedKeys={selectedKeys.map((e) => e.key.toString())}
                        openKeys={collapsed ? [] : openKeys}
                        onOpenChange={setOpenKeys}
                        items={menuItems}
                        onClick={handleMenuClick}
                        className="p-2 border-none h-full overflow-auto"
                    />
                </Sider>

                <Button
                    type="default"
                    size="small"
                    onClick={() => setCollapsed(!collapsed)}
                    className="duration-75 fixed hidden md:flex items-center justify-center w-6 h-6 rounded-full shadow-md hover:shadow-lg transition-all z-50 border border-gray-200"
                    style={{
                        left: collapsed ? 80 : 260,
                        top: '50%',
                        transform: 'translate(-50%, -50%)',
                        padding: 0,
                        minWidth: 24,
                    }}
                    icon={collapsed ? <RightOutlined /> : <LeftOutlined />}
                />

                {/* Main Router View */}
                {waterMarkInfo?.enabled ? (
                    <Content
                        className={`transition-all duration-300 ${collapsed ? 'md:ml-20' : 'md:ml-[260px]'} relative z-10 flex flex-col`}
                    >
                        {themeTabsEnabled && (
                            <div className="sticky top-0 w-full z-20" style={{ backgroundColor: token.colorBgContainer }}>
                                <ManagerPageTabs
                                    availableMenus={availableMenus}
                                    availableMenusLoading={loggedUser.isAccessibleMenusLoading}
                                    tabSize={themeTabSize}
                                    storageKey={managerTabsStorageKey}
                                />
                            </div>
                        )}
                        <div className="flex-1 overflow-auto">
                            <Watermark
                                content={watermarkContent}
                                font={{ color: watermarkFontColor }}
                                zIndex={10}
                            >
                                <div className="p-6">
                                    <Routes>
                                        {availableMenus.map((menu) => (
                                            <Route
                                                key={menu.key.toString()}
                                                path={menu.path.replace(parentPath, "")}
                                                element={
                                                    <div key={location.key} className={pageAnimationClass}>
                                                        {menu.page ? menu.page : <>NO IMPLEMENTATIONS</>}
                                                    </div>
                                                }
                                            />
                                        ))}
                                    </Routes>
                                </div>
                            </Watermark>
                        </div>

                    </Content>
                ) : (
                    <Content
                        className={`transition-all duration-300 ${collapsed ? 'md:ml-20' : 'md:ml-[260px]'} relative z-10 flex flex-col`}
                    >
                        {themeTabsEnabled && (
                            <div className="sticky top-0 w-full z-20" style={{ backgroundColor: token.colorBgContainer }}>
                                <ManagerPageTabs
                                    availableMenus={availableMenus}
                                    availableMenusLoading={loggedUser.isAccessibleMenusLoading}
                                    tabSize={themeTabSize}
                                    storageKey={managerTabsStorageKey}
                                />
                            </div>
                        )}
                        <div className="flex-1 p-6 overflow-auto">
                            <Routes>
                                {availableMenus.map((menu) => (
                                    <Route
                                        key={menu.key.toString()}
                                        path={menu.path.replace(parentPath, "")}
                                        element={
                                            <div key={location.key} className={pageAnimationClass}>
                                                {menu.page ? menu.page : <>NO IMPLEMENTATIONS</>}
                                            </div>
                                        }
                                    />
                                ))}
                            </Routes>
                        </div>
                    </Content>
                )}

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

            <ThemeSettingsModal
                open={themeModalOpen}
                currentThemeKey={currentThemeKey}
                enableTabs={themeTabsEnabled}
                tabSize={themeTabSize}
                pageAnimation={pageAnimation}
                onClose={() => setThemeModalOpen(false)}
                onThemeChange={handleThemeChange}
                onTabsEnabledChange={(enabled) => setThemeTabsEnabled(enabled)}
                onTabSizeChange={(size) => setThemeTabSize(size)}
                onPageAnimationChange={handlePageAnimationChange}
            />
        </Layout>
    );
}
