import {useEffect, useState} from "react";
import {Button, Col, Collapse, Form, Input, Modal, Row, Select, Space} from "antd";
import {DeleteOutlined, PlusOutlined, ExclamationCircleOutlined} from "@ant-design/icons";
import type {
    AiEndpointResponseConfig,
    AiProviderRequestConfig,
    AiProviderResponseConfig,
    AiUsageJsonPathConfig,
    DefaultProviderConfigsVO
} from "@/types/ai/ai.types.ts";
import {useTranslation} from "react-i18next";

interface AiProviderConfigFormProps {
    value?: {
        requestConfig?: string;
        responseConfig?: string;
    };
    onChange?: (value: { requestConfig?: string; responseConfig?: string }) => void;
    defaultConfigs?: DefaultProviderConfigsVO | null;
}

export function AiProviderConfigForm({ value, onChange, defaultConfigs }: AiProviderConfigFormProps) {
    const { t } = useTranslation();
    const [requestConfig, setRequestConfig] = useState<AiProviderRequestConfig>({ headers: {} });
    const [responseConfig, setResponseConfig] = useState<AiProviderResponseConfig>({});
    const [selectedTemplate, setSelectedTemplate] = useState<string>('custom');

    useEffect(() => {
        if (value?.requestConfig) {
            try {
                setRequestConfig(JSON.parse(value.requestConfig));
            } catch {
                setRequestConfig({ headers: {} });
            }
        }
        if (value?.responseConfig) {
            try {
                setResponseConfig(JSON.parse(value.responseConfig));
            } catch {
                setResponseConfig({});
            }
        }
    }, [value]);

    const handleRequestChange = (newConfig: AiProviderRequestConfig) => {
        setRequestConfig(newConfig);
        onChange?.({
            requestConfig: JSON.stringify(newConfig),
            responseConfig: value?.responseConfig || JSON.stringify(responseConfig)
        });
    };

    const handleResponseChange = (newConfig: AiProviderResponseConfig) => {
        setResponseConfig(newConfig);
        onChange?.({
            requestConfig: value?.requestConfig || JSON.stringify(requestConfig),
            responseConfig: JSON.stringify(newConfig)
        });
    };

    const handleTemplateChange = (template: string) => {
        if (template !== 'custom' && template !== selectedTemplate) {
            Modal.confirm({
                title: t('pages.aiProviderManager.modal.configTemplate.confirmTitle'),
                icon: <ExclamationCircleOutlined />,
                content: t('pages.aiProviderManager.modal.configTemplate.confirmContent'),
                okText: t('common.confirm'),
                cancelText: t('common.cancel'),
                onOk: () => {
                    applyTemplate(template);
                }
            });
        } else {
            applyTemplate(template);
        }
    };

    const applyTemplate = (template: string) => {
        setSelectedTemplate(template);
        if (!defaultConfigs) return;

        if (template === 'openai') {
            handleResponseChange(defaultConfigs.openai);
        } else if (template === 'anthropic') {
            handleResponseChange(defaultConfigs.anthropic);
        } else {
            handleResponseChange({});
        }
    };

    const templateOptions = [
        { label: t('pages.aiProviderManager.modal.configTemplate.custom'), value: 'custom' },
        ...(defaultConfigs ? [
            { label: t('pages.aiProviderManager.modal.configTemplate.openai'), value: 'openai' },
            { label: t('pages.aiProviderManager.modal.configTemplate.anthropic'), value: 'anthropic' }
        ] : [])
    ];

    return (
        <Space direction="vertical" className="w-full" size="middle">
            {defaultConfigs && (
                <Form.Item label={t('pages.aiProviderManager.modal.configTemplate.label')}>
                    <Select
                        value={selectedTemplate}
                        onChange={handleTemplateChange}
                        options={templateOptions}
                    />
                </Form.Item>
            )}

            <RequestConfigForm value={requestConfig} onChange={handleRequestChange} />
            <ResponseConfigForm value={responseConfig} onChange={handleResponseChange} />
        </Space>
    );
}

