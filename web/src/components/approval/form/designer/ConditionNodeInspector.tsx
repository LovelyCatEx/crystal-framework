import {Alert, Button, Card, Empty, InputNumber, Radio, Select, Space, Typography} from "antd";
import {DeleteOutlined, PlusOutlined} from "@ant-design/icons";
import {useMemo} from "react";
import {useTranslation} from "react-i18next";
import type {
    ConditionLeaf,
    ConditionNode,
    ConditionNodeConfig,
    ConditionRoute,
} from "@/types/approval/approval-node-config.ts";
import {
    ApprovalFieldType,
    ApprovalFlowNodeType,
    ConditionLogic,
    ConditionOperator,
} from "@/types/approval/approval-enums.ts";
import type {
    ApprovalFieldSchema,
    ApprovalFormSchema,
} from "@/types/approval/approval-form-schema.types.ts";
import {APPROVAL_FIELD_OPERATORS} from "@/types/approval/approval-form-schema.types.ts";
import type {ApprovalFlowNode} from "@/types/approval/approval-flow-node.types.ts";
import {getApprovalFieldType} from "@/i18n/enum-helpers.ts";

export interface ConditionNodeInspectorProps {
    /** The CONDITION node whose config we're editing. Only its `config` string is touched. */
    node: ApprovalFlowNode;
    /**
     * Definition-level schema — drives the field / operator dropdowns. When null the inspector
     * shows a guided-empty state (schema must exist before conditions are meaningful, see rule 39).
     */
    definitionFormSchema: ApprovalFormSchema | null;
    /** All rete nodes in the current graph — used to populate the target-node dropdown. */
    graphNodes: ApprovalFlowNode[];
    /**
     * Called with the new stringified [ConditionNodeConfig]. Passing through `onNodeChange('config', ...)`
     * on the parent side reuses the same persistence path CONFIG uses for APPROVAL / CC nodes.
     */
    onConfigChange: (nextConfigJson: string) => void;
}

/**
 * Cheaper than deep-tree UX: the inspector guarantees each route's condition is a single
 * [ConditionGroup] at the root with only [ConditionLeaf] children. This matches the vast
 * majority of real-world "if field A ≥ X and field B == Y then route to Z" needs without
 * asking the user to hand-build nested trees. If nested groups are ever needed, this file
 * grows a recursive renderer — the persisted JSON shape already supports it.
 */
interface FlatConditionGroup {
    type: 'group';
    logic: ConditionLogic;
    children: ConditionLeaf[];
}

function ensureGroupShape(condition: ConditionNode | undefined): FlatConditionGroup {
    if (condition && condition.type === 'group') {
        return {
            type: 'group',
            logic: condition.logic,
            children: condition.children.filter((c): c is ConditionLeaf => c.type === 'condition'),
        };
    }
    if (condition && condition.type === 'condition') {
        return {type: 'group', logic: ConditionLogic.AND, children: [condition]};
    }
    return {type: 'group', logic: ConditionLogic.AND, children: []};
}

function parseConfig(node: ApprovalFlowNode): ConditionNodeConfig {
    if (!node.config) return {routes: []};
    try {
        const parsed = JSON.parse(node.config) as Partial<ConditionNodeConfig>;
        return {routes: Array.isArray(parsed.routes) ? parsed.routes : []};
    } catch {
        return {routes: []};
    }
}

/** Emit a fresh empty leaf for the current schema — falls back to string EQ when no field is available. */
function newLeaf(schema: ApprovalFormSchema | null): ConditionLeaf {
    const first = schema?.fields[0];
    return {
        type: 'condition',
        field: first?.key ?? '',
        operator: first ? (APPROVAL_FIELD_OPERATORS[first.type][0] as ConditionOperator) : ConditionOperator.EQ,
        value: '',
    };
}

