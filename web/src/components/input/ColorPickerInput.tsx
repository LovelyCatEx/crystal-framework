/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {ColorPicker} from "antd";

export interface ColorPickerInputProps {
    value?: string;
    onChange?: (value: string) => void;
    disabled?: boolean;
}

export function ColorPickerInput({value, onChange, disabled}: ColorPickerInputProps) {
    return (
        <ColorPicker
            value={value || undefined}
            disabled={disabled}
            showText
            onChange={(color) => onChange?.(color.toHexString())}
        />
    );
}
