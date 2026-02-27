import {Card, Flex, Statistic, Tag} from "antd";
import {type ReactNode, useEffect, useMemo, useState} from "react";
import useSWR from "swr";
import {getMetric} from "../api/actuator.api.ts";
import {actuatorMetricsToTranslationMap} from "../i18n/actuator-metrics.ts";

export function ActuatorMetricRenderComponent(props: {
    metricName: string,
    refreshInterval?: number,
    measuredValueRender?: (value: string | number, basicUnit: string | undefined, name: string) => ReactNode
}) {
    const [refreshing, setRefreshing] = useState<boolean>(false);

    const { data: basicMetricResult } = useSWR(
        props.metricName ? `/getMetric/${props.metricName}` : null,
        () => getMetric(
            props.metricName
        ).then((res) => res.data)
    )

    const [selectableTags, setSelectableTags] = useState<{ tag: string, options: string[], selectedIndex: number }[]>([]);
    useEffect(() => {
        setSelectableTags((basicMetricResult?.availableTags ?? []).map((tag) => {
            return {
                tag: tag.tag,
                options: tag.values,
                selectedIndex: -1
            }
        }));
    }, [basicMetricResult]);

    const { data: metricResult, mutate: refreshData } = useSWR(
        props.metricName ? `/getMetric/${props.metricName}/${selectableTags.map((tag) => `${tag.tag}:${tag.selectedIndex}`).join(',')}` : null,
        () => getMetric(
            props.metricName,
            selectableTags
                .filter((tag) => tag.selectedIndex >= 0 && tag.selectedIndex < tag.options.length)
                .map((tag) => {
                    return {
                        tagName: tag.tag,
                        optionName: tag.options[tag.selectedIndex],
                    }
                })
        ).then((res) => res.data),
        {
            refreshInterval: props.refreshInterval ? props.refreshInterval : undefined,
        }
    )

    const handleSelectedTagChange = (tagGroupIndex: number, optionIndex: number, checked: boolean) => {
        const t = [...selectableTags];
        if (checked) {
            t[tagGroupIndex].selectedIndex = optionIndex;
        } else {
            t[tagGroupIndex].selectedIndex = -1;
        }

        setSelectableTags(t);

        setRefreshing(true);
        refreshData().finally(() => {
            setRefreshing(false);
        })
    }

    const displayMetricName = useMemo(() => {
        return basicMetricResult?.name
            ? actuatorMetricsToTranslationMap.get(basicMetricResult.name)
            ?? basicMetricResult?.name
            : 'unknown'
    }, [basicMetricResult])

    return (
        <Card className="mb-4" loading={refreshing}>
            <p className="text-lg font-bold">{displayMetricName}</p>
            <p className="text-gray-600">{basicMetricResult?.description}</p>

            {(basicMetricResult?.availableTags ?? []).length > 0 && (
                <p className="mt-4 mb-2 text-lg">标签</p>
            )}
            {selectableTags.map((tag, index) => (
                <div className="mb-4">
                    <p className="mb-2"><Tag color="blue">{tag.tag}</Tag></p>
                    <Flex gap="small" wrap align="center">
                        可选值：{tag.options.map((tagName, optionIndex) => (
                        <Tag.CheckableTag
                            checked={tag.selectedIndex == optionIndex}
                            onChange={(checked) => {
                                handleSelectedTagChange(index, optionIndex, checked);
                            }}
                        >
                            {tagName}
                        </Tag.CheckableTag>
                    ))}
                    </Flex>
                </div>
            ))}

            {(metricResult?.measurements ?? []).length > 0 && (
                <p className="mt-4 mb-2 text-lg">测量值</p>
            )}
            {(metricResult?.measurements ?? [])
                .map((measurement) =>
                    props.measuredValueRender?.(measurement.value, metricResult?.baseUnit, measurement.statistic)
                    ?? (
                        <Statistic
                            title={measurement.statistic}
                            value={measurement.value}
                            suffix={metricResult?.baseUnit}
                        />
                    )
                )
            }
        </Card>
    )
}