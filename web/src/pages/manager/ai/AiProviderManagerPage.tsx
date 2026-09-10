import {Col, Form, Input, InputNumber, Row, Select, Switch, Tabs} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    AiProviderManagerController,
    getDefaultProviderConfigs,
    type ManagerCreateAiProviderDTO,
    type ManagerReadAiProviderDTO
} from "@/api/ai/ai-provider.api.ts";
import {useEffect, useRef, useState} from "react";
import {useAiProviderTableColumns} from "@/components/columns/AiProviderEntityColumns.tsx";
import {useTranslation} from "react-i18next";
import {useManagerQueryParams} from "@/compositions/use-manager-query-params.ts";
import {AiProviderProtocolType, type DefaultProviderConfigsVO} from "@/types/ai/ai.types.ts";
import {getAiProviderProtocolType} from "@/i18n/enum-helpers.ts";
import {AiProviderConfigForm} from "@/components/ai/AiProviderConfigForm.tsx";

export default function AiProviderManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const { filters, setFilter, syncToUrl, initialQueryValues } = useManagerQueryParams({ schema: { id: 'string' } });
    const {t} = useTranslation();
    const columns = useAiProviderTableColumns();
    const [defaultConfigs, setDefaultConfigs] = useState<DefaultProviderConfigsVO | null>(null);

    useEffect(() => {
        pageRef.current?.refreshData?.({ resetPage: true });
    }, [filters.id]);

    useEffect(() => {
        getDefaultProviderConfigs().then(res => {
            if (res.data) {
                setDefaultConfigs(res.data);
            }
        }).catch(() => {
            // Silent fail
        });
    }, []);

    const protocolTypeOptions = [
        { label: getAiProviderProtocolType(AiProviderProtocolType.OPENAI), value: AiProviderProtocolType.OPENAI },
        { label: getAiProviderProtocolType(AiProviderProtocolType.GEMINI), value: AiProviderProtocolType.GEMINI },
        { label: getAiProviderProtocolType(AiProviderProtocolType.ANTHROPIC), value: AiProviderProtocolType.ANTHROPIC },
    ];

    return (
        <ManagerPageContainer
            ref={pageRef}
            entityName={t('entityNames.aiProvider')}
            title={t('pages.aiProviderManager.title')}
            subtitle={t('pages.aiProviderManager.subtitle')}
            columns={columns}
            searchKeywords={['name', 'key', 'description']}
            queryParamsSync={syncToUrl}
            initialQueryValues={initialQueryValues}
            simpleFilters={[
                { field: 'id', operator: 'eq', value: filters.id },
            ]}
            tableActions={[
                {
                    label: <span>{t('pages.aiProviderManager.filter.id')}</span>,
                    children: <Input
                        className="rounded-xl"
                        placeholder={t('pages.aiProviderManager.filter.idPlaceholder')}
                        defaultValue={filters.id}
                        allowClear
                        onPressEnter={(e) => setFilter('id', (e.target as HTMLInputElement).value || undefined)}
                        onChange={(e) => { if (e.target.value === '') setFilter('id', undefined); }}
                    />,
                },
            ]}
            editModalFormChildren={
                <Tabs
                    items={[
                        {
                            key: 'basic',
                            label: t('pages.aiProviderManager.modal.tabs.basic'),
                            children: (
                                <>
                                    <Row gutter={24}>
                                        <Col span={12}>
                                            <Form.Item
                                                name="name"
                                                label={t('pages.aiProviderManager.modal.name.label')}
                                                rules={[{ required: true, message: t('pages.aiProviderManager.modal.name.required') }]}
                                            >
                                                <Input placeholder={t('pages.aiProviderManager.modal.name.placeholder')} />
                                            </Form.Item>
                                        </Col>
                                        <Col span={12}>
                                            <Form.Item
                                                name="key"
                                                label={t('pages.aiProviderManager.modal.key.label')}
                                                rules={[{ required: true, message: t('pages.aiProviderManager.modal.key.required') }]}
                                            >
                                                <Input placeholder={t('pages.aiProviderManager.modal.key.placeholder')} />
                                            </Form.Item>
                                        </Col>
                                    </Row>
                                    <Row gutter={24}>
                                        <Col span={12}>
                                            <Form.Item
                                                name="protocolType"
                                                label={t('pages.aiProviderManager.modal.protocolType.label')}
                                                rules={[{ required: true, message: t('pages.aiProviderManager.modal.protocolType.required') }]}
                                            >
                                                <Select
                                                    placeholder={t('pages.aiProviderManager.modal.protocolType.placeholder')}
                                                    options={protocolTypeOptions}
                                                />
                                            </Form.Item>
                                        </Col>
                                        <Col span={12}>
                                            <Form.Item
                                                name="baseUrl"
                                                label={t('pages.aiProviderManager.modal.baseUrl.label')}
                                                rules={[{ required: true, message: t('pages.aiProviderManager.modal.baseUrl.required') }]}
                                            >
                                                <Input placeholder={t('pages.aiProviderManager.modal.baseUrl.placeholder')} />
                                            </Form.Item>
                                        </Col>
                                    </Row>
                                    <Form.Item
                                        name="apiKey"
                                        label={t('pages.aiProviderManager.modal.apiKey.label')}
                                        rules={[{ required: true, message: t('pages.aiProviderManager.modal.apiKey.required') }]}
                                    >
                                        <Input.Password placeholder={t('pages.aiProviderManager.modal.apiKey.placeholder')} />
                                    </Form.Item>
                                    <Row gutter={24}>
                                        <Col span={12}>
                                            <Form.Item
                                                name="chatCompletionsPath"
                                                label={t('pages.aiProviderManager.modal.chatCompletionsPath.label')}
                                            >
                                                <Input placeholder={t('pages.aiProviderManager.modal.chatCompletionsPath.placeholder')} />
                                            </Form.Item>
                                        </Col>
                                        <Col span={12}>
                                            <Form.Item
                                                name="embeddingPath"
                                                label={t('pages.aiProviderManager.modal.embeddingPath.label')}
                                            >
                                                <Input placeholder={t('pages.aiProviderManager.modal.embeddingPath.placeholder')} />
                                            </Form.Item>
                                        </Col>
                                    </Row>
                                    <Form.Item name="description" label={t('pages.aiProviderManager.modal.description.label')}>
                                        <Input.TextArea rows={2} placeholder={t('pages.aiProviderManager.modal.description.placeholder')} />
                                    </Form.Item>
                                    <Row gutter={24}>
                                        <Col span={12}>
                                            <Form.Item
                                                name="enabled"
                                                label={t('pages.aiProviderManager.modal.enabled.label')}
                                                valuePropName="checked"
                                                initialValue={true}
                                            >
                                                <Switch />
                                            </Form.Item>
                                        </Col>
                                        <Col span={12}>
                                            <Form.Item
                                                name="sort"
                                                label={t('pages.aiProviderManager.modal.sort.label')}
                                                initialValue={0}
                                            >
                                                <InputNumber className="w-full" placeholder={t('pages.aiProviderManager.modal.sort.placeholder')} />
                                            </Form.Item>
                                        </Col>
                                    </Row>
                                </>
                            )
                        },
                        {
                            key: 'advanced',
                            label: t('pages.aiProviderManager.modal.tabs.advanced'),
                            children: (
                                <Form.Item
                                    noStyle
                                    shouldUpdate={(prevValues, currentValues) =>
                                        prevValues.requestConfig !== currentValues.requestConfig ||
                                        prevValues.responseConfig !== currentValues.responseConfig
                                    }
                                >
                                    {({ getFieldValue, setFieldsValue }) => (
                                        <AiProviderConfigForm
                                            value={{
                                                requestConfig: getFieldValue('requestConfig'),
                                                responseConfig: getFieldValue('responseConfig')
                                            }}
                                            onChange={(value) => setFieldsValue(value)}
                                            defaultConfigs={defaultConfigs}
                                        />
                                    )}
                                </Form.Item>
                            )
                        }
                    ]}
                />
            }
            query={async (props: ManagerReadAiProviderDTO) => {
                return (await AiProviderManagerController.query(props)).data!
            }}
            delete={async (props) => {
                return (await AiProviderManagerController.delete(props)).data!
            }}
            update={async (props) => {
                return (await AiProviderManagerController.update(props)).data!
            }}
            create={async (props) => {
                return (await AiProviderManagerController.create(props as ManagerCreateAiProviderDTO)).data!
            }}
        >
        </ManagerPageContainer>
    )
}
