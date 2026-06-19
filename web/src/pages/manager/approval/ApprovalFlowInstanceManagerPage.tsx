import {Select} from "antd";
import {useEffect, useRef} from "react";
import {useTranslation} from "react-i18next";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {useApprovalFlowInstanceTableColumns} from "@/components/columns/ApprovalFlowInstanceEntityColumns.tsx";
import {ApprovalFlowInstanceManagerController} from "@/api/approval/approval-flow-instance.api.ts";
import {ApprovalFlowInstanceStatus, ResourceScope} from "@/types/approval/approval-enums.ts";
import {getApprovalFlowInstanceStatus} from "@/i18n/enum-helpers.ts";
import {useManagerQueryParams} from "@/compositions/use-manager-query-params.ts";

const SYSTEM_SCOPE_ID = '0';
const STATUS_FILTER_ALL = '-1';

export default function ApprovalFlowInstanceManagerPage() {
    const {t} = useTranslation();
    const columns = useApprovalFlowInstanceTableColumns();
    const pageRef = useRef<ManagerPageContainerRef | null>(null);

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
        pageRef.current?.refreshData({resetPage: true});
    }, [filters.status]);

    return (
        <>
            <ActionBarComponent
                title={t('pages.approvalFlowInstanceManager.title')}
                subtitle={t('pages.approvalFlowInstanceManager.subtitle')}
            />
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
                        label: t('pages.approvalFlowInstanceManager.filter.status'),
                        renderValue: ({value, onChange}) => (
                            <Select
                                className="flex-1"
                                value={value !== undefined ? String(value) : undefined}
                                allowClear
                                placeholder={t('pages.approvalFlowInstanceManager.filter.all')}
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
                        label: <span>{t('pages.approvalFlowInstanceManager.filter.status')}</span>,
                        children: (
                            <Select
                                defaultValue={filters.status !== undefined ? String(filters.status) : STATUS_FILTER_ALL}
                                style={{width: 140}}
                                options={[
                                    {value: STATUS_FILTER_ALL, label: t('pages.approvalFlowInstanceManager.filter.all')},
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
                        scope: ResourceScope.SYSTEM,
                        scopeId: SYSTEM_SCOPE_ID,
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
