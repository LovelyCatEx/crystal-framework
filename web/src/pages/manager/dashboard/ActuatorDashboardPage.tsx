import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {getAvailableMetricsList} from "@/api/actuator.api.ts";
import useSWR from "swr";
import {ActuatorMetricRenderComponent} from "@/components/ActuatorMetricRenderComponent.tsx";
import {sortByArrayOrder} from "@/utils/map.ts";
import {actuatorMetricsOrder} from "@/i18n/enum-orders.ts";
import {Statistic} from "antd";
import {formatTimestamp} from "@/utils/datetime.utils.ts";
import {useTranslation} from "react-i18next";

export function ActuatorDashboardPage() {
    const { t } = useTranslation();
    const { data: metrics } = useSWR(
        'getAvailableMetricsList',
        () => getAvailableMetricsList().then((res) => res.data.names),
    )

    return (
        <>
            <ActionBarComponent title={t('pages.actuatorDashboard.title')} subtitle={t('pages.actuatorDashboard.subtitle')} />

            {/* Actuator Metrics */}
            <div className="flex flex-col space-y-2">
                <p className="text-lg font-bold">Actuator {t('components.dashboard.systemMetrics.monitoring')}</p>

                <div className="columns-1 sm:columns-2 lg:columns-3 gap-4">
                    {sortByArrayOrder((metrics ?? []), actuatorMetricsOrder).map((metricName) => (
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
                                        suffix = t('components.dashboard.systemMetrics.units.seconds')
                                    } else if (lowerCaseBasicUnit === 'threads') {
                                        suffix = t('components.dashboard.systemMetrics.units.threads')
                                    } if (lowerCaseBasicUnit === 'connections') {
                                        renderedName = t('components.dashboard.systemMetrics.metrics.connections')
                                        suffix = ''
                                    } else if (lowerCaseBasicUnit === 'files') {
                                        renderedName = t('components.dashboard.systemMetrics.metrics.files')
                                        suffix = ''
                                    }

                                    if (name === 'COUNT') {
                                        suffix = ''
                                    }

                                    if (metricName === 'process.start.time') {
                                        renderedName = t('components.dashboard.systemMetrics.metrics.time')
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
