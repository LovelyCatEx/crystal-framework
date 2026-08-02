import {Tag} from "antd";
import {useTranslation} from "react-i18next";
import {CopyableToolTip} from "@/components/CopyableToolTip.tsx";
import {getApprovalFlowDefinitionStatus} from "@/i18n/enum-helpers.ts";
import {ApprovalFlowDefinitionStatus} from "@/types/approval/approval-flow-definition.types.ts";
import type {ApprovalFlowDefinition} from "@/types/approval/approval-flow-definition.types.ts";
import type {EntityTableColumns} from "../table/entity-table.types.ts";

export function useApprovalFlowDefinitionTableColumns(): EntityTableColumns<ApprovalFlowDefinition> {
    const {t} = useTranslation();

    return [
        {
            title: t('components.columns.approvalFlowDefinition.name'),
            dataIndex: "name",
            key: "name",
            render: (_: unknown, row: ApprovalFlowDefinition) => (
                <div className="flex flex-col items-start gap-0.5">
                    <span>{row.name}</span>
                    <CopyableToolTip title={row.id}>
                        <Tag color="purple" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">
                            {t('components.columns.approvalFlowDefinition.definitionId')}: {row.id}
                        </Tag>
                    </CopyableToolTip>
                </div>
            )
        },
        {
            title: t('components.columns.approvalFlowDefinition.description'),
            dataIndex: "description",
            key: "description",
            render: (_: unknown, row: ApprovalFlowDefinition) => row.description || '-'
        },
        {
            title: t('components.columns.approvalFlowDefinition.currentVersion'),
            dataIndex: "currentVersion",
            key: "currentVersion",
            render: (_: unknown, row: ApprovalFlowDefinition) => (
                <Tag color="blue">v{row.currentVersion}</Tag>
            )
        },
        {
            title: t('components.columns.approvalFlowDefinition.status'),
            dataIndex: "status",
            key: "status",
            render: (_: unknown, row: ApprovalFlowDefinition) => {
                const colorMap: Record<number, string> = {
                    [ApprovalFlowDefinitionStatus.DRAFT]: 'default',
                    [ApprovalFlowDefinitionStatus.PUBLISHED]: 'green',
                    [ApprovalFlowDefinitionStatus.DISABLED]: 'red',
                };
                return (
                    <Tag color={colorMap[row.status] ?? 'default'}>
                        {getApprovalFlowDefinitionStatus(row.status)}
                    </Tag>
                );
            }
        },
    ];
}
