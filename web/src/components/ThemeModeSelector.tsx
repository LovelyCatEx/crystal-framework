/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {Button, Segmented} from "antd";
import {MoonOutlined, SunOutlined} from "@ant-design/icons";
import type {ThemeMode} from "@/types/theme.types.ts";
import {useDeviceType} from "@/compositions/use-device-type.ts";

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
    const isMobile = useDeviceType() === "mobile";

    if (isMobile) {
        return (
            <Button
                type="text"
                icon={value === "dark" ? <MoonOutlined /> : <SunOutlined />}
                onClick={() => onChange(value === "dark" ? "light" : "dark")}
            />
        );
    }

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
