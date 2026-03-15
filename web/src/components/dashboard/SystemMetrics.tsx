import {Badge, Card, Divider, message, Progress, Segmented} from "antd";
import { useEffect, useRef, useState } from "react";
import {
    AppstoreOutlined,
    CloudOutlined,
    DatabaseOutlined,
    DesktopOutlined,
    ThunderboltOutlined
} from "@ant-design/icons";
import { getSystemMetrics } from "@/api/dashboard.api.ts";
import type { SystemMetricsVO } from "@/types/dashboard.types.ts";

const autoRefreshOptions = [
    { label: "1s", value: 1000 },
    { label: "3s", value: 3000 },
    { label: "5s", value: 5000 },
    { label: "1m", value: 60000 },
    { label: "3m", value: 180000 },
    { label: "5m", value: 300000 },
    { label: "10m", value: 600000 },
    { label: "15m", value: 900000 },
    { label: "30m", value: 1800000 },
];

interface SystemMetricConfig {
    key: keyof Omit<SystemMetricsVO, "gcMetrics" | "serverInfo">;
    label: string;
    unit: "bytes" | "percent" | "count" | "core" | "unit";
    totalUnit: "bytes" | "percent" | "count" | "core" | "unit";
    strokeColor: string;
}

const systemMetricConfig: SystemMetricConfig[] = [
    { key: "cpuUsage", label: "CPU 使用率", unit: "percent", totalUnit: "core", strokeColor: "#3b82f6" },
    { key: "memoryUsage", label: "内存占用", unit: "bytes", totalUnit: "bytes", strokeColor: "#10b981" },
    { key: "jvmHeapMemory", label: "JVM 堆内存", unit: "bytes", totalUnit: "bytes", strokeColor: "#8b5cf6" },
    { key: "jvmNonHeapMemory", label: "JVM 非堆内存", unit: "bytes", totalUnit: "bytes", strokeColor: "#f59e0b" },
    { key: "systemLoad", label: "系统负载", unit: "unit", totalUnit: "core", strokeColor: "#f97316" },
    { key: "diskUsage", label: "磁盘使用率", unit: "bytes", totalUnit: "bytes", strokeColor: "#6366f1" },
    { key: "dbConnections", label: "数据库连接池", unit: "count", totalUnit: "count", strokeColor: "#ec4899" },
];

function formatNumber(num: number): string {
    return num.toLocaleString("zh-CN");
}

function formatBytes(bytes: number): string {
    if (bytes === 0) return "0 B";
    const k = 1024;
    const sizes = ["B", "KB", "MB", "GB", "TB"];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + " " + sizes[i];
}

function formatMetricValue(used: number, unit: "bytes" | "percent" | "count" | "core" | "unit"): string {
    switch (unit) {
        case "bytes":
            return formatBytes(used);
        case "percent":
            return `${Math.round(used)}%`;
        case "count":
            return formatNumber(used);
        case "core":
            return `${used} Cores`;
        case "unit":
            return `${used} Unit`;
        default:
            return String(used);
    }
}

function formatMetricTotal(total: number, unit: "bytes" | "percent" | "count" | "core" | "unit"): string {
    switch (unit) {
        case "bytes":
            return formatBytes(total);
        case "percent":
            return `${Math.round(total)}%`;
        case "count":
            return formatNumber(total);
        case "core":
            return `${total} Cores`;
        case "unit":
            return `${total} Unit`;
        default:
            return String(total);
    }
}

