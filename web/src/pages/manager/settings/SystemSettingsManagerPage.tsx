import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {useSWRComposition} from "@/compositions/use-swr.ts";
import {getSystemSettingsSchema, updateSystemMaintenanceMode, updateSystemSettings} from "@/api/system/system-settings.api.ts";
import type {MenuProps} from "antd";
import {
    Button,
    Card,
    Checkbox,
    Dropdown,
    Form,
    Input,
    InputNumber,
    message,
    Modal,
    Radio,
    Switch,
    Tabs,
    theme
} from "antd";
import type {GetSystemSettingsSchemaData, SystemSettingsSchema} from "@/types/system/system-settings.types.ts";
import {SystemSettingsItemValueType} from "@/types/system/system-settings.types.ts";
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

const { useToken } = theme;

function SettingsGroup(props: {
    group: string;
    items: [string, SystemSettingsSchema][];
    loading: boolean;
    isFirst: boolean;
}) {
    const { token } = useToken();
    const settingsGroupToTranslationMap = useSettingsGroupToTranslationMap();
    const translatedGroup = settingsGroupToTranslationMap.get(props.group);

    return (
        <div className={props.isFirst ? "" : "mt-8"}>
            <div className="flex items-center mb-6 pb-3" style={{ borderBottom: `1px solid ${token.colorBorder}` }}>
                {translatedGroup?.icon && (
                    <span className="text-xl mr-3" style={{ color: token.colorPrimary }}>
                        {translatedGroup.icon}
                    </span>
                )}
                <h3 className="text-lg font-semibold" style={{ color: token.colorTextHeading }}>
                    {translatedGroup?.label ?? props.group}
                </h3>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {props.items.map(([key, value]) => (
                    <div key={key} className="bg-slate-50/50 dark:bg-slate-200/5 rounded-xl p-4 hover:bg-slate-50 dark:hover:bg-slate-200/10 transition-colors">
                        <SettingsItem 
                            settingsKey={key} 
                            schema={value}
                            loading={props.loading}
                        />
                    </div>
                ))}
            </div>
        </div>
    );
}

function buildTabItems(
    data: GetSystemSettingsSchemaData,
    settingsTabToTranslationMap: Map<string, string>,
    isLoading: boolean,
    refreshing: boolean
) {
    const tabs = Array.from(
        new Set(
            Object.values(data.items)
                .map(item => item.tab)
                .filter(tab => tab !== null)
        )
    ).sort();

    return tabs.map(tab => {
        const groupsInTab = data.groups.filter(group => 
            Object.values(data.items).some(item => 
                item.tab === tab && item.group === group
            )
        );

        const groupElements = groupsInTab.map((group, index) => {
            const items = Object.entries(data.items)
                .filter(([_, value]) => value.group === group)
                .sort(([, a], [, b]) => a.sort - b.sort);

            return (
                <SettingsGroup
                    key={group}
                    group={group}
                    items={items}
                    loading={isLoading || refreshing}
                    isFirst={index === 0}
                />
            );
        });

        return {
            key: tab,
            label: settingsTabToTranslationMap.get(tab) ?? tab,
            children: <div className="py-4">{groupElements}</div>
        };
    });
}

