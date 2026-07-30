import {Select} from "antd";
import type {MergedFieldSchema} from "@/types/approval/approval-form-schema.types.ts";

export interface SelectFieldRendererProps {
    field: MergedFieldSchema;
    value?: string | string[];
    onChange?: (value: string | string[] | undefined) => void;
}

export function SelectFieldRenderer({field, value, onChange}: SelectFieldRendererProps) {
    const options = field.options ?? [];
    const multiple = field.validation?.multiple ?? false;
    return (
        <Select
            className="w-full"
            mode={multiple ? 'multiple' : undefined}
            value={value ?? (multiple ? [] : undefined)}
            placeholder={field.placeholder ?? undefined}
            disabled={field.readonly}
            options={options}
            onChange={(v) => onChange?.(v)}
            allowClear
        />
    );
}
