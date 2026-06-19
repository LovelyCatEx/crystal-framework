import React, {useEffect, useState} from "react";
import {Spin, Tag} from "antd";
import {useTranslation} from "react-i18next";
import {ApprovalFlowDefinitionManagerController} from "@/api/approval/approval-flow-definition.api.ts";
import type {ApprovalFlowDefinition} from "@/types/approval/approval-flow-definition.types.ts";
import type {ApprovalFlowInstance} from "@/types/approval/approval-flow-instance.types.ts";
import {ApprovalFlowInstanceStatus, ResourceScope} from "@/types/approval/approval-enums.ts";
import {getApprovalFlowInstanceStatus, getApprovalFlowScope} from "@/i18n/enum-helpers.ts";
import {UserChipById} from "@/components/UserChipById.tsx";
import {TenantMemberChip} from "@/components/TenantMemberChip.tsx";
import {CopyableToolTip} from "@/components/CopyableToolTip.tsx";
import type {EntityTableColumns} from "../table/entity-table.types.ts";

const ApprovalFlowDefinitionDisplay: React.FC<{ definitionId: string }> = ({ definitionId }) => {
    const { t } = useTranslation();
    const [definition, setDefinition] = useState<ApprovalFlowDefinition | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        setLoading(true);
        ApprovalFlowDefinitionManagerController.getById(definitionId)
            .then((data) => setDefinition(data))
            .catch(() => setDefinition(null))
            .finally(() => setLoading(false));
    }, [definitionId]);

    if (loading) {
        return <Spin size="small" />;
    }

    if (!definition) {
        return (
            <CopyableToolTip title={definitionId}>
                <Tag color="red" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">
                    {t('components.columns.approvalFlowInstance.unknownDefinition')}
                </Tag>
            </CopyableToolTip>
        );
    }

    return (
        <div className="flex flex-col gap-0.5">
            <span>{definition.name}</span>
            <CopyableToolTip title={definitionId}>
                <Tag color="purple" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">
                    {t('components.columns.approvalFlowInstance.definitionId')}: {definitionId}
                </Tag>
            </CopyableToolTip>
        </div>
    );
};

const InitiatorDisplay: React.FC<{ scope: number; initiatorId: string }> = ({ scope, initiatorId }) => {
    if (scope === ResourceScope.TENANT) {
        return <TenantMemberChip memberId={initiatorId} />;
    }
    return <UserChipById userId={initiatorId} />;
};

export function useApprovalFlowInstanceTableColumns(): EntityTableColumns<ApprovalFlowInstance> {
    const { t } = useTranslation();

    const statusColorMap: Record<number, string> = {
        [ApprovalFlowInstanceStatus.IN_PROGRESS]: 'processing',
        [ApprovalFlowInstanceStatus.APPROVED]: 'green',
        [ApprovalFlowInstanceStatus.REJECTED]: 'red',
        [ApprovalFlowInstanceStatus.CANCELLED]: 'default',
    };

    return [
        {
            title: t('components.columns.approvalFlowInstance.scope'),
            dataIndex: 'scope',
            key: 'scope',
            render: (_: unknown, row: ApprovalFlowInstance) => (
                <Tag color={row.scope === ResourceScope.SYSTEM ? 'blue' : 'gold'}>
                    {getApprovalFlowScope(row.scope)}
                </Tag>
            ),
        },
        {
            title: t('components.columns.approvalFlowInstance.definition'),
            dataIndex: 'definitionId',
            key: 'definitionId',
            render: (_: unknown, row: ApprovalFlowInstance) => (
                <ApprovalFlowDefinitionDisplay definitionId={row.definitionId} />
            ),
        },
        {
            title: t('components.columns.approvalFlowInstance.definitionVersion'),
            dataIndex: 'definitionVersion',
            key: 'definitionVersion',
            render: (_: unknown, row: ApprovalFlowInstance) => (
                <Tag color="blue">v{row.definitionVersion}</Tag>
            ),
        },
        {
            title: t('components.columns.approvalFlowInstance.initiator'),
            dataIndex: 'initiatorId',
            key: 'initiatorId',
            render: (_: unknown, row: ApprovalFlowInstance) => (
                <InitiatorDisplay scope={row.scope} initiatorId={row.initiatorId} />
            ),
        },
        {
            title: t('components.columns.approvalFlowInstance.status'),
            dataIndex: 'status',
            key: 'status',
            render: (_: unknown, row: ApprovalFlowInstance) => (
                <Tag color={statusColorMap[row.status] ?? 'default'}>
                    {getApprovalFlowInstanceStatus(row.status)}
                </Tag>
            ),
        },
    ];
}
