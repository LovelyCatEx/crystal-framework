/**
 * Utilities backing the approval form schema system (M1).
 *
 * Covers three responsibilities:
 *   1. Safe JSON <-> schema parsing (rejects malformed persisted values).
 *   2. Merging definition-level schema with node-level overlays into `MergedFieldSchema[]`
 *      (see decision 2: approval nodes default `readonly = true`).
 *   3. Diff / merge helpers for runtime `formData` (see decision 6: task submits diff,
 *      backend merges into instance).
 *
 * Contract frozen in `.claude/research/approval-form-design-decisions.md`.
 */

import {ApprovalFlowNodeType} from "@/types/approval/approval-enums.ts";
import {
    APPROVAL_FORM_SCHEMA_VERSION,
    type ApprovalFieldOverride,
    type ApprovalFieldSchema,
    type ApprovalFormSchema,
    type MergedFieldSchema,
    type NodeFormOverlay,
} from "@/types/approval/approval-form-schema.types.ts";

/**
 * Parse a persisted definition-level schema. Returns `null` for null / empty / malformed
 * input so callers can treat "no schema" and "invalid schema" the same way (skip form).
 */
export function parseFormSchema(raw: string | null | undefined): ApprovalFormSchema | null {
    if (!raw || !raw.trim()) return null;
    try {
        const parsed = JSON.parse(raw) as Partial<ApprovalFormSchema>;
        if (!parsed || !Array.isArray(parsed.fields)) return null;
        return {
            version: typeof parsed.version === 'number' ? parsed.version : APPROVAL_FORM_SCHEMA_VERSION,
            fields: parsed.fields as ApprovalFieldSchema[],
            groups: Array.isArray(parsed.groups) ? parsed.groups : undefined,
        };
    } catch {
        return null;
    }
}

/** Parse a persisted node-level overlay. Same null-tolerance rules as `parseFormSchema`. */
export function parseNodeOverlay(raw: string | null | undefined): NodeFormOverlay | null {
    if (!raw || !raw.trim()) return null;
    try {
        const parsed = JSON.parse(raw) as Partial<NodeFormOverlay>;
        if (!parsed || typeof parsed.fieldOverrides !== 'object' || parsed.fieldOverrides === null) return null;
        return {
            version: typeof parsed.version === 'number' ? parsed.version : APPROVAL_FORM_SCHEMA_VERSION,
            fieldOverrides: parsed.fieldOverrides as Record<string, ApprovalFieldOverride>,
        };
    } catch {
        return null;
    }
}

/** Serialize a definition-level schema for persistence. */
export function stringifyFormSchema(schema: ApprovalFormSchema): string {
    return JSON.stringify(schema);
}

/** Serialize a node-level overlay for persistence. */
export function stringifyNodeOverlay(overlay: NodeFormOverlay): string {
    return JSON.stringify(overlay);
}

/**
 * Merge overlays into the definition schema, producing per-field flags the renderer can
 * consume without re-checking overlays.
 *
 * Rules (decision 2):
 *   - Field not in `overlay.fieldOverrides` -> inherit definition values verbatim.
 *   - Per-property `override.visible / readonly / required` supersede definition values.
 *   - `readonly` falls back to the field's own `field.readonly` baseline when the overlay
 *     does not override it (APPROVAL included) — the definition decides the default.
 *   - CC nodes force `readonly = true` regardless of overlay (CC never mutates data).
 *   - When `finalVisible === false` or `finalReadonly === true`, `finalRequired` is
 *     forced to `false` — an invisible / immutable field cannot be "required" of the user.
 */
export function mergeFieldOverrides(
    definition: ApprovalFormSchema,
    overlay: NodeFormOverlay | null,
    options: { nodeType: number },
): MergedFieldSchema[] {
    const overrides = overlay?.fieldOverrides ?? {};
    const isCcNode = options.nodeType === ApprovalFlowNodeType.CC;

    return definition.fields.map(field => {
        const override = overrides[field.key];

        const finalVisible = override?.visible ?? field.visible;

        // CC nodes never mutate data, so they are hard-locked readonly regardless of overlay.
        // Every other node (APPROVAL included) falls back to the field's own readonly baseline
        // when the node overlay does not override it.
        const finalReadonly: boolean = isCcNode ? true : (override?.readonly ?? field.readonly);

        const rawRequired = override?.required ?? field.required;
        const finalRequired = !finalVisible || finalReadonly ? false : rawRequired;

        return {
            ...field,
            visible: finalVisible,
            readonly: finalReadonly,
            required: finalRequired,
        };
    });
}

/**
 * Shallow structural equality — sufficient for form field values (primitives, arrays,
 * simple objects). Avoids pulling in lodash; the project has no lodash dependency.
 */
export function isFormValueEqual(a: unknown, b: unknown): boolean {
    if (a === b) return true;
    if (a === null || b === null || a === undefined || b === undefined) return false;
    if (typeof a !== typeof b) return false;

    if (Array.isArray(a) && Array.isArray(b)) {
        if (a.length !== b.length) return false;
        for (let i = 0; i < a.length; i++) {
            if (!isFormValueEqual(a[i], b[i])) return false;
        }
        return true;
    }

    if (typeof a === 'object' && typeof b === 'object') {
        const aKeys = Object.keys(a as object);
        const bKeys = Object.keys(b as object);
        if (aKeys.length !== bKeys.length) return false;
        for (const k of aKeys) {
            if (!isFormValueEqual((a as Record<string, unknown>)[k], (b as Record<string, unknown>)[k])) return false;
        }
        return true;
    }

    return false;
}

/**
 * Compute the diff between the initial snapshot and current form values (decision 6).
 * Only keys whose values have changed appear in the returned object; unchanged and
 * absent-from-current keys are dropped. Callers stringify the result and send it as
 * `HandleApprovalFlowTaskDTO.formData`.
 */
export function diffFormData(
    initial: Record<string, unknown>,
    current: Record<string, unknown>,
): Record<string, unknown> {
    const diff: Record<string, unknown> = {};
    for (const key of Object.keys(current)) {
        if (!isFormValueEqual(current[key], initial[key])) {
            diff[key] = current[key];
        }
    }
    return diff;
}

/**
 * Shallow merge of a diff into a base form data map (decision 6). Top-level keys in
 * `diff` overwrite `base`; nested structures are replaced wholesale, not deep-merged —
 * the schema field values are treated as atomic units.
 */
export function mergeFormData(
    base: Record<string, unknown>,
    diff: Record<string, unknown>,
): Record<string, unknown> {
    return {...base, ...diff};
}
