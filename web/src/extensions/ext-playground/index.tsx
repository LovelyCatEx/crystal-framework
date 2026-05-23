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
                    chartTitle: '指标趋势',
                    syncCrosshair: '同步十字',
                    metrics: {
                        cpuUsage: 'CPU 使用率',
                        cpuLoadAverage: 'CPU 负载',
                        cpuProcessors: 'CPU 核心数',
                        memoryTotal: '内存总量',
                        memoryUsed: '内存使用',
                        jvmHeapMax: 'JVM 堆最大值',
                        jvmHeapUsed: 'JVM 堆使用',
                        jvmNonHeapCommitted: 'JVM 非堆已分配',
                        jvmNonHeapUsed: 'JVM 非堆使用',
                        diskTotal: '磁盘总量',
                        diskUsed: '磁盘使用',
                        dbConnectionsActive: '数据库活跃连接',
                        dbConnectionsMax: '数据库最大连接',
                        gcCount: 'GC 次数',
                        gcTime: 'GC 时间',
                    },
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
                    chartTitle: 'Metrics Trend',
                    syncCrosshair: 'Sync Crosshair',
                    metrics: {
                        cpuUsage: 'CPU Usage',
                        cpuLoadAverage: 'CPU Load Average',
                        cpuProcessors: 'CPU Processors',
                        memoryTotal: 'Memory Total',
                        memoryUsed: 'Memory Used',
                        jvmHeapMax: 'JVM Heap Max',
                        jvmHeapUsed: 'JVM Heap Used',
                        jvmNonHeapCommitted: 'JVM NonHeap Committed',
                        jvmNonHeapUsed: 'JVM NonHeap Used',
                        diskTotal: 'Disk Total',
                        diskUsed: 'Disk Used',
                        dbConnectionsActive: 'DB Connections Active',
                        dbConnectionsMax: 'DB Connections Max',
                        gcCount: 'GC Count',
                        gcTime: 'GC Time',
                    },
                },
            },
        },
    },
};

export default extPlaygroundPlugin;
