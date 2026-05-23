import {useCallback, useEffect, useRef, useState} from "react";
import {useTranslation} from "react-i18next";
import {Card, Col, Row, Segmented, Switch, theme} from "antd";
import {LineChartOutlined} from "@ant-design/icons";
import * as echarts from "echarts";
import {SystemMetrics} from "@/components/dashboard/SystemMetrics.tsx";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {MetricChart, type MetricChartConfig} from "../components/MetricChart.tsx";
import {queryMetric} from "../api/system-monitor.api.ts";
import type {MetricPoint} from "../types/system-monitor.types.ts";

const {useToken} = theme;

const METRICS: MetricChartConfig[] = [
    {type: "CPU_USAGE", labelKey: "pages.systemMonitor.metrics.cpuUsage", color: "#3b82f6", yMin: 0, yMax: 100},
    {type: "CPU_LOAD_AVERAGE", labelKey: "pages.systemMonitor.metrics.cpuLoadAverage", color: "#8b5cf6"},
    {type: "CPU_PROCESSORS", labelKey: "pages.systemMonitor.metrics.cpuProcessors", color: "#06b6d4"},
    {type: "MEMORY_TOTAL", labelKey: "pages.systemMonitor.metrics.memoryTotal", color: "#10b981"},
    {type: "MEMORY_USED", labelKey: "pages.systemMonitor.metrics.memoryUsed", color: "#34d399"},
    {type: "JVM_HEAP_MAX", labelKey: "pages.systemMonitor.metrics.jvmHeapMax", color: "#f59e0b"},
    {type: "JVM_HEAP_USED", labelKey: "pages.systemMonitor.metrics.jvmHeapUsed", color: "#f97316"},
    {type: "JVM_NONHEAP_COMMITTED", labelKey: "pages.systemMonitor.metrics.jvmNonHeapCommitted", color: "#ef4444"},
    {type: "JVM_NONHEAP_USED", labelKey: "pages.systemMonitor.metrics.jvmNonHeapUsed", color: "#dc2626"},
    {type: "DISK_TOTAL", labelKey: "pages.systemMonitor.metrics.diskTotal", color: "#6366f1"},
    {type: "DISK_USED", labelKey: "pages.systemMonitor.metrics.diskUsed", color: "#818cf8"},
    {type: "DB_CONNECTIONS_ACTIVE", labelKey: "pages.systemMonitor.metrics.dbConnectionsActive", color: "#ec4899"},
    {type: "DB_CONNECTIONS_MAX", labelKey: "pages.systemMonitor.metrics.dbConnectionsMax", color: "#f472b6"},
    {type: "GC_COUNT", labelKey: "pages.systemMonitor.metrics.gcCount", color: "#14b8a6"},
    {type: "GC_TIME", labelKey: "pages.systemMonitor.metrics.gcTime", color: "#2dd4bf"},
];

const DURATION_OPTIONS = [
    {label: "1m", value: "1m"},
    {label: "5m", value: "5m"},
    {label: "15m", value: "15m"},
    {label: "30m", value: "30m"},
    {label: "1h", value: "1h"},
];

export function SystemMonitorPage() {
    const {t} = useTranslation();
    const {token} = useToken();
    const [duration, setDuration] = useState("1m");
    const [dataMap, setDataMap] = useState<Record<string, MetricPoint[]>>({});
    const [syncEnabled, setSyncEnabled] = useState(true);
    const timerRef = useRef<number | null>(null);

    const fetchAll = useCallback(async () => {
        const results = await Promise.all(
            METRICS.map(m => queryMetric(m.type, duration).catch(() => null))
        );
        const map: Record<string, MetricPoint[]> = {};
        results.forEach((res, i) => {
            map[METRICS[i].type] = res?.data?.data ?? [];
        });
        setDataMap(map);
    }, [duration]);

    useEffect(() => {
        fetchAll();
        timerRef.current = window.setInterval(fetchAll, 5000);
        return () => {
            if (timerRef.current) clearInterval(timerRef.current);
        };
    }, [fetchAll]);

    useEffect(() => {
        if (!syncEnabled) {
            echarts.disconnect("monitor-metrics");
            return;
        }
        const timer = setTimeout(() => echarts.connect("monitor-metrics"), 100);
        return () => {
            clearTimeout(timer);
            echarts.disconnect("monitor-metrics");
        };
    }, [syncEnabled]);

    return (
        <>
            <ActionBarComponent
                title={t('pages.systemMonitor.title')}
                subtitle={t('pages.systemMonitor.subtitle')}
            />
            <SystemMetrics />
            <Card
                className="rounded-3xl border-none shadow-sm"
                title={
                    <div className="flex items-center justify-between w-full">
                        <div className="flex items-center gap-2">
                            <LineChartOutlined style={{fontSize: 18, color: token.colorTextHeading}} />
                            <span style={{fontSize: 16, fontWeight: 600, color: token.colorTextHeading}}>
                                {t('pages.systemMonitor.chartTitle')}
                            </span>
                            <Switch size="small" checked={syncEnabled} onChange={setSyncEnabled} />
                            <span style={{fontSize: 12, color: token.colorTextSecondary}}>
                                {t('pages.systemMonitor.syncCrosshair')}
                            </span>
                        </div>
                        <Segmented options={DURATION_OPTIONS} value={duration} onChange={v => setDuration(v as string)} size="small" />
                    </div>
                }
            >
                <Row gutter={[12, 12]}>
                    {METRICS.map(m => (
                        <Col xs={24} md={12} xxl={8} key={m.type}>
                            <div className="p-3 rounded-xl" style={{background: "#fafafa"}}>
                                <div style={{fontSize: 13, fontWeight: 600, marginBottom: 4, color: token.colorTextSecondary}}>
                                    {t(m.labelKey)}
                                </div>
                                <MetricChart config={m} data={dataMap[m.type] ?? []} />
                            </div>
                        </Col>
                    ))}
                </Row>
            </Card>
        </>
    );
}
