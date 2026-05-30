import {Col, Form, Input, InputNumber, Row, Select} from "antd";
import {useEffect, useRef, useState} from "react";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    type ManagerCreateTenantTireBenefitValueDTO,
    TenantTireBenefitValueManagerController,
    TenantTireBenefitFeatureManagerController,
} from "@/api/tenant/tenant-benefit.api.ts";
import {TenantBenefitType, type TenantTireBenefitFeature} from "@/types/tenant/tenant-benefit.types.ts";
import {useTenantTireBenefitValueTableColumns} from "@/components/columns/TenantTireBenefitValueEntityColumns.tsx";
import {useTranslation} from "react-i18next";
import {useManagerQueryParams} from "@/compositions/use-manager-query-params.ts";
import {TenantTireTypeManagerController} from "@/api/tenant/tenant-tire-type.api.ts";

export default function TenantTireBenefitValueManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const {t} = useTranslation();
    const columns = useTenantTireBenefitValueTableColumns();
    const { filters, setFilter, syncToUrl, initialQueryValues } = useManagerQueryParams({ schema: { tireTypeId: 'string', featureId: 'string' } });
    const [tireTypes, setTireTypes] = useState<{ id: string; name: string }[]>([]);
    const [features, setFeatures] = useState<TenantTireBenefitFeature[]>([]);

    useEffect(() => {
        TenantTireTypeManagerController.list().then((res) => {
            setTireTypes(res.data || []);
        });
        TenantTireBenefitFeatureManagerController.list().then((res) => {
            setFeatures(res.data || []);
        });
    }, []);

    useEffect(() => {
        pageRef.current?.refreshData?.({ resetPage: true });
    }, [filters.tireTypeId, filters.featureId]);

    return (
        <ManagerPageContainer
            ref={pageRef}
            entityName={t('entityNames.tenantTireBenefitValue')}
            title={t('pages.tenantTireBenefitValueManager.title')}
            subtitle={t('pages.tenantTireBenefitValueManager.subtitle')}
            columns={columns}
            searchKeywords={['featureValue']}
            queryParamsSync={syncToUrl}
            initialQueryValues={initialQueryValues}
            simpleFilters={[
                { field: 'tireTypeId', operator: 'eq', value: filters.tireTypeId },
                { field: 'featureId', operator: 'eq', value: filters.featureId },
            ]}
            tableActions={[
                {
                    label: <span>{t('pages.tenantTireBenefitValueManager.filter.tireTypeId')}</span>,
                    children: <Select
                        className="w-40"
                        placeholder={t('pages.tenantTireBenefitValueManager.filter.tireTypeIdPlaceholder')}
                        allowClear
                        value={filters.tireTypeId}
                        onChange={(value) => setFilter('tireTypeId', value || undefined)}
                        options={(tireTypes ?? []).map((t: { id: string; name: string }) => ({ value: t.id, label: t.name }))}
                    />,
                },
                {
                    label: <span>{t('pages.tenantTireBenefitValueManager.filter.featureId')}</span>,
                    children: <Select
                        className="w-40"
                        placeholder={t('pages.tenantTireBenefitValueManager.filter.featureIdPlaceholder')}
                        allowClear
                        value={filters.featureId}
                        onChange={(value) => setFilter('featureId', value || undefined)}
                        options={(features ?? []).map((f) => ({ value: f.id, label: `${f.name} (${f.featureKey})` }))}
                    />,
                },
            ]}
            editModalFormChildren={<BenefitValueModalFormFields features={features} tireTypes={tireTypes} />}
            query={async (props) => {
                return (await TenantTireBenefitValueManagerController.query(props)).data!
            }}
            delete={async (props) => {
                return (await TenantTireBenefitValueManagerController.delete(props)).data!
            }}
            update={async (props) => {
                if (typeof (props as Record<string, unknown>).featureValue !== 'string') {
                    (props as Record<string, unknown>).featureValue = String((props as Record<string, unknown>).featureValue ?? '');
                }
                return (await TenantTireBenefitValueManagerController.update(props)).data!
            }}
            create={async (props) => {
                if (typeof (props as Record<string, unknown>).featureValue !== 'string') {
                    (props as Record<string, unknown>).featureValue = String((props as Record<string, unknown>).featureValue ?? '');
                }
                return (await TenantTireBenefitValueManagerController.create(props as ManagerCreateTenantTireBenefitValueDTO)).data!
            }}
        >
        </ManagerPageContainer>
    )
}

interface BenefitValueModalFormFieldsProps {
    features: TenantTireBenefitFeature[];
    tireTypes: { id: string; name: string }[];
}

