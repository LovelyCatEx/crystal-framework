import {Form, Select} from "antd";
import {useTranslation} from "react-i18next";
import {ApprovalFlowApproveMode, ApprovalFlowApproverStrategy, ApprovalFlowNodeType} from "@/types/approval/approval-enums.ts";
import {ResourceScope} from "@/types/BaseScopedEntity.ts";
import {getApprovalFlowApproveMode} from "@/i18n/enum-helpers.ts";
import {TenantMemberIdsSelector} from "@/components/selector/TenantMemberIdsSelector.tsx";
import {UserIdsSelector} from "@/components/selector/UserIdsSelector.tsx";
import {TenantRoleIdsSelector} from "@/components/selector/RoleIdsSelector/TenantRoleIdsSelector.tsx";
import {UserRoleIdsSelector} from "@/components/selector/RoleIdsSelector/UserRoleIdsSelector.tsx";
import {TenantMessageChannelIdsSelector} from "@/components/selector/MessageChannelIdsSelector/TenantMessageChannelIdsSelector.tsx";
import {SystemMessageChannelIdsSelector} from "@/components/selector/MessageChannelIdsSelector/SystemMessageChannelIdsSelector.tsx";
import type {ApprovalFlowNode} from "@/types/approval/approval-flow-node.types.ts";

interface ApprovalNodeInspectorProps {
    node: ApprovalFlowNode;
    scope: number;
    scopeId: string;
    onConfigChange: (patch: Record<string, unknown>) => void;
}

export function ApprovalNodeInspector({ node, scope, scopeId, onConfigChange }: ApprovalNodeInspectorProps) {
    const { t } = useTranslation();

    if (node.type === ApprovalFlowNodeType.CC) {
        return (
            <CcNodeInspector node={node} scope={scope} scopeId={scopeId} onConfigChange={onConfigChange} />
        );
    }

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
                        tenantId={scopeId}
                        value={(config.strategyParams?.memberIds as string[]) ?? []}
                        onChange={(ids) => onConfigChange({
                            strategy: ApprovalFlowApproverStrategy.SPECIFIED_USER,
                            strategyParams: { memberIds: ids },
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

interface CcConfigShape {
    userIds: string[];
    roleIds: string[];
    channelIds: string[];
}

function CcNodeInspector({ node, scope, scopeId, onConfigChange }: ApprovalNodeInspectorProps) {
    const { t } = useTranslation();

    const parseCcConfig = (): CcConfigShape => {
        if (!node.config) return { userIds: [], roleIds: [], channelIds: [] };
        try {
            const parsed = JSON.parse(node.config) as Partial<CcConfigShape>;
            return {
                userIds: parsed.userIds ?? [],
                roleIds: parsed.roleIds ?? [],
                channelIds: parsed.channelIds ?? [],
            };
        } catch {
            return { userIds: [], roleIds: [], channelIds: [] };
        }
    };

    const config = parseCcConfig();

    return (
        <>
            <Form.Item label={t('components.approvalEditor.inspector.ccAssignees')}>
                {scope === ResourceScope.TENANT ? (
                    <TenantMemberIdsSelector
                        tenantId={scopeId}
                        value={config.userIds}
                        onChange={(ids) => onConfigChange({ userIds: ids })}
                    />
                ) : (
                    <UserIdsSelector
                        value={config.userIds}
                        onChange={(ids) => onConfigChange({ userIds: ids })}
                    />
                )}
            </Form.Item>
            <Form.Item label={t('components.approvalEditor.inspector.ccRoles')}>
                {scope === ResourceScope.TENANT ? (
                    <TenantRoleIdsSelector
                        tenantId={scopeId}
                        value={config.roleIds}
                        onChange={(ids) => onConfigChange({ roleIds: ids })}
                    />
                ) : (
                    <UserRoleIdsSelector
                        value={config.roleIds}
                        onChange={(ids) => onConfigChange({ roleIds: ids })}
                    />
                )}
            </Form.Item>
            <Form.Item label={t('components.approvalEditor.inspector.ccChannels')}>
                {scope === ResourceScope.TENANT ? (
                    <TenantMessageChannelIdsSelector
                        tenantId={scopeId}
                        value={config.channelIds}
                        onChange={(ids) => onConfigChange({ channelIds: ids })}
                    />
                ) : (
                    <SystemMessageChannelIdsSelector
                        value={config.channelIds}
                        onChange={(ids) => onConfigChange({ channelIds: ids })}
                    />
                )}
            </Form.Item>
        </>
    );
}
