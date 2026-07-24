import {theme} from "antd";
import {MailOutlined, NotificationOutlined, RobotOutlined} from "@ant-design/icons";
import {ChannelType, type MessageChannel} from "@/types/message-channel/message-channel.types.ts";

interface MessageChannelChipProps {
    channel: MessageChannel;
}

function ChannelIcon({ channelType }: { channelType: number }) {
    switch (channelType) {
        case ChannelType.EMAIL:
            return <MailOutlined />;
        case ChannelType.LARK:
            return <RobotOutlined />;
        default:
            return <NotificationOutlined />;
    }
}

export function MessageChannelChip({ channel }: MessageChannelChipProps) {
    const { token } = theme.useToken();

    return (
        <div
            className="inline-flex items-center gap-2 pl-3 pr-3 py-1"
            style={{
                border: `1px solid ${token.colorBorder}`,
                borderRadius: '9999px',
                background: 'transparent',
            }}
        >
            <ChannelIcon channelType={channel.channelType} />
            <span className="text-sm whitespace-nowrap">{channel.name}</span>
        </div>
    );
}
