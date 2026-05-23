import {useEffect, useRef} from "react";
import * as echarts from "echarts";
import type {MetricPoint} from "../types/system-monitor.types.ts";

export interface MetricChartConfig {
    type: string;
    labelKey: string;
    color: string;
    yMin?: number;
    yMax?: number;
}

export function MetricChart({config, data}: { config: MetricChartConfig; data: MetricPoint[] }) {
    const domRef = useRef<HTMLDivElement>(null);
    const chartRef = useRef<echarts.ECharts | null>(null);

    useEffect(() => {
        if (!domRef.current) return;
        chartRef.current = echarts.init(domRef.current);
        chartRef.current.group = "monitor-metrics";
        chartRef.current.setOption({
            animation: false,
            grid: {left: 10, right: 10, top: 10, bottom: 30},
            tooltip: {trigger: "axis", axisPointer: {type: "cross"}},
            xAxis: {type: "time", axisLabel: {formatter: "{HH}:{mm}", fontSize: 10}},
            yAxis: {type: "value", min: config.yMin, max: config.yMax, splitLine: {lineStyle: {type: "dashed"}}},
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
        if (!chartRef.current || data.length === 0) return;
        const source = data
            .map(p => [Number(p.timestamp), Number(p.value)] as [number, number])
            .filter(([, v]) => !isNaN(v))
            .sort((a, b) => a[0] - b[0]);
        chartRef.current.setOption({dataset: {source}});
    }, [data]);

    return <div ref={domRef} style={{width: "100%", height: 160}} />;
}
