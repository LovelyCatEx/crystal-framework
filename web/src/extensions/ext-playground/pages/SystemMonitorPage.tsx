import {useCallback, useEffect, useRef, useState} from "react";
import {useTranslation} from "react-i18next";
import {Card, Col, Row, Segmented, Select, Switch, theme} from "antd";
import {LineChartOutlined} from "@ant-design/icons";
import * as echarts from "echarts";
import {SystemMetrics} from "@/components/dashboard/SystemMetrics.tsx";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {MetricChart, type MetricChartConfig} from "../components/MetricChart.tsx";
import {queryMetric} from "../api/system-monitor.api.ts";
import type {MetricPoint} from "../types/system-monitor.types.ts";

const {useToken} = theme;

function formatBytes(v: number): string {
    if (v === 0) return "0";
    const k = 1024;
    const sizes = ["", "K", "M", "G", "T"];
    const i = Math.min(Math.floor(Math.log(Math.abs(v)) / Math.log(k)), sizes.length - 1);
    return (v / Math.pow(k, i)).toFixed(i > 0 ? 1 : 0) + " " + sizes[i] + "B";
}

const METRICS: MetricChartConfig[] = [
    {type: "CPU_USAGE", labelKey: "pages.systemMonitor.metrics.cpuUsage", color: "#3b82f6", yMin: 0, yMax: 100},
    {type: "CPU_LOAD_AVERAGE", labelKey: "pages.systemMonitor.metrics.cpuLoadAverage", color: "#8b5cf6"},
    {type: "MEMORY_USED", labelKey: "pages.systemMonitor.metrics.memoryUsed", color: "#34d399", formatValue: formatBytes},
    {type: "JVM_HEAP_USED", labelKey: "pages.systemMonitor.metrics.jvmHeapUsed", color: "#f97316", formatValue: formatBytes},
    {type: "JVM_NONHEAP_COMMITTED", labelKey: "pages.systemMonitor.metrics.jvmNonHeapCommitted", color: "#ef4444", formatValue: formatBytes},
    {type: "JVM_NONHEAP_USED", labelKey: "pages.systemMonitor.metrics.jvmNonHeapUsed", color: "#dc2626", formatValue: formatBytes},
    {type: "GC_COUNT", labelKey: "pages.systemMonitor.metrics.gcCount", color: "#14b8a6"},
    {type: "GC_TIME", labelKey: "pages.systemMonitor.metrics.gcTime", color: "#2dd4bf", formatValue: v => v.toFixed(0) + " ms"},
    {type: "DISK_USED", labelKey: "pages.systemMonitor.metrics.diskUsed", color: "#818cf8", formatValue: formatBytes},
    {type: "DB_CONNECTIONS_ACTIVE", labelKey: "pages.systemMonitor.metrics.dbConnectionsActive", color: "#ec4899"},
];

const DURATION_OPTIONS: {labelKey: string; value: string}[] = [
    {labelKey: "pages.systemMonitor.durations.m1", value: "1m"},
    {labelKey: "pages.systemMonitor.durations.m5", value: "5m"},
    {labelKey: "pages.systemMonitor.durations.m15", value: "15m"},
    {labelKey: "pages.systemMonitor.durations.m30", value: "30m"},
    {labelKey: "pages.systemMonitor.durations.h1", value: "1h"},
    {labelKey: "pages.systemMonitor.durations.h3", value: "3h"},
    {labelKey: "pages.systemMonitor.durations.h5", value: "5h"},
    {labelKey: "pages.systemMonitor.durations.h12", value: "12h"},
    {labelKey: "pages.systemMonitor.durations.d1", value: "1d"},
    {labelKey: "pages.systemMonitor.durations.d3", value: "3d"},
    {labelKey: "pages.systemMonitor.durations.d5", value: "5d"},
    {labelKey: "pages.systemMonitor.durations.d7", value: "7d"},
    {labelKey: "pages.systemMonitor.durations.d14", value: "14d"},
];

export function SystemMonitorPage() {
    const {t} = useTranslation();
    const {token} = useToken();
    const [duration, setDuration] = useState("1m");
    const [dataMap, setDataMap] = useState<Record<string, MetricPoint[]>>({});
    const [syncEnabled, setSyncEnabled] = useState(true);
    const [columns, setColumns] = useState<string>("auto");
    const timerRef = useRef<number | null>(null);

    const colSpan = columns === "1" ? 24 : columns === "2" ? 12 : columns === "3" ? 8 : undefined;

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
                        <div className="flex items-center gap-2">
                            <Segmented
                                options={[
                                    {value: "auto", label: t('pages.systemMonitor.columns.auto')},
                                    {value: "1", label: "1"},
                                    {value: "2", label: "2"},
                                    {value: "3", label: "3"},
                                ]}
                                value={columns}
                                onChange={v => setColumns(v as string)}
                                size="small"
                            />
                            <Select
                            options={DURATION_OPTIONS.map(o => ({label: t(o.labelKey), value: o.value}))}
                            value={duration}
                            onChange={setDuration}
                            size="small"
                            style={{width: 100}}
                        />
                    </div>
                }
            >
                <Row gutter={[12, 12]}>
                    {METRICS.map(m => (
                        <Col {...(colSpan ? {xs: 24, sm: colSpan, md: colSpan, lg: colSpan, xxl: colSpan} : {xs: 24, sm: 24, lg: 12, xxl: 8})} key={m.type}>
                            <div className="p-3 rounded-xl" style={{background: "#fafafa"}}>
                                <div style={{fontSize: 13, fontWeight: 600, marginBottom: 4, color: token.colorTextSecondary}}>
                                    {t(m.labelKey)}
                                </div>
                                <MetricChart config={m} data={dataMap[m.type] ?? []} duration={duration} />
                            </div>
                        </Col>
                    ))}
                </Row>
            </Card>
        </>
    );
}
