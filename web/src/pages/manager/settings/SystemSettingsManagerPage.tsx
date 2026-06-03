import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {useSWRComposition} from "@/compositions/use-swr.ts";
import {
    getSystemSettingsSchema,
    updateSystemMaintenanceMode,
    updateSystemSettings
} from "@/api/system/system-settings.api.ts";
import type {MenuProps} from "antd";
import {Button, Card, Dropdown, Form, message, Modal} from "antd";
import {useEffect, useState} from "react";
import {
    useSettingsGroupToTranslationMap,
    useSettingsKeyToTranslationMap,
    useSettingsTabToTranslationMap
} from "@/i18n/system-settings.tsx";
import {
    ApiOutlined,
    ExclamationCircleFilled,
    ExportOutlined,
    ImportOutlined,
    SaveOutlined,
    ToolOutlined
} from "@ant-design/icons";
import {downloadJson, importJsonFromFile} from "@/utils/file-download.ts";
import {useTranslation} from "react-i18next";
import {useMaintenanceStatus} from "@/compositions/use-maintenance.ts";
import {settingsGroupExtraRenderers, settingsItemRenderers} from "@/pages/manager/settings/settings-renderers.tsx";
import {SettingsRendererContainer} from "@/components/settings/SettingsRendererContainer.tsx";
import {mergeRenderers} from "@/components/settings/merge-renderers.ts";
import {pluginRegistry} from "@/plugin/registry.ts";

export default function SystemSettingsManagerPage() {
    const [refreshing, setRefreshing] = useState(false);
    const {t} = useTranslation();
    const settingsTabToTranslationMap = useSettingsTabToTranslationMap();
    const settingsGroupToTranslationMap = useSettingsGroupToTranslationMap();
    const settingsKeyToTranslationMap = useSettingsKeyToTranslationMap();
    const itemRenderers = mergeRenderers(settingsItemRenderers, pluginRegistry.getSettingsItemRenderers('system'));
    const groupExtraRenderers = mergeRenderers(settingsGroupExtraRenderers, pluginRegistry.getSettingsGroupExtraRenderers('system'));

    const {data, isLoading, mutate} = useSWRComposition(
        'settings-schema',
        () => getSystemSettingsSchema().then((res) => res.data),
        () => void message.error(t('pages.systemSettingsManager.fetchFailed'))
    )

    const {maintenanceMode: isInMaintenance, mutate: mutateMaintenance} = useMaintenanceStatus();

    const [modal, contextHolder] = Modal.useModal();
    const [form] = Form.useForm();

    useEffect(() => {
        if (!data) {
            return;
        }

        const kv = Object.fromEntries(
            Object.entries(data.items)
                .map(([key, schema]) => [key, schema.value])
        );

        form.setFieldsValue(kv)
    }, [data]);

    useEffect(() => {
        setRefreshing(isLoading);
    }, [isLoading]);

    const updateSettings = (props: Record<string, string | null>) => {
        updateSystemSettings(props)
            .then(() => {
                void message.success(t('pages.systemSettingsManager.saveSuccess'))
            })
            .catch(() => {
                void message.error(t('pages.systemSettingsManager.saveFailed'))
            })
            .finally(() => {
                setRefreshing(true)
                mutate()
                    .finally(() => {
                        setRefreshing(false);
                    })
            })
    }

    const importSettings = () => {
        importJsonFromFile<Record<string, string | null>>()
            .then((res) => {
                if (res) {
                    form.setFieldsValue(res)
                } else {
                    void message.warning(t('pages.systemSettingsManager.importEmpty'))
                }
            })
            .catch(() => {
                void message.error(t('pages.systemSettingsManager.importFailed'))
            })
    }

    const exportSettings = () => {
        const content = JSON.stringify(form.getFieldsValue(), undefined, 2);
        downloadJson(content, "settings")
    };

    const switchMaintenanceMode = () => {
        modal.confirm({
            title: isInMaintenance
                ? t('pages.systemSettingsManager.maintenanceConfirmDisableTitle')
                : t('pages.systemSettingsManager.maintenanceConfirmEnableTitle'),
            icon: <ExclamationCircleFilled/>,
            content: isInMaintenance
                ? t('pages.systemSettingsManager.maintenanceConfirmDisableContent')
                : t('pages.systemSettingsManager.maintenanceConfirmEnableContent'),
            okText: t('pages.systemSettingsManager.maintenanceConfirmOk'),
            cancelText: t('pages.systemSettingsManager.maintenanceConfirmCancel'),
            centered: true,
            onOk() {
                return updateSystemMaintenanceMode(!isInMaintenance)
                    .then(() => {
                        void mutateMaintenance();
                    })
                    .catch(() => {
                        void message.error(t('pages.systemSettingsManager.switchMaintenanceModeFailed'));
                    });
            },
        });
    };

    return (
        <>
            {contextHolder}
            <ActionBarComponent
                title={t('pages.systemSettingsManager.title')}
                subtitle={t('pages.systemSettingsManager.subtitle')}
                titleActions={
                    <div className="flex items-center space-x-3">
                        <Dropdown
                            menu={{
                                items: [
                                    {
                                        key: 'import',
                                        label: t('pages.systemSettingsManager.importConfig'),
                                        icon: <ImportOutlined/>,
                                        onClick: importSettings
                                    },
                                    {
                                        key: 'export',
                                        label: t('pages.systemSettingsManager.exportConfig'),
                                        icon: <ExportOutlined/>,
                                        onClick: exportSettings
                                    },
                                    {type: 'divider'},
                                    {
                                        key: 'maintenance',
                                        label: t('pages.systemSettingsManager.maintenanceMode'),
                                        icon: isInMaintenance ? <ApiOutlined/> : <ToolOutlined/>,
                                        onClick: switchMaintenanceMode,
                                        danger: isInMaintenance
                                    }
                                ] as MenuProps['items']
                            }}
                            placement="bottomRight"
                        >
                            <Button
                                icon={<ToolOutlined/>}
                                size="large"
                                className="rounded-xl h-12 px-6"
                            >
                                {t('pages.systemSettingsManager.operation')}
                            </Button>
                        </Dropdown>
                        <Button
                            type="primary"
                            icon={<SaveOutlined/>}
                            size="large"
                            className="rounded-xl h-12 px-8 shadow-lg"
                            onClick={() => {
                                form.submit();
                            }}
                        >
                            {t('pages.systemSettingsManager.saveSettings')}
                        </Button>
                    </div>
                }
            />

            <div className="animate-in slide-in-from-bottom-4 duration-500">
                <Card className="rounded-2xl shadow-sm border-none overflow-hidden mb-6"
                      styles={{body: {paddingTop: 8}}}>
                    <Form form={form} layout="vertical" onFinish={updateSettings}>
                        <SettingsRendererContainer
                            data={data}
                            loading={isLoading || refreshing}
                            tabTranslationMap={settingsTabToTranslationMap}
                            groupTranslationMap={settingsGroupToTranslationMap}
                            keyTranslationMap={settingsKeyToTranslationMap}
                            enumTranslator={(key, value) =>
                                t(`pages.systemSettingsManager.enums.${key}.${value}`)
                            }
                            itemRenderers={itemRenderers}
                            groupExtraRenderers={groupExtraRenderers}
                        />
                    </Form>
                </Card>
            </div>
        </>
    )
}
