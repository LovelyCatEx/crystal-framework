import type React from "react";
import {Avatar, Popover, Spin, Tag} from "antd";
import {UserOutlined} from "@ant-design/icons";
import {useTranslation} from "react-i18next";
import {ResourceScope} from "@/types/BaseScopedEntity.ts";
import {useUserProfile} from "@/compositions/use-user-profile.ts";
import {useTenantMemberProfile} from "@/compositions/use-tenant-member-profile.ts";
import {UserCard} from "@/components/card/pop/UserCard.tsx";
import {TenantMemberPopCard} from "@/components/card/pop/TenantMemberPopCard.tsx";

export interface ScopedUserDisplayProps {
    scope: ResourceScope;
    resourceId: string;
    size?: 'small' | 'default';
    showName?: boolean;
}

interface ResolvedDisplay {
    avatar: string | null;
    name: string;
    popover: React.ReactNode;
}

const SystemUserDisplay: React.FC<Omit<ScopedUserDisplayProps, 'scope'>> = ({resourceId, size = 'small', showName = true}) => {
    const {userProfile, isUserProfileLoading} = useUserProfile(resourceId);
    if (isUserProfileLoading) return <Spin size="small"/>;
    if (!userProfile) return <UnknownTag/>;
    const resolved: ResolvedDisplay = {
        avatar: userProfile.avatar,
        name: userProfile.nickname || userProfile.username || resourceId,
        popover: <UserCard userId={resourceId}/>,
    };
    return <DisplayInner resolved={resolved} size={size} showName={showName}/>;
};

const TenantMemberDisplay: React.FC<Omit<ScopedUserDisplayProps, 'scope'>> = ({resourceId, size = 'small', showName = true}) => {
    const {member, isLoading} = useTenantMemberProfile(resourceId);
    if (isLoading) return <Spin size="small"/>;
    if (!member) return <UnknownTag/>;
    const resolved: ResolvedDisplay = {
        avatar: member.avatar,
        name: member.nickname || member.name || resourceId,
        popover: <TenantMemberPopCard memberId={resourceId}/>,
    };
    return <DisplayInner resolved={resolved} size={size} showName={showName}/>;
};

const DisplayInner: React.FC<{resolved: ResolvedDisplay; size: 'small' | 'default'; showName: boolean}> = ({resolved, size, showName}) => (
    <Popover content={resolved.popover} trigger="hover" placement="right">
        <div className="inline-flex items-center gap-2 cursor-pointer">
            <Avatar
                size={size}
                src={resolved.avatar ?? undefined}
                icon={<UserOutlined/>}
            />
            {showName && <span className="leading-none">{resolved.name}</span>}
        </div>
    </Popover>
);

const UnknownTag: React.FC = () => {
    const {t} = useTranslation();
    return (
        <Tag color="red" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">
            {t('components.scopedUserDisplay.unknown')}
        </Tag>
    );
};

export const ScopedUserDisplay: React.FC<ScopedUserDisplayProps> = ({scope, resourceId, size = 'small', showName = true}) => {
    if (scope === ResourceScope.SYSTEM) {
        return <SystemUserDisplay resourceId={resourceId} size={size} showName={showName}/>;
    }
    return <TenantMemberDisplay resourceId={resourceId} size={size} showName={showName}/>;
};
