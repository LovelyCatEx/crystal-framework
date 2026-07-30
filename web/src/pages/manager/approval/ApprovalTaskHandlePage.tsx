import {Button, Empty, Input, message, Modal, Select, Spin, Tabs} from "antd";
import {useEffect, useMemo, useRef, useState} from "react";
import {useTranslation} from "react-i18next";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {useApprovalFlowTaskTableColumns} from "@/components/columns/ApprovalFlowTaskEntityColumns.tsx";
import {
    getApprovalFlowTaskFormView,
    handleApprovalFlowTask,
    queryMyApprovalFlowTasks,
} from "@/api/approval/approval-flow-task.api.ts";
import type {ApprovalFlowTask} from "@/types/approval/approval-flow-task.types.ts";
import {ApprovalFlowNodeType, ApprovalFlowTaskStatus, ResourceScope} from "@/types/approval/approval-enums.ts";
import {getApprovalFlowTaskStatus} from "@/i18n/enum-helpers.ts";
import {useUserTenants} from "@/compositions/use-tenant.ts";
import {useManagerQueryParams} from "@/compositions/use-manager-query-params.ts";
import {ApprovalFlowViewerButton} from "@/components/approval/viewer/ApprovalFlowViewerOverlay.tsx";
import {
    ApprovalFormRenderer,
    type ApprovalFormRendererRef,
} from "@/components/approval/form/ApprovalFormRenderer.tsx";
import {
    diffFormData,
    mergeFieldOverrides,
    parseFormSchema,
    parseNodeOverlay,
} from "@/utils/approval-form-utils.ts";
import type {MergedFieldSchema} from "@/types/approval/approval-form-schema.types.ts";

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

    const [formLoading, setFormLoading] = useState(false);
    const [formLoadFailed, setFormLoadFailed] = useState(false);
    const [mergedFields, setMergedFields] = useState<MergedFieldSchema[] | null>(null);
    const [formGroups, setFormGroups] = useState<Array<{key: string; label: string}> | undefined>(undefined);
    const [baselineFormData, setBaselineFormData] = useState<Record<string, unknown>>({});
    const formRef = useRef<ApprovalFormRendererRef | null>(null);

    useEffect(() => {
        pageRef.current?.refreshData({resetPage: true});
    }, [activeScope, statusFilter, tenantId]);

    useEffect(() => {
        if (!handlingTask) {
            setMergedFields(null);
            setFormGroups(undefined);
            setBaselineFormData({});
            setFormLoadFailed(false);
            return;
        }
        setFormLoading(true);
        setFormLoadFailed(false);
        (async () => {
            try {
                const resp = await getApprovalFlowTaskFormView(handlingTask.id);
                const view = resp.data;
                const schema = parseFormSchema(view?.formSchemaSnapshot ?? null);
                const overlay = parseNodeOverlay(view?.nodeFormSchema ?? null);
                const nodeType = (view?.nodeType ?? ApprovalFlowNodeType.APPROVAL) as ApprovalFlowNodeType;
                if (!schema || schema.fields.length === 0) {
                    setMergedFields(null);
                    setFormGroups(undefined);
                    setBaselineFormData({});
                    return;
                }
                const merged = mergeFieldOverrides(schema, overlay, {nodeType});
                setMergedFields(merged);
                setFormGroups(schema.groups);
                const parsedBaseline: Record<string, unknown> = view?.instanceFormData
                    ? JSON.parse(view.instanceFormData) as Record<string, unknown>
                    : {};
                setBaselineFormData(parsedBaseline);
            } catch {
                setFormLoadFailed(true);
                setMergedFields(null);
                setFormGroups(undefined);
                setBaselineFormData({});
            } finally {
                setFormLoading(false);
            }
        })();
    }, [handlingTask]);

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
            let formDataPayload: string | undefined;
            if (mergedFields && mergedFields.length > 0 && formRef.current) {
                const values = await formRef.current.validate();
                const diff = diffFormData(baselineFormData, values);
                if (Object.keys(diff).length > 0) {
                    formDataPayload = JSON.stringify(diff);
                }
            }
            await handleApprovalFlowTask({
                taskId: handlingTask.id,
                approved,
                comment: comment.trim() || undefined,
                formData: formDataPayload,
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
                width={mergedFields && mergedFields.length > 0 ? 640 : undefined}
            >
                <div className="flex flex-col gap-3 py-2">
                    {formLoading ? (
                        <div className="flex justify-center items-center py-8">
                            <Spin/>
                        </div>
                    ) : formLoadFailed ? (
                        <Empty description={t('pages.approvalTaskHandle.modal.formLoadFailed')}/>
                    ) : mergedFields && mergedFields.length > 0 ? (
                        <ApprovalFormRenderer
                            ref={formRef}
                            fields={mergedFields}
                            groups={formGroups}
                            initialValues={baselineFormData}
                        />
                    ) : null}
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
