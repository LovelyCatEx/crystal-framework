import {ActionBarComponent} from "../../../components/ActionBarComponent.tsx";
import {getAvailableMetricsList} from "../../../api/actuator.api.ts";
import useSWR from "swr";
import {ActuatorMetricRenderComponent} from "../../../components/ActuatorMetricRenderComponent.tsx";
import {sortByMapOrder} from "../../../utils/map.ts";
import {actuatorMetricsToTranslationMap} from "../../../i18n/actuator-metrics.ts";
import {Statistic} from "antd";
import {formatTimestamp} from "../../../utils/datetime.utils.ts";

export function DashboardPage() {
    const { data: metrics } = useSWR(
        'getAvailableMetricsList',
        () => getAvailableMetricsList().then((res) => res.data.names),
    )

    return (
        <>
            <ActionBarComponent title="仪表盘" subtitle="在此处查看系统基本信息" />

            {/* Actuator Metrics */}
            <div className="flex flex-col space-y-2">
                <p className="text-lg font-bold">Actuator 监控</p>

                <div className="columns-1 sm:columns-2 lg:columns-3 gap-4">
                    {sortByMapOrder((metrics ?? []), actuatorMetricsToTranslationMap).map((metricName) => (
                        <div key={metricName} className="break-inside-avoid mb-4">
                            <ActuatorMetricRenderComponent
                                metricName={metricName}
                                refreshInterval={5}
                                measuredValueRender={(value, basicUnit, name) => {
                                    let renderedName = name;
                                    let suffix = basicUnit;
                                    let renderedValue = value;

                                    const lowerCaseBasicUnit = basicUnit?.toString().toLowerCase()

                                    if (lowerCaseBasicUnit === 'bytes') {
                                        suffix = 'MB'
                                        renderedValue = Math.round(Number.parseInt(value.toString()) / 1024 / 1024)
                                    } else if (lowerCaseBasicUnit === 'seconds') {
                                        suffix = '秒'
                                    } else if (lowerCaseBasicUnit === 'threads') {
                                        suffix = '线程'
                                    } if (lowerCaseBasicUnit === 'connections') {
                                        renderedName = '连接数'
                                        suffix = ''
                                    } else if (lowerCaseBasicUnit === 'files') {
                                        renderedName = '文件数'
                                        suffix = ''
                                    }

                                    if (name === 'COUNT') {
                                        suffix = ''
                                    }

                                    if (metricName === 'process.start.time') {
                                        renderedName = '时间'
                                        renderedValue = formatTimestamp(Number.parseInt(value.toString()) * 1000)
                                        suffix = ''
                                    }

                                    return <Statistic
                                        title={renderedName}
                                        value={renderedValue}
                                        suffix={suffix}
                                    />
                                }}
                            />
                        </div>
                    ))}
                </div>
            </div>
        </>
    )
}