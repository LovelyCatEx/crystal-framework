import {ActionBarComponent} from "../../../components/ActionBarComponent.tsx";
import {getAvailableMetricsList, getMetric} from "../../../api/actuator.api.ts";
import useSWR from "swr";
import {useEffect, useState} from "react";
import {ActuatorMetricRenderComponent} from "../../../components/ActuatorMetricRenderComponent.tsx";
import type {ActuatorMetricResult} from "../../../types/actuator.types.ts";

export function DashboardPage() {
    const { data: metrics } = useSWR(
        'getAvailableMetricsList',
        () => getAvailableMetricsList().then((res) => res.data.names),
    )

    const [d, setD] = useState<ActuatorMetricResult[]>([])

    useEffect(() => {
        if (!metrics) {
            return;
        }

        for (let i = 0; i < metrics.length; i++) {
            const metric = metrics[i];
            getMetric(metric)
                .then((res) => {
                    setD((prev) => [...prev, res.data]);
                });
        }
    }, [metrics]);

    return (
        <>
            <ActionBarComponent title="仪表盘" />
            <div>{d.map((e) => (
                <ActuatorMetricRenderComponent data={e} />
            ))}</div>
        </>
    )
}