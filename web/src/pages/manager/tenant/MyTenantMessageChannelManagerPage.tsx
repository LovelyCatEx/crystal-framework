import {Button, Spin} from "antd";
import React from "react";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {useUserTenants} from "@/compositions/use-tenant.ts";
import {MessageChannelManagerPanel, type MessageChannelManagerPanelRef} from "@/components/tenant/MessageChannelManagerPanel.tsx";
import {PlusOutlined} from "@ant-design/icons";
import {useTranslation} from "react-i18next";

const I18N_PREFIX = 'pages.myTenantMessageChannelManager';

export default function MyTenantMessageChannelManagerPage() {
    const {t} = useTranslation();
    const {currentTenant, isJoinedTenantsLoading} = useUserTenants();
    const currentTenantId = currentTenant?.tenantId ?? null;
    const panelRef = React.useRef<MessageChannelManagerPanelRef | null>(null);

    if (isJoinedTenantsLoading) {
        return (
            <>
                <ActionBarComponent title={t(`${I18N_PREFIX}.title`)} subtitle={t(`${I18N_PREFIX}.subtitle`)}/>
                <div style={{display: 'flex', justifyContent: 'center', alignItems: 'center', height: 256}}>
                    <Spin size="large"/>
                </div>
            </>
        );
    }

    return (
        <>
            <ActionBarComponent
                title={t(`${I18N_PREFIX}.title`)}
                subtitle={t(`${I18N_PREFIX}.subtitle`)}
                titleActions={
                    currentTenantId ? (
                        <Button
                            type="primary"
                            icon={<PlusOutlined/>}
                            size="large"
                            className="rounded-xl h-12 shadow-lg"
                            onClick={() => panelRef.current?.openCreateModal()}
                        >
                            {t(`${I18N_PREFIX}.addChannel`)}
                        </Button>
                    ) : null
                }
            />
            {currentTenantId && (
                <MessageChannelManagerPanel
                    ref={panelRef}
                    tenantId={currentTenantId}
                    i18nPrefix={I18N_PREFIX}
                />
            )}
        </>
    );
}
