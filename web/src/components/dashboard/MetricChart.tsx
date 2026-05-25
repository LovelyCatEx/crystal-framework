import {useEffect, useRef} from "react";
import * as echarts from "echarts";
import type {MetricPoint} from "@/types/monitor/system-monitor.types.ts";

function timeFormat(duration: string): string {
    if (duration === "1m") return "{mm}:{ss}";
    if (duration.endsWith("d")) {
        const d = parseInt(duration);
        return d >= 7 ? "{MM}-{dd}" : "{MM}-{dd} {HH}:{mm}";
    }
    if (duration.endsWith("h")) {
        const h = parseInt(duration);
        return h >= 12 ? "{MM}-{dd} {HH}:{mm}" : "{HH}:{mm}";
    }
    return "{HH}:{mm}";
}

export interface MetricChartConfig {
    type: string;
    labelKey: string;
    color: string;
    yMin?: number;
    yMax?: number;
    formatValue?: (v: number) => string;
}

export function MetricChart({config, data, duration}: { config: MetricChartConfig; data: MetricPoint[]; duration: string }) {
    const domRef = useRef<HTMLDivElement>(null);
    const chartRef = useRef<echarts.ECharts | null>(null);

    useEffect(() => {
        if (!domRef.current) return;
        chartRef.current = echarts.init(domRef.current);
        chartRef.current.group = "monitor-metrics";
        chartRef.current.setOption({
            animation: false,
            grid: {left: 10, right: 10, top: 10, bottom: 30},
            tooltip: {trigger: "axis", axisPointer: {type: "cross"}, valueFormatter: config.formatValue},
            xAxis: {type: "time", axisLabel: {formatter: timeFormat(duration), fontSize: 10}},
            yAxis: {type: "value", min: config.yMin, max: config.yMax, splitLine: {lineStyle: {type: "dashed"}}, axisLabel: {formatter: config.formatValue}},
            dataset: {source: []},
            series: [{type: "line", smooth: true, symbol: "none", lineStyle: {width: 2, color: config.color}, areaStyle: {color: config.color + "20"}}],
        });

        const observer = new ResizeObserver(() => chartRef.current?.resize());
        observer.observe(domRef.current);

        return () => {
            observer.disconnect();
            chartRef.current?.dispose();
            chartRef.current = null;
        };
    }, []);

    useEffect(() => {
        if (!chartRef.current) return;
        chartRef.current.setOption({xAxis: {axisLabel: {formatter: timeFormat(duration)}}});
    }, [duration]);

    useEffect(() => {
        if (!chartRef.current || data.length === 0) return;
        const source = data
            .map(p => [Number(p.timestamp), Number(p.value)] as [number, number])
            .filter(([, v]) => !isNaN(v))
            .sort((a, b) => a[0] - b[0]);
        chartRef.current.setOption({dataset: {source}});
    }, [data]);

    return <div ref={domRef} style={{width: "100%", height: 180}} />;
}