export function ConditionNodeInspector(props: ConditionNodeInspectorProps) {
    const {node, definitionFormSchema, graphNodes, onConfigChange} = props;
    const {t} = useTranslation();

    const config = useMemo(() => parseConfig(node), [node.config]);

    // Only unsaved CONDITION nodes have id === '' (see ApprovalEditor's emptyNode). Target
    // dropdowns can only reference nodes with a persisted id — warn the user until they save.
    const targetableNodes = graphNodes.filter(n =>
        n.id
        && n.id !== node.id
        && n.type !== ApprovalFlowNodeType.START
    );
    const fieldByKey = useMemo(() => {
        const map = new Map<string, ApprovalFieldSchema>();
        for (const f of definitionFormSchema?.fields ?? []) map.set(f.key, f);
        return map;
    }, [definitionFormSchema]);

    if (!definitionFormSchema || definitionFormSchema.fields.length === 0) {
        return <Empty description={t('components.conditionNodeInspector.noSchemaHint')}/>;
    }

    const emit = (nextRoutes: ConditionRoute[]) => {
        onConfigChange(JSON.stringify({routes: nextRoutes}));
    };

    const addRoute = () => {
        const nextRoutes: ConditionRoute[] = [
            ...config.routes,
            {
                targetNodeId: '',
                condition: {
                    type: 'group',
                    logic: ConditionLogic.AND,
                    children: [newLeaf(definitionFormSchema)],
                },
            },
        ];
        emit(nextRoutes);
    };

    const patchRoute = (index: number, partial: Partial<ConditionRoute>) => {
        emit(config.routes.map((r, i) => i === index ? {...r, ...partial} : r));
    };

    const removeRoute = (index: number) => {
        emit(config.routes.filter((_, i) => i !== index));
    };

    const patchLeaf = (routeIndex: number, leafIndex: number, partial: Partial<ConditionLeaf>) => {
        const route = config.routes[routeIndex];
        const group = ensureGroupShape(route.condition);
        const nextLeaves = group.children.map((l, i) => i === leafIndex ? {...l, ...partial} : l);
        patchRoute(routeIndex, {
            condition: {type: 'group', logic: group.logic, children: nextLeaves},
        });
    };

    const addLeaf = (routeIndex: number) => {
        const route = config.routes[routeIndex];
        const group = ensureGroupShape(route.condition);
        patchRoute(routeIndex, {
            condition: {
                type: 'group',
                logic: group.logic,
                children: [...group.children, newLeaf(definitionFormSchema)],
            },
        });
    };

    const removeLeaf = (routeIndex: number, leafIndex: number) => {
        const route = config.routes[routeIndex];
        const group = ensureGroupShape(route.condition);
        patchRoute(routeIndex, {
            condition: {
                type: 'group',
                logic: group.logic,
                children: group.children.filter((_, i) => i !== leafIndex),
            },
        });
    };

    const changeGroupLogic = (routeIndex: number, logic: ConditionLogic) => {
        const route = config.routes[routeIndex];
        const group = ensureGroupShape(route.condition);
        patchRoute(routeIndex, {
            condition: {type: 'group', logic, children: group.children},
        });
    };

    const fieldOptions = (definitionFormSchema?.fields ?? []).map(f => ({
        value: f.key,
        label: `${f.label || f.key} (${getApprovalFieldType(f.type as ApprovalFieldType)})`,
    }));

    const targetNodeOptions = targetableNodes.map(n => ({
        value: n.id,
        label: n.name || n.nodeKey || n.id,
    }));

    return (
        <div className="flex flex-col gap-3">
            <Typography.Title level={5} className="!mb-0">
                {t('components.conditionNodeInspector.title')}
            </Typography.Title>
            <Typography.Text type="secondary" className="text-xs">
                {t('components.conditionNodeInspector.subtitle')}
            </Typography.Text>

            {graphNodes.some(n => !n.id) && (
                <Alert
                    type="warning"
                    showIcon
                    className="!py-1 !text-xs"
                    message={t('components.conditionNodeInspector.unsavedNodesWarning')}
                />
            )}

            {config.routes.length === 0 ? (
                <Empty description={t('components.conditionNodeInspector.emptyRoutes')}/>
            ) : (
                config.routes.map((route, routeIndex) => {
                    const group = ensureGroupShape(route.condition);
                    return (
                        <Card
                            key={routeIndex}
                            size="small"
                            title={t('components.conditionNodeInspector.routeTitle', {n: routeIndex + 1})}
                            extra={
                                <Button
                                    type="text"
                                    danger
                                    size="small"
                                    icon={<DeleteOutlined/>}
                                    onClick={() => removeRoute(routeIndex)}
                                >
                                    {t('components.conditionNodeInspector.deleteRoute')}
                                </Button>
                            }
                        >
                            <div className="flex flex-col gap-2">
                                <div className="flex items-center gap-2">
                                    <Typography.Text className="!text-xs">
                                        {t('components.conditionNodeInspector.target')}:
                                    </Typography.Text>
                                    <Select
                                        size="small"
                                        className="flex-1"
                                        placeholder={t('components.conditionNodeInspector.selectTarget')}
                                        value={route.targetNodeId || undefined}
                                        options={targetNodeOptions}
                                        onChange={(v) => patchRoute(routeIndex, {targetNodeId: v})}
                                    />
                                </div>
                                <div className="flex items-center gap-2">
                                    <Typography.Text className="!text-xs">
                                        {t('components.conditionNodeInspector.logic')}:
                                    </Typography.Text>
                                    <Radio.Group
                                        size="small"
                                        value={group.logic}
                                        onChange={(e) => changeGroupLogic(routeIndex, e.target.value)}
                                    >
                                        <Radio.Button value={ConditionLogic.AND}>AND</Radio.Button>
                                        <Radio.Button value={ConditionLogic.OR}>OR</Radio.Button>
                                    </Radio.Group>
                                </div>

                                {group.children.length === 0 ? (
                                    <Empty description={t('components.conditionNodeInspector.emptyLeaves')}/>
                                ) : (
                                    group.children.map((leaf, leafIndex) => (
                                        <LeafRow
                                            key={leafIndex}
                                            leaf={leaf}
                                            fieldByKey={fieldByKey}
                                            fieldOptions={fieldOptions}
                                            onChange={(patch) => patchLeaf(routeIndex, leafIndex, patch)}
                                            onDelete={() => removeLeaf(routeIndex, leafIndex)}
                                            deleteAriaLabel={t('components.conditionNodeInspector.deleteLeaf')}
                                        />
                                    ))
                                )}

                                <Button
                                    type="dashed"
                                    size="small"
                                    icon={<PlusOutlined/>}
                                    onClick={() => addLeaf(routeIndex)}
                                >
                                    {t('components.conditionNodeInspector.addLeaf')}
                                </Button>
                            </div>
                        </Card>
                    );
                })
            )}

            <Space>
                <Button type="primary" icon={<PlusOutlined/>} onClick={addRoute}>
                    {t('components.conditionNodeInspector.addRoute')}
                </Button>
            </Space>
        </div>
    );
}

