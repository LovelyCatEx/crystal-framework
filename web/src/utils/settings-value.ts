import {SystemSettingsItemValueType} from "@/types/system/system-settings.types.ts";

/**
 * Minimal structural subset of a settings item schema, shared by system / tenant settings.
 */
export interface SettingsValueSchemaLike {
    valueType: string;
    value: string | null;
    isSecret: boolean;
}

const ARRAY_VALUE_TYPES: ReadonlySet<string> = new Set<string>([
    SystemSettingsItemValueType.ENUM_MULTIPLE,
    SystemSettingsItemValueType.STRING_ARRAY,
    SystemSettingsItemValueType.NUMBER_ARRAY,
    SystemSettingsItemValueType.DECIMAL_ARRAY,
    SystemSettingsItemValueType.BOOLEAN_ARRAY,
]);

/**
 * Array types (including ENUM_MULTIPLE) are transported / stored as a JSON string array,
 * and represented as string[] in the form.
 */
export function isArrayValueType(valueType: string): boolean {
    return ARRAY_VALUE_TYPES.has(valueType);
}

function deserializeOne(schema: SettingsValueSchemaLike): unknown {
    if (schema.isSecret) {
        return '';
    }
    const raw = schema.value;
    if (schema.valueType === SystemSettingsItemValueType.BOOLEAN) {
        return raw === 'true';
    }
    if (
        schema.valueType === SystemSettingsItemValueType.NUMBER ||
        schema.valueType === SystemSettingsItemValueType.DECIMAL
    ) {
        if (raw === null || raw === undefined || raw === '') {
            return null;
        }
        const n = Number(raw);
        return Number.isFinite(n) ? n : null;
    }
    if (isArrayValueType(schema.valueType)) {
        if (raw === null || raw === undefined) {
            return [];
        }
        try {
            const parsed = JSON.parse(raw);
            return Array.isArray(parsed) ? parsed.map((item) => String(item)) : [];
        } catch {
            return [];
        }
    }
    return raw;
}

function serializeOne(schema: SettingsValueSchemaLike, value: unknown): string | null {
    if (schema.isSecret) {
        return typeof value === 'string' && value.length > 0 ? value : null;
    }
    if (value === null || value === undefined) {
        return null;
    }
    if (schema.valueType === SystemSettingsItemValueType.BOOLEAN) {
        return String(Boolean(value));
    }
    if (
        schema.valueType === SystemSettingsItemValueType.NUMBER ||
        schema.valueType === SystemSettingsItemValueType.DECIMAL
    ) {
        if (value === '') {
            return null;
        }
        return String(value);
    }
    if (isArrayValueType(schema.valueType)) {
        const arr = Array.isArray(value) ? value : [value];
        return JSON.stringify(arr.map((item) => String(item)));
    }
    return value as string;
}

/**
 * Maps a backend schema into form field values. Secret fields are always initialized to an
 * empty string (real secret values never leave the backend); array types are JSON.parse'd
 * into string[]; everything else passes through unchanged.
 */
export function deserializeSettingsValues(
    items: Record<string, SettingsValueSchemaLike>,
): Record<string, unknown> {
    return Object.fromEntries(
        Object.entries(items).map(([key, schema]) => [key, deserializeOne(schema)]),
    );
}

/**
 * Maps form field values into the payload submitted to the backend. Secret fields collapse
 * empty input to null (backend interprets this as "keep existing value"); array types are
 * JSON.stringify'd into a string array; everything else passes through unchanged.
 */
export function serializeSettingsValues(
    items: Record<string, SettingsValueSchemaLike>,
    values: Record<string, unknown>,
): Record<string, string | null> {
    return Object.fromEntries(
        Object.entries(values).map(([key, value]) => {
            const schema = items[key];
            return [key, schema ? serializeOne(schema, value) : (value as string | null)];
        }),
    );
}

export interface SettingsChange {
    key: string;
    valueType: string;
    isSecret: boolean;
    before: unknown;
    after: unknown;
}

function normalize(value: unknown): unknown {
    if (value === undefined || value === null || value === '') {
        return null;
    }
    return value;
}

function valuesEqual(schema: SettingsValueSchemaLike, a: unknown, b: unknown): boolean {
    const na = normalize(a);
    const nb = normalize(b);
    if (isArrayValueType(schema.valueType)) {
        const aa = Array.isArray(na) ? na : (na === null ? [] : [na]);
        const bb = Array.isArray(nb) ? nb : (nb === null ? [] : [nb]);
        if (aa.length !== bb.length) return false;
        return aa.every((item, idx) => String(item) === String(bb[idx]));
    }
    return na === nb;
}

/**
 * Computes the set of changed settings between the baseline (as materialized by
 * deserializeSettingsValues) and the current form values. Only keys declared in `items` are
 * considered. Secret fields whose current value is empty are treated as "unchanged"
 * (backend interprets empty secret as "keep existing value").
 */
export function diffSettingsValues(
    items: Record<string, SettingsValueSchemaLike>,
    baseline: Record<string, unknown>,
    current: Record<string, unknown>,
): SettingsChange[] {
    const changes: SettingsChange[] = [];
    for (const [key, schema] of Object.entries(items)) {
        // antd Form.useWatch only surfaces mounted Form.Item values; fields whose tab has
        // never been visited will be absent from `current`. Treat them as unchanged.
        if (!(key in current)) {
            continue;
        }
        const b = baseline[key];
        const c = current[key];
        if (schema.isSecret && normalize(c) === null) {
            continue;
        }
        if (!valuesEqual(schema, b, c)) {
            changes.push({key, valueType: schema.valueType, isSecret: schema.isSecret, before: b, after: c});
        }
    }
    return changes;
}
