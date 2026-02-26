import {ActionBarComponent} from "../../../components/ActionBarComponent.tsx";
import {useSWRComposition} from "../../../compositions/swr.ts";
import {getSystemSettingsSchema, updateSystemSettings} from "../../../api/system-settings.ts";
import {Button, Card, Col, Form, Input, InputNumber, message, Row, Switch} from "antd";
import {SystemSettingsItemValueType, type SystemSettingsSchema} from "../../../types/system-settings.ts";
import {useEffect, useState} from "react";
import {settingsGroupToTranslationMap, settingsKeyToTranslationMap} from "../../../i18n/system-settings.tsx";
import {ExportOutlined, ImportOutlined, SaveOutlined, ToolOutlined} from "@ant-design/icons";
import {downloadJson, importJsonFromFile} from "../../../utils/file-download.ts";

export function SystemSettingsManagerPage() {
    const [refreshing, setRefreshing] = useState(false);

    const { data, isLoading, mutate } = useSWRComposition(
        'settings-schema',
        () => getSystemSettingsSchema().then((res) => res.data),
        () => void message.error("无法获取系统设置")
    )

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
                void message.success("系统设置已保存")
            })
            .catch(() => {
                void message.error("系统设置保存失败")
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
                    void message.warning("导入的配置为空")
                }
            })
            .catch(() => {
                void message.error("导入配置失败")
            })
    }

    const exportSettings = () => {
        const content = JSON.stringify(form.getFieldsValue(), undefined, 2);
        downloadJson(content, "settings")
    };

    return (
        <>
            <ActionBarComponent
                title="系统设置"
                subtitle="编辑所有系统设置"
                titleActions={
                    <Button
                        type="primary"
                        icon={<SaveOutlined />}
                        size="large"
                        className="rounded-xl h-12 shadow-lg"
                        onClick={() => {
                            form.submit();
                        }}
                    >
                        保存设置
                    </Button>
                }
            />

            <Card className="mb-4">
                <div className="text-lg font-bold mb-4 flex items-center space-x-2">
                    <ToolOutlined />
                    <span>操作</span>
                </div>

                <div className="flex items-center space-x-2">
                    <Button
                        icon={<ImportOutlined />}
                        onClick={importSettings}
                    >
                        导入配置
                    </Button>
                    <Button
                        icon={<ExportOutlined />}
                        onClick={exportSettings}
                    >
                        导出配置
                    </Button>
                </div>
            </Card>

            <Form form={form} layout="vertical" onFinish={updateSettings}>
                {data && (
                    data.groups.map((group) => {
                        const translatedGroup = settingsGroupToTranslationMap.get(group)
                        return (
                            <Card loading={isLoading || refreshing} key={group} className="mb-4">
                                <div className="text-lg font-bold mb-4 flex items-center">
                                    {translatedGroup?.icon ? (
                                        <>
                                            {translatedGroup.icon}
                                            <span className="mr-2"></span>
                                        </>
                                    ) : null}

                                    {translatedGroup?.label ?? group}
                                </div>

                                {(() => {
                                    const items = Object.entries(data.items)
                                        .filter(([_, value]) => value.group === group)
                                        .sort(([, a], [, b]) => a.sort - b.sort);

                                    const chunks = [];
                                    for (let i = 0; i < items.length; i += 3) {
                                        chunks.push(items.slice(i, i + 3));
                                    }

                                    console.log(chunks)

                                    return chunks.map((chunk, chunkIndex) => (
                                        <Row gutter={[16, 16]} key={chunkIndex}>
                                            {chunk.map(([key, value]) => (
                                                <Col
                                                    xs={24}
                                                    sm={12}
                                                    md={8}
                                                    lg={8}
                                                    xl={8}
                                                    key={key}
                                                >
                                                    <SettingsItem settingsKey={key} schema={value} />
                                                </Col>
                                            ))}
                                        </Row>
                                    ));
                                })()}
                            </Card>
                        )
                    })
                )}
            </Form>
        </>
    )
}

function SettingsItem(props: { settingsKey: string, schema: SystemSettingsSchema }) {
    const [formItemProps, setFormItemProps] = useState<Record<string, unknown>>();

    return (
        <Form.Item
            key={props.settingsKey}
            name={props.settingsKey}
            label={settingsKeyToTranslationMap.get(props.settingsKey) ?? props.settingsKey}
            {...formItemProps}
        >
            {props.schema.valueType == SystemSettingsItemValueType.STRING ? (
                <>
                    <Input className="w-full rounded-lg h-8 flex items-center" placeholder={props.schema.defaultValue ?? ''} />
                </>
            ) : props.schema.valueType == SystemSettingsItemValueType.NUMBER ? (
                <>
                    <InputNumber
                        className="w-full rounded-lg h-8 flex items-center"
                        defaultValue={Number.parseFloat(props.schema.defaultValue ?? '0')}
                    />
                </>
            ) : props.schema.valueType == SystemSettingsItemValueType.DECIMAL ? (
                <>
                    <InputNumber
                        className="w-full rounded-lg h-8 flex items-center"
                        defaultValue={Number.parseFloat(props.schema.defaultValue ?? '0')}
                    />
                </>
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

                    <Switch />
                </>
            ) : (
                <>
                    <span>NO RENDER FOUND FOR THIS SETTINGS ITEM</span>
                </>
            )}
        </Form.Item>
    )
}