interface LeafRowProps {
    leaf: ConditionLeaf;
    fieldByKey: Map<string, ApprovalFieldSchema>;
    fieldOptions: Array<{value: string; label: string}>;
    onChange: (patch: Partial<ConditionLeaf>) => void;
    onDelete: () => void;
    deleteAriaLabel: string;
}

function LeafRow({leaf, fieldByKey, fieldOptions, onChange, onDelete, deleteAriaLabel}: LeafRowProps) {
    const field = fieldByKey.get(leaf.field);
    const allowedOperators = field ? APPROVAL_FIELD_OPERATORS[field.type as ApprovalFieldType] : [];
    const operatorOptions = allowedOperators.map(op => ({value: op, label: op.toUpperCase()}));

    const changeField = (nextField: string) => {
        const nextFieldSchema = fieldByKey.get(nextField);
        const nextAllowed = nextFieldSchema ? APPROVAL_FIELD_OPERATORS[nextFieldSchema.type as ApprovalFieldType] : [];
        const nextOperator = (nextAllowed[0] ?? ConditionOperator.EQ) as ConditionOperator;
        onChange({field: nextField, operator: nextOperator, value: '', values: undefined});
    };

    return (
        <div className="flex items-center gap-1">
            <Select
                size="small"
                className="!w-44"
                value={leaf.field || undefined}
                options={fieldOptions}
                onChange={changeField}
            />
            <Select
                size="small"
                className="!w-24"
                value={leaf.operator}
                options={operatorOptions}
                onChange={(v) => onChange({operator: v as ConditionOperator})}
                disabled={!field}
            />
            <LeafValueEditor
                field={field}
                operator={leaf.operator}
                value={leaf.value}
                values={leaf.values}
                onChange={(patch) => onChange(patch)}
            />
            <Button
                type="text"
                size="small"
                danger
                icon={<DeleteOutlined/>}
                onClick={onDelete}
                aria-label={deleteAriaLabel}
            />
        </div>
    );
}

