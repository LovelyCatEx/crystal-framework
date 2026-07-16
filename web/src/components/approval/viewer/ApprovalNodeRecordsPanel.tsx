import {Empty, Timeline, Typography} from "antd";
import {useMemo} from "react";
import {useTranslation} from "react-i18next";
import type {ApprovalFlowRecordVO} from "@/types/approval/approval-flow-instance-details.types.ts";
import type {ApprovalFlowNode} from "@/types/approval/approval-flow-node.types.ts";
import {ApprovalFlowRecordAction} from "@/types/approval/approval-enums.ts";
import {ResourceScope} from "@/types/BaseScopedEntity.ts";
import {UserChipById} from "@/components/UserChipById.tsx";
import {TenantMemberChip} from "@/components/TenantMemberChip.tsx";
import {getApprovalFlowRecordAction} from "@/i18n/enum-helpers.ts";

export interface ApprovalNodeRecordsPanelProps {
    records: ApprovalFlowRecordVO[];
    selectedNodeId: string | null;
    nodes: ApprovalFlowNode[];
    scope: number;
}

function getActionColor(action: number): string {
    switch (action) {
        case ApprovalFlowRecordAction.APPROVE:
            return 'green';
        case ApprovalFlowRecordAction.REJECT:
            return 'red';
        case ApprovalFlowRecordAction.INITIATE:
            return 'blue';
        case ApprovalFlowRecordAction.SYSTEM_FORWARD:
            return 'gray';
        default:
            return 'gray';
    }
}

export function ApprovalNodeRecordsPanel({records, selectedNodeId, nodes, scope}: ApprovalNodeRecordsPanelProps) {
    const {t} = useTranslation();

    const filteredRecords = useMemo(() => {
        const source = selectedNodeId === null
            ? records
            : records.filter(r => r.nodeId === selectedNodeId);
        return [...source].sort((a, b) => Number(b.createdTime) - Number(a.createdTime));
    }, [records, selectedNodeId]);

    const nodeNameById = useMemo(() => {
        const map = new Map<string, string>();
        for (const n of nodes) {
            map.set(n.id, n.name);
        }
        return map;
    }, [nodes]);

    const renderEmpty = () => {
        if (selectedNodeId !== null) {
            return <Empty description={t('components.approvalFlowViewer.records.emptyForNode')}/>;
        }
        return <Empty description={t('components.approvalFlowViewer.records.empty')}/>;
    };

    return (
        <div className="flex flex-col gap-3">
            <Typography.Title level={5} className="!mb-0">
                {t('components.approvalFlowViewer.records.title')}
            </Typography.Title>

            {selectedNodeId === null && (
                <Typography.Text type="secondary" className="text-sm">
                    {t('components.approvalFlowViewer.records.selectNodeHint')}
                </Typography.Text>
            )}

            {filteredRecords.length === 0 ? (
                renderEmpty()
            ) : (
                <Timeline
                    items={filteredRecords.map(record => {
                        const nodeName = nodeNameById.get(record.nodeId);
                        return {
                            color: getActionColor(record.action),
                            children: (
                                <div className="flex flex-col gap-1 text-sm">
                                    <div className="flex items-center gap-2">
                                        <span className="text-gray-500">
                                            {t('components.approvalFlowViewer.records.operator')}:
                                        </span>
                                        {scope === ResourceScope.TENANT
                                            ? <TenantMemberChip memberId={record.operatorId}/>
                                            : <UserChipById userId={record.operatorId}/>
                                        }
                                    </div>
                                    <div>
                                        <span className="text-gray-500">
                                            {t('components.approvalFlowViewer.records.action')}:
                                        </span>{' '}
                                        {getApprovalFlowRecordAction(record.action)}
                                    </div>
                                    {nodeName && (
                                        <div>
                                            <span className="text-gray-500">
                                                {t('components.approvalFlowViewer.records.node')}:
                                            </span>{' '}
                                            {nodeName}
                                        </div>
                                    )}
                                    {record.comment && record.comment.trim().length > 0 && (
                                        <div>
                                            <span className="text-gray-500">
                                                {t('components.approvalFlowViewer.records.comment')}:
                                            </span>{' '}
                                            {record.comment}
                                        </div>
                                    )}
                                    <div>
                                        <span className="text-gray-500">
                                            {t('components.approvalFlowViewer.records.time')}:
                                        </span>{' '}
                                        {new Date(Number(record.createdTime)).toLocaleString()}
                                    </div>
                                </div>
                            ),
                        };
                    })}
                />
            )}
        </div>
    );
}
