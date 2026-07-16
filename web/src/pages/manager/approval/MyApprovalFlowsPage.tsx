import {Empty, Select, Spin, Tabs} from "antd";
import {useEffect, useMemo, useRef, useState} from "react";
import {useTranslation} from "react-i18next";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {useApprovalFlowInstanceTableColumns} from "@/components/columns/ApprovalFlowInstanceEntityColumns.tsx";
import {queryMyApprovalFlowInstances} from "@/api/approval/approval-flow-instance.api.ts";
import {ApprovalFlowInstanceStatus, ResourceScope} from "@/types/approval/approval-enums.ts";
import {getApprovalFlowInstanceStatus} from "@/i18n/enum-helpers.ts";
import {useUserTenants} from "@/compositions/use-tenant.ts";
import {useManagerQueryParams} from "@/compositions/use-manager-query-params.ts";

const SYSTEM_SCOPE_ID = '0';
const STATUS_FILTER_ALL = '-1';

export default function MyApprovalFlowsPage() {
    const {t} = useTranslation();
    const {currentTenant, isJoinedTenantsLoading} = useUserTenants();
    const columns = useApprovalFlowInstanceTableColumns();
    const pageRef = useRef<ManagerPageContainerRef | null>(null);

    const {filters, setFilter, syncToUrl, initialQueryValues} = useManagerQueryParams({
        schema: {status: 'number'},
    });

    const tenantId = currentTenant?.tenantId ?? null;

    const [desiredScope, setDesiredScope] = useState<ResourceScope>(
        tenantId ? ResourceScope.TENANT : ResourceScope.SYSTEM
    );

    const activeScope: ResourceScope = !tenantId && desiredScope === ResourceScope.TENANT
        ? ResourceScope.SYSTEM
        : desiredScope;

    useEffect(() => {
        pageRef.current?.refreshData({resetPage: true});
    }, [activeScope, filters.status, tenantId]);

    const tabItems = useMemo(
        () => [
            {
                key: String(ResourceScope.SYSTEM),
                label: t('pages.myApprovalFlows.tab.system'),
            },
            ...(tenantId
                ? [{
                    key: String(ResourceScope.TENANT),
                    label: t('pages.myApprovalFlows.tab.tenant'),
                }]
                : []),
        ],
        [tenantId, t]
    );

    const statusOptions = useMemo(
        () => [
            ApprovalFlowInstanceStatus.IN_PROGRESS,
            ApprovalFlowInstanceStatus.APPROVED,
            ApprovalFlowInstanceStatus.REJECTED,
            ApprovalFlowInstanceStatus.CANCELLED,
        ].map(value => ({label: getApprovalFlowInstanceStatus(value), value})),
        [t]
    );

    const effectiveScopeId = activeScope === ResourceScope.TENANT
        ? (tenantId ?? SYSTEM_SCOPE_ID)
        : SYSTEM_SCOPE_ID;

    const renderHeader = (
        <ActionBarComponent
            title={t('pages.myApprovalFlows.title')}
            subtitle={t('pages.myApprovalFlows.subtitle')}
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

    return (
        <>
            {renderHeader}
            <Tabs
                activeKey={String(activeScope)}
                onChange={(key) => setDesiredScope(Number(key) as ResourceScope)}
                items={tabItems}
            />
            {activeScope === ResourceScope.TENANT && !tenantId ? (
                <Empty description={t('pages.myApprovalFlows.noTenantTip')}/>
            ) : (
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
                            label: t('pages.myApprovalFlows.filter.status'),
                            renderValue: ({value, onChange}) => (
                                <Select
                                    className="flex-1"
                                    value={value !== undefined ? String(value) : undefined}
                                    allowClear
                                    placeholder={t('pages.myApprovalFlows.filter.all')}
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
                            label: <span>{t('pages.myApprovalFlows.filter.status')}</span>,
                            children: (
                                <Select
                                    defaultValue={filters.status !== undefined ? String(filters.status) : STATUS_FILTER_ALL}
                                    style={{width: 140}}
                                    options={[
                                        {value: STATUS_FILTER_ALL, label: t('pages.myApprovalFlows.filter.all')},
                                        ...statusOptions.map(o => ({label: o.label, value: String(o.value)})),
                                    ]}
                                    onChange={(value) => setFilter('status', value === STATUS_FILTER_ALL ? undefined : Number(value))}
                                />
                            ),
                        },
                    ]}
                    query={async (props) => (
                        await queryMyApprovalFlowInstances({
                            ...props,
                            scope: activeScope,
                            scopeId: effectiveScopeId,
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