interface LeafValueEditorProps {
    field: ApprovalFieldSchema | undefined;
    operator: ConditionOperator;
    value: unknown;
    values: unknown[] | undefined;
    onChange: (patch: Partial<ConditionLeaf>) => void;
}

/**
 * Value input dispatcher. Chooses the right control based on `field.type` and whether the
 * current operator is a set-membership one (`IN`). Options types show a select; NUMBER
 * uses InputNumber; BOOLEAN a Switch-flavored select; the rest fall back to a plain text
 * select-free input. Keeps things minimal — power-users can hand-edit the raw JSON via the
 * inspector's collapsible view.
 */
function LeafValueEditor({field, operator, value, values, onChange}: LeafValueEditorProps) {
    if (!field) return <span className="text-xs text-gray-400">-</span>;

    const isInOperator = operator === ConditionOperator.IN;

    if (field.type === ApprovalFieldType.BOOLEAN) {
        return (
            <Select
                size="small"
                className="!w-28"
                value={typeof value === 'boolean' ? String(value) : undefined}
                options={[{value: 'true', label: 'true'}, {value: 'false', label: 'false'}]}
                onChange={(v) => onChange({value: v === 'true'})}
            />
        );
    }

    if (field.type === ApprovalFieldType.NUMBER) {
        return (
            <InputNumber
                size="small"
                className="!w-32"
                value={typeof value === 'number' ? value : null}
                onChange={(v) => onChange({value: v ?? null})}
            />
        );
    }

    const isOptionField =
        field.type === ApprovalFieldType.SELECT
        || field.type === ApprovalFieldType.RADIO
        || field.type === ApprovalFieldType.CHECKBOX;
    if (isOptionField) {
        const opts = (field.options ?? []).map(o => ({value: o.value, label: o.label}));
        if (isInOperator) {
            return (
                <Select
                    size="small"
                    className="!min-w-36"
                    mode="multiple"
                    value={(values ?? []) as string[]}
                    options={opts}
                    onChange={(v) => onChange({values: v as unknown[], value: undefined})}
                />
            );
        }
        return (
            <Select
                size="small"
                className="!min-w-36"
                value={typeof value === 'string' ? value : undefined}
                options={opts}
                onChange={(v) => onChange({value: v})}
            />
        );
    }

    // Fallback: string input for TEXT / TEXTAREA / DATE / DATETIME. Dates persist as
    // ISO strings by contract, so a plain string editor is fine here.
    if (isInOperator) {
        return (
            <Select
                size="small"
                className="!min-w-36"
                mode="tags"
                value={(values ?? []) as string[]}
                onChange={(v) => onChange({values: v as unknown[], value: undefined})}
            />
        );
    }
    return (
        <Select
            size="small"
            className="!min-w-36"
            mode="tags"
            maxCount={1}
            value={value ? [String(value)] : []}
            onChange={(v) => onChange({value: v[0] ?? ''})}
        />
    );
}
