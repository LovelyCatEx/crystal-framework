import {Button, Modal, Select, Space, Tooltip} from "antd";
import {JsonEditor} from "./JsonEditor.tsx";
import {useMemo, useState} from "react";
import {useTranslation} from "react-i18next";
import {ChannelType} from "@/types/message-channel/message-channel.types.ts";
import {
    isEmptyConfig,
    MESSAGE_CHANNEL_PRESETS,
    serializePreset
} from "@/utils/message-channel.utils.ts";
import {getMessageChannelPreset} from "@/i18n/enum-helpers.ts";

export interface MessageChannelConfigEditorProps {
    value?: string;
    onChange?: (value: string) => void;
    placeholder?: string;
    channelType?: ChannelType;
}

export function MessageChannelConfigEditor({
                                               value,
                                               onChange,
                                               placeholder,
                                               channelType
                                           }: MessageChannelConfigEditorProps) {
    const {t} = useTranslation();
    const [selectedPresetKey, setSelectedPresetKey] = useState<string>();
    const [lastChannelType, setLastChannelType] = useState<ChannelType | undefined>(channelType);

    if (channelType !== lastChannelType) {
        setLastChannelType(channelType);
        setSelectedPresetKey(undefined);
    }

    const presets = useMemo(
        () => (channelType !== undefined ? MESSAGE_CHANNEL_PRESETS[channelType] ?? [] : []),
        [channelType]
    );

    const disabled = channelType === undefined;

    const applyPreset = () => {
        const preset = presets.find(it => it.key === selectedPresetKey);
        if (!preset) return;
        const writePreset = () => onChange?.(serializePreset(preset));
        if (!isEmptyConfig(value)) {
            Modal.confirm({
                title: t('components.messageChannelConfig.applyOverwriteTitle'),
                content: t('components.messageChannelConfig.applyOverwriteContent'),
                onOk: writePreset
            });
            return;
        }
        writePreset();
    };

    return (
        <Space orientation="vertical" className="w-full" size="middle">
            <Space className="w-full">
                <Select
                    placeholder={t('components.messageChannelConfig.selectTemplate')}
                    value={selectedPresetKey}
                    onChange={setSelectedPresetKey}
                    disabled={disabled}
                    options={presets.map(it => ({
                        label: channelType !== undefined ? getMessageChannelPreset(channelType, it.key) : it.key,
                        value: it.key
                    }))}
                    className="min-w-48"
                />
                <Tooltip
                    title={
                        disabled
                            ? t('components.messageChannelConfig.disabledHint')
                            : t('components.messageChannelConfig.applyTemplateTooltip')
                    }
                >
                    <Button
                        type="primary"
                        onClick={applyPreset}
                        disabled={disabled || selectedPresetKey === undefined}
                    >
                        {t('components.messageChannelConfig.applyTemplate')}
                    </Button>
                </Tooltip>
            </Space>
            <JsonEditor
                value={value}
                onChange={onChange}
                placeholder={placeholder}
            />
        </Space>
    );
}
