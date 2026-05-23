import {Modal, Radio, Space, Switch, Tabs, Tag} from "antd";
import {CheckOutlined} from "@ant-design/icons";
import {
    getThemeByKey,
    setStoredPageAnimation,
    setStoredTabEnabled,
    setStoredTabSize,
    setStoredThemeKey,
    themeColors,
    type ThemeTabSize
} from "@/global/theme-config.ts";
import type {PageAnimationType, ThemeColor} from "@/types/theme.types.ts";
import {useTranslation} from "react-i18next";
import {useState} from "react";

interface ThemeColorPickerModalProps {
    open: boolean;
    currentThemeKey: string;
    enableTabs: boolean;
    tabSize: ThemeTabSize;
    pageAnimation: PageAnimationType;
    onClose: () => void;
    onThemeChange: (theme: ThemeColor) => void;
    onTabsEnabledChange: (enabled: boolean) => void;
    onTabSizeChange: (size: ThemeTabSize) => void;
    onPageAnimationChange: (animation: PageAnimationType) => void;
}

export function ThemeSettingsModal({
    open,
    currentThemeKey,
    enableTabs,
    tabSize,
    pageAnimation,
    onClose,
    onThemeChange,
    onTabsEnabledChange,
    onTabSizeChange,
    onPageAnimationChange,
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

    const handlePageAnimationChange = (animation: string) => {
        setStoredPageAnimation(animation as PageAnimationType);
        onPageAnimationChange(animation as PageAnimationType);
    };

    const pageAnimationOptions: { value: PageAnimationType; label: string }[] = [
        { value: 'none', label: t('components.themeSettings.pageAnimation.none') },
        { value: 'fade', label: t('components.themeSettings.pageAnimation.fade') },
        { value: 'slide-left', label: t('components.themeSettings.pageAnimation.slideLeft') },
        { value: 'slide-right', label: t('components.themeSettings.pageAnimation.slideRight') },
        { value: 'slide-up', label: t('components.themeSettings.pageAnimation.slideUp') },
        { value: 'scale', label: t('components.themeSettings.pageAnimation.scale') },
    ];

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
        {
            key: 'page-animation',
            label: t('components.themeSettings.pageAnimation.title'),
            children: (
                <div className="py-4">
                    <p className="text-gray-500 mb-4 text-sm text-center">
                        {t('components.themeSettings.pageAnimation.description')}
                    </p>
                    <div className="grid grid-cols-3 gap-3">
                        {pageAnimationOptions.map(opt => {
                            const selected = pageAnimation === opt.value;
                            return (
                                <div
                                    key={opt.value}
                                    onClick={() => handlePageAnimationChange(opt.value)}
                                    className={`
                                        flex flex-col items-center gap-2 p-3 rounded-xl border-2 cursor-pointer
                                        transition-all duration-200 hover:shadow-md
                                        ${selected ? 'shadow-md' : 'border-gray-200 hover:border-gray-300'}
                                    `}
                                    style={{
                                        borderColor: selected ? 'var(--theme-color-primary, #FF8DA1)' : undefined,
                                    }}
                                >
                                    <div
                                        className="w-full h-14 rounded-lg bg-gray-100 dark:bg-gray-700 flex items-center justify-center overflow-hidden"
                                    >
                                        <div
                                            className="w-6 h-6 rounded transition-all"
                                            style={{
                                                backgroundColor: 'var(--theme-color-primary, #FF8DA1)',
                                                animation: `demo-${opt.value} 2s ease-in-out infinite`,
                                            }}
                                        />
                                    </div>
                                    <span className="text-xs font-medium">{opt.label}</span>
                                    {selected && (
                                        <CheckOutlined style={{ color: 'var(--theme-color-primary, #FF8DA1)', fontSize: 14 }} />
                                    )}
                                </div>
                            );
                        })}
                    </div>
                    <style>{`
                        @keyframes demo-none {
                            0%, 100% { opacity: 1; }
                        }
                        @keyframes demo-fade {
                            0%, 100% { opacity: 1; }
                            50% { opacity: 0.15; }
                        }
                        @keyframes demo-slide-left {
                            0%, 100% { transform: translateX(-10px); opacity: 0.3; }
                            50% { transform: translateX(10px); opacity: 1; }
                        }
                        @keyframes demo-slide-right {
                            0%, 100% { transform: translateX(10px); opacity: 0.3; }
                            50% { transform: translateX(-10px); opacity: 1; }
                        }
                        @keyframes demo-slide-up {
                            0%, 100% { transform: translateY(8px); opacity: 0.3; }
                            50% { transform: translateY(-8px); opacity: 1; }
                        }
                        @keyframes demo-scale {
                            0%, 100% { transform: scale(0.6); opacity: 0.3; }
                            50% { transform: scale(1); opacity: 1; }
                        }
                    `}</style>
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
