import {Col, Form, Input, InputNumber, Row, Select, Switch, Tabs} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    AiModelManagerController,
    type ManagerCreateAiModelDTO,
    type ManagerReadAiModelDTO, type ManagerUpdateAiModelDTO
} from "@/api/ai/ai-model.api.ts";
import {useEffect, useRef, useState} from "react";
import {useAiModelTableColumns} from "@/components/columns/AiModelEntityColumns.tsx";
import {useTranslation} from "react-i18next";
import {useManagerQueryParams} from "@/compositions/use-manager-query-params.ts";
import {AiProviderManagerController} from "@/api/ai/ai-provider.api.ts";
import type {AiProviderEntity} from "@/types/ai/ai.types.ts";
import {AiModelCapability} from "@/types/ai/ai.types.ts";
import {getAiModelCapability} from "@/i18n/enum-helpers.ts";
import {AiModelRequestConfigForm} from "@/components/ai/AiModelRequestConfigForm.tsx";

export default function AiModelManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const { filters, setFilter, syncToUrl, initialQueryValues } = useManagerQueryParams({ schema: { id: 'string' } });
    const {t} = useTranslation();
    const columns = useAiModelTableColumns();
    const [providers, setProviders] = useState<AiProviderEntity[]>([]);

    useEffect(() => {
        // Load all providers for the dropdown
        AiProviderManagerController.list().then(res => {
            if (res.data) {
                setProviders(res.data);
            }
        });
    }, []);

    useEffect(() => {
        pageRef.current?.refreshData?.({ resetPage: true });
    }, [filters.id]);

    const capabilityOptions = [
        { label: getAiModelCapability(AiModelCapability.TEXT_GENERATION), value: AiModelCapability.TEXT_GENERATION },
        { label: getAiModelCapability(AiModelCapability.IMAGE_GENERATION), value: AiModelCapability.IMAGE_GENERATION },
        { label: getAiModelCapability(AiModelCapability.AUDIO_GENERATION), value: AiModelCapability.AUDIO_GENERATION },
        { label: getAiModelCapability(AiModelCapability.VIDEO_GENERATION), value: AiModelCapability.VIDEO_GENERATION },
        { label: getAiModelCapability(AiModelCapability.VISION), value: AiModelCapability.VISION },
        { label: getAiModelCapability(AiModelCapability.AUDIO_TRANSCRIPTION), value: AiModelCapability.AUDIO_TRANSCRIPTION },
        { label: getAiModelCapability(AiModelCapability.EMBEDDING), value: AiModelCapability.EMBEDDING },
        { label: getAiModelCapability(AiModelCapability.CODE_GENERATION), value: AiModelCapability.CODE_GENERATION },
    ];

    return (
        <ManagerPageContainer
            ref={pageRef}
            entityName={t('entityNames.aiModel')}
            title={t('pages.aiModelManager.title')}
            subtitle={t('pages.aiModelManager.subtitle')}
            columns={columns}
            searchKeywords={['modelName', 'displayName', 'key']}
            queryParamsSync={syncToUrl}
            initialQueryValues={initialQueryValues}
            simpleFilters={[
                { field: 'id', operator: 'eq', value: filters.id },
            ]}
            tableActions={[
                {
                    label: <span>{t('pages.aiModelManager.tableActions.idFilter.label')}</span>,
                    children: <Input
                        className="rounded-xl"
                        placeholder={t('pages.aiModelManager.tableActions.idFilter.placeholder')}
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
                            label: t('pages.aiModelManager.modal.tabs.basic'),
                            children: (
                                <>
                                    <Form.Item
                                        name="providerId"
                                        label={t('pages.aiModelManager.modal.providerId.label')}
                                        rules={[{ required: true, message: t('pages.aiModelManager.modal.providerId.required') }]}
                                    >
                                        <Select
                                            placeholder={t('pages.aiModelManager.modal.providerId.placeholder')}
                                            options={providers.map(p => ({ label: p.name, value: p.id }))}
                                        />
                                    </Form.Item>
                                    <Row gutter={24}>
                                        <Col span={12}>
                                            <Form.Item
                                                name="key"
                                                label={t('pages.aiModelManager.modal.key.label')}
                                                rules={[{ required: true, message: t('pages.aiModelManager.modal.key.required') }]}
                                            >
                                                <Input placeholder={t('pages.aiModelManager.modal.key.placeholder')} />
                                            </Form.Item>
                                        </Col>
                                        <Col span={12}>
                                            <Form.Item
                                                name="modelName"
                                                label={t('pages.aiModelManager.modal.modelName.label')}
                                                rules={[{ required: true, message: t('pages.aiModelManager.modal.modelName.required') }]}
                                            >
                                                <Input placeholder={t('pages.aiModelManager.modal.modelName.placeholder')} />
                                            </Form.Item>
                                        </Col>
                                    </Row>
                                    <Form.Item
                                        name="displayName"
                                        label={t('pages.aiModelManager.modal.displayName.label')}
                                        rules={[{ required: true, message: t('pages.aiModelManager.modal.displayName.required') }]}
                                    >
                                        <Input placeholder={t('pages.aiModelManager.modal.displayName.placeholder')} />
                                    </Form.Item>
                                    <Form.Item name="description" label={t('pages.aiModelManager.modal.description.label')}>
                                        <Input.TextArea rows={2} placeholder={t('pages.aiModelManager.modal.description.placeholder')} />
                                    </Form.Item>
                                    <Form.Item
                                        name="capabilities"
                                        label={t('pages.aiModelManager.modal.capabilities.label')}
                                        normalize={(value) => {
                                            return Array.isArray(value) ? JSON.stringify(value) : value;
                                        }}
                                        getValueFromEvent={(value) => value}
                                        getValueProps={(value) => {
                                            if (typeof value === 'string') {
                                                try {
                                                    return { value: JSON.parse(value) };
                                                } catch {
                                                    return { value: [] };
                                                }
                                            }
                                            return { value: value || [] };
                                        }}
                                    >
                                        <Select
                                            mode="multiple"
                                            placeholder={t('pages.aiModelManager.modal.capabilities.placeholder')}
                                            options={capabilityOptions}
                                        />
                                    </Form.Item>
                                    <Row gutter={24}>
                                        <Col span={12}>
                                            <Form.Item
                                                name="contextWindowTokens"
                                                label={t('pages.aiModelManager.modal.contextWindowTokens.label')}
                                                rules={[{ required: true, message: t('pages.aiModelManager.modal.contextWindowTokens.required') }]}
                                            >
                                                <InputNumber className="w-full" placeholder={t('pages.aiModelManager.modal.contextWindowTokens.placeholder')} min={0} />
                                            </Form.Item>
                                        </Col>
                                        <Col span={12}>
                                            <Form.Item
                                                name="maxOutputTokens"
                                                label={t('pages.aiModelManager.modal.maxOutputTokens.label')}
                                            >
                                                <InputNumber className="w-full" placeholder={t('pages.aiModelManager.modal.maxOutputTokens.placeholder')} min={0} />
                                            </Form.Item>
                                        </Col>
                                    </Row>
                                    <Row gutter={24}>
                                        <Col span={12}>
                                            <Form.Item
                                                name="enabled"
                                                label={t('pages.aiModelManager.modal.enabled.label')}
                                                valuePropName="checked"
                                                initialValue={true}
                                            >
                                                <Switch />
                                            </Form.Item>
                                        </Col>
                                        <Col span={12}>
                                            <Form.Item
                                                name="sort"
                                                label={t('pages.aiModelManager.modal.sort.label')}
                                                initialValue={0}
                                            >
                                                <InputNumber className="w-full" placeholder={t('pages.aiModelManager.modal.sort.placeholder')} />
                                            </Form.Item>
                                        </Col>
                                    </Row>
                                </>
                            )
                        },
                        {
                            key: 'pricing',
                            label: t('pages.aiModelManager.modal.tabs.pricing'),
                            children: (
                                <>
                                    <Row gutter={24}>
                                        <Col span={12}>
                                            <Form.Item
                                                name="inputPricePerMillion"
                                                label={t('pages.aiModelManager.modal.inputPricePerMillion.label')}
                                                initialValue="0"
                                            >
                                                <Input placeholder={t('pages.aiModelManager.modal.inputPricePerMillion.placeholder')} />
                                            </Form.Item>
                                        </Col>
                                        <Col span={12}>
                                            <Form.Item
                                                name="outputPricePerMillion"
                                                label={t('pages.aiModelManager.modal.outputPricePerMillion.label')}
                                                initialValue="0"
                                            >
                                                <Input placeholder={t('pages.aiModelManager.modal.outputPricePerMillion.placeholder')} />
                                            </Form.Item>
                                        </Col>
                                    </Row>
                                    <Row gutter={24}>
                                        <Col span={12}>
                                            <Form.Item
                                                name="cacheReadPricePerMillion"
                                                label={t('pages.aiModelManager.modal.cacheReadPricePerMillion.label')}
                                            >
                                                <Input placeholder={t('pages.aiModelManager.modal.cacheReadPricePerMillion.placeholder')} />
                                            </Form.Item>
                                        </Col>
                                        <Col span={12}>
                                            <Form.Item
                                                name="cacheWritePricePerMillion"
                                                label={t('pages.aiModelManager.modal.cacheWritePricePerMillion.label')}
                                            >
                                                <Input placeholder={t('pages.aiModelManager.modal.cacheWritePricePerMillion.placeholder')} />
                                            </Form.Item>
                                        </Col>
                                    </Row>
                                    <Form.Item
                                        name="currency"
                                        label={t('pages.aiModelManager.modal.currency.label')}
                                        rules={[{ required: true, message: t('pages.aiModelManager.modal.currency.required') }]}
                                        initialValue="USD"
                                    >
                                        <Input placeholder={t('pages.aiModelManager.modal.currency.placeholder')} />
                                    </Form.Item>
                                </>
                            )
                        },
                        {
                            key: 'advanced',
                            label: t('pages.aiModelManager.modal.tabs.advanced'),
                            children: (
                                <>
                                    <Form.Item name="requestConfig" hidden>
                                        <Input />
                                    </Form.Item>
                                    <Form.Item
                                        noStyle
                                        shouldUpdate={(prevValues, currentValues) =>
                                            prevValues.requestConfig !== currentValues.requestConfig
                                        }
                                    >
                                        {({ getFieldValue, setFieldsValue }) => (
                                            <AiModelRequestConfigForm
                                                value={getFieldValue('requestConfig')}
                                                onChange={(value) => setFieldsValue({ requestConfig: value })}
                                            />
                                        )}
                                    </Form.Item>
                                </>
                            )
                        }
                    ]}
                />
            }
            query={async (props: ManagerReadAiModelDTO) => {
                return (await AiModelManagerController.query(props)).data!
            }}
            delete={async (props) => {
                return (await AiModelManagerController.delete(props)).data!
            }}
            update={async (props: ManagerUpdateAiModelDTO) => {
                // Convert capabilities array to JSON string
                if (Array.isArray(props.capabilities)) {
                    props.capabilities = JSON.stringify(props.capabilities);
                }
                return (await AiModelManagerController.update(props)).data!
            }}
            create={async (props) => {
                // Convert capabilities array to JSON string
                if (Array.isArray((props as ManagerCreateAiModelDTO).capabilities)) {
                    (props as ManagerCreateAiModelDTO).capabilities = JSON.stringify((props as ManagerCreateAiModelDTO).capabilities);
                }
                return (await AiModelManagerController.create(props as ManagerCreateAiModelDTO)).data!
            }}
        >
        </ManagerPageContainer>
    )
}
