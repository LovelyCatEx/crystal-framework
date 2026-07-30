import {InputNumber} from "antd";
import type {MergedFieldSchema} from "@/types/approval/approval-form-schema.types.ts";

export interface NumberFieldRendererProps {
    field: MergedFieldSchema;
    value?: number | null;
    onChange?: (value: number | null) => void;
}

export function NumberFieldRenderer({field, value, onChange}: NumberFieldRendererProps) {
    return (
        <InputNumber
            className="w-full"
            value={value ?? null}
            placeholder={field.placeholder ?? undefined}
            disabled={field.readonly}
            min={field.validation?.min}
            max={field.validation?.max}
            precision={field.validation?.precision}
            onChange={(v) => onChange?.(v as number | null)}
        />
    );
}
