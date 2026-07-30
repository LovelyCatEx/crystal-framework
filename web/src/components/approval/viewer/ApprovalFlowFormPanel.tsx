import {Divider, Empty, Tag, Timeline, Typography} from "antd";
import {useMemo} from "react";
import {useTranslation} from "react-i18next";
import type {
    ApprovalFlowInstanceDetailsVO,
    ApprovalFlowRecordVO,
} from "@/types/approval/approval-flow-instance-details.types.ts";
import type {ApprovalFlowTask} from "@/types/approval/approval-flow-task.types.ts";
import {ApprovalFieldType, ApprovalFlowNodeType, ApprovalFlowRecordAction} from "@/types/approval/approval-enums.ts";
import {ResourceScope} from "@/types/BaseScopedEntity.ts";
import {getApprovalFieldType, getApprovalFlowRecordAction} from "@/i18n/enum-helpers.ts";
import {UserChipById} from "@/components/chip/UserChipById.tsx";
import {TenantMemberChip} from "@/components/chip/TenantMemberChip.tsx";
import type {MergedFieldSchema} from "@/types/approval/approval-form-schema.types.ts";
import {ApprovalFormRenderer} from "@/components/approval/form/ApprovalFormRenderer.tsx";
import {mergeFieldOverrides, parseFormSchema} from "@/utils/approval-form-utils.ts";

export interface ApprovalFlowFormPanelProps {
    details: ApprovalFlowInstanceDetailsVO;
}

/**
 * Right-panel content for the viewer's "Form" tab. Two sections:
 *
 *   1. **Snapshot** — the current `instance.formData` rendered read-only with M2's shared
 *      renderer (so viewers see the exact same widgets initiators/approvers used). Fields
 *      hidden by the schema's own `visible=false` never render.
 *   2. **Change log** — a timeline of every action taken on the instance. Each initiate row
 *      shows the initial value dump; each approve/reject row shows the diff the operator
 *      submitted for that step (task.formData is a diff by decision 6, so we surface it
 *      verbatim). No backwards value replay: initial snapshots are not persisted, so we
 *      deliberately only show "new value" per diff — matches the design doc's "who changed
 *      which fields at which step" requirement.
 */
export function ApprovalFlowFormPanel({details}: ApprovalFlowFormPanelProps) {
    const {t} = useTranslation();
    const {instance, records, tasks} = details;

    // Definition-level schema drives snapshot rendering. Node overlays deliberately do NOT
    // apply here — the viewer shows the instance's data with its authoring shape, not the
    // reader's per-node view of it. Definition.formSchema is used verbatim; the "not pinned
    // to instance.definitionVersion" caveat carries over from M4's form-view endpoint.
    const schema = useMemo(() => parseFormSchema(details.definition.formSchema), [details.definition]);

    const snapshotFields = useMemo<MergedFieldSchema[]>(() => {
        if (!schema) return [];
        return mergeFieldOverrides(schema, null, {nodeType: ApprovalFlowNodeType.START});
    }, [schema]);

    const snapshotValues = useMemo<Record<string, unknown>>(() => {
        if (!instance.formData) return {};
        try {
            return JSON.parse(instance.formData) as Record<string, unknown>;
        } catch {
            return {};
        }
    }, [instance.formData]);

    // Fastest lookup by id — tasks in records are matched by (nodeId + operatorId + createdTime)
    // approximation: a task's status change publishes a record with the same node/operator, so
    // we join on that triple.
    const taskByRecord = useMemo(() => joinRecordsToTasks(records, tasks), [records, tasks]);
    const fieldMetaByKey = useMemo(() => {
        const map = new Map<string, {label: string; type: ApprovalFieldType; options: Map<string, string>}>();
        for (const f of schema?.fields ?? []) {
            const options = new Map<string, string>();
            for (const opt of f.options ?? []) options.set(opt.value, opt.label);
            map.set(f.key, {label: f.label || f.key, type: f.type, options});
        }
        return map;
    }, [schema]);

    if (!schema || schema.fields.length === 0) {
        return <Empty description={t('components.approvalFlowViewer.form.noSchema')}/>;
    }

    const sortedRecords = [...records].sort((a, b) => Number(a.createdTime) - Number(b.createdTime));

    return (
        <div className="flex flex-col gap-4">
            <div>
                <Typography.Title level={5} className="!mb-0">
                    {t('components.approvalFlowViewer.form.snapshotTitle')}
                </Typography.Title>
                <Typography.Text type="secondary" className="text-xs">
                    {t('components.approvalFlowViewer.form.snapshotSubtitle')}
                </Typography.Text>
                <div className="mt-3">
                    {snapshotFields.length === 0 ? (
                        <Empty description={t('components.approvalFlowViewer.form.noSchema')}/>
                    ) : (
                        <ApprovalFormRenderer
                            fields={snapshotFields}
                            groups={schema.groups}
                            initialValues={snapshotValues}
                            readonly
                        />
                    )}
                </div>
            </div>

            <Divider className="!my-1"/>

            <div>
                <Typography.Title level={5} className="!mb-0">
                    {t('components.approvalFlowViewer.form.timelineTitle')}
                </Typography.Title>
                <Typography.Text type="secondary" className="text-xs">
                    {t('components.approvalFlowViewer.form.timelineSubtitle')}
                </Typography.Text>

                {sortedRecords.length === 0 ? (
                    <Empty description={t('components.approvalFlowViewer.form.emptyTimeline')} className="mt-2"/>
                ) : (
                    <Timeline
                        className="mt-3"
                        items={sortedRecords.map(record => ({
                            color: getActionColor(record.action),
                            children: (
                                <TimelineRow
                                    record={record}
                                    task={taskByRecord.get(record.id) ?? null}
                                    scope={instance.scope}
                                    snapshotValues={snapshotValues}
                                    fieldMetaByKey={fieldMetaByKey}
                                />
                            ),
                        }))}
                    />
                )}
            </div>
        </div>
    );
}

