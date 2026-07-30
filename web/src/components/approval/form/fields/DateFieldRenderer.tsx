import {DatePicker} from "antd";
import dayjs, {type Dayjs} from "dayjs";
import type {MergedFieldSchema} from "@/types/approval/approval-form-schema.types.ts";

export interface DateFieldRendererProps {
    field: MergedFieldSchema;
    /** ISO 8601 date string, e.g. "2026-07-29". Persisted format across the schema system. */
    value?: string | null;
    onChange?: (value: string | null) => void;
    /** When true, includes a time picker; also switches the persisted value to "YYYY-MM-DDTHH:mm:ss". */
    withTime?: boolean;
}

const DATE_FORMAT = 'YYYY-MM-DD';
const DATETIME_FORMAT = 'YYYY-MM-DDTHH:mm:ss';

export function DateFieldRenderer({field, value, onChange, withTime}: DateFieldRendererProps) {
    const format = withTime ? DATETIME_FORMAT : DATE_FORMAT;
    const dayjsValue: Dayjs | null = value ? dayjs(value) : null;
    const validation = field.validation;

    return (
        <DatePicker
            className="w-full"
            showTime={withTime}
            value={dayjsValue}
            format={format}
            placeholder={field.placeholder ?? undefined}
            disabled={field.readonly}
            disabledDate={(current) => {
                if (!current) return false;
                if (validation?.minDate && current.isBefore(dayjs(validation.minDate), 'day')) return true;
                if (validation?.maxDate && current.isAfter(dayjs(validation.maxDate), 'day')) return true;
                return false;
            }}
            onChange={(next) => onChange?.(next ? next.format(format) : null)}
        />
    );
}
