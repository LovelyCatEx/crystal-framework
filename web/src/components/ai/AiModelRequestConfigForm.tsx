import {useEffect, useState} from "react";
import {Button, Col, Divider, Form, Input, InputNumber, Row, Space} from "antd";
import {DeleteOutlined, PlusOutlined} from "@ant-design/icons";
import {useTranslation} from "react-i18next";

interface AiModelRequestConfig {
    temperature?: number | null;
    maxOutputTokens?: number | null;
    additionalBody?: Record<string, any>;
}

interface AiModelRequestConfigFormProps {
    value?: string;
    onChange?: (value: string) => void;
}

export function AiModelRequestConfigForm({ value, onChange }: AiModelRequestConfigFormProps) {
    const { t } = useTranslation();
    const [config, setConfig] = useState<AiModelRequestConfig>({
        temperature: null,
        maxOutputTokens: null,
        additionalBody: {}
    });
    const [additionalBodyEntries, setAdditionalBodyEntries] = useState<Array<{ key: string; value: string }>>([]);

    useEffect(() => {
        if (value) {
            try {
                const parsed = JSON.parse(value) as AiModelRequestConfig;
                setConfig(parsed);
                if (parsed.additionalBody) {
                    setAdditionalBodyEntries(
                        Object.entries(parsed.additionalBody).map(([key, val]) => ({
                            key,
                            value: typeof val === 'string' ? val : JSON.stringify(val)
                        }))
                    );
                }
            } catch {
                setConfig({ temperature: null, maxOutputTokens: null, additionalBody: {} });
                setAdditionalBodyEntries([]);
            }
        }
    }, [value]);

    const handleConfigChange = (newConfig: AiModelRequestConfig) => {
        setConfig(newConfig);
        onChange?.(JSON.stringify(newConfig));
    };

    const handleTemperatureChange = (val: number | null) => {
        handleConfigChange({ ...config, temperature: val });
    };

    const handleMaxOutputTokensChange = (val: number | null) => {
        handleConfigChange({ ...config, maxOutputTokens: val });
    };

    const handleAdditionalBodyChange = (entries: Array<{ key: string; value: string }>) => {
        setAdditionalBodyEntries(entries);
        const additionalBody = entries.reduce((acc, { key, value }) => {
            if (key) {
                try {
                    acc[key] = JSON.parse(value);
                } catch {
                    acc[key] = value;
                }
            }
            return acc;
        }, {} as Record<string, any>);
        handleConfigChange({ ...config, additionalBody });
    };

    const handleAddEntry = () => {
        handleAdditionalBodyChange([...additionalBodyEntries, { key: '', value: '' }]);
    };

    return (
        <Space direction="vertical" className="w-full" size="middle">
            <Row gutter={16}>
                <Col span={12}>
                    <Form.Item label={t('pages.aiModelManager.modal.requestConfig.temperature')}>
                        <InputNumber
                            className="w-full"
                            value={config.temperature}
                            onChange={handleTemperatureChange}
                            placeholder="0.7"
                            min={0}
                            max={2}
                            step={0.1}
                        />
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item label={t('pages.aiModelManager.modal.requestConfig.maxOutputTokens')}>
                        <InputNumber
                            className="w-full"
                            value={config.maxOutputTokens}
                            onChange={handleMaxOutputTokensChange}
                            placeholder="4096"
                            min={1}
                        />
                    </Form.Item>
                </Col>
            </Row>

            <Divider className="my-2" />

            <div className="text-base mb-3">{t('pages.aiModelManager.modal.requestConfig.additionalBody')}</div>
            <Space direction="vertical" className="w-full" size="small">
                {additionalBodyEntries.map((entry, index) => (
                    <Row key={index} gutter={12}>
                        <Col span={10}>
                            <Input
                                placeholder={t('pages.aiModelManager.modal.requestConfig.additionalBodyKey')}
                                value={entry.key}
                                onChange={(e) => {
                                    const newEntries = [...additionalBodyEntries];
                                    newEntries[index].key = e.target.value;
                                    handleAdditionalBodyChange(newEntries);
                                }}
                            />
                        </Col>
                        <Col span={12}>
                            <Input
                                placeholder={t('pages.aiModelManager.modal.requestConfig.additionalBodyValue')}
                                value={entry.value}
                                onChange={(e) => {
                                    const newEntries = [...additionalBodyEntries];
                                    newEntries[index].value = e.target.value;
                                    handleAdditionalBodyChange(newEntries);
                                }}
                            />
                        </Col>
                        <Col span={2}>
                            <Button
                                danger
                                icon={<DeleteOutlined />}
                                onClick={() => handleAdditionalBodyChange(additionalBodyEntries.filter((_, i) => i !== index))}
                            />
                        </Col>
                    </Row>
                ))}
                <Button
                    type="dashed"
                    icon={<PlusOutlined />}
                    onClick={handleAddEntry}
                    className="w-full"
                >
                    {t('pages.aiModelManager.modal.requestConfig.addAdditionalBody')}
                </Button>
            </Space>
        </Space>
    );
}
