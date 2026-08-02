import {Empty, Form, Typography} from "antd";
import type {Rule} from "antd/es/form";
import {forwardRef, useEffect, useImperativeHandle, useMemo} from "react";
import {useTranslation} from "react-i18next";
import type {MergedFieldSchema} from "@/types/approval/approval-form-schema.types.ts";
import {renderField} from "./fields/renderField.tsx";
import type {DictOptionsLoader} from "./fields/DictFieldRenderer.tsx";
import {ApprovalFieldType} from "@/types/approval/approval-enums.ts";

export interface ApprovalFormRendererProps {
    /** Merged schema — usually produced by `mergeFieldOverrides` in `approval-form-utils.ts`. */
    fields: MergedFieldSchema[];
    /** Optional group definitions matching `field.groupKey`. Unknown groupKeys are rendered ungrouped. */
    groups?: Array<{key: string; label: string}>;
    /** Initial form values keyed by `field.key`. */
    initialValues?: Record<string, unknown>;
    /**
     * Called whenever any field value changes. Receives the full current values map,
     * matching antd `Form.onValuesChange`'s second argument.
     */
    onValuesChange?: (values: Record<string, unknown>) => void;
    /**
     * Force every field to be non-editable regardless of per-field `readonly`. Used by
     * the viewer's form snapshot tab (M8).
     */
    readonly?: boolean;
    /**
     * Loader for DICT field options. Consumers supply a definition- or instance-scoped loader;
     * scope is resolved server-side. Omitted in contexts with no DICT fields (options stay empty).
     */
    loadDictOptions?: DictOptionsLoader;
}

export interface ApprovalFormRendererRef {
    /** Runs antd Form validation. Rejects when any field fails. Resolves with current values. */
    validate: () => Promise<Record<string, unknown>>;
    /** Snapshot of current form values without triggering validation. */
    getValues: () => Record<string, unknown>;
    /** Resets values back to `initialValues`. */
    reset: () => void;
}

interface RenderableField {
    field: MergedFieldSchema;
}

function buildRules(field: MergedFieldSchema, t: (key: string) => string): Rule[] {
    const rules: Rule[] = [];

    if (field.required) {
        rules.push({required: true, message: t('components.approvalFormRenderer.error.required')});
    }

    const validation = field.validation;
    if (!validation) return rules;

    switch (field.type) {
        case ApprovalFieldType.TEXT:
        case ApprovalFieldType.TEXTAREA:
            if (validation.maxLength !== undefined) {
                rules.push({max: validation.maxLength, type: 'string', message: t('components.approvalFormRenderer.error.maxLength')});
            }
            if (validation.pattern) {
                rules.push({pattern: new RegExp(validation.pattern), message: t('components.approvalFormRenderer.error.pattern')});
            }
            break;
        case ApprovalFieldType.NUMBER:
            if (validation.min !== undefined) {
                rules.push({type: 'number', min: validation.min, message: t('components.approvalFormRenderer.error.min')});
            }
            if (validation.max !== undefined) {
                rules.push({type: 'number', max: validation.max, message: t('components.approvalFormRenderer.error.max')});
            }
            break;
        case ApprovalFieldType.CHECKBOX:
            if (validation.minCount !== undefined) {
                rules.push({
                    validator: async (_, value: unknown) => {
                        const arr = Array.isArray(value) ? value : [];
                        if (arr.length < validation.minCount!) throw new Error(t('components.approvalFormRenderer.error.minCount'));
                    },
                });
            }
            if (validation.maxCount !== undefined) {
                rules.push({
                    validator: async (_, value: unknown) => {
                        const arr = Array.isArray(value) ? value : [];
                        if (arr.length > validation.maxCount!) throw new Error(t('components.approvalFormRenderer.error.maxCount'));
                    },
                });
            }
            break;
    }

    return rules;
}

/**
 * Renders an antd Form driven by a merged schema. Fields with `visible === false` are
 * skipped entirely. Grouped fields (`groupKey` set and matched to a `groups[]` entry)
 * are rendered under their group header; the rest fall into the "ungrouped" bucket
 * shown first.
 *
 * Ref exposes `validate()` / `getValues()` / `reset()` for parent orchestration —
 * used by the initiate modal (M3) and the approval handle modal (M4) to gate submit.
 */
function ApprovalFormRendererInner(
    props: ApprovalFormRendererProps,
    ref: React.Ref<ApprovalFormRendererRef>,
) {
    const {fields, groups, initialValues, onValuesChange, readonly, loadDictOptions} = props;
    const {t} = useTranslation();
    const [form] = Form.useForm();

    const effectiveFields = useMemo<MergedFieldSchema[]>(
        () => fields
            .filter(f => f.visible)
            .map(f => readonly ? {...f, readonly: true} : f),
        [fields, readonly],
    );

    useEffect(() => {
        form.resetFields();
        if (initialValues) form.setFieldsValue(initialValues);
    }, [form, initialValues]);

    useImperativeHandle(ref, () => ({
        validate: async () => {
            await form.validateFields();
            return form.getFieldsValue();
        },
        getValues: () => form.getFieldsValue(),
        reset: () => {
            form.resetFields();
            if (initialValues) form.setFieldsValue(initialValues);
        },
    }), [form, initialValues]);

    const grouped = useMemo(() => {
        const ungrouped: RenderableField[] = [];
        const byGroup = new Map<string, RenderableField[]>();
        const groupKeys = new Set((groups ?? []).map(g => g.key));
        for (const field of effectiveFields) {
            const gk = field.groupKey;
            if (gk && groupKeys.has(gk)) {
                if (!byGroup.has(gk)) byGroup.set(gk, []);
                byGroup.get(gk)!.push({field});
            } else {
                ungrouped.push({field});
            }
        }
        return {ungrouped, byGroup};
    }, [effectiveFields, groups]);

    if (effectiveFields.length === 0) {
        return <Empty description={t('components.approvalFormRenderer.empty')}/>;
    }

    const renderOne = ({field}: RenderableField) => (
        <Form.Item
            key={field.key}
            name={field.key}
            label={field.label}
            tooltip={field.description ?? undefined}
            rules={buildRules(field, t)}
            valuePropName={field.type === ApprovalFieldType.BOOLEAN ? 'checked' : 'value'}
        >
            {renderField({field, loadDictOptions})}
        </Form.Item>
    );

    return (
        <Form
            form={form}
            layout="vertical"
            initialValues={initialValues}
            onValuesChange={(_, all) => onValuesChange?.(all)}
            disabled={readonly}
        >
            {grouped.ungrouped.map(renderOne)}
            {(groups ?? []).map(group => {
                const groupFields = grouped.byGroup.get(group.key);
                if (!groupFields || groupFields.length === 0) return null;
                return (
                    <div key={group.key} className="mb-2">
                        <Typography.Title level={5} className="!mb-3">
                            {group.label}
                        </Typography.Title>
                        {groupFields.map(renderOne)}
                    </div>
                );
            })}
        </Form>
    );
}

export const ApprovalFormRenderer = forwardRef<ApprovalFormRendererRef, ApprovalFormRendererProps>(ApprovalFormRendererInner);
