import {Avatar, Button, Divider, Dropdown, Layout, Menu, Space} from "antd";
import {LogoutOutlined, MenuFoldOutlined, MenuUnfoldOutlined, UserOutlined} from "@ant-design/icons";
import {Route, Routes, useLocation, useNavigate} from "react-router-dom";
import {Content, Header} from "antd/es/layout/layout";
import Sider from "antd/es/layout/Sider";
import {clearUserAuthentication} from "../../utils/token.utils.ts";
import {useEffect, useMemo, useState} from "react";
import {buildDocumentTitle, ProjectDisplayName} from "../../global/global-settings.ts";
import {useLoggedUser} from "../../compositions/use-logged-user.ts";
import {computeAccessibleMenus, menuPathLogin, menuPathProfile} from "../../router";
import './ManagerContainerPageStyles.css';

export function ManagerContainerPage({ parentPath }: { parentPath: string }) {
    const loggedUser = useLoggedUser();
    const [collapsed, setCollapsed] = useState(false);
    const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
    const navigate = useNavigate();
    const location = useLocation();

    const availableMenus = useMemo(() => {
        return computeAccessibleMenus(loggedUser.accessibleMenuPaths);
    }, [loggedUser.accessibleMenuPaths]);

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

    return (
        <Layout className="min-h-screen bg-[#f8fafc]">
            <Header className="fixed top-0 left-0 w-full h-16 px-6 flex items-center justify-between z-50 backdrop-blur-md border-b border-gray-100 shadow-sm">
                <div className="flex items-center gap-2 cursor-pointer" onClick={() => navigate('/')}>
                    <img src="/logo.svg" alt="Logo" className="w-8 h-8"/>
                    <span className="text-2xl font-bold tracking-tight text-gray-900">
                        {ProjectDisplayName}
                    </span>
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

                    <Dropdown
                        menu={{
                            items: [
                                { key: 'profile', label: '个人中心', icon: <UserOutlined /> },
                                {
                                    key: 'logout',
                                    label: '退出登录',
                                    icon: <LogoutOutlined />,
                                    danger: true,
                                },
                            ],
                            onClick: (e) => {
                                if (e.key === 'profile') {
                                    navigate(menuPathProfile);
                                } else if (e.key === 'logout') {
                                    clearUserAuthentication();
                                    navigate(menuPathLogin);
                                }
                            },
                        }}
                    >
                        <Space className="cursor-pointer">
                            <Avatar
                                style={{ backgroundColor: '#2563eb' }}
                                icon={<UserOutlined />}
                            />
                            <span className="hidden sm:inline font-medium text-gray-700">
                                {loggedUser?.userProfile?.nickname}
                            </span>
                        </Space>
                    </Dropdown>
                </div>
            </Header>

            <Layout className="mt-16">
                {/* Left-Side Menu */}
                <Sider
                    width={240}
                    trigger={null}
                    collapsible
                    collapsed={collapsed}
                    className="hidden md:block overflow-auto h-[calc(100vh-64px)] fixed left-0 border-r border-gray-100"
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
                        items={availableMenus}
                        onClick={handleMenuClick}
                        className="py-4 px-2 border-none"
                    />
                </Sider>

                {/* Main Router View */}
                <Content
                    className={`p-6 transition-all duration-300 ${collapsed ? 'md:ml-20' : 'md:ml-[240px]'} relative z-10`}
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
                                <span className="text-lg font-semibold text-gray-900">菜单</span>
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
                                items={availableMenus}
                                selectedKeys={selectedKeys.map((e) => e.key.toString())}
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
        </Layout>
    );
}