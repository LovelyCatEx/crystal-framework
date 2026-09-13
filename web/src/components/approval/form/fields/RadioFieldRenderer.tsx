/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {Radio} from "antd";
import type {MergedFieldSchema} from "@/types/approval/approval-form-schema.types.ts";

export interface RadioFieldRendererProps {
    field: MergedFieldSchema;
    value?: string;
    onChange?: (value: string) => void;
}

export function RadioFieldRenderer({field, value, onChange}: RadioFieldRendererProps) {
    const options = field.options ?? [];
    return (
        <Radio.Group
            value={value ?? undefined}
            disabled={field.readonly}
            options={options}
            onChange={(e) => onChange?.(e.target.value)}
        />
    );
}
