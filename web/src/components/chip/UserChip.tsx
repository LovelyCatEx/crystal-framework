import {Popover, theme} from "antd";
import {AvatarResource} from "@/components/resource/AvatarResource.tsx";
import {UserCard} from "@/components/card/pop/UserCard.tsx";

interface UserChipProps {
    userId?: string | null;
    avatar?: string | null;
    name: string;
}

export function UserChip({ userId, avatar, name }: UserChipProps) {
    const { token } = theme.useToken();

    const chip = (
        <div
            className="inline-flex items-center gap-2 pl-1 pr-3 py-1 cursor-pointer transition hover:opacity-80"
            style={{
                border: `1px solid ${token.colorBorder}`,
                borderRadius: '9999px',
                background: 'transparent',
            }}
        >
            <AvatarResource fileEntityId={avatar} size={24} />
            <span className="text-sm whitespace-nowrap">{name}</span>
        </div>
    );

    if (!userId) return chip;

    return (
        <Popover content={<UserCard userId={userId} />} trigger="click" placement="bottomLeft">
            {chip}
        </Popover>
    );
}