export function SystemMetrics() {
    const [systemMetrics, setSystemMetrics] = useState<SystemMetricsVO | null>(null);
    const [loading, setLoading] = useState(false);
    const [refreshInterval, setRefreshInterval] = useState<number>(5000);
    const [lastUpdated, setLastUpdated] = useState<Date | null>(null);
    const timerRef = useRef<number | null>(null);

    const loadSystemMetrics = async () => {
        setLoading(true);
        try {
            const res = await getSystemMetrics();
            setSystemMetrics(res.data);
            setLastUpdated(new Date());
        } catch (error) {
            void message.warning("无法获取系统监控报告");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadSystemMetrics();
    }, []);

    useEffect(() => {
        if (timerRef.current) {
            clearInterval(timerRef.current);
        }

        timerRef.current = setInterval(() => {
            loadSystemMetrics();
        }, refreshInterval);

        return () => {
            if (timerRef.current) {
                clearInterval(timerRef.current);
            }
        };
    }, [refreshInterval]);

    return (
        <Card
            title={
                <div className="flex items-center justify-between w-full">
                    <div className="flex items-center gap-3">
                        <span className="text-sm font-bold text-slate-800 flex items-center gap-2">
                            <ThunderboltOutlined /> 系统资源监控
                        </span>
                        {lastUpdated && (
                            <span className="text-xs text-gray-400">
                                最后更新于 {lastUpdated.toLocaleTimeString("zh-CN", { hour: "2-digit", minute: "2-digit", second: "2-digit" })}
                            </span>
                        )}
                    </div>
                    <Segmented
                        options={autoRefreshOptions}
                        value={refreshInterval}
                        onChange={(value) => setRefreshInterval(value as number)}
                        className="bg-slate-100"
                        size="small"
                    />
                </div>
            }
            className="rounded-3xl border-none shadow-sm mb-8"
        >
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                {systemMetricConfig.map((config) => {
                    const metric = systemMetrics?.[config.key];
                    if (!metric) return null;

                    const displayValue = formatMetricValue(metric.used, config.unit);
                    const totalDisplay = formatMetricTotal(metric.total, config.totalUnit);

                    return (
                        <div key={config.key} className="p-5 bg-slate-50 rounded-2xl">
                            <div className="flex justify-between items-center mb-4">
                                <span className="text-sm font-medium text-slate-500">
                                    {config.label}
                                </span>
                                <span className="text-base font-bold text-slate-700">
                                    {displayValue}
                                </span>
                            </div>
                            <Progress
                                percent={metric.usage}
                                strokeColor={config.strokeColor}
                                showInfo={false}
                                strokeWidth={8}
                                className="rounded-full mb-3"
                            />
                            <div className="flex justify-between text-xs text-slate-400">
                                <span>使用率: {Math.round(metric.usage)}%</span>
                                <span>{totalDisplay}</span>
                            </div>
                        </div>
                    );
                })}
                {systemMetrics?.gcMetrics && (
                    <div className="p-5 bg-slate-50 rounded-2xl">
                        <div className="flex justify-between items-center mb-4">
                            <span className="text-sm font-medium text-slate-500">
                                GC 暂停时间
                            </span>
                            <span className="text-base font-bold text-slate-700">
                                {systemMetrics.gcMetrics.avgTime} ms
                            </span>
                        </div>
                        <div
                            className="h-2 rounded-full mb-3"
                            style={{
                                backgroundColor: "#f9731620",
                            }}
                        >
                            <div
                                className="h-full rounded-full transition-all duration-300"
                                style={{
                                    width: `${Math.min(systemMetrics.gcMetrics.avgTime, 100)}%`,
                                    backgroundColor: "#f97316",
                                }}
                            />
                        </div>
                        <div className="flex justify-between text-xs text-slate-400">
                            <span>累计: {systemMetrics.gcMetrics.totalTime / 1000} s</span>
                            <span>次数: {systemMetrics.gcMetrics.count}</span>
                        </div>
                    </div>
                )}
            </div>
            <Divider className="my-4" />
            <div className="flex items-center justify-between px-2 flex-wrap gap-4">
                <div className="flex items-center space-x-6 flex-wrap gap-y-2">
                    <div className="flex items-center space-x-2">
                        <DesktopOutlined className="text-slate-400" />
                        <span className="text-xs text-slate-500">
                            服务器: {systemMetrics?.serverInfo?.serverName ?? "-"}
                        </span>
                    </div>
                    <div className="flex items-center space-x-2">
                        <DatabaseOutlined className="text-slate-400" />
                        <span className="text-xs text-slate-500">
                            数据库: {systemMetrics?.serverInfo?.databaseVersion ?? "-"}
                        </span>
                    </div>
                    <div className="flex items-center space-x-2">
                        <CloudOutlined className="text-slate-400" />
                        <span className="text-xs text-slate-500">
                            Redis: {systemMetrics?.serverInfo?.redisVersion ?? "-"}
                        </span>
                    </div>
                    <div className="flex items-center space-x-2">
                        <AppstoreOutlined className="text-slate-400" />
                        <span className="text-xs text-slate-500">
                            版本: {systemMetrics?.serverInfo?.projectVersion ?? "-"}
                        </span>
                    </div>
                </div>
                <div className="flex items-center space-x-2">
                    <Badge status="processing" color="blue" />
                    <span className="text-xs text-slate-500">
                        系统运行时间: {systemMetrics?.serverInfo?.uptime ?? "-"}
                    </span>
                </div>
            </div>
        </Card>
    );
}
