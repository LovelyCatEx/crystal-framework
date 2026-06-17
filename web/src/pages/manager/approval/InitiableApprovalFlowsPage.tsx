import {Empty, Spin, Tabs} from "antd";
import {useEffect, useMemo, useRef, useState} from "react";
import {useTranslation} from "react-i18next";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {useApprovalFlowDefinitionTableColumns} from "@/components/columns/ApprovalFlowDefinitionEntityColumns.tsx";
import {ApprovalFlowDefinitionManagerController} from "@/api/approval/approval-flow-definition.api.ts";
import {ApprovalFlowDefinitionStatus, ResourceScope} from "@/types/approval/approval-flow-definition.types.ts";
import {useUserTenants} from "@/compositions/use-tenant.ts";

const SYSTEM_SCOPE_ID = '0';

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
                    showRowActions={false}
                    columns={columns}
                    searchKeywords={['name']}
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
        </>
    );
}
