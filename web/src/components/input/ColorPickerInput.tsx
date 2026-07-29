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
