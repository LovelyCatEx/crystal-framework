import {Input} from "antd";
import type {MergedFieldSchema} from "@/types/approval/approval-form-schema.types.ts";

export interface TextareaFieldRendererProps {
    field: MergedFieldSchema;
    value?: string;
    onChange?: (value: string) => void;
}

export function TextareaFieldRenderer({field, value, onChange}: TextareaFieldRendererProps) {
    return (
        <Input.TextArea
            value={value ?? ''}
            placeholder={field.placeholder ?? undefined}
            disabled={field.readonly}
            maxLength={field.validation?.maxLength}
            rows={4}
            onChange={(e) => onChange?.(e.target.value)}
        />
    );
}
