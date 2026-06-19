import {Select} from "antd";
import {useEffect, useRef, useState} from "react";
import {useTranslation} from "react-i18next";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {TenantSelectorWithDetail} from "@/components/tenant/TenantSelectorWithDetail.tsx";
import {useApprovalFlowInstanceTableColumns} from "@/components/columns/ApprovalFlowInstanceEntityColumns.tsx";
import {ApprovalFlowInstanceManagerController} from "@/api/approval/approval-flow-instance.api.ts";
import {ApprovalFlowInstanceStatus, ResourceScope} from "@/types/approval/approval-enums.ts";
import {getApprovalFlowInstanceStatus} from "@/i18n/enum-helpers.ts";
import {useManagerQueryParams} from "@/compositions/use-manager-query-params.ts";

const STATUS_FILTER_ALL = '-1';

export default function TenantApprovalFlowInstanceManagerPage() {
    const {t} = useTranslation();
    const columns = useApprovalFlowInstanceTableColumns();
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const [selectedTenantId, setSelectedTenantId] = useState<string | null>(null);

    const {filters, setFilter, syncToUrl, initialQueryValues} = useManagerQueryParams({
        schema: {status: 'number'},
    });

    const statusOptions = [
        ApprovalFlowInstanceStatus.IN_PROGRESS,
        ApprovalFlowInstanceStatus.APPROVED,
        ApprovalFlowInstanceStatus.REJECTED,
        ApprovalFlowInstanceStatus.CANCELLED,
    ].map(value => ({label: getApprovalFlowInstanceStatus(value), value}));

    useEffect(() => {
        if (selectedTenantId) {
            pageRef.current?.refreshData({resetPage: true});
        }
    }, [selectedTenantId, filters.status]);

    return (
        <>
            <ActionBarComponent
                title={t('pages.tenantApprovalFlowInstanceManager.title')}
                subtitle={t('pages.tenantApprovalFlowInstanceManager.subtitle')}
            />
            <TenantSelectorWithDetail
                value={selectedTenantId}
                onChange={setSelectedTenantId}
            />
            {selectedTenantId && (
                <ManagerPageContainer
                    ref={pageRef}
                    className="mt-4"
                    entityName={t('entityNames.approvalFlowInstance')}
                    title=""
                    subtitle=""
                    showActionBar={false}
                    readonlyMode
                    showRowActions={false}
                    columns={columns}
                    queryParamsSync={syncToUrl}
                    initialQueryValues={initialQueryValues}
                    filterableFields={[
                        {
                            field: 'status',
                            type: 'number' as const,
                            label: t('pages.tenantApprovalFlowInstanceManager.filter.status'),
                            renderValue: ({value, onChange}) => (
                                <Select
                                    className="flex-1"
                                    value={value !== undefined ? String(value) : undefined}
                                    allowClear
                                    placeholder={t('pages.tenantApprovalFlowInstanceManager.filter.all')}
                                    options={statusOptions.map(o => ({label: o.label, value: String(o.value)}))}
                                    onChange={(v) => onChange(v !== undefined ? Number(v) : undefined)}
                                />
                            ),
                        },
                    ]}
                    simpleFilters={[
                        {field: 'status', operator: 'eq', value: filters.status},
                    ]}
                    tableActions={[
                        {
                            label: <span>{t('pages.tenantApprovalFlowInstanceManager.filter.status')}</span>,
                            children: (
                                <Select
                                    defaultValue={filters.status !== undefined ? String(filters.status) : STATUS_FILTER_ALL}
                                    style={{width: 140}}
                                    options={[
                                        {value: STATUS_FILTER_ALL, label: t('pages.tenantApprovalFlowInstanceManager.filter.all')},
                                        ...statusOptions.map(o => ({label: o.label, value: String(o.value)})),
                                    ]}
                                    onChange={(value) => setFilter('status', value === STATUS_FILTER_ALL ? undefined : Number(value))}
                                />
                            ),
                        },
                    ]}
                    query={async (props) => (
                        await ApprovalFlowInstanceManagerController.query({
                            ...props,
                            scope: ResourceScope.TENANT,
                            scopeId: selectedTenantId,
                        })
                    ).data!}
                    create={async () => {
                    }}
                    update={async () => {
                    }}
                    delete={async () => {
                    }}
                />
            )}
        </>
    );
}
