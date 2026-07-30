import type {ReactNode} from "react";
import {ApprovalFieldType} from "@/types/approval/approval-enums.ts";
import type {MergedFieldSchema} from "@/types/approval/approval-form-schema.types.ts";
import {TextFieldRenderer} from "./TextFieldRenderer.tsx";
import {TextareaFieldRenderer} from "./TextareaFieldRenderer.tsx";
import {NumberFieldRenderer} from "./NumberFieldRenderer.tsx";
import {BooleanFieldRenderer} from "./BooleanFieldRenderer.tsx";
import {SelectFieldRenderer} from "./SelectFieldRenderer.tsx";
import {RadioFieldRenderer} from "./RadioFieldRenderer.tsx";
import {CheckboxFieldRenderer} from "./CheckboxFieldRenderer.tsx";
import {DateFieldRenderer} from "./DateFieldRenderer.tsx";

export interface RenderFieldProps {
    field: MergedFieldSchema;
    value?: unknown;
    onChange?: (value: unknown) => void;
}

/**
 * Dispatches to a type-specific renderer. The renderer components are intentionally thin
 * antd wrappers: value / onChange are threaded through by antd `Form.Item`'s implicit
 * value/onChange contract when used with `name` — see `ApprovalFormRenderer` where each
 * field is wrapped in a `Form.Item name={field.key}`.
 *
 * Unknown types fall through to a plain text renderer so a schema forward-compatibility
 * mismatch never crashes the form; the field just degrades to a text input.
 */
export function renderField(props: RenderFieldProps): ReactNode {
    const {field, value, onChange} = props;
    switch (field.type) {
        case ApprovalFieldType.TEXT:
            return <TextFieldRenderer field={field} value={value as string} onChange={onChange as (v: string) => void}/>;
        case ApprovalFieldType.TEXTAREA:
            return <TextareaFieldRenderer field={field} value={value as string} onChange={onChange as (v: string) => void}/>;
        case ApprovalFieldType.NUMBER:
            return <NumberFieldRenderer field={field} value={value as number | null} onChange={onChange as (v: number | null) => void}/>;
        case ApprovalFieldType.BOOLEAN:
            return <BooleanFieldRenderer field={field} value={value as boolean} onChange={onChange as (v: boolean) => void}/>;
        case ApprovalFieldType.SELECT:
            return <SelectFieldRenderer field={field} value={value as string | string[]} onChange={onChange as (v: string | string[] | undefined) => void}/>;
        case ApprovalFieldType.RADIO:
            return <RadioFieldRenderer field={field} value={value as string} onChange={onChange as (v: string) => void}/>;
        case ApprovalFieldType.CHECKBOX:
            return <CheckboxFieldRenderer field={field} value={value as string[]} onChange={onChange as (v: string[]) => void}/>;
        case ApprovalFieldType.DATE:
            return <DateFieldRenderer field={field} value={value as string | null} onChange={onChange as (v: string | null) => void}/>;
        case ApprovalFieldType.DATETIME:
            return <DateFieldRenderer field={field} value={value as string | null} onChange={onChange as (v: string | null) => void} withTime/>;
        default:
            return <TextFieldRenderer field={field} value={value as string} onChange={onChange as (v: string) => void}/>;
    }
}
