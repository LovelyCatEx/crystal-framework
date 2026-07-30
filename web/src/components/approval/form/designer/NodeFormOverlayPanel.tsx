import {Alert, Empty, Switch, Table, Tag, Tooltip, Typography} from "antd";
import type {ColumnsType} from "antd/es/table";
import {useMemo} from "react";
import {useTranslation} from "react-i18next";
import {ApprovalFieldType, ApprovalFlowNodeType} from "@/types/approval/approval-enums.ts";
import type {
    ApprovalFieldOverride,
    ApprovalFieldSchema,
    ApprovalFormSchema,
    NodeFormOverlay,
} from "@/types/approval/approval-form-schema.types.ts";
import {APPROVAL_FORM_SCHEMA_VERSION} from "@/types/approval/approval-form-schema.types.ts";
import {getApprovalFieldType} from "@/i18n/enum-helpers.ts";
import {parseNodeOverlay, stringifyNodeOverlay} from "@/utils/approval-form-utils.ts";

export interface NodeFormOverlayPanelProps {
    /**
     * The APPROVAL / CC node whose overlay we're editing. Only these two node types render
     * a form to their operator, so the panel is only meaningful for them (see decision 2).
     */
    nodeType: number;
    /** Definition-level schema — drives the field list. Overlay may only reference these keys (decision 7). */
    definitionFormSchema: ApprovalFormSchema | null;
    /**
     * The node's persisted overlay JSON string, i.e. `ApprovalFlowNodeEntity.formSchema` — the
     * on-disk shape is `NodeFormOverlay` (see `.claude/research/approval-form-design-decisions.md`).
     */
    nodeFormSchemaRaw: string | null;
    /** Called with the new serialized overlay JSON (or empty string to clear). */
    onOverlayChange: (nextRaw: string) => void;
}

/**
 * The three visibility flags a node overlay may adjust for each definition-level field.
 * Kept in a tuple so the row rendering can iterate deterministically.
 */
const OVERRIDE_KEYS = ['visible', 'readonly', 'required'] as const;
type OverrideKey = typeof OVERRIDE_KEYS[number];

interface FieldRow {
    key: string;                  // schema field key
    field: ApprovalFieldSchema;
    override: ApprovalFieldOverride | undefined;
}

function isOverrideEmpty(o: ApprovalFieldOverride | undefined): boolean {
    if (!o) return true;
    return o.visible === undefined && o.readonly === undefined && o.required === undefined;
}

function pruneOverride(o: ApprovalFieldOverride | undefined): ApprovalFieldOverride | undefined {
    if (!o) return undefined;
    const kept: ApprovalFieldOverride = {};
    if (o.visible !== undefined) kept.visible = o.visible;
    if (o.readonly !== undefined) kept.readonly = o.readonly;
    if (o.required !== undefined) kept.required = o.required;
    return isOverrideEmpty(kept) ? undefined : kept;
}

/**
 * Renders one row per definition-level field, each showing a three-state control for
 * `visible` / `readonly` / `required`. "State" is one of:
 *   - `undefined`  → inherit from the definition (rendered as a "-" ghost + tooltip)
 *   - `true`/`false` → explicit override, rendered as a Switch
 *
 * CC nodes force `readonly=true` regardless of overlay (decision 2), so the readonly
 * switch is disabled and locked visually.
 *
 * On any toggle we serialize the whole overlay and hand it back — persistence is at the
 * graph level via `updateApprovalFlowGraph` (decision 3, existing save() in ApprovalEditor
 * already ships `formSchema` per node).
 */
