import {Popover, Spin, theme} from "antd";
import {AvatarResource} from "@/components/AvatarResource.tsx";
import {UserCard} from "@/components/card/pop/UserCard.tsx";
import {useUserProfile} from "@/compositions/use-user-profile.ts";

interface UserChipByIdProps {
    userId: string;
}

export function UserChipById({ userId }: UserChipByIdProps) {
    const { token } = theme.useToken();
    const { userProfile, isUserProfileLoading } = useUserProfile(userId);

    if (isUserProfileLoading) {
        return <Spin size="small" />;
    }

    const chip = (
        <div
            className="inline-flex items-center gap-2 pl-1 pr-3 py-1 cursor-pointer transition hover:opacity-80"
            style={{
                border: `1px solid ${token.colorBorder}`,
                borderRadius: '9999px',
                background: 'transparent',
            }}
        >
            <AvatarResource url={userProfile?.avatar} size={24} />
            <span className="text-sm whitespace-nowrap">
                {userProfile?.nickname || userProfile?.username || userId}
            </span>
        </div>
    );

    return (
        <Popover content={<UserCard userId={userId} />} trigger="click" placement="bottomLeft">
            {chip}
        </Popover>
    );
}
