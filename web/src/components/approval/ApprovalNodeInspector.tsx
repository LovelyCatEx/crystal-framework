import {Form, Select} from "antd";
import {useTranslation} from "react-i18next";
import {ApprovalFlowApproveMode, ApprovalFlowApproverStrategy} from "@/types/approval/approval-enums.ts";
import {ResourceScope} from "@/types/BaseScopedEntity.ts";
import {getApprovalFlowApproveMode} from "@/i18n/enum-helpers.ts";
import {TenantMemberIdsSelector} from "@/components/selector/TenantMemberIdsSelector.tsx";
import {UserIdsSelector} from "@/components/selector/UserIdsSelector.tsx";
import type {ApprovalFlowNode} from "@/types/approval/approval-flow-node.types.ts";

interface ApprovalNodeInspectorProps {
    node: ApprovalFlowNode;
    scope: number;
    tenantId: string;
    onConfigChange: (patch: Record<string, unknown>) => void;
}

export function ApprovalNodeInspector({ node, scope, tenantId, onConfigChange }: ApprovalNodeInspectorProps) {
    const { t } = useTranslation();

    const getConfig = () => {
        if (!node.config) return { approveMode: 0, strategy: 0, strategyParams: {} as Record<string, unknown> };
        try { return JSON.parse(node.config); }
        catch { return { approveMode: 0, strategy: 0, strategyParams: {} }; }
    };

    const config = getConfig();

    const approveModeOptions = Object.values(ApprovalFlowApproveMode)
        .filter((v): v is number => typeof v === 'number')
        .map(v => ({ value: v, label: getApprovalFlowApproveMode(v) }));

    return (
        <>
            <Form.Item label={t('components.approvalEditor.inspector.approveMode')}>
                <Select
                    value={config.approveMode}
                    options={approveModeOptions}
                    onChange={(value) => onConfigChange({ approveMode: value })}
                />
            </Form.Item>
            <Form.Item label={t('components.approvalEditor.inspector.approvers')}>
                {scope === ResourceScope.TENANT ? (
                    <TenantMemberIdsSelector
                        tenantId={tenantId}
                        value={(config.strategyParams?.userIds as string[]) ?? []}
                        onChange={(ids) => onConfigChange({
                            strategy: ApprovalFlowApproverStrategy.SPECIFIED_USER,
                            strategyParams: { userIds: ids },
                        })}
                    />
                ) : (
                    <UserIdsSelector
                        value={(config.strategyParams?.userIds as string[]) ?? []}
                        onChange={(ids) => onConfigChange({
                            strategy: ApprovalFlowApproverStrategy.SPECIFIED_USER,
                            strategyParams: { userIds: ids },
                        })}
                    />
                )}
            </Form.Item>
        </>
    );
}
