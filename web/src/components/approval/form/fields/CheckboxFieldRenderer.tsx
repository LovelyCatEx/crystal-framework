import {Checkbox} from "antd";
import type {MergedFieldSchema} from "@/types/approval/approval-form-schema.types.ts";

export interface CheckboxFieldRendererProps {
    field: MergedFieldSchema;
    value?: string[];
    onChange?: (value: string[]) => void;
}

export function CheckboxFieldRenderer({field, value, onChange}: CheckboxFieldRendererProps) {
    const options = field.options ?? [];
    return (
        <Checkbox.Group
            value={value ?? []}
            disabled={field.readonly}
            options={options}
            onChange={(vals) => onChange?.(vals as string[])}
        />
    );
}
