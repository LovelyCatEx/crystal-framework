import {Col, Form, Input, InputNumber, Row, Select} from "antd";
import {useEffect, useRef} from "react";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    type ManagerCreateTenantTireBenefitFeatureDTO,
    type ManagerReadTenantTireBenefitFeatureDTO,
} from "@/api/tenant/tenant-benefit.api.ts";
import {TenantBenefitType, type TenantTireBenefitFeature} from "@/types/tenant/tenant-benefit.types.ts";
import {useTenantTireBenefitFeatureTableColumns} from "@/components/columns/TenantTireBenefitFeatureEntityColumns.tsx";
import {useProtectedController} from "@/components/base/ProtectedControllerWarningWrapper.tsx";
import {getTenantBenefitType} from "@/i18n/enum-helpers.ts";
import {useTranslation} from "react-i18next";
import {useManagerQueryParams} from "@/compositions/use-manager-query-params.ts";

export default function TenantTireBenefitFeatureManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const {t} = useTranslation();
    const { controller } = useProtectedController<TenantTireBenefitFeature, ManagerCreateTenantTireBenefitFeatureDTO, ManagerReadTenantTireBenefitFeatureDTO>();
    const columns = useTenantTireBenefitFeatureTableColumns();
    const { filters, setFilter, syncToUrl, initialQueryValues } = useManagerQueryParams({ schema: { featureType: 'string' } });

    useEffect(() => {
        pageRef.current?.refreshData?.({ resetPage: true });
    }, [filters.featureType]);

    return (
        <ManagerPageContainer
            ref={pageRef}
            entityName={t('entityNames.tenantTireBenefitFeature')}
            title={t('pages.tenantTireBenefitFeatureManager.title')}
            subtitle={t('pages.tenantTireBenefitFeatureManager.subtitle')}
            columns={columns}
            searchKeywords={['featureKey', 'name']}
            queryParamsSync={syncToUrl}
            initialQueryValues={initialQueryValues}
            simpleFilters={[
                { field: 'featureType', operator: 'eq', value: filters.featureType },
            ]}
            tableActions={[
                {
                    label: <span>{t('pages.tenantTireBenefitFeatureManager.filter.featureType')}</span>,
                    children: <Select
                        className="w-40"
                        placeholder={t('pages.tenantTireBenefitFeatureManager.filter.featureTypePlaceholder')}
                        allowClear
                        value={filters.featureType}
                        onChange={(value) => setFilter('featureType', value || undefined)}
                        options={[
                            { value: String(TenantBenefitType.BOOLEAN), label: getTenantBenefitType(TenantBenefitType.BOOLEAN) },
                            { value: String(TenantBenefitType.LIMIT), label: getTenantBenefitType(TenantBenefitType.LIMIT) },
                            { value: String(TenantBenefitType.ENUM), label: getTenantBenefitType(TenantBenefitType.ENUM) },
                        ]}
                    />,
                },
            ]}
            editModalFormChildren={<FeatureModalFormFields />}
            query={async (props) => {
                return (await controller.query(props)).data!
            }}
            delete={async (props) => {
                return (await controller.delete(props)).data!
            }}
            update={async (props) => {
                if (typeof (props as Record<string, unknown>).defaultValue !== 'string') {
                    (props as Record<string, unknown>).defaultValue = String((props as Record<string, unknown>).defaultValue ?? '');
                }
                return (await controller.update(props)).data!
            }}
            create={async (props) => {
                if (typeof (props as Record<string, unknown>).defaultValue !== 'string') {
                    (props as Record<string, unknown>).defaultValue = String((props as Record<string, unknown>).defaultValue ?? '');
                }
                return (await controller.create(props as ManagerCreateTenantTireBenefitFeatureDTO)).data!
            }}
        >
        </ManagerPageContainer>
    )
}

