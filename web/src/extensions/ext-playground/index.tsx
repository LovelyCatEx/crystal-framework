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
                    durations: {
                        m1: '1 分钟',
                        m5: '5 分钟',
                        m15: '15 分钟',
                        m30: '30 分钟',
                        h1: '1 小时',
                        h3: '3 小时',
                        h5: '5 小时',
                        h12: '12 小时',
                        d1: '1 天',
                        d3: '3 天',
                        d5: '5 天',
                        d7: '7 天',
                        d14: '14 天',
                    },
                    metrics: {
                        cpuUsage: 'CPU 使用率 (%)',
                        cpuLoadAverage: 'CPU 负载',
                        memoryUsed: '内存使用 (GB)',
                        jvmHeapUsed: 'JVM 堆使用 (GB)',
                        jvmNonHeapCommitted: 'JVM 非堆已分配 (GB)',
                        jvmNonHeapUsed: 'JVM 非堆使用 (GB)',
                        diskUsed: '磁盘使用 (GB)',
                        dbConnectionsActive: '数据库活跃连接',
                        gcCount: 'GC 次数',
                        gcTime: 'GC 时间 (ms)',
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
                    durations: {
                        m1: '1 Minute',
                        m5: '5 Minutes',
                        m15: '15 Minutes',
                        m30: '30 Minutes',
                        h1: '1 Hour',
                        h3: '3 Hours',
                        h5: '5 Hours',
                        h12: '12 Hours',
                        d1: '1 Day',
                        d3: '3 Days',
                        d5: '5 Days',
                        d7: '7 Days',
                        d14: '14 Days',
                    },
                    metrics: {
                        cpuUsage: 'CPU Usage',
                        cpuLoadAverage: 'CPU Load Average',
                        memoryUsed: 'Memory Used',
                        jvmHeapUsed: 'JVM Heap Used',
                        jvmNonHeapCommitted: 'JVM NonHeap Committed',
                        jvmNonHeapUsed: 'JVM NonHeap Used',
                        diskUsed: 'Disk Used',
                        dbConnectionsActive: 'DB Connections Active',
                        gcCount: 'GC Count',
                        gcTime: 'GC Time',
                    },
                },
            },
        },
    },
};

export default extPlaygroundPlugin;
