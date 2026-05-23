import {Modal, Radio, Space, Switch, Tabs, Tag} from "antd";
import {CheckOutlined} from "@ant-design/icons";
import {
    getThemeByKey,
    setStoredTabEnabled,
    setStoredTabSize,
    setStoredThemeKey,
    themeColors,
    type ThemeTabSize
} from "@/global/theme-config.ts";
import type {ThemeColor} from "@/types/theme.types.ts";
import {useTranslation} from "react-i18next";
import {useState} from "react";

interface ThemeColorPickerModalProps {
    open: boolean;
    currentThemeKey: string;
    enableTabs: boolean;
    tabSize: ThemeTabSize;
    onClose: () => void;
    onThemeChange: (theme: ThemeColor) => void;
    onTabsEnabledChange: (enabled: boolean) => void;
    onTabSizeChange: (size: ThemeTabSize) => void;
}

export function ThemeSettingsModal({
    open,
    currentThemeKey,
    enableTabs,
    tabSize,
    onClose,
    onThemeChange,
    onTabsEnabledChange,
    onTabSizeChange,
}: ThemeColorPickerModalProps) {
    const {t} = useTranslation();
    const [activeTab, setActiveTab] = useState<string>('theme');

    const handleThemeSelect = (themeKey: string) => {
        const theme = getThemeByKey(themeKey);
        setStoredThemeKey(themeKey);
        onThemeChange(theme);
    };

    const handleThemeTabsEnabled = (enabled: boolean) => {
        setStoredTabEnabled(enabled);
        onTabsEnabledChange(enabled);
    };

    const handleThemeTabsSizeChanged = (size: string) => {
        setStoredTabSize(size);
        onTabSizeChange(size as ThemeTabSize);
    };

    const tabItems = [
        {
            key: 'theme',
            label: t('components.themeSettings.themeColor.title'),
            children: (
                <div className="py-4">
                    <p className="text-gray-500 mb-4 text-sm text-center">
                        {t('components.themeSettings.themeColor.description')}
                    </p>
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
            ),
        },
        {
            key: 'tabs',
            label: t('components.themeSettings.tabs.title'),
            children: (
                <div className="py-4 space-y-6">
                    <div className="flex items-center justify-between">
                        <div>
                            <div className="font-medium">{t('components.themeSettings.tabs.enableTabs')}</div>
                            <div className="text-sm text-gray-500">{t('components.themeSettings.tabs.enableTabsDesc')}</div>
                        </div>
                        <Switch
                            checked={enableTabs}
                            onChange={(v) => handleThemeTabsEnabled(v)}
                        />
                    </div>

                    <div className="flex items-center justify-between">
                        <div>
                            <div className="font-medium">{t('components.themeSettings.tabs.tabSize')}</div>
                            <div className="text-sm text-gray-500">{t('components.themeSettings.tabs.tabSizeDesc')}</div>
                        </div>
                        <Radio.Group
                            value={tabSize}
                            onChange={(e) => handleThemeTabsSizeChanged(e.target.value)}
                            optionType="button"
                            buttonStyle="solid"
                        >
                            <Radio.Button value="small">{t('components.themeSettings.tabs.sizeSmall')}</Radio.Button>
                            <Radio.Button value="middle">{t('components.themeSettings.tabs.sizeMiddle')}</Radio.Button>
                            <Radio.Button value="large">{t('components.themeSettings.tabs.sizeLarge')}</Radio.Button>
                        </Radio.Group>
                    </div>
                </div>
            ),
        },
    ];

    return (
        <Modal
            title={t('components.themeSettings.title')}
            open={open}
            onCancel={onClose}
            footer={null}
            width={520}
        >
            <Tabs
                activeKey={activeTab}
                onChange={setActiveTab}
                items={tabItems}
                className="theme-settings-tabs"
            />
        </Modal>
    );
}
