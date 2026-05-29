import {Col, Form, Input, Row, Select} from "antd";
import {useEffect, useRef} from "react";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    type ManagerCreateTenantTireBenefitFeatureDTO,
    TenantTireBenefitFeatureManagerController
} from "@/api/tenant/tenant-benefit.api.ts";
import {useTenantTireBenefitFeatureTableColumns} from "@/components/columns/TenantTireBenefitFeatureEntityColumns.tsx";
import {useTranslation} from "react-i18next";
import {useManagerQueryParams} from "@/compositions/use-manager-query-params.ts";

export default function TenantTireBenefitFeatureManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const {t} = useTranslation();
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
                            { value: '0', label: t('components.columns.tenantTireBenefitFeature.typeBoolean') },
                            { value: '1', label: t('components.columns.tenantTireBenefitFeature.typeLimit') },
                            { value: '2', label: t('components.columns.tenantTireBenefitFeature.typeEnum') },
                        ]}
                    />,
                },
            ]}
            editModalFormChildren={
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
                                        { value: 0, label: t('components.columns.tenantTireBenefitFeature.typeBoolean') },
                                        { value: 1, label: t('components.columns.tenantTireBenefitFeature.typeLimit') },
                                        { value: 2, label: t('components.columns.tenantTireBenefitFeature.typeEnum') },
                                    ]}
                                />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item name="defaultValue" label={t('pages.tenantTireBenefitFeatureManager.modal.defaultValue.label')}>
                                <Input className="w-full rounded-lg h-10 flex items-center" placeholder={t('pages.tenantTireBenefitFeatureManager.modal.defaultValue.placeholder')} maxLength={255} showCount />
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
            }
            query={async (props) => {
                return (await TenantTireBenefitFeatureManagerController.query(props)).data!
            }}
            delete={async (props) => {
                return (await TenantTireBenefitFeatureManagerController.delete(props)).data!
            }}
            update={async (props) => {
                return (await TenantTireBenefitFeatureManagerController.update(props)).data!
            }}
            create={async (props) => {
                return (await TenantTireBenefitFeatureManagerController.create(props as ManagerCreateTenantTireBenefitFeatureDTO)).data!
            }}
        >
        </ManagerPageContainer>
    )
}
