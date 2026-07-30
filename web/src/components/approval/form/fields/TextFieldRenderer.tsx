import {Input} from "antd";
import type {MergedFieldSchema} from "@/types/approval/approval-form-schema.types.ts";

export interface TextFieldRendererProps {
    field: MergedFieldSchema;
    value?: string;
    onChange?: (value: string) => void;
}

export function TextFieldRenderer({field, value, onChange}: TextFieldRendererProps) {
    return (
        <Input
            value={value ?? ''}
            placeholder={field.placeholder ?? undefined}
            disabled={field.readonly}
            maxLength={field.validation?.maxLength}
            onChange={(e) => onChange?.(e.target.value)}
        />
    );
}
