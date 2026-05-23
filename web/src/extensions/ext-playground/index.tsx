import {MonitorOutlined} from "@ant-design/icons";
import type {CrystalWebPlugin} from "@/plugin/types.ts";
import {SystemMonitorPage} from "./pages/SystemMonitorPage.tsx";

const extPlaygroundPlugin: CrystalWebPlugin = {
    configure(registry) {
        registry.addAdminMenu({
            key: '/manager/ext/system-monitor',
            path: '/manager/ext/system-monitor',
            icon: <MonitorOutlined />,
            label: 'menu.admin.systemMonitor',
            page: <SystemMonitorPage />,
            group: 'monitor',
        });
    },

    i18nResources: {
        'zh-CN': {
            menu: {
                admin: {
                    systemMonitor: '系统监控',
                },
            },
            pages: {
                systemMonitor: {
                    title: '系统监控',
                    subtitle: '实时查看服务器 CPU、内存、磁盘和 JVM 等系统指标',
                },
            },
        },
        'en-US': {
            menu: {
                admin: {
                    systemMonitor: 'System Monitor',
                },
            },
            pages: {
                systemMonitor: {
                    title: 'System Monitor',
                    subtitle: 'Real-time view of CPU, memory, disk, JVM and other system metrics',
                },
            },
        },
    },
};

export default extPlaygroundPlugin;