const FeatureModalFormFields: React.FC = () => {
    const form = Form.useFormInstance();
    const { t } = useTranslation();
    const featureType: number | undefined = Form.useWatch('featureType', form);

    const prevFeatureType = useRef<number | undefined>(undefined);
    useEffect(() => {
        if (prevFeatureType.current === featureType) return;
        prevFeatureType.current = featureType;
        if (featureType === TenantBenefitType.BOOLEAN) form.setFieldValue('defaultValue', 'false');
        else if (featureType === TenantBenefitType.LIMIT) form.setFieldValue('defaultValue', '0');
        else if (featureType === TenantBenefitType.ENUM) form.setFieldValue('defaultValue', '');
    }, [featureType, form]);

    return (
        <>
            <Row gutter={24}>
                <Col span={12}>
                    <Form.Item name="featureKey" label={t('pages.tenantTireBenefitFeatureManager.modal.featureKey.label')} rules={[{ required: true, message: t('pages.tenantTireBenefitFeatureManager.modal.featureKey.required') }, { max: 64, message: t('pages.tenantTireBenefitFeatureManager.modal.featureKey.maxLength') }]}>
                        <Input className="w-full rounded-lg h-10 flex items-center" placeholder={t('pages.tenantTireBenefitFeatureManager.modal.featureKey.placeholder')} maxLength={64} showCount />
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item name="name" label={t('pages.tenantTireBenefitFeatureManager.modal.name.label')} rules={[{ required: true, message: t('pages.tenantTireBenefitFeatureManager.modal.name.required') }, { max: 128, message: t('pages.tenantTireBenefitFeatureManager.modal.name.maxLength') }]}>
                        <Input className="w-full rounded-lg h-10 flex items-center" placeholder={t('pages.tenantTireBenefitFeatureManager.modal.name.placeholder')} maxLength={128} showCount />
                    </Form.Item>
                </Col>
            </Row>
            <Row gutter={24}>
                <Col span={12}>
                    <Form.Item name="featureType" label={t('pages.tenantTireBenefitFeatureManager.modal.featureType.label')} rules={[{ required: true, message: t('pages.tenantTireBenefitFeatureManager.modal.featureType.required') }]}>
                        <Select
                            className="w-full"
                            placeholder={t('pages.tenantTireBenefitFeatureManager.modal.featureType.placeholder')}
                            options={[
                                { value: TenantBenefitType.BOOLEAN, label: getTenantBenefitType(TenantBenefitType.BOOLEAN) },
                                { value: TenantBenefitType.LIMIT, label: getTenantBenefitType(TenantBenefitType.LIMIT) },
                                { value: TenantBenefitType.ENUM, label: getTenantBenefitType(TenantBenefitType.ENUM) },
                            ]}
                        />
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item noStyle shouldUpdate>
                        {({ getFieldValue }) => {
                            const featureType: number | undefined = getFieldValue('featureType');

                            const commonProps = {
                                name: 'defaultValue',
                                label: t('pages.tenantTireBenefitFeatureManager.modal.defaultValue.label'),
                            };

                            if (featureType === TenantBenefitType.BOOLEAN) {
                                return (
                                    <Form.Item key="dv-boolean" {...commonProps} preserve={false}>
                                        <Select
                                            className="w-full"
                                            placeholder={t('pages.tenantTireBenefitFeatureManager.modal.defaultValue.placeholderBoolean')}
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
                                    <Form.Item key="dv-limit" {...commonProps} preserve={false}>
                                        <InputNumber className="w-full" min={0} placeholder={t('pages.tenantTireBenefitFeatureManager.modal.defaultValue.placeholderLimit')} />
                                    </Form.Item>
                                );
                            }
                            if (featureType === TenantBenefitType.ENUM) {
                                return (
                                    <Form.Item
                                        key="dv-enum"
                                        {...commonProps}
                                                                               rules={[{ required: true, message: t('pages.tenantTireBenefitFeatureManager.modal.defaultValue.requiredEnum') }]}
                                        getValueProps={(value: string | string[]) => {
                                            if (Array.isArray(value)) return { value };
                                            return { value: value ? value.split(',').filter(Boolean) : [] };
                                        }}
                                        getValueFromEvent={(value: string[]) => value.join(',')}
                                    >
                                        <Select
                                            className="w-full"
                                            mode="tags"
                                            placeholder={t('pages.tenantTireBenefitFeatureManager.modal.defaultValue.placeholderEnum')}
                                            tokenSeparators={[',']}
                                        />
                                    </Form.Item>
                                );
                            }
                            return (
                                <Form.Item key="dv-text" {...commonProps} preserve={false}>
                                    <Input className="w-full rounded-lg h-10 flex items-center" placeholder={t('pages.tenantTireBenefitFeatureManager.modal.defaultValue.placeholder')} maxLength={255} showCount />
                                </Form.Item>
                            );
                        }}
                    </Form.Item>
                </Col>
            </Row>
            <Form.Item name="description" label={t('pages.tenantTireBenefitFeatureManager.modal.description.label')} rules={[{ max: 512, message: t('pages.tenantTireBenefitFeatureManager.modal.description.maxLength') }]}>
                <Input.TextArea
                    className="w-full rounded-lg"
                    placeholder={t('pages.tenantTireBenefitFeatureManager.modal.description.placeholder')}
                    rows={4}
                    maxLength={512}
                    showCount
                />
            </Form.Item>
        </>
    );
};
