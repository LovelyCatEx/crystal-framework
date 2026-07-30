import {Switch} from "antd";
import type {MergedFieldSchema} from "@/types/approval/approval-form-schema.types.ts";

export interface BooleanFieldRendererProps {
    field: MergedFieldSchema;
    value?: boolean;
    onChange?: (value: boolean) => void;
}

export function BooleanFieldRenderer({field, value, onChange}: BooleanFieldRendererProps) {
    return (
        <Switch
            checked={!!value}
            disabled={field.readonly}
            onChange={(v) => onChange?.(v)}
        />
    );
}
