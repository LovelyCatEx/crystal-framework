import {SystemSettingsItemValueType} from "@/types/system/system-settings.types.ts";

/**
 * Minimal structural subset of a settings item schema, shared by system / tenant settings.
 */
export interface SettingsValueSchemaLike {
    valueType: string;
    value: string | null;
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

function deserializeOne(valueType: string, raw: string | null): unknown {
    if (raw === null || raw === undefined || !isArrayValueType(valueType)) {
        return raw;
    }
    try {
        const parsed = JSON.parse(raw);
        return Array.isArray(parsed) ? parsed.map((item) => String(item)) : [];
    } catch {
        return [];
    }
}

function serializeOne(valueType: string, value: unknown): string | null {
    if (!isArrayValueType(valueType)) {
        return value as string | null;
    }
    if (value === null || value === undefined) {
        return null;
    }
    const arr = Array.isArray(value) ? value : [value];
    return JSON.stringify(arr.map((item) => String(item)));
}

/**
 * Maps a backend schema into form field values: array types are JSON.parse'd into string[],
 * everything else passes through unchanged.
 */
export function deserializeSettingsValues(
    items: Record<string, SettingsValueSchemaLike>,
): Record<string, unknown> {
    return Object.fromEntries(
        Object.entries(items).map(([key, schema]) => [key, deserializeOne(schema.valueType, schema.value)]),
    );
}

/**
 * Maps form field values into the payload submitted to the backend: array types are
 * JSON.stringify'd into a string array, everything else passes through unchanged.
 */
export function serializeSettingsValues(
    items: Record<string, SettingsValueSchemaLike>,
    values: Record<string, unknown>,
): Record<string, string | null> {
    return Object.fromEntries(
        Object.entries(values).map(([key, value]) => {
            const schema = items[key];
            return [key, schema ? serializeOne(schema.valueType, value) : (value as string | null)];
        }),
    );
}
