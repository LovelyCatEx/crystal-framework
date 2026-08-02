import {Select, Spin} from "antd";
import {useEffect, useState} from "react";
import type {ApprovalFieldOption, MergedFieldSchema} from "@/types/approval/approval-form-schema.types.ts";

/**
 * Loads the selectable options for a single DICT field. Supplied by the consumer (initiate page
 * resolves against the definition; handle / viewer pages resolve against the instance snapshot),
 * so the renderer stays context-agnostic. Scope is derived server-side, never passed from here.
 */
export type DictOptionsLoader = (fieldKey: string) => Promise<ApprovalFieldOption[]>;

export interface DictFieldRendererProps {
    field: MergedFieldSchema;
    value?: string | string[];
    onChange?: (value: string | string[] | undefined) => void;
    loadDictOptions?: DictOptionsLoader;
}

export function DictFieldRenderer({field, value, onChange, loadDictOptions}: DictFieldRendererProps) {
    const multiple = field.validation?.multiple ?? false;
    const [options, setOptions] = useState<ApprovalFieldOption[]>([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (!loadDictOptions) return;
        let cancelled = false;
        setLoading(true);
        loadDictOptions(field.key)
            .then((items) => {
                if (!cancelled) setOptions(items);
            })
            .catch(() => {
                if (!cancelled) setOptions([]);
            })
            .finally(() => {
                if (!cancelled) setLoading(false);
            });
        return () => {
            cancelled = true;
        };
    }, [field.key, loadDictOptions]);

    return (
        <Select
            className="w-full"
            mode={multiple ? 'multiple' : undefined}
            value={value ?? (multiple ? [] : undefined)}
            placeholder={field.placeholder ?? undefined}
            disabled={field.readonly}
            loading={loading}
            notFoundContent={loading ? <Spin size="small"/> : undefined}
            options={options}
            onChange={(v) => onChange?.(v)}
            allowClear
        />
    );
}
