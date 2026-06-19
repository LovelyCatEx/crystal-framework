import {Empty, Select, Spin} from "antd";
import {useEffect, useRef} from "react";
import {useTranslation} from "react-i18next";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {useApprovalFlowInstanceTableColumns} from "@/components/columns/ApprovalFlowInstanceEntityColumns.tsx";
import {ApprovalFlowInstanceManagerController} from "@/api/approval/approval-flow-instance.api.ts";
import {ApprovalFlowInstanceStatus, ResourceScope} from "@/types/approval/approval-enums.ts";
import {getApprovalFlowInstanceStatus} from "@/i18n/enum-helpers.ts";
import {useUserTenants} from "@/compositions/use-tenant.ts";
import {useManagerQueryParams} from "@/compositions/use-manager-query-params.ts";

const STATUS_FILTER_ALL = '-1';

export default function MyTenantApprovalFlowInstanceManagerPage() {
    const {t} = useTranslation();
    const {currentTenant, isJoinedTenantsLoading} = useUserTenants();
    const columns = useApprovalFlowInstanceTableColumns();
    const pageRef = useRef<ManagerPageContainerRef | null>(null);

    const {filters, setFilter, syncToUrl, initialQueryValues} = useManagerQueryParams({
        schema: {status: 'number'},
    });

    const tenantId = currentTenant?.tenantId ?? null;

    const statusOptions = [
        ApprovalFlowInstanceStatus.IN_PROGRESS,
        ApprovalFlowInstanceStatus.APPROVED,
        ApprovalFlowInstanceStatus.REJECTED,
        ApprovalFlowInstanceStatus.CANCELLED,
    ].map(value => ({label: getApprovalFlowInstanceStatus(value), value}));

    useEffect(() => {
        pageRef.current?.refreshData({resetPage: true});
    }, [filters.status, tenantId]);

    const renderHeader = (
        <ActionBarComponent
            title={t('pages.myTenantApprovalFlowInstanceManager.title')}
            subtitle={t('pages.myTenantApprovalFlowInstanceManager.subtitle')}
        />
    );

    if (isJoinedTenantsLoading) {
        return (
            <>
                {renderHeader}
                <div className="flex justify-center items-center h-64">
                    <Spin size="large"/>
                </div>
            </>
        );
    }

    if (!tenantId) {
        return (
            <>
                {renderHeader}
                <Empty description={t('pages.myTenantApprovalFlowInstanceManager.noTenantTip')}/>
            </>
        );
    }

    return (
        <>
            {renderHeader}
            <ManagerPageContainer
                ref={pageRef}
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
                        label: t('pages.myTenantApprovalFlowInstanceManager.filter.status'),
                        renderValue: ({value, onChange}) => (
                            <Select
                                className="flex-1"
                                value={value !== undefined ? String(value) : undefined}
                                allowClear
                                placeholder={t('pages.myTenantApprovalFlowInstanceManager.filter.all')}
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
                        label: <span>{t('pages.myTenantApprovalFlowInstanceManager.filter.status')}</span>,
                        children: (
                            <Select
                                defaultValue={filters.status !== undefined ? String(filters.status) : STATUS_FILTER_ALL}
                                style={{width: 140}}
                                options={[
                                    {value: STATUS_FILTER_ALL, label: t('pages.myTenantApprovalFlowInstanceManager.filter.all')},
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
                        scopeId: tenantId,
                    })
                ).data!}
                create={async () => {
                }}
                update={async () => {
                }}
                delete={async () => {
                }}
            />
        </>
    );
}