export function SystemSettingsManagerPage() {
    const [refreshing, setRefreshing] = useState(false);
    const {t} = useTranslation();
    const settingsTabToTranslationMap = useSettingsTabToTranslationMap();

    const { data, isLoading, mutate } = useSWRComposition(
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
            icon: <ExclamationCircleFilled />,
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
                                        icon: <ImportOutlined />,
                                        onClick: importSettings
                                    },
                                    {
                                        key: 'export',
                                        label: t('pages.systemSettingsManager.exportConfig'),
                                        icon: <ExportOutlined />,
                                        onClick: exportSettings
                                    },
                                    {
                                        type: 'divider'
                                    },
                                    {
                                        key: 'maintenance',
                                        label: t('pages.systemSettingsManager.maintenanceMode'),
                                        icon: isInMaintenance ? <ApiOutlined /> : <ToolOutlined />,
                                        onClick: switchMaintenanceMode,
                                        danger: isInMaintenance
                                    }
                                ] as MenuProps['items']
                            }}
                            placement="bottomRight"
                        >
                            <Button
                                icon={<ToolOutlined />}
                                size="large"
                                className="rounded-xl h-12 px-6"
                            >
                                {t('pages.systemSettingsManager.operation')}
                            </Button>
                        </Dropdown>
                        <Button
                            type="primary"
                            icon={<SaveOutlined />}
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
                <Card className="rounded-2xl shadow-sm border-none overflow-hidden mb-6" styles={{ body: { paddingTop: 8 } }}>
                    <Form form={form} layout="vertical" onFinish={updateSettings}>
                        {data && (
                            <Tabs 
                                items={buildTabItems(data, settingsTabToTranslationMap, isLoading, refreshing)} 
                                className="settings-tabs" 
                            />
                        )}
                    </Form>
                </Card>
            </div>
        </>
    )
}

function SettingsItem(props: { settingsKey: string, schema: SystemSettingsSchema, loading?: boolean }) {
    const { token } = useToken();
    const { t } = useTranslation();
    const [formItemProps, setFormItemProps] = useState<Record<string, unknown>>();
    const settingsKeyToTranslationMap = useSettingsKeyToTranslationMap();

    return (
        <Form.Item
            key={props.settingsKey}
            name={props.settingsKey}
            label={
                <span className="text-sm font-medium" style={{ color: token.colorTextSecondary }}>
                    {settingsKeyToTranslationMap.get(props.settingsKey) ?? props.settingsKey}
                </span>
            }
            className="mb-0"
            {...formItemProps}
        >
            {props.schema.valueType == SystemSettingsItemValueType.STRING ? (
                <Input 
                    className="rounded-lg h-10" 
                    placeholder={props.schema.defaultValue ?? ''} 
                    disabled={props.loading}
                />
            ) : props.schema.valueType == SystemSettingsItemValueType.NUMBER ? (
                <InputNumber
                    className="w-full rounded-lg h-10"
                    defaultValue={Number.parseFloat(props.schema.defaultValue ?? '0')}
                    disabled={props.loading}
                />
            ) : props.schema.valueType == SystemSettingsItemValueType.DECIMAL ? (
                <InputNumber
                    className="w-full rounded-lg h-10"
                    defaultValue={Number.parseFloat(props.schema.defaultValue ?? '0')}
                    disabled={props.loading}
                />
            ) : props.schema.valueType == SystemSettingsItemValueType.BOOLEAN ? (
                <>
                    {(() => {
                        setFormItemProps({
                            getValueProps: (value: string | boolean) => ({
                                checked: value === 'true' || value === true
                            })
                        })
                        return <></>
                    })()}
                    <Switch disabled={props.loading} />
                </>
            ) : props.schema.valueType == SystemSettingsItemValueType.ENUM_SINGLE ? (
                <Radio.Group
                    className="flex flex-col space-y-2"
                    disabled={props.loading}
                    options={(props.schema.enumValues ?? []).map((item) => {
                        return {
                            value: item,
                            label: t(`pages.systemSettingsManager.enums.${props.settingsKey}.${item}`),
                        }
                    })}
                />
            ) : props.schema.valueType == SystemSettingsItemValueType.ENUM_MULTIPLE ? (
                <Checkbox.Group
                    className="flex flex-col space-y-2"
                    disabled={props.loading}
                    options={(props.schema.enumValues ?? []).map((item) => {
                        return {
                            value: item,
                            label: t(`pages.systemSettingsManager.enums.${props.settingsKey}.${item}`),
                        }
                    })}
                />
            ) : (
                <span className="text-red-500 text-sm">NO RENDER FOUND FOR THIS SETTINGS ITEM</span>
            )}
        </Form.Item>
    )
}