import React, {useEffect, useState} from "react";
import {Spin, Tag} from "antd";
import {useTranslation} from "react-i18next";
import {ApprovalFlowInstanceManagerController} from "@/api/approval/approval-flow-instance.api.ts";
import {getApprovalFlowDefinitionDetails} from "@/api/approval/approval-flow-definition.api.ts";
import type {ApprovalFlowTask} from "@/types/approval/approval-flow-task.types.ts";
import {ApprovalFlowTaskStatus, ResourceScope} from "@/types/approval/approval-enums.ts";
import {getApprovalFlowScope, getApprovalFlowTaskStatus} from "@/i18n/enum-helpers.ts";
import {CopyableToolTip} from "@/components/CopyableToolTip.tsx";
import type {EntityTableColumns} from "../table/entity-table.types.ts";

const FlowDisplay: React.FC<{ instanceId: string }> = ({instanceId}) => {
    const {t} = useTranslation();
    const [name, setName] = useState<string | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        let cancelled = false;
        setLoading(true);
        ApprovalFlowInstanceManagerController.getById(instanceId)
            .then((instance) => instance
                ? getApprovalFlowDefinitionDetails(instance.definitionId)
                : Promise.reject(new Error('instance not found')))
            .then((res) => {
                if (!cancelled) setName(res.data?.definition.name ?? null);
            })
            .catch(() => {
                if (!cancelled) setName(null);
            })
            .finally(() => {
                if (!cancelled) setLoading(false);
            });
        return () => {
            cancelled = true;
        };
    }, [instanceId]);

    if (loading) {
        return <Spin size="small"/>;
    }

    return (
        <div className="flex flex-col gap-0.5">
            <span>{name ?? t('components.columns.approvalFlowTask.unknownFlow')}</span>
            <CopyableToolTip title={instanceId}>
                <Tag color="purple" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">
                    {t('components.columns.approvalFlowTask.instanceId')}: {instanceId}
                </Tag>
            </CopyableToolTip>
        </div>
    );
};

const NodeDisplay: React.FC<{ instanceId: string; nodeId: string }> = ({instanceId, nodeId}) => {
    const {t} = useTranslation();
    const [name, setName] = useState<string | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        let cancelled = false;
        setLoading(true);
        ApprovalFlowInstanceManagerController.getById(instanceId)
            .then((instance) => instance
                ? getApprovalFlowDefinitionDetails(instance.definitionId)
                : Promise.reject(new Error('instance not found')))
            .then((res) => {
                if (!cancelled) {
                    setName(res.data?.nodes.find((n) => n.id === nodeId)?.name ?? null);
                }
            })
            .catch(() => {
                if (!cancelled) setName(null);
            })
            .finally(() => {
                if (!cancelled) setLoading(false);
            });
        return () => {
            cancelled = true;
        };
    }, [instanceId, nodeId]);

    if (loading) {
        return <Spin size="small"/>;
    }

    if (!name) {
        return (
            <CopyableToolTip title={nodeId}>
                <Tag color="red" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">
                    {t('components.columns.approvalFlowTask.unknownNode')}
                </Tag>
            </CopyableToolTip>
        );
    }

    return <Tag color="geekblue">{name}</Tag>;
};

export function useApprovalFlowTaskTableColumns(): EntityTableColumns<ApprovalFlowTask> {
    const {t} = useTranslation();

    const statusColorMap: Record<number, string> = {
        [ApprovalFlowTaskStatus.PENDING]: 'processing',
        [ApprovalFlowTaskStatus.APPROVED]: 'green',
        [ApprovalFlowTaskStatus.REJECTED]: 'red',
        [ApprovalFlowTaskStatus.SKIPPED]: 'default',
    };

    return [
        {
            title: t('components.columns.approvalFlowTask.scope'),
            dataIndex: 'scope',
            key: 'scope',
            render: (_: unknown, row: ApprovalFlowTask) => (
                <Tag color={row.scope === ResourceScope.SYSTEM ? 'blue' : 'gold'}>
                    {getApprovalFlowScope(row.scope)}
                </Tag>
            ),
        },
        {
            title: t('components.columns.approvalFlowTask.flow'),
            dataIndex: 'instanceId',
            key: 'instanceId',
            render: (_: unknown, row: ApprovalFlowTask) => (
                <FlowDisplay instanceId={row.instanceId}/>
            ),
        },
        {
            title: t('components.columns.approvalFlowTask.node'),
            dataIndex: 'nodeId',
            key: 'nodeId',
            render: (_: unknown, row: ApprovalFlowTask) => (
                <NodeDisplay instanceId={row.instanceId} nodeId={row.nodeId}/>
            ),
        },
        {
            title: t('components.columns.approvalFlowTask.status'),
            dataIndex: 'status',
            key: 'status',
            render: (_: unknown, row: ApprovalFlowTask) => (
                <Tag color={statusColorMap[row.status] ?? 'default'}>
                    {getApprovalFlowTaskStatus(row.status)}
                </Tag>
            ),
        },
        {
            title: t('components.columns.approvalFlowTask.comment'),
            dataIndex: 'comment',
            key: 'comment',
            render: (_: unknown, row: ApprovalFlowTask) => (
                row.comment ? <span>{row.comment}</span> : <span className="text-gray-400">-</span>
            ),
        },
    ];
}
