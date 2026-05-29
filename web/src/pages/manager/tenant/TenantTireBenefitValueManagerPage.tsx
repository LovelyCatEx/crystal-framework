import {Col, Form, Input, Row, Select} from "antd";
import {useEffect, useRef, useState} from "react";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    type ManagerCreateTenantTireBenefitValueDTO,
    TenantTireBenefitValueManagerController,
} from "@/api/tenant/tenant-benefit.api.ts";
import {useTenantTireBenefitValueTableColumns} from "@/components/columns/TenantTireBenefitValueEntityColumns.tsx";
import {useTranslation} from "react-i18next";
import {useManagerQueryParams} from "@/compositions/use-manager-query-params.ts";
import {TenantTireTypeManagerController} from "@/api/tenant/tenant-tire-type.api.ts";
import {TenantTireBenefitFeatureManagerController} from "@/api/tenant/tenant-benefit.api.ts";

export default function TenantTireBenefitValueManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const {t} = useTranslation();
    const columns = useTenantTireBenefitValueTableColumns();
    const { filters, setFilter, syncToUrl, initialQueryValues } = useManagerQueryParams({ schema: { tireTypeId: 'string', featureId: 'string' } });
    const [tireTypes, setTireTypes] = useState<{ id: string; name: string }[]>([]);
    const [features, setFeatures] = useState<{ id: string; featureKey: string; name: string }[]>([]);

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
                        options={(features ?? []).map((f: { id: string; featureKey: string }) => ({ value: f.id, label: f.featureKey }))}
                    />,
                },
            ]}
            editModalFormChildren={
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
                                    options={(features ?? []).map((f: { id: string; featureKey: string; name: string }) => ({ value: f.id, label: `${f.featureKey} - ${f.name}` }))}
                                />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Form.Item name="featureValue" label={t('pages.tenantTireBenefitValueManager.modal.featureValue.label')} rules={[{ required: true, message: t('pages.tenantTireBenefitValueManager.modal.featureValue.required') }]}>
                        <Input className="w-full rounded-lg h-10 flex items-center" placeholder={t('pages.tenantTireBenefitValueManager.modal.featureValue.placeholder')} maxLength={255} showCount />
                    </Form.Item>
                </>
            }
            query={async (props) => {
                return (await TenantTireBenefitValueManagerController.query(props)).data!
            }}
            delete={async (props) => {
                return (await TenantTireBenefitValueManagerController.delete(props)).data!
            }}
            update={async (props) => {
                return (await TenantTireBenefitValueManagerController.update(props)).data!
            }}
            create={async (props) => {
                return (await TenantTireBenefitValueManagerController.create(props as ManagerCreateTenantTireBenefitValueDTO)).data!
            }}
        >
        </ManagerPageContainer>
    )
}
