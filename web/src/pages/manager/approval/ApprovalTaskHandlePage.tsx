import {Button, Empty, Input, message, Modal, Select, Spin, Tabs} from "antd";
import {useEffect, useMemo, useRef, useState} from "react";
import {useTranslation} from "react-i18next";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {useApprovalFlowTaskTableColumns} from "@/components/columns/ApprovalFlowTaskEntityColumns.tsx";
import {
    handleApprovalFlowTask,
    queryMyApprovalFlowTasks,
} from "@/api/approval/approval-flow-task.api.ts";
import type {ApprovalFlowTask} from "@/types/approval/approval-flow-task.types.ts";
import {ApprovalFlowTaskStatus, ResourceScope} from "@/types/approval/approval-enums.ts";
import {getApprovalFlowTaskStatus} from "@/i18n/enum-helpers.ts";
import {useUserTenants} from "@/compositions/use-tenant.ts";
import {useManagerQueryParams} from "@/compositions/use-manager-query-params.ts";
import {ApprovalFlowViewerButton} from "@/components/approval/viewer/ApprovalFlowViewerOverlay.tsx";

const SYSTEM_SCOPE_ID = '0';
const STATUS_FILTER_ALL = '-1';

export default function ApprovalTaskHandlePage() {
    const {t} = useTranslation();
    const {currentTenant, isJoinedTenantsLoading} = useUserTenants();
    const columns = useApprovalFlowTaskTableColumns();
    const pageRef = useRef<ManagerPageContainerRef | null>(null);

    const {syncToUrl, initialQueryValues, getInitialParam} = useManagerQueryParams();

    const tenantId = currentTenant?.tenantId ?? null;

    const [statusFilter, setStatusFilter] = useState<number | undefined>(() => {
        const raw = getInitialParam('status');
        return raw !== undefined ? Number(raw) : ApprovalFlowTaskStatus.PENDING;
    });

    const [desiredScope, setDesiredScope] = useState<ResourceScope>(
        tenantId ? ResourceScope.TENANT : ResourceScope.SYSTEM
    );

    const activeScope: ResourceScope = !tenantId && desiredScope === ResourceScope.TENANT
        ? ResourceScope.SYSTEM
        : desiredScope;

    const [handlingTask, setHandlingTask] = useState<ApprovalFlowTask | null>(null);
    const [comment, setComment] = useState('');
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        pageRef.current?.refreshData({resetPage: true});
    }, [activeScope, statusFilter, tenantId]);

    const tabItems = useMemo(
        () => [
            {
                key: String(ResourceScope.SYSTEM),
                label: t('pages.approvalTaskHandle.tab.system'),
            },
            ...(tenantId
                ? [{
                    key: String(ResourceScope.TENANT),
                    label: t('pages.approvalTaskHandle.tab.tenant'),
                }]
                : []),
        ],
        [tenantId, t]
    );

    const statusOptions = useMemo(
        () => [
            ApprovalFlowTaskStatus.PENDING,
            ApprovalFlowTaskStatus.APPROVED,
            ApprovalFlowTaskStatus.REJECTED,
            ApprovalFlowTaskStatus.SKIPPED,
        ].map(value => ({label: getApprovalFlowTaskStatus(value), value})),
        [t]
    );

    const effectiveScopeId = activeScope === ResourceScope.TENANT
        ? (tenantId ?? SYSTEM_SCOPE_ID)
        : SYSTEM_SCOPE_ID;

    const openHandleModal = (task: ApprovalFlowTask) => {
        setComment('');
        setHandlingTask(task);
    };

    const submitHandle = async (approved: boolean) => {
        if (!handlingTask) return;
        setSubmitting(true);
        try {
            await handleApprovalFlowTask({
                taskId: handlingTask.id,
                approved,
                comment: comment.trim() || undefined,
            });
            void message.success(t('pages.approvalTaskHandle.modal.success'));
            setHandlingTask(null);
            pageRef.current?.refreshData({resetPage: false});
        } catch {
            void message.error(t('pages.approvalTaskHandle.modal.failed'));
        } finally {
            setSubmitting(false);
        }
    };

    const renderHeader = (
        <ActionBarComponent
            title={t('pages.approvalTaskHandle.title')}
            subtitle={t('pages.approvalTaskHandle.subtitle')}
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
                <Empty description={t('pages.approvalTaskHandle.noTenantTip')}/>
            ) : (
                <ManagerPageContainer
                    ref={pageRef}
                    entityName={t('entityNames.approvalFlowTask')}
                    title=""
                    subtitle=""
                    showActionBar={false}
                    readonlyMode
                    columns={columns}
                    queryParamsSync={syncToUrl}
                    initialQueryValues={initialQueryValues}
                    tableRowActionsRender={(record: ApprovalFlowTask) => (
                        <div className="flex gap-3 items-center">
                            {record.status === ApprovalFlowTaskStatus.PENDING && (
                                <a onClick={() => openHandleModal(record)}>
                                    {t('pages.approvalTaskHandle.action.handle')}
                                </a>
                            )}
                            <ApprovalFlowViewerButton instanceId={record.instanceId}/>
                        </div>
                    )}
                    simpleFilters={[
                        {field: 'status', operator: 'eq', value: statusFilter},
                    ]}
                    tableActions={[
                        {
                            label: <span>{t('pages.approvalTaskHandle.filter.status')}</span>,
                            children: (
                                <Select
                                    defaultValue={statusFilter !== undefined ? String(statusFilter) : STATUS_FILTER_ALL}
                                    style={{width: 140}}
                                    options={[
                                        {value: STATUS_FILTER_ALL, label: t('pages.approvalTaskHandle.filter.all')},
                                        ...statusOptions.map(o => ({label: o.label, value: String(o.value)})),
                                    ]}
                                    onChange={(value) => setStatusFilter(value === STATUS_FILTER_ALL ? undefined : Number(value))}
                                />
                            ),
                        },
                    ]}
                    query={async (props) => (
                        await queryMyApprovalFlowTasks({
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
            <Modal
                open={handlingTask !== null}
                title={t('pages.approvalTaskHandle.modal.title')}
                footer={null}
                confirmLoading={submitting}
                onCancel={() => setHandlingTask(null)}
                destroyOnHidden
            >
                <div className="flex flex-col gap-3 py-2">
                    <span>{t('pages.approvalTaskHandle.modal.comment')}</span>
                    <Input.TextArea
                        rows={3}
                        value={comment}
                        onChange={(e) => setComment(e.target.value)}
                        placeholder={t('pages.approvalTaskHandle.modal.commentPlaceholder')}
                    />
                    <div className="flex justify-end gap-2 mt-2">
                        <Button onClick={() => setHandlingTask(null)} disabled={submitting}>
                            {t('pages.approvalTaskHandle.modal.cancel')}
                        </Button>
                        <Button danger loading={submitting} onClick={() => submitHandle(false)}>
                            {t('pages.approvalTaskHandle.modal.reject')}
                        </Button>
                        <Button type="primary" loading={submitting} onClick={() => submitHandle(true)}>
                            {t('pages.approvalTaskHandle.modal.approve')}
                        </Button>
                    </div>
                </div>
            </Modal>
        </>
    );
}
