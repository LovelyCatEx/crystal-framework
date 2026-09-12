import {useEffect, useState} from "react";
import {Button, Col, Divider, Form, Input, Modal, Row, Select, Space} from "antd";
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
                okText: t('pages.aiProviderManager.modal.configTemplate.confirmButton'),
                cancelText: t('pages.aiProviderManager.modal.configTemplate.cancelButton'),
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
        <Space direction="vertical" className="w-full" size="small">
            {defaultConfigs && (
                <>
                    <Form.Item label={t('pages.aiProviderManager.modal.configTemplate.label')} className="mb-2">
                        <Select
                            value={selectedTemplate}
                            onChange={handleTemplateChange}
                            options={templateOptions}
                        />
                    </Form.Item>
                    <Divider className="my-2" />
                </>
            )}
            <RequestConfigForm value={requestConfig} onChange={handleRequestChange} />
            <Divider className="my-2" />
            <ResponseConfigForm value={responseConfig} onChange={handleResponseChange} />
        </Space>
    );
}

function RequestConfigForm({ value, onChange }: { value: AiProviderRequestConfig; onChange: (v: AiProviderRequestConfig) => void }) {
    const { t } = useTranslation();
    const [headers, setHeaders] = useState<Array<{ key: string; value: string }>>(() =>
        Object.entries(value.headers || {}).map(([key, value]) => ({ key, value }))
    );
    const [isInitialized, setIsInitialized] = useState(false);

    useEffect(() => {
        if (!isInitialized && value.headers && Object.keys(value.headers).length > 0) {
            setHeaders(Object.entries(value.headers).map(([key, val]) => ({ key, value: val })));
            setIsInitialized(true);
        }
    }, [value.headers, isInitialized]);

    const handleHeadersChange = (newHeaders: Array<{ key: string; value: string }>) => {
        setHeaders(newHeaders);
        const headersObj = newHeaders.reduce((acc, { key, value }) => {
            if (key) acc[key] = value;
            return acc;
        }, {} as Record<string, string>);
        onChange({ ...value, headers: headersObj });
    };

    const handleAddHeader = () => {
        const newHeaders = [...headers, { key: '', value: '' }];
        handleHeadersChange(newHeaders);
    };

    return (
        <>
            <div className="text-base mb-3">{t('pages.aiProviderManager.modal.requestConfig.headers')}</div>
            <Space direction="vertical" className="w-full" size="small">
                {headers.map((header, index) => (
                    <Row key={index} gutter={12}>
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
                    onClick={handleAddHeader}
                    className="w-full"
                >
                    {t('pages.aiProviderManager.modal.requestConfig.addHeader')}
                </Button>
            </Space>
        </>
    );
}

function ResponseConfigForm({ value, onChange }: { value: AiProviderResponseConfig; onChange: (v: AiProviderResponseConfig) => void }) {
    const { t } = useTranslation();

    return (
        <>
            <div className="text-base mb-3">{t('pages.aiProviderManager.modal.responseConfig.chatCompletions')}</div>
            <EndpointResponseConfigForm
                value={value.chatCompletions || {}}
                onChange={(v) => onChange({ ...value, chatCompletions: v })}
            />

            <Divider />

            <div className="text-base mb-3">{t('pages.aiProviderManager.modal.responseConfig.embedding')}</div>
            <EndpointResponseConfigForm
                value={value.embedding || {}}
                onChange={(v) => onChange({ ...value, embedding: v })}
                hideFinishReason
            />
        </>
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
        <Space direction="vertical" className="w-full" size="middle">
            <Row gutter={16}>
                <Col span={12}>
                    <Form.Item label={t('pages.aiProviderManager.modal.responseConfig.contentPath')} className="mb-0">
                        <Input
                            placeholder={t('pages.aiProviderManager.modal.responseConfig.contentPathPlaceholder')}
                            value={value.contentPath || ''}
                            onChange={(e) => onChange({ ...value, contentPath: e.target.value || null })}
                        />
                    </Form.Item>
                </Col>
                {!hideFinishReason && (
                    <Col span={12}>
                        <Form.Item label={t('pages.aiProviderManager.modal.responseConfig.finishReasonPath')} className="mb-0">
                            <Input
                                placeholder={t('pages.aiProviderManager.modal.responseConfig.finishReasonPathPlaceholder')}
                                value={value.finishReasonPath || ''}
                                onChange={(e) => onChange({ ...value, finishReasonPath: e.target.value || null })}
                            />
                        </Form.Item>
                    </Col>
                )}
            </Row>
            <Row gutter={16}>
                <Col span={12}>
                    <Form.Item label={t('pages.aiProviderManager.modal.responseConfig.providerRequestIdPath')} className="mb-0">
                        <Input
                            placeholder={t('pages.aiProviderManager.modal.responseConfig.providerRequestIdPathPlaceholder')}
                            value={value.providerRequestIdPath || ''}
                            onChange={(e) => onChange({ ...value, providerRequestIdPath: e.target.value || null })}
                        />
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item label={t('pages.aiProviderManager.modal.responseConfig.errorMessagePath')} className="mb-0">
                        <Input
                            placeholder={t('pages.aiProviderManager.modal.responseConfig.errorMessagePathPlaceholder')}
                            value={value.errorMessagePath || ''}
                            onChange={(e) => onChange({ ...value, errorMessagePath: e.target.value || null })}
                        />
                    </Form.Item>
                </Col>
            </Row>

            <div className="text-sm text-gray-600 mt-2">{t('pages.aiProviderManager.modal.responseConfig.usageTitle')}</div>
            <UsageConfigForm
                value={value.usage || {}}
                onChange={(v) => onChange({ ...value, usage: v })}
            />
        </Space>
    );
}

function UsageConfigForm({ value, onChange }: { value: AiUsageJsonPathConfig; onChange: (v: AiUsageJsonPathConfig) => void }) {
    const { t } = useTranslation();

    return (
        <Space direction="vertical" className="w-full" size="middle">
            <Row gutter={16}>
                <Col span={8}>
                    <Form.Item label={t('pages.aiProviderManager.modal.responseConfig.inputTokensPath')} className="mb-0">
                        <Input
                            placeholder={t('pages.aiProviderManager.modal.responseConfig.inputTokensPathPlaceholder')}
                            value={value.inputTokensPath || ''}
                            onChange={(e) => onChange({ ...value, inputTokensPath: e.target.value || null })}
                        />
                    </Form.Item>
                </Col>
                <Col span={8}>
                    <Form.Item label={t('pages.aiProviderManager.modal.responseConfig.outputTokensPath')} className="mb-0">
                        <Input
                            placeholder={t('pages.aiProviderManager.modal.responseConfig.outputTokensPathPlaceholder')}
                            value={value.outputTokensPath || ''}
                            onChange={(e) => onChange({ ...value, outputTokensPath: e.target.value || null })}
                        />
                    </Form.Item>
                </Col>
                <Col span={8}>
                    <Form.Item label={t('pages.aiProviderManager.modal.responseConfig.totalTokensPath')} className="mb-0">
                        <Input
                            placeholder={t('pages.aiProviderManager.modal.responseConfig.totalTokensPathPlaceholder')}
                            value={value.totalTokensPath || ''}
                            onChange={(e) => onChange({ ...value, totalTokensPath: e.target.value || null })}
                        />
                    </Form.Item>
                </Col>
            </Row>
            <Row gutter={16}>
                <Col span={12}>
                    <Form.Item label={t('pages.aiProviderManager.modal.responseConfig.cacheReadTokensPath')} className="mb-0">
                        <Input
                            placeholder={t('pages.aiProviderManager.modal.responseConfig.cacheReadTokensPathPlaceholder')}
                            value={value.cacheReadTokensPath || ''}
                            onChange={(e) => onChange({ ...value, cacheReadTokensPath: e.target.value || null })}
                        />
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item label={t('pages.aiProviderManager.modal.responseConfig.cacheWriteTokensPath')} className="mb-0">
                        <Input
                            placeholder={t('pages.aiProviderManager.modal.responseConfig.cacheWriteTokensPathPlaceholder')}
                            value={value.cacheWriteTokensPath || ''}
                            onChange={(e) => onChange({ ...value, cacheWriteTokensPath: e.target.value || null })}
                        />
                    </Form.Item>
                </Col>
            </Row>
        </Space>
    );
}
