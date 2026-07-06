import {Checkbox, Space} from "antd";
import {ChannelType} from "@/types/tenant/tenant-message-channel.types.ts";
import {getChannelType} from "@/i18n/enum-helpers.ts";

interface SystemMessageChannelIdsSelectorProps {
    value?: string[] | null;
    onChange?: (value: string[]) => void;
}

const SYSTEM_CHANNEL_OPTIONS: Array<{ id: keyof typeof ChannelType; typeId: number }> = [
    { id: 'EMAIL', typeId: ChannelType.EMAIL },
    { id: 'LARK', typeId: ChannelType.LARK },
];

export function SystemMessageChannelIdsSelector({ value, onChange }: SystemMessageChannelIdsSelectorProps) {
    const selected = value ?? [];

    return (
        <Checkbox.Group
            value={selected}
            onChange={(checked) => onChange?.(checked as string[])}
        >
            <Space direction="vertical">
                {SYSTEM_CHANNEL_OPTIONS.map((option) => (
                    <Checkbox key={option.id} value={option.id}>
                        {getChannelType(option.typeId)}
                    </Checkbox>
                ))}
            </Space>
        </Checkbox.Group>
    );
}
