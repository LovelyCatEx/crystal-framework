/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {Popover, Spin, theme} from "antd";
import {useTranslation} from "react-i18next";
import {AvatarResource} from "@/components/resource/AvatarResource.tsx";
import {TenantMemberPopCard} from "@/components/card/pop/TenantMemberPopCard.tsx";
import {useTenantMemberProfile} from "@/compositions/use-tenant-member-profile.ts";

interface TenantMemberChipProps {
    memberId: string;
}

export function TenantMemberChip({ memberId }: TenantMemberChipProps) {
    const { t } = useTranslation();
    const { token } = theme.useToken();
    const { member, isLoading } = useTenantMemberProfile(memberId);

    if (isLoading) {
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
            <AvatarResource url={member?.avatar} size={24} />
            <span className="text-sm whitespace-nowrap">
                {member?.nickname || member?.name || t('components.chip.tenantMember.unknown')}
            </span>
        </div>
    );

    return (
        <Popover content={<TenantMemberPopCard memberId={memberId} />} trigger="click" placement="bottomLeft">
            {chip}
        </Popover>
    );
}