const BenefitValueModalFormFields: React.FC<BenefitValueModalFormFieldsProps> = ({ features, tireTypes }) => {
    const form = Form.useFormInstance();
    const { t } = useTranslation();
    const featureId: string | undefined = Form.useWatch('featureId', form);

    const prevFeatureId = useRef<string | undefined>(undefined);
    useEffect(() => {
        if (!featureId || prevFeatureId.current === featureId) return;
        prevFeatureId.current = featureId;
        const f = features.find(f => f.id === featureId);
        if (!f) return;
        if (f.featureType === TenantBenefitType.BOOLEAN) form.setFieldValue('featureValue', 'false');
        else if (f.featureType === TenantBenefitType.LIMIT) form.setFieldValue('featureValue', '0');
        else if (f.featureType === TenantBenefitType.ENUM) {
            const firstOption = (f.defaultValue || '').split(',')[0]?.trim() || '';
            form.setFieldValue('featureValue', firstOption);
        }
    }, [featureId, features, form]);

    return (
        <>
            <Row gutter={24}>
                <Col span={12}>
                    <Form.Item name="tireTypeId" label={t('pages.tenantTireBenefitValueManager.modal.tireTypeId.label')} rules={[{ required: true, message: t('pages.tenantTireBenefitValueManager.modal.tireTypeId.required') }]}>
                        <Select
                            className="w-full"
                            placeholder={t('pages.tenantTireBenefitValueManager.modal.tireTypeId.placeholder')}
                            options={(tireTypes ?? []).map((t: { id: string; name: string }) => ({ value: t.id, label: t.name }))}
                        />
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item name="featureId" label={t('pages.tenantTireBenefitValueManager.modal.featureId.label')} rules={[{ required: true, message: t('pages.tenantTireBenefitValueManager.modal.featureId.required') }]}>
                        <Select
                            className="w-full"
                            placeholder={t('pages.tenantTireBenefitValueManager.modal.featureId.placeholder')}
                            options={(features ?? []).map((f) => ({ value: f.id, label: `${f.name} (${f.featureKey})` }))}
                        />
                    </Form.Item>
                </Col>
            </Row>
            <Form.Item noStyle shouldUpdate>
                {({ getFieldValue }) => {
                    const featureId: string | undefined = getFieldValue('featureId');
                    const feature = features.find(f => f.id === featureId);
                    const featureType = feature?.featureType;

                    const commonProps = {
                        name: 'featureValue',
                        label: t('pages.tenantTireBenefitValueManager.modal.featureValue.label'),
                        rules: [{ required: true, message: t('pages.tenantTireBenefitValueManager.modal.featureValue.required') }],
                    };

                    if (featureType === TenantBenefitType.BOOLEAN) {
                        return (
                            <Form.Item key="fv-boolean" {...commonProps}>
                                <Select
                                    className="w-full"
                                    placeholder={t('pages.tenantTireBenefitValueManager.modal.featureValue.placeholder')}
                                    options={[
                                        { value: 'true', label: t('pages.tenantTireBenefitValueManager.modal.featureValue.booleanTrue') },
                                        { value: 'false', label: t('pages.tenantTireBenefitValueManager.modal.featureValue.booleanFalse') },
                                    ]}
                                />
                            </Form.Item>
                        );
                    }
                    if (featureType === TenantBenefitType.LIMIT) {
                        return (
                            <Form.Item key="fv-limit" {...commonProps}>
                                <InputNumber className="w-full" min={0} placeholder={t('pages.tenantTireBenefitValueManager.modal.featureValue.placeholder')} />
                            </Form.Item>
                        );
                    }
                    if (featureType === TenantBenefitType.ENUM) {
                        const enumOptions = (feature?.defaultValue || '')
                            .split(',')
                            .filter(Boolean)
                            .map((opt: string) => ({ value: opt, label: opt }));
                        return (
                            <Form.Item key="fv-enum" {...commonProps}>
                                <Select
                                    className="w-full"
                                    placeholder={t('pages.tenantTireBenefitValueManager.modal.featureValue.placeholderEnum')}
                                    options={enumOptions}
                                />
                            </Form.Item>
                        );
                    }
                    return (
                        <Form.Item key="fv-text" {...commonProps}>
                            <Input className="w-full rounded-lg h-10 flex items-center" placeholder={t('pages.tenantTireBenefitValueManager.modal.featureValue.placeholder')} maxLength={255} showCount />
                        </Form.Item>
                    );
                }}
            </Form.Item>
        </>
    );
};