function getActionColor(action: number): string {
    switch (action) {
        case ApprovalFlowRecordAction.APPROVE:
            return 'green';
        case ApprovalFlowRecordAction.REJECT:
            return 'red';
        case ApprovalFlowRecordAction.INITIATE:
            return 'blue';
        default:
            return 'gray';
    }
}

interface TimelineRowProps {
    record: ApprovalFlowRecordVO;
    task: ApprovalFlowTask | null;
    scope: number;
    snapshotValues: Record<string, unknown>;
    fieldMetaByKey: Map<string, {label: string; type: ApprovalFieldType; options: Map<string, string>}>;
}

function TimelineRow({record, task, scope, snapshotValues, fieldMetaByKey}: TimelineRowProps) {
    const {t} = useTranslation();
    const isInitiate = record.action === ApprovalFlowRecordAction.INITIATE;

    // Approve/reject rows read the task's formData diff (decision 6). Initiate rows have no
    // task — we surface a diff-shaped view of the initial values instead so the two rows
    // render with identical structure. Snapshot values are the source of truth for INITIATE
    // because the initial payload has been merged into instance.formData already.
    const changes = useMemo<Array<{key: string; value: unknown}>>(() => {
        if (isInitiate) {
            return Object.entries(snapshotValues).map(([key, value]) => ({key, value}));
        }
        const raw = task?.formData;
        if (!raw) return [];
        try {
            const diff = JSON.parse(raw) as Record<string, unknown>;
            return Object.entries(diff).map(([key, value]) => ({key, value}));
        } catch {
            return [];
        }
    }, [isInitiate, task?.formData, snapshotValues]);

    return (
        <div className="flex flex-col gap-1 text-sm">
            <div className="flex items-center gap-2">
                <span className="text-gray-500">
                    {t('components.approvalFlowViewer.records.operator')}:
                </span>
                {scope === ResourceScope.TENANT
                    ? <TenantMemberChip memberId={record.operatorId}/>
                    : <UserChipById userId={record.operatorId}/>}
            </div>
            <div>
                <span className="text-gray-500">
                    {t('components.approvalFlowViewer.records.action')}:
                </span>{' '}
                {getApprovalFlowRecordAction(record.action)}
            </div>
            {record.comment && record.comment.trim().length > 0 && (
                <div>
                    <span className="text-gray-500">
                        {t('components.approvalFlowViewer.records.comment')}:
                    </span>{' '}
                    {record.comment}
                </div>
            )}
            <div>
                <span className="text-gray-500">
                    {t('components.approvalFlowViewer.records.time')}:
                </span>{' '}
                {new Date(Number(record.createdTime)).toLocaleString()}
            </div>

            {changes.length > 0 && (
                <div className="mt-1 flex flex-col gap-1">
                    <span className="text-gray-500 text-xs">
                        {isInitiate
                            ? t('components.approvalFlowViewer.form.initiateFields')
                            : t('components.approvalFlowViewer.form.changedFields')}:
                    </span>
                    <div className="flex flex-wrap gap-1">
                        {changes.map(({key, value}) => (
                            <FieldChip
                                key={key}
                                fieldKey={key}
                                value={value}
                                meta={fieldMetaByKey.get(key)}
                            />
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
}

interface FieldChipProps {
    fieldKey: string;
    value: unknown;
    meta: {label: string; type: ApprovalFieldType; options: Map<string, string>} | undefined;
}

/**
 * Renders one changed field as `label: displayValue` inside a Tag. Falls back to the raw
 * field key when the field is no longer in the current schema (definition edits post-instance
 * — unlikely but possible if a definition is edited between versions).
 */
function FieldChip({fieldKey, value, meta}: FieldChipProps) {
    const label = meta?.label ?? fieldKey;
    const display = formatFieldValue(value, meta);
    return (
        <Tag className="!m-0 !text-xs">
            <span className="font-medium">{label}</span>
            {meta && meta.label !== fieldKey && (
                <span className="text-gray-400 mx-1 font-mono">({fieldKey})</span>
            )}
            <span className="mx-1">:</span>
            <span>{display}</span>
            {meta && (
                <span className="ml-1 text-[10px] text-gray-400">
                    {getApprovalFieldType(meta.type)}
                </span>
            )}
        </Tag>
    );
}

function formatFieldValue(
    value: unknown,
    meta: {type: ApprovalFieldType; options: Map<string, string>} | undefined,
): string {
    if (value === null || value === undefined) return '—';
    if (Array.isArray(value)) {
        return value.map(v => resolveOptionLabel(v, meta)).join(', ');
    }
    if (typeof value === 'boolean') return value ? 'true' : 'false';
    return resolveOptionLabel(value, meta);
}

function resolveOptionLabel(
    value: unknown,
    meta: {type: ApprovalFieldType; options: Map<string, string>} | undefined,
): string {
    if (typeof value === 'string' && meta) {
        const optionLabel = meta.options.get(value);
        if (optionLabel) return optionLabel;
    }
    return String(value);
}

/**
 * Join records to tasks by (nodeId, operatorId, timestamp proximity). A task is created before
 * its APPROVE / REJECT record, so we pick the task on the same node whose id lets the
 * timestamps align. This is a heuristic — the ideal fix is to have `record.taskId` on the
 * backend, but adding that is out of M8's scope; the heuristic covers the standard single-task
 * case reliably and degrades gracefully in the OR-mode case (worst outcome: the diff shows up
 * on the wrong record among concurrent same-node approvers).
 */
function joinRecordsToTasks(
    records: ApprovalFlowRecordVO[],
    tasks: ApprovalFlowTask[],
): Map<string, ApprovalFlowTask> {
    const bucketByNodeAndOperator = new Map<string, ApprovalFlowTask[]>();
    for (const task of tasks) {
        const key = `${task.nodeId}|${task.assigneeId}`;
        if (!bucketByNodeAndOperator.has(key)) bucketByNodeAndOperator.set(key, []);
        bucketByNodeAndOperator.get(key)!.push(task);
    }
    // Sort each bucket by modifiedTime desc so the first pop is the freshest task.
    for (const bucket of bucketByNodeAndOperator.values()) {
        bucket.sort((a, b) => Number(b.modifiedTime) - Number(a.modifiedTime));
    }
    const out = new Map<string, ApprovalFlowTask>();
    for (const record of records) {
        if (record.action !== ApprovalFlowRecordAction.APPROVE
            && record.action !== ApprovalFlowRecordAction.REJECT) continue;
        const key = `${record.nodeId}|${record.operatorId}`;
        const bucket = bucketByNodeAndOperator.get(key);
        if (!bucket || bucket.length === 0) continue;
        // Pick the task whose modifiedTime is closest to the record's createdTime.
        const target = Number(record.createdTime);
        let bestIndex = 0;
        let bestDelta = Math.abs(Number(bucket[0].modifiedTime) - target);
        for (let i = 1; i < bucket.length; i++) {
            const delta = Math.abs(Number(bucket[i].modifiedTime) - target);
            if (delta < bestDelta) {
                bestIndex = i;
                bestDelta = delta;
            }
        }
        out.set(record.id, bucket[bestIndex]);
        bucket.splice(bestIndex, 1);
    }
    return out;
}
