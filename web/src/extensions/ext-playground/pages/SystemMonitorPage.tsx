import {useEffect, useRef} from "react";
import {useTranslation} from "react-i18next";
import * as echarts from "echarts";
import {SystemMetrics} from "@/components/dashboard/SystemMetrics.tsx";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {StandardCard} from "@/components/StandardCard.tsx";
import {queryMetric} from "../api/system-monitor.api.ts";

export function SystemMonitorPage() {
    const {t} = useTranslation();
    const chartRef = useRef<HTMLDivElement>(null);
    const chartInstance = useRef<echarts.ECharts | null>(null);
    const timerRef = useRef<number | null>(null);

    useEffect(() => {
        if (!chartRef.current) return;

        chartInstance.current = echarts.init(chartRef.current);

        chartInstance.current.setOption({
            title: {text: "CPU Usage (Last 1 Minute)"},
            tooltip: {trigger: "axis"},
            xAxis: {
                type: "time",
                axisLabel: {formatter: "{HH}:{mm}:{ss}"},
            },
            yAxis: {
                type: "value",
                min: 0,
                max: 100,
                axisLabel: {formatter: "{value} %"},
            },
            series: [{
                type: "line",
                smooth: true,
                data: [],
                symbol: "none",
                lineStyle: {width: 2, color: "#3b82f6"},
                areaStyle: {color: "rgba(59, 130, 246, 0.1)"},
            }],
        });

        const fetchData = async () => {
            const res = await queryMetric("CPU_USAGE", "1m");
            const points = res.data?.data ?? [];
            chartInstance.current?.setOption({
                series: [{
                    data: points.map(p => [p.timestamp, p.value]),
                }],
            });
        };

        fetchData();
        timerRef.current = window.setInterval(fetchData, 3000);

        const handleResize = () => chartInstance.current?.resize();
        window.addEventListener("resize", handleResize);

        return () => {
            if (timerRef.current) clearInterval(timerRef.current);
            window.removeEventListener("resize", handleResize);
            chartInstance.current?.dispose();
            chartInstance.current = null;
        };
    }, []);

    return (
        <>
            <ActionBarComponent
                title={t('pages.systemMonitor.title')}
                subtitle={t('pages.systemMonitor.subtitle')}
            />
            <SystemMetrics />
            <div style={{padding: "0 0 24px"}}>
                <StandardCard>
                    <div ref={chartRef} style={{width: "100%", height: 350}} />
                </StandardCard>
            </div>
        </>
    );
}
