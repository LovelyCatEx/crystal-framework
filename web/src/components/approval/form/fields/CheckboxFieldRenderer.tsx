/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

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
