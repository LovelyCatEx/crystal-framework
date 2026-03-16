import {Segmented} from "antd";
import {MoonOutlined, SunOutlined} from "@ant-design/icons";
import type {ThemeMode} from "@/types/theme.types.ts";

interface ThemeModeSelectorProps {
    value: ThemeMode;
    onChange: (mode: ThemeMode) => void;
    size?: "small" | "middle" | "large";
    shape?: "default" | "round";
}

export function ThemeModeSelector({
    value,
    onChange,
    size = "middle",
    shape = "round",
}: ThemeModeSelectorProps) {
    return (
        <Segmented
            value={value}
            onChange={(val) => onChange(val as ThemeMode)}
            size={size}
            shape={shape}
            options={[
                { value: 'light', icon: <SunOutlined /> },
                { value: 'dark', icon: <MoonOutlined /> },
            ]}
        />
    );
}
