import {ToolOutlined, AppstoreOutlined} from "@ant-design/icons";
import type {CrystalWebPlugin} from "@/plugin/types.ts";
import {PlaygroundPage} from "./pages/PlaygroundPage.tsx";

const extPlaygroundPlugin: CrystalWebPlugin = {
    configure(registry) {
        registry.addMenuGroup({
            name: 'playground',
            icon: <AppstoreOutlined />,
            label: 'menu.groups.playground',
        });

        registry.addAdminMenu({
            key: '/manager/ext-playground',
            path: '/manager/ext-playground',
            icon: <ToolOutlined />,
            label: 'menu.extPlayground',
            page: <PlaygroundPage />,
            group: 'playground',
        });

        registry.addPublicMenu({
            key: '/manager/ext-playground',
            path: '/manager/ext-playground',
            icon: <ToolOutlined />,
            label: 'menu.extPlayground',
            page: <PlaygroundPage />,
            group: 'playground',
        });

        registry.addTopLevelRoute({
            path: '/ext-playground',
            element: <PlaygroundPage />,
        });
    },

    i18nResources: {
        'zh-CN': {
            menu: {
                groups: {
                    playground: '演示',
                },
                extPlayground: '扩展演示',
            },
            pages: {
                extPlayground: {
                    title: '扩展演示',
                    description: '这是一个来自 <code>ext-playground</code> 插件的演示页面。它展示了插件系统无需修改框架源码即可注入路由、菜单和页面的能力。',
                    secondary: '来自 ext-playground 插件的问候！',
                },
            },
        },
        'en-US': {
            menu: {
                groups: {
                    playground: 'Playground',
                },
                extPlayground: 'Ext Playground',
            },
            pages: {
                extPlayground: {
                    title: 'Ext Playground',
                    description: 'This is a demo page from the <code>ext-playground</code> plugin. It demonstrates the plugin system\'s ability to inject routes, menus, and pages without modifying the framework source code.',
                    secondary: 'Hello from ext-playground plugin!',
                },
            },
        },
    },
};

export default extPlaygroundPlugin;
