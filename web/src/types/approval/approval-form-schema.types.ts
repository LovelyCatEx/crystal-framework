/**
 * Approval form schema types. Persisted as JSON strings inside
 * `definition.formSchema` and `node.formSchema` (see backend `ApprovalFlowDefinitionEntity`
 * / `ApprovalFlowNodeEntity`). Runtime values live in `instance.formData` / `task.formData`
 * as flat `Record<string, unknown>` keyed by `ApprovalFieldSchema.key`.
 *
 * See `.claude/research/approval-form-design-decisions.md` for the frozen contract and the
 * seven confirmed design decisions.
 */

import {ApprovalFieldType} from "@/types/approval/approval-enums.ts";

/** Regex enforcing camelCase for field keys. Empty string / underscore prefix / non-alphanumeric all rejected. */
export const APPROVAL_FIELD_KEY_PATTERN = /^[a-z][a-zA-Z0-9]*$/;

/** Current schema version. Bump if the JSON contract changes in a non-backward-compatible way. */
export const APPROVAL_FORM_SCHEMA_VERSION = 1;

/** One selectable option, used by SELECT / RADIO / CHECKBOX field types. */
export interface ApprovalFieldOption {
    value: string;
    label: string;
}

/**
 * Per-field validation constraints. Fields not applicable to the current type are ignored
 * silently by the renderer / validator — a NUMBER field will ignore `pattern`, etc.
 */
export interface ApprovalFieldValidation {
    /** TEXT / TEXTAREA */
    maxLength?: number;
    /** TEXT — regex source string, applied via `new RegExp(pattern)` */
    pattern?: string;
    /** NUMBER */
    min?: number;
    /** NUMBER */
    max?: number;
    /** NUMBER — decimal places */
    precision?: number;
    /** CHECKBOX */
    minCount?: number;
    /** CHECKBOX */
    maxCount?: number;
    /** DATE / DATETIME — ISO 8601 string */
    minDate?: string;
    /** DATE / DATETIME — ISO 8601 string */
    maxDate?: string;
    /** SELECT — multi-select mode */
    multiple?: boolean;
}

/** One field in the definition-level schema. */
export interface ApprovalFieldSchema {
    /** camelCase, unique within the schema. Matches `APPROVAL_FIELD_KEY_PATTERN`. */
    key: string;
    type: ApprovalFieldType;
    label: string;
    description?: string | null;
    placeholder?: string | null;
    defaultValue?: unknown;
    required: boolean;
    readonly: boolean;
    visible: boolean;
    validation?: ApprovalFieldValidation;
    /** Required for SELECT / RADIO / CHECKBOX; null / omitted for other types. */
    options?: ApprovalFieldOption[] | null;
    /** Optional group membership; must reference `ApprovalFormSchema.groups[].key` if set. */
    groupKey?: string | null;
}

/** Optional one-level grouping for layout. Nested groups are intentionally not allowed. */
export interface ApprovalFieldGroup {
    key: string;
    label: string;
}

/** Definition-level schema. Stored serialized in `ApprovalFlowDefinitionEntity.formSchema`. */
export interface ApprovalFormSchema {
    version: number;
    fields: ApprovalFieldSchema[];
    groups?: ApprovalFieldGroup[];
}

/**
 * Per-field override applied by a node. Only the three visibility flags are mutable — a
 * node cannot introduce new fields or change a field's type / validation / options (see
 * decision 7 in the research doc). Missing properties inherit from the definition-level
 * schema.
 */
export interface ApprovalFieldOverride {
    visible?: boolean;
    readonly?: boolean;
    required?: boolean;
}

/** Node-level overlay. Stored serialized in `ApprovalFlowNodeEntity.formSchema`. */
export interface NodeFormOverlay {
    version: number;
    fieldOverrides: Record<string, ApprovalFieldOverride>;
}

/**
 * Runtime-computed field schema, produced by `mergeFieldOverrides`. Identical to
 * `ApprovalFieldSchema` shape-wise so renderers can consume it uniformly; the three
 * visibility flags are guaranteed non-null after merging.
 */
export type MergedFieldSchema = Omit<ApprovalFieldSchema, 'required' | 'readonly' | 'visible'> & {
    required: boolean;
    readonly: boolean;
    visible: boolean;
};

/**
 * Which CONDITION operators are semantically valid for each field type. Enforced on the
 * frontend when building CONDITION routes and on the backend inside `ApprovalFlowGraphValidator`
 * (see decision 4).
 *
 * The string values match backend `ConditionOperator` (`eq` / `ne` / `gt` / `gte` / `lt` /
 * `lte` / `contains` / `in`).
 */
export const APPROVAL_FIELD_OPERATORS: Record<ApprovalFieldType, readonly string[]> = {
    [ApprovalFieldType.TEXT]:     ['eq', 'ne', 'contains'],
    [ApprovalFieldType.TEXTAREA]: ['contains'],
    [ApprovalFieldType.NUMBER]:   ['eq', 'ne', 'gt', 'gte', 'lt', 'lte'],
    [ApprovalFieldType.BOOLEAN]:  ['eq', 'ne'],
    [ApprovalFieldType.SELECT]:   ['eq', 'ne', 'in'],
    [ApprovalFieldType.RADIO]:    ['eq', 'ne', 'in'],
    [ApprovalFieldType.CHECKBOX]: ['contains', 'in'],
    [ApprovalFieldType.DATE]:     ['eq', 'ne', 'gt', 'gte', 'lt', 'lte'],
    [ApprovalFieldType.DATETIME]: ['eq', 'ne', 'gt', 'gte', 'lt', 'lte'],
} as const;