function RequestConfigForm({ value, onChange }: { value: AiProviderRequestConfig; onChange: (v: AiProviderRequestConfig) => void }) {
    const { t } = useTranslation();
    const [headers, setHeaders] = useState<Array<{ key: string; value: string }>>(
        Object.entries(value.headers || {}).map(([key, value]) => ({ key, value }))
    );

    useEffect(() => {
        setHeaders(Object.entries(value.headers || {}).map(([key, val]) => ({ key, value: val })));
    }, [value]);

    const handleHeadersChange = (newHeaders: Array<{ key: string; value: string }>) => {
        setHeaders(newHeaders);
        const headersObj = newHeaders.reduce((acc, { key, value }) => {
            if (key) acc[key] = value;
            return acc;
        }, {} as Record<string, string>);
        onChange({ ...value, headers: headersObj });
    };

    return (
        <div>
            <div className="mb-2 font-medium text-sm">{t('pages.aiProviderManager.modal.requestConfig.headers')}</div>
            <Space direction="vertical" className="w-full">
                {headers.map((header, index) => (
                    <Row key={index} gutter={8}>
                        <Col span={10}>
                            <Input
                                placeholder={t('pages.aiProviderManager.modal.requestConfig.headerKey')}
                                value={header.key}
                                onChange={(e) => {
                                    const newHeaders = [...headers];
                                    newHeaders[index].key = e.target.value;
                                    handleHeadersChange(newHeaders);
                                }}
                            />
                        </Col>
                        <Col span={12}>
                            <Input
                                placeholder={t('pages.aiProviderManager.modal.requestConfig.headerValue')}
                                value={header.value}
                                onChange={(e) => {
                                    const newHeaders = [...headers];
                                    newHeaders[index].value = e.target.value;
                                    handleHeadersChange(newHeaders);
                                }}
                            />
                        </Col>
                        <Col span={2}>
                            <Button
                                danger
                                icon={<DeleteOutlined />}
                                onClick={() => handleHeadersChange(headers.filter((_, i) => i !== index))}
                            />
                        </Col>
                    </Row>
                ))}
                <Button
                    type="dashed"
                    icon={<PlusOutlined />}
                    onClick={() => handleHeadersChange([...headers, { key: '', value: '' }])}
                    className="w-full"
                    size="small"
                >
                    {t('pages.aiProviderManager.modal.requestConfig.addHeader')}
                </Button>
            </Space>
        </div>
    );
}

function ResponseConfigForm({ value, onChange }: { value: AiProviderResponseConfig; onChange: (v: AiProviderResponseConfig) => void }) {
    const { t } = useTranslation();

    return (
        <Collapse
            size="small"
            items={[
                {
                    key: 'chatCompletions',
                    label: t('pages.aiProviderManager.modal.responseConfig.chatCompletions'),
                    children: (
                        <EndpointResponseConfigForm
                            value={value.chatCompletions || {}}
                            onChange={(v) => onChange({ ...value, chatCompletions: v })}
                        />
                    )
                },
                {
                    key: 'embedding',
                    label: t('pages.aiProviderManager.modal.responseConfig.embedding'),
                    children: (
                        <EndpointResponseConfigForm
                            value={value.embedding || {}}
                            onChange={(v) => onChange({ ...value, embedding: v })}
                            hideFinishReason
                        />
                    )
                }
            ]}
        />
    );
}

