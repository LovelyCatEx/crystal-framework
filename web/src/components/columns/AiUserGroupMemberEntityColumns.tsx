import type {EntityTableColumns} from "../table/entity-table.types.ts";
import type {AiUserGroupMemberEntity} from "@/api/ai/ai-user-group-member.api.ts";
import {useTranslation} from "react-i18next";
import {CopyableToolTip} from "../CopyableToolTip.tsx";
import {Space} from "antd";
import {UserChipById} from "@/components/chip/UserChipById.tsx";

export function useAiUserGroupMemberTableColumns(): EntityTableColumns<AiUserGroupMemberEntity> {
    const { t } = useTranslation();

    return [
        {
            title: t('components.columns.aiUserGroupMember.recordId'),
            dataIndex: 'id',
            key: 'id',
            render: (_, row) => (
                <Space direction="vertical" size={0}>
                    <CopyableToolTip title={row.id}>
                        <span className="text-xs font-mono">{row.id}</span>
                    </CopyableToolTip>
                </Space>
            )
        },
        {
            title: t('components.columns.aiUserGroupMember.user'),
            dataIndex: 'userId',
            key: 'userId',
            render: (_, row) => (
                <UserChipById userId={row.userId} />
            )
        }
    ];
}
