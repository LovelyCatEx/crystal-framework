import {Empty, message, Modal, Spin, Tabs} from "antd";
import {useEffect, useMemo, useRef, useState} from "react";
import {useTranslation} from "react-i18next";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {useApprovalFlowDefinitionTableColumns} from "@/components/columns/ApprovalFlowDefinitionEntityColumns.tsx";
import {
    ApprovalFlowDefinitionManagerController,
    getApprovalFlowDefinitionDetails,
} from "@/api/approval/approval-flow-definition.api.ts";
import {startApprovalFlow} from "@/api/approval/approval-flow-instance.api.ts";
import {ApprovalFlowDefinitionStatus, ResourceScope} from "@/types/approval/approval-flow-definition.types.ts";
import type {ApprovalFlowDefinition} from "@/types/approval/approval-flow-definition.types.ts";
import type {ApprovalFlowNode} from "@/types/approval/approval-flow-node.types.ts";
import {ApprovalFlowNodeType} from "@/types/approval/approval-enums.ts";
import type {MergedFieldSchema} from "@/types/approval/approval-form-schema.types.ts";
import {mergeFieldOverrides, parseFormSchema, parseNodeOverlay} from "@/utils/approval-form-utils.ts";
import {
    ApprovalFormRenderer,
    type ApprovalFormRendererRef,
} from "@/components/approval/form/ApprovalFormRenderer.tsx";
import {useUserTenants} from "@/compositions/use-tenant.ts";

const SYSTEM_SCOPE_ID = '0';

/**
 * Resolve the "initiate" node — the single node the START node points to. START itself
 * carries no form overlay (see `ApprovalFlowGraphValidator` rule 17: START has exactly
 * one outgoing edge), so the initiator's field visibility is dictated by that successor.
 * Returns null if the graph is malformed or START has no successor found in the node list.
 */
function resolveInitiateNode(nodes: ApprovalFlowNode[], edges: Array<{sourceNodeId: string; targetNodeId: string}>): ApprovalFlowNode | null {
    const startNode = nodes.find(n => n.type === ApprovalFlowNodeType.START);
    if (!startNode) return null;
    const outEdge = edges.find(e => e.sourceNodeId === startNode.id);
    if (!outEdge) return null;
    return nodes.find(n => n.id === outEdge.targetNodeId) ?? null;
}

