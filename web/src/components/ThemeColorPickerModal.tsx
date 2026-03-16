import {Modal, Radio, Space, Tag} from "antd";
import {CheckOutlined} from "@ant-design/icons";
import {themeColors, getThemeByKey, setStoredThemeKey} from "@/global/theme-config.ts";
import type {ThemeColor} from "@/types/theme.types.ts";

interface ThemeColorPickerModalProps {
    open: boolean;
    currentThemeKey: string;
    onClose: () => void;
    onThemeChange: (theme: ThemeColor) => void;
}

export function ThemeColorPickerModal({
    open,
    currentThemeKey,
    onClose,
    onThemeChange,
}: ThemeColorPickerModalProps) {
    const handleThemeSelect = (themeKey: string) => {
        const theme = getThemeByKey(themeKey);
        setStoredThemeKey(themeKey);
        onThemeChange(theme);
    };

    return (
        <Modal
            title="自定义主题色"
            open={open}
            onCancel={onClose}
            footer={null}
            width={480}
        >
            <div className="py-4">
                <p className="text-gray-500 mb-4 text-sm text-center">选择你喜欢的主题色，将会应用到整个系统界面</p>
                <Radio.Group
                    value={currentThemeKey}
                    onChange={(e) => handleThemeSelect(e.target.value)}
                    className="w-full"
                >
                    <Space wrap size="middle" className="w-full justify-center">
                        {themeColors.map((theme) => (
                            <Radio.Button
                                key={theme.key}
                                value={theme.key}
                                className="!h-auto !p-0 !border-0 !bg-transparent"
                            >
                                <div
                                    className={`
                                        flex flex-col items-center gap-2 p-3 rounded-xl border-2 cursor-pointer
                                        transition-all duration-200 hover:shadow-md
                                        ${currentThemeKey === theme.key
                                            ? 'border-current shadow-md'
                                            : 'border-gray-200 hover:border-gray-300'
                                        }
                                    `}
                                    style={{
                                        borderColor: currentThemeKey === theme.key ? theme.colorPrimary : undefined,
                                    }}
                                >
                                    <div
                                        className="w-12 h-12 rounded-full flex items-center justify-center shadow-sm"
                                        style={{ backgroundColor: theme.colorPrimary }}
                                    >
                                        {currentThemeKey === theme.key && (
                                            <CheckOutlined className="text-white text-lg" />
                                        )}
                                    </div>
                                    <Tag
                                        className="!border-0 !text-xs"
                                        style={{
                                            color: theme.colorPrimary,
                                            backgroundColor: theme.itemSelectedBg,
                                        }}
                                    >
                                        {theme.name}
                                    </Tag>
                                </div>
                            </Radio.Button>
                        ))}
                    </Space>
                </Radio.Group>
            </div>
        </Modal>
    );
}
