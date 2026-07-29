import {Input} from "antd";
import {useCallback} from "react";

interface EnhancedNumberInputProps {
    value?: string | number | null;
    onChange?: (value: string | undefined) => void;
    placeholder?: string;
    className?: string;
    allowClear?: boolean;
}

export function EnhancedNumberInput({ value, onChange, placeholder, className, allowClear }: EnhancedNumberInputProps) {
    const handleChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
        const raw = e.target.value;
        // Only allow digits, minus sign, and decimal point
        const filtered = raw.replace(/[^0-9.-]/g, '');
        onChange?.(filtered || undefined);
    }, [onChange]);

    return (
        <Input
            className={className}
            value={value !== undefined && value !== null ? String(value) : ''}
            onChange={handleChange}
            placeholder={placeholder}
            allowClear={allowClear}
            // Prevent non-numeric physical input on supported browsers
            onKeyDown={(e) => {
                if (
                    e.key.length === 1 &&
                    !/[0-9.-]/.test(e.key) &&
                    e.key !== 'Backspace' &&
                    e.key !== 'Delete' &&
                    e.key !== 'ArrowLeft' &&
                    e.key !== 'ArrowRight' &&
                    e.key !== 'Tab' &&
                    !e.metaKey &&
                    !e.ctrlKey
                ) {
                    e.preventDefault();
                }
            }}
        />
    );
}