export default function InitiableApprovalFlowsPage() {
    const {t} = useTranslation();
    const {currentTenant, isJoinedTenantsLoading} = useUserTenants();
    const columns = useApprovalFlowDefinitionTableColumns();
    const pageRef = useRef<ManagerPageContainerRef | null>(null);

    const tenantId = currentTenant?.tenantId ?? null;

    const [desiredScope, setDesiredScope] = useState<ResourceScope>(
        tenantId ? ResourceScope.TENANT : ResourceScope.SYSTEM
    );

    const activeScope: ResourceScope = !tenantId && desiredScope === ResourceScope.TENANT
        ? ResourceScope.SYSTEM
        : desiredScope;

    const [initiatingDefinition, setInitiatingDefinition] = useState<ApprovalFlowDefinition | null>(null);
    const [submitting, setSubmitting] = useState(false);
    const [detailsLoading, setDetailsLoading] = useState(false);
    const [mergedFields, setMergedFields] = useState<MergedFieldSchema[] | null>(null);
    const [formGroups, setFormGroups] = useState<Array<{key: string; label: string}> | undefined>(undefined);
    const formRef = useRef<ApprovalFormRendererRef | null>(null);

    useEffect(() => {
        if (!initiatingDefinition) {
            setMergedFields(null);
            setFormGroups(undefined);
            return;
        }
        const schema = parseFormSchema(initiatingDefinition.formSchema);
        if (!schema || schema.fields.length === 0) {
            setMergedFields(null);
            setFormGroups(undefined);
            return;
        }
        setDetailsLoading(true);
        (async () => {
            try {
                const resp = await getApprovalFlowDefinitionDetails(initiatingDefinition.id);
                const details = resp.data;
                const initiateNode: ApprovalFlowNode | null = resolveInitiateNode(details?.nodes ?? [], details?.edges ?? []);
                const overlay = parseNodeOverlay(initiateNode?.formSchema ?? null);
                const merged = mergeFieldOverrides(schema, overlay, {
                    nodeType: initiateNode?.type ?? ApprovalFlowNodeType.START,
                });
                setMergedFields(merged);
                setFormGroups(schema.groups);
            } catch {
                setMergedFields(mergeFieldOverrides(schema, null, {nodeType: ApprovalFlowNodeType.START}));
                setFormGroups(schema.groups);
            } finally {
                setDetailsLoading(false);
            }
        })();
    }, [initiatingDefinition]);

    useEffect(() => {
        pageRef.current?.refreshData({resetPage: true});
    }, [activeScope, tenantId]);

    const tabItems = useMemo(
        () => [
            {
                key: String(ResourceScope.SYSTEM),
                label: t('pages.initiableApprovalFlows.tab.system'),
            },
            ...(tenantId
                ? [{
                    key: String(ResourceScope.TENANT),
                    label: t('pages.initiableApprovalFlows.tab.tenant'),
                }]
                : []),
        ],
        [tenantId, t]
    );

    const effectiveScopeId = activeScope === ResourceScope.TENANT
        ? (tenantId ?? SYSTEM_SCOPE_ID)
        : SYSTEM_SCOPE_ID;

    const renderHeader = (
        <ActionBarComponent
            title={t('pages.initiableApprovalFlows.title')}
            subtitle={t('pages.initiableApprovalFlows.subtitle')}
        />
    );

    const handleInitiate = async () => {
        if (!initiatingDefinition) return;
        let formData = '{}';
        if (mergedFields && mergedFields.length > 0 && formRef.current) {
            try {
                const values = await formRef.current.validate();
                formData = JSON.stringify(values ?? {});
            } catch {
                return;
            }
        }
        setSubmitting(true);
        try {
            await startApprovalFlow({definitionId: initiatingDefinition.id, formData});
            void message.success(t('pages.initiableApprovalFlows.modal.success'));
            setInitiatingDefinition(null);
        } catch {
            void message.error(t('pages.initiableApprovalFlows.modal.failed'));
        } finally {
            setSubmitting(false);
        }
    };

    if (isJoinedTenantsLoading) {
        return (
            <>
                {renderHeader}
                <div className="flex justify-center items-center h-64">
                    <Spin size="large"/>
                </div>
            </>
        );
    }

    return (
        <>
            {renderHeader}
            <Tabs
                activeKey={String(activeScope)}
                onChange={(key) => setDesiredScope(Number(key) as ResourceScope)}
                items={tabItems}
            />
            {activeScope === ResourceScope.TENANT && !tenantId ? (
                <Empty description={t('pages.initiableApprovalFlows.noTenantTip')}/>
            ) : (
                <ManagerPageContainer
                    ref={pageRef}
                    entityName={t('entityNames.approvalFlowDefinition')}
                    title=""
                    subtitle=""
                    showActionBar={false}
                    readonlyMode
                    columns={columns}
                    searchKeywords={['name']}
                    tableRowActionsRender={(record) => (
                        <a onClick={() => setInitiatingDefinition(record)}>
                            {t('pages.initiableApprovalFlows.action.initiate')}
                        </a>
                    )}
                    query={async (props) => (
                        await ApprovalFlowDefinitionManagerController.query({
                            ...props,
                            scope: activeScope,
                            scopeId: effectiveScopeId,
                            query: {
                                type: 'group',
                                logic: 'and',
                                children: [
                                    {
                                        type: 'condition',
                                        field: 'status',
                                        operator: 'eq',
                                        value: ApprovalFlowDefinitionStatus.PUBLISHED,
                                    },
                                    ...(props.query ? [props.query] : []),
                                ],
                            },
                        } as Parameters<typeof ApprovalFlowDefinitionManagerController.query>[0])
                    ).data!}
                    create={async () => {
                    }}
                    update={async () => {
                    }}
                    delete={async () => {
                    }}
                />
            )}
            <Modal
                open={initiatingDefinition !== null}
                title={initiatingDefinition
                    ? t('pages.initiableApprovalFlows.modal.title', {name: initiatingDefinition.name})
                    : ''}
                okText={t('pages.initiableApprovalFlows.modal.confirm')}
                cancelText={t('pages.initiableApprovalFlows.modal.cancel')}
                confirmLoading={submitting}
                onOk={handleInitiate}
                onCancel={() => setInitiatingDefinition(null)}
                destroyOnHidden
                width={mergedFields && mergedFields.length > 0 ? 640 : undefined}
            >
                {detailsLoading ? (
                    <div className="flex justify-center items-center py-8">
                        <Spin/>
                    </div>
                ) : mergedFields && mergedFields.length > 0 ? (
                    <ApprovalFormRenderer
                        ref={formRef}
                        fields={mergedFields}
                        groups={formGroups}
                    />
                ) : (
                    <Empty description={t('pages.initiableApprovalFlows.modal.formPlaceholder')}/>
                )}
            </Modal>
        </>
    );
}