function EndpointResponseConfigForm({
    value,
    onChange,
    hideFinishReason
}: {
    value: AiEndpointResponseConfig;
    onChange: (v: AiEndpointResponseConfig) => void;
    hideFinishReason?: boolean;
}) {
    const { t } = useTranslation();

    return (
        <Space direction="vertical" className="w-full" size="small">
            <Form.Item label={t('pages.aiProviderManager.modal.responseConfig.contentPath')} className="mb-2">
                <Input
                    placeholder={t('pages.aiProviderManager.modal.responseConfig.contentPathPlaceholder')}
                    value={value.contentPath || ''}
                    onChange={(e) => onChange({ ...value, contentPath: e.target.value || null })}
                    size="small"
                />
            </Form.Item>
            {!hideFinishReason && (
                <Form.Item label={t('pages.aiProviderManager.modal.responseConfig.finishReasonPath')} className="mb-2">
                    <Input
                        placeholder={t('pages.aiProviderManager.modal.responseConfig.finishReasonPathPlaceholder')}
                        value={value.finishReasonPath || ''}
                        onChange={(e) => onChange({ ...value, finishReasonPath: e.target.value || null })}
                        size="small"
                    />
                </Form.Item>
            )}
            <Form.Item label={t('pages.aiProviderManager.modal.responseConfig.providerRequestIdPath')} className="mb-2">
                <Input
                    placeholder={t('pages.aiProviderManager.modal.responseConfig.providerRequestIdPathPlaceholder')}
                    value={value.providerRequestIdPath || ''}
                    onChange={(e) => onChange({ ...value, providerRequestIdPath: e.target.value || null })}
                    size="small"
                />
            </Form.Item>
            <Form.Item label={t('pages.aiProviderManager.modal.responseConfig.errorMessagePath')} className="mb-2">
                <Input
                    placeholder={t('pages.aiProviderManager.modal.responseConfig.errorMessagePathPlaceholder')}
                    value={value.errorMessagePath || ''}
                    onChange={(e) => onChange({ ...value, errorMessagePath: e.target.value || null })}
                    size="small"
                />
            </Form.Item>
            <div className="border-t pt-2 mt-2">
                <div className="mb-2 font-medium text-xs">{t('pages.aiProviderManager.modal.responseConfig.usageTitle')}</div>
                <UsageConfigForm
                    value={value.usage || {}}
                    onChange={(v) => onChange({ ...value, usage: v })}
                />
            </div>
        </Space>
    );
}

function UsageConfigForm({ value, onChange }: { value: AiUsageJsonPathConfig; onChange: (v: AiUsageJsonPathConfig) => void }) {
    const { t } = useTranslation();

    return (
        <Space direction="vertical" className="w-full" size="small">
            <Form.Item label={t('pages.aiProviderManager.modal.responseConfig.inputTokensPath')} className="mb-2">
                <Input
                    placeholder={t('pages.aiProviderManager.modal.responseConfig.inputTokensPathPlaceholder')}
                    value={value.inputTokensPath || ''}
                    onChange={(e) => onChange({ ...value, inputTokensPath: e.target.value || null })}
                    size="small"
                />
            </Form.Item>
            <Form.Item label={t('pages.aiProviderManager.modal.responseConfig.outputTokensPath')} className="mb-2">
                <Input
                    placeholder={t('pages.aiProviderManager.modal.responseConfig.outputTokensPathPlaceholder')}
                    value={value.outputTokensPath || ''}
                    onChange={(e) => onChange({ ...value, outputTokensPath: e.target.value || null })}
                    size="small"
                />
            </Form.Item>
            <Form.Item label={t('pages.aiProviderManager.modal.responseConfig.totalTokensPath')} className="mb-2">
                <Input
                    placeholder={t('pages.aiProviderManager.modal.responseConfig.totalTokensPathPlaceholder')}
                    value={value.totalTokensPath || ''}
                    onChange={(e) => onChange({ ...value, totalTokensPath: e.target.value || null })}
                    size="small"
                />
            </Form.Item>
            <Form.Item label={t('pages.aiProviderManager.modal.responseConfig.cacheReadTokensPath')} className="mb-2">
                <Input
                    placeholder={t('pages.aiProviderManager.modal.responseConfig.cacheReadTokensPathPlaceholder')}
                    value={value.cacheReadTokensPath || ''}
                    onChange={(e) => onChange({ ...value, cacheReadTokensPath: e.target.value || null })}
                    size="small"
                />
            </Form.Item>
            <Form.Item label={t('pages.aiProviderManager.modal.responseConfig.cacheWriteTokensPath')} className="mb-2">
                <Input
                    placeholder={t('pages.aiProviderManager.modal.responseConfig.cacheWriteTokensPathPlaceholder')}
                    value={value.cacheWriteTokensPath || ''}
                    onChange={(e) => onChange({ ...value, cacheWriteTokensPath: e.target.value || null })}
                    size="small"
                />
            </Form.Item>
        </Space>
    );
}
