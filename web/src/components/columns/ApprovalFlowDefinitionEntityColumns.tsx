import {Tag} from "antd";
import {useTranslation} from "react-i18next";
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
