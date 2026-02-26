import type {ActuatorMetricResult} from "../types/actuator.types.ts";
import {Card, Statistic, Tag} from "antd";

export function ActuatorMetricRenderComponent(props: { data: ActuatorMetricResult }) {

    return (
        <Card className="mb-4">
            <p className="text-lg font-bold">{props.data.name}</p>
            <p className="text-gray-600">{props.data.description}</p>

            {props.data.availableTags.length > 0 && (
                <p className="mt-4 mb-2 text-lg">标签</p>
            )}
            {props.data.availableTags.map((tag) => (
                <div className="mb-4">
                    <p className="mb-2"><Tag color="blue">{tag.tag}</Tag></p>
                    <div className="flex flex-row flex-wrap gap-2 items-center box-border min-w-0 min-h-0">
                        可选值：{tag.values.map((tag) => (
                            <Tag>{tag}</Tag>
                        ))}
                    </div>
                </div>
            ))}

            {props.data.measurements.length > 0 && (
                <p className="mt-4 mb-2 text-lg">测量值</p>
            )}
            {props.data.measurements.map((measurement) => (
                <div>
                    <Statistic title={measurement.statistic} value={measurement.value} suffix={props.data.baseUnit} />
                </div>
            ))}
        </Card>
    )
}