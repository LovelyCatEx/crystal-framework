import {Badge, Card, Divider, message, Progress, Segmented, theme} from "antd";
import {useEffect, useRef, useState} from "react";
import {useTranslation} from "react-i18next";
import {
    AppstoreOutlined,
    CloudOutlined,
    DatabaseOutlined,
    DesktopOutlined,
    ThunderboltOutlined
} from "@ant-design/icons";
import {getSystemMetrics} from "@/api/dashboard.api.ts";
import type {SystemMetricsVO} from "@/types/dashboard.types.ts";

const { useToken } = theme;

interface SystemMetricConfig {
    key: keyof Omit<SystemMetricsVO, "gcMetrics" | "serverInfo">;
    labelKey: string;
    unit: "bytes" | "percent" | "count" | "core" | "unit";
    totalUnit: "bytes" | "percent" | "count" | "core" | "unit";
    strokeColor: string;
}

function formatNumber(num: number): string {
    return num.toLocaleString();
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
    const { token } = useToken();
    const { t } = useTranslation();
    const [systemMetrics, setSystemMetrics] = useState<SystemMetricsVO | null>(null);
    const [loading, setLoading] = useState(false);
    const [refreshInterval, setRefreshInterval] = useState<number>(5000);
    const [lastUpdated, setLastUpdated] = useState<Date | null>(null);
    const timerRef = useRef<number | null>(null);

    const autoRefreshOptions = [
        { label: t('components.dashboard.systemMetrics.refreshOptions.1s'), value: 1000 },
        { label: t('components.dashboard.systemMetrics.refreshOptions.3s'), value: 3000 },
        { label: t('components.dashboard.systemMetrics.refreshOptions.5s'), value: 5000 },
        { label: t('components.dashboard.systemMetrics.refreshOptions.1m'), value: 60000 },
        { label: t('components.dashboard.systemMetrics.refreshOptions.3m'), value: 180000 },
        { label: t('components.dashboard.systemMetrics.refreshOptions.5m'), value: 300000 },
        { label: t('components.dashboard.systemMetrics.refreshOptions.10m'), value: 600000 },
        { label: t('components.dashboard.systemMetrics.refreshOptions.15m'), value: 900000 },
        { label: t('components.dashboard.systemMetrics.refreshOptions.30m'), value: 1800000 },
    ];

    const systemMetricConfig: SystemMetricConfig[] = [
        { key: "cpuUsage", labelKey: "cpuUsage", unit: "percent", totalUnit: "core", strokeColor: "#3b82f6" },
        { key: "memoryUsage", labelKey: "memoryUsage", unit: "bytes", totalUnit: "bytes", strokeColor: "#10b981" },
        { key: "jvmHeapMemory", labelKey: "jvmHeapMemory", unit: "bytes", totalUnit: "bytes", strokeColor: "#8b5cf6" },
        { key: "jvmNonHeapMemory", labelKey: "jvmNonHeapMemory", unit: "bytes", totalUnit: "bytes", strokeColor: "#f59e0b" },
        { key: "systemLoad", labelKey: "systemLoad", unit: "unit", totalUnit: "core", strokeColor: "#f97316" },
        { key: "diskUsage", labelKey: "diskUsage", unit: "bytes", totalUnit: "bytes", strokeColor: "#6366f1" },
        { key: "dbConnections", labelKey: "dbConnections", unit: "count", totalUnit: "count", strokeColor: "#ec4899" },
    ];

    const loadSystemMetrics = async () => {
        setLoading(true);
        try {
            const res = await getSystemMetrics();
            setSystemMetrics(res.data);
            setLastUpdated(new Date());
        } catch (error) {
            void message.warning(t('components.dashboard.systemMetrics.loadFailed'));
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
                    <div className="flex items-center gap-3" style={{ color: token.colorTextHeading }}>
                        <span
                            className="text-sm font-bold flex items-center gap-2"
                        >
                            <ThunderboltOutlined /> {t('components.dashboard.systemMetrics.title')}
                        </span>
                        {lastUpdated && (
                            <span
                                className={`text-xs flex items-center gap-1 transition-colors duration-300 ${loading ? 'text-blue-400' : ''}`}
                                style={{ color: loading ? token.colorPrimary : token.colorTextSecondary }}
                            >
                                {t('components.dashboard.systemMetrics.lastUpdated')} {lastUpdated.toLocaleTimeString(undefined, { hour: "2-digit", minute: "2-digit", second: "2-digit" })}
                            </span>
                        )}
                    </div>
                    <Segmented
                        options={autoRefreshOptions}
                        value={refreshInterval}
                        onChange={(value) => setRefreshInterval(value as number)}
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
                        <div
                            key={config.key}
                            className="p-5 rounded-2xl bg-slate-50 dark:bg-slate-200/5"
                        >
                            <div className="flex justify-between items-center mb-4">
                                <span
                                    className="text-sm font-medium"
                                    style={{ color: token.colorTextSecondary }}
                                >
                                    {t(`components.dashboard.systemMetrics.metrics.${config.labelKey}`)}
                                </span>
                                <span
                                    className="text-base font-bold"
                                    style={{ color: token.colorText }}
                                >
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
                            <div
                                className="flex justify-between text-xs"
                                style={{ color: token.colorTextTertiary }}
                            >
                                <span>{t('components.dashboard.systemMetrics.units.usage')}: {Math.round(metric.usage)}%</span>
                                <span>{totalDisplay}</span>
                            </div>
                        </div>
                    );
                })}
                {systemMetrics?.gcMetrics && (
                    <div
                        className="p-5 rounded-2xl bg-slate-50 dark:bg-slate-200/5"
                    >
                        <div className="flex justify-between items-center mb-4">
                            <span
                                className="text-sm font-medium"
                                style={{ color: token.colorTextSecondary }}
                            >
                                {t('components.dashboard.systemMetrics.metrics.gcPauseTime')}
                            </span>
                            <span
                                className="text-base font-bold"
                                style={{ color: token.colorText }}
                            >
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
                        <div
                            className="flex justify-between text-xs"
                            style={{ color: token.colorTextTertiary }}
                        >
                            <span>Total: {systemMetrics.gcMetrics.totalTime / 1000} s</span>
                            <span>Count: {systemMetrics.gcMetrics.count}</span>
                        </div>
                    </div>
                )}
            </div>
            <Divider className="my-4" />
            <div className="flex items-center justify-between px-2 flex-wrap gap-4">
                <div className="flex items-center space-x-6 flex-wrap gap-y-2">
                    <div className="flex items-center space-x-2">
                        <DesktopOutlined style={{ color: token.colorTextTertiary }} />
                        <span className="text-xs" style={{ color: token.colorTextSecondary }}>
                            Server: {systemMetrics?.serverInfo?.serverName ?? "-"}
                        </span>
                    </div>
                    <div className="flex items-center space-x-2">
                        <DatabaseOutlined style={{ color: token.colorTextTertiary }} />
                        <span className="text-xs" style={{ color: token.colorTextSecondary }}>
                            DB: {systemMetrics?.serverInfo?.databaseVersion ?? "-"}
                        </span>
                    </div>
                    <div className="flex items-center space-x-2">
                        <CloudOutlined style={{ color: token.colorTextTertiary }} />
                        <span className="text-xs" style={{ color: token.colorTextSecondary }}>
                            Redis: {systemMetrics?.serverInfo?.redisVersion ?? "-"}
                        </span>
                    </div>
                    <div className="flex items-center space-x-2">
                        <AppstoreOutlined style={{ color: token.colorTextTertiary }} />
                        <span className="text-xs" style={{ color: token.colorTextSecondary }}>
                            Version: {systemMetrics?.serverInfo?.projectVersion ?? "-"}
                        </span>
                    </div>
                </div>
                <div className="flex items-center space-x-2">
                    <Badge status="processing" color="blue" />
                    <span className="text-xs" style={{ color: token.colorTextSecondary }}>
                        Uptime: {systemMetrics?.serverInfo?.uptime ?? "-"}
                    </span>
                </div>
            </div>
        </Card>
    );
}