export function NodeFormOverlayPanel(props: NodeFormOverlayPanelProps) {
    const {nodeType, definitionFormSchema, nodeFormSchemaRaw, onOverlayChange} = props;
    const {t} = useTranslation();

    const overlay: NodeFormOverlay | null = useMemo(
        () => parseNodeOverlay(nodeFormSchemaRaw),
        [nodeFormSchemaRaw],
    );

    const rows: FieldRow[] = useMemo(() => {
        if (!definitionFormSchema) return [];
        const overrides = overlay?.fieldOverrides ?? {};
        return definitionFormSchema.fields.map(field => ({
            key: field.key,
            field,
            override: overrides[field.key],
        }));
    }, [definitionFormSchema, overlay]);

    if (!definitionFormSchema || definitionFormSchema.fields.length === 0) {
        return <Empty description={t('components.nodeFormOverlay.empty')} className="!my-4"/>;
    }

    const isCcNode = nodeType === ApprovalFlowNodeType.CC;
    const isApprovalNode = nodeType === ApprovalFlowNodeType.APPROVAL;

    const emitOverlayUpdate = (fieldKey: string, patch: Partial<ApprovalFieldOverride>) => {
        const current = overlay?.fieldOverrides ?? {};
        const merged = pruneOverride({...current[fieldKey], ...patch});
        const nextOverrides: Record<string, ApprovalFieldOverride> = {...current};
        if (merged) {
            nextOverrides[fieldKey] = merged;
        } else {
            delete nextOverrides[fieldKey];
        }
        if (Object.keys(nextOverrides).length === 0) {
            onOverlayChange('');
            return;
        }
        const next: NodeFormOverlay = {
            version: overlay?.version ?? APPROVAL_FORM_SCHEMA_VERSION,
            fieldOverrides: nextOverrides,
        };
        onOverlayChange(stringifyNodeOverlay(next));
    };

    /**
     * Resolve the actual value shown on the switch for a given (field, dimension). This mirrors
     * `mergeFieldOverrides`'s runtime rules so the operator sees the exact truth the renderer
     * will use at approval time.
     */
    const resolvedFor = (row: FieldRow, dimension: OverrideKey): boolean => {
        const {field, override} = row;
        if (dimension === 'readonly' && isCcNode) return true;
        if (dimension === 'readonly' && isApprovalNode) return override?.readonly ?? true;
        const overrideValue = override?.[dimension];
        if (overrideValue !== undefined) return overrideValue;
        return field[dimension];
    };

    /**
     * When the operator flips a switch, we set an explicit override. But if they toggle it back
     * to the definition default we clear the override (patch value = undefined) so the overlay
     * stays minimal — this is what makes future schema edits "just work" without stale overrides.
     */
    const handleToggle = (row: FieldRow, dimension: OverrideKey, next: boolean) => {
        if (dimension === 'readonly' && isCcNode) return; // locked
        const defaultValue = dimension === 'readonly' && isApprovalNode ? true : row.field[dimension];
        const patch: Partial<ApprovalFieldOverride> =
            next === defaultValue
                ? {[dimension]: undefined}
                : {[dimension]: next};
        emitOverlayUpdate(row.key, patch);
    };

    const columns: ColumnsType<FieldRow> = [
        {
            title: t('components.nodeFormOverlay.column.field'),
            key: 'field',
            render: (_, row) => (
                <div className="flex flex-col">
                    <Typography.Text ellipsis className="!text-sm">
                        {row.field.label || row.field.key}
                    </Typography.Text>
                    <div className="flex items-center gap-1 mt-0.5">
                        <Typography.Text type="secondary" className="!text-xs font-mono">
                            {row.field.key}
                        </Typography.Text>
                        <Tag color="blue" className="!m-0 !text-[10px]">
                            {getApprovalFieldType(row.field.type as ApprovalFieldType)}
                        </Tag>
                    </div>
                </div>
            ),
        },
        {
            title: t('components.nodeFormOverlay.column.visible'),
            key: 'visible',
            width: 90,
            align: 'center',
            render: (_, row) => (
                <Switch
                    checked={resolvedFor(row, 'visible')}
                    onChange={(v) => handleToggle(row, 'visible', v)}
                />
            ),
        },
        {
            title: t('components.nodeFormOverlay.column.readonly'),
            key: 'readonly',
            width: 90,
            align: 'center',
            render: (_, row) => {
                const disabled = isCcNode;
                const switchEl = (
                    <Switch
                        checked={resolvedFor(row, 'readonly')}
                        disabled={disabled}
                        onChange={(v) => handleToggle(row, 'readonly', v)}
                    />
                );
                return disabled ? (
                    <Tooltip title={t('components.nodeFormOverlay.ccLockedTooltip')}>{switchEl}</Tooltip>
                ) : switchEl;
            },
        },
        {
            title: t('components.nodeFormOverlay.column.required'),
            key: 'required',
            width: 90,
            align: 'center',
            render: (_, row) => (
                <Switch
                    checked={resolvedFor(row, 'required')}
                    onChange={(v) => handleToggle(row, 'required', v)}
                />
            ),
        },
    ];

    return (
        <div className="flex flex-col gap-2">
            <Typography.Title level={5} className="!mb-0">
                {t('components.nodeFormOverlay.title')}
            </Typography.Title>
            <Typography.Text type="secondary" className="text-xs">
                {t('components.nodeFormOverlay.subtitle')}
            </Typography.Text>
            {isCcNode && (
                <Alert
                    type="info"
                    showIcon
                    message={t('components.nodeFormOverlay.ccInfo')}
                    className="!py-1 !text-xs"
                />
            )}
            {isApprovalNode && (
                <Alert
                    type="info"
                    showIcon
                    message={t('components.nodeFormOverlay.approvalInfo')}
                    className="!py-1 !text-xs"
                />
            )}
            <Table
                size="small"
                rowKey="key"
                columns={columns}
                dataSource={rows}
                pagination={false}
            />
        </div>
    );
}
