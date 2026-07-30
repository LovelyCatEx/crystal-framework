import {Divider, Empty, Typography} from "antd";
import {useMemo} from "react";
import {useTranslation} from "react-i18next";
import {ApprovalFieldType} from "@/types/approval/approval-enums.ts";
import type {
    ApprovalFieldSchema,
    ApprovalFormSchema,
} from "@/types/approval/approval-form-schema.types.ts";
import {
    APPROVAL_FIELD_KEY_PATTERN,
    APPROVAL_FORM_SCHEMA_VERSION,
} from "@/types/approval/approval-form-schema.types.ts";
import {ApprovalFormFieldList} from "./ApprovalFormFieldList.tsx";
import {ApprovalFieldPropertyPanel} from "./ApprovalFieldPropertyPanel.tsx";

export interface ApprovalFormDesignerProps {
    /** The current definition-level schema, or null when the definition has none yet. */
    schema: ApprovalFormSchema | null;
    /** Currently selected field key (drives the property panel). Controlled by the parent. */
    selectedKey: string | null;
    onSelectedKeyChange: (key: string | null) => void;
    /**
     * Fired after any mutation (field add/delete/reorder/property edit, group edit). The parent
     * owns persistence — for definition-level schemas the parent normally saves via the standard
     * `ApprovalFlowDefinitionManagerController.update({ id, formSchema })` (decision 3).
     */
    onSchemaChange: (schema: ApprovalFormSchema) => void;
    /**
     * Optional right-hand-side content in the header row (e.g. a Save button). Placed inline
     * with the title/subtitle so parents don't need a separate row for their action affordance.
     */
    headerRight?: React.ReactNode;
}

const DEFAULT_KEY_PREFIX = 'field';

/** Generate a unique camelCase key like `field1`, `field2`, ... to seed a newly added field. */
function nextDefaultKey(existing: ApprovalFieldSchema[]): string {
    const taken = new Set(existing.map(f => f.key));
    let i = 1;
    while (taken.has(`${DEFAULT_KEY_PREFIX}${i}`)) i++;
    return `${DEFAULT_KEY_PREFIX}${i}`;
}

/**
 * Whole-field validation status used to red-outline entries in the field list. Mirrors the
 * per-field checks in `ApprovalFieldPropertyPanel` so the two views stay in agreement.
 */
function computeInvalidKeys(fields: ApprovalFieldSchema[]): Set<string> {
    const invalid = new Set<string>();
    const seen = new Map<string, number>();
    for (const f of fields) {
        if (!f.key.trim() || !APPROVAL_FIELD_KEY_PATTERN.test(f.key)) {
            invalid.add(f.key);
        }
        seen.set(f.key, (seen.get(f.key) ?? 0) + 1);
    }
    for (const [k, count] of seen) {
        if (count > 1) invalid.add(k);
    }
    return invalid;
}

export function ApprovalFormDesigner(props: ApprovalFormDesignerProps) {
    const {schema, selectedKey, onSelectedKeyChange, onSchemaChange, headerRight} = props;
    const {t} = useTranslation();

    const effectiveSchema: ApprovalFormSchema = schema ?? {
        version: APPROVAL_FORM_SCHEMA_VERSION,
        fields: [],
        groups: [],
    };

    const invalidKeys = useMemo(() => computeInvalidKeys(effectiveSchema.fields), [effectiveSchema.fields]);

    const emit = (partial: Partial<ApprovalFormSchema>) => {
        onSchemaChange({...effectiveSchema, ...partial});
    };

    const handleAdd = () => {
        const newKey = nextDefaultKey(effectiveSchema.fields);
        const newField: ApprovalFieldSchema = {
            key: newKey,
            type: ApprovalFieldType.TEXT,
            label: newKey,
            description: null,
            placeholder: null,
            defaultValue: null,
            required: false,
            readonly: false,
            visible: true,
            validation: {},
            options: null,
            groupKey: null,
        };
        emit({fields: [...effectiveSchema.fields, newField]});
        onSelectedKeyChange(newKey);
    };

    const handleDelete = (key: string) => {
        emit({fields: effectiveSchema.fields.filter(f => f.key !== key)});
        if (selectedKey === key) onSelectedKeyChange(null);
    };

    const handleReorder = (fields: ApprovalFieldSchema[]) => {
        emit({fields});
    };

    const handleFieldChange = (updated: ApprovalFieldSchema, originalKey: string) => {
        // Special-case key rename: keep list position, but if the *selected* field is the one
        // whose key changed, follow it so the property panel keeps editing the same entry.
        const nextFields = effectiveSchema.fields.map(f => f.key === originalKey ? updated : f);
        emit({fields: nextFields});
        if (selectedKey === originalKey && updated.key !== originalKey) {
            onSelectedKeyChange(updated.key);
        }
    };

    const selectedField = selectedKey
        ? effectiveSchema.fields.find(f => f.key === selectedKey) ?? null
        : null;

    return (
        <div className="flex flex-col gap-3">
            <div className="flex items-start justify-between gap-3">
                <div className="min-w-0">
                    <Typography.Title level={5} className="!mb-0">
                        {t('components.approvalFormDesigner.title')}
                    </Typography.Title>
                    <Typography.Text type="secondary" className="text-xs">
                        {t('components.approvalFormDesigner.subtitle')}
                    </Typography.Text>
                </div>
                {headerRight && <div className="shrink-0">{headerRight}</div>}
            </div>

            <ApprovalFormFieldList
                fields={effectiveSchema.fields}
                selectedKey={selectedKey}
                invalidKeys={invalidKeys}
                onSelect={onSelectedKeyChange}
                onDelete={handleDelete}
                onReorder={handleReorder}
                onAdd={handleAdd}
            />

            <Divider className="!my-2"/>

            {selectedField ? (
                <ApprovalFieldPropertyPanel
                    key={selectedField.key}
                    field={selectedField}
                    allFields={effectiveSchema.fields}
                    groups={effectiveSchema.groups}
                    onChange={(updated) => handleFieldChange(updated, selectedField.key)}
                />
            ) : (
                <Empty description={t('components.approvalFormDesigner.property.empty')}/>
            )}
        </div>
    );
}
