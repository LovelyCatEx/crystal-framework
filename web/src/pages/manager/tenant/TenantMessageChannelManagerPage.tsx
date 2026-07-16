import {Button} from "antd";
import React, {useState} from "react";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {TenantSelectorWithDetail} from "@/components/tenant/TenantSelectorWithDetail.tsx";
import {MessageChannelManagerPanel, type MessageChannelManagerPanelRef} from "@/components/tenant/MessageChannelManagerPanel.tsx";
import {PlusOutlined} from "@ant-design/icons";
import {useTranslation} from "react-i18next";
import {ResourceScope} from "@/types/BaseScopedEntity.ts";

const I18N_PREFIX = 'pages.tenantMessageChannelManager';

export default function TenantMessageChannelManagerPage() {
    const {t} = useTranslation();
    const [selectedTenantId, setSelectedTenantId] = useState<string | null>(null);
    const panelRef = React.useRef<MessageChannelManagerPanelRef | null>(null);

    return (
        <>
            <ActionBarComponent
                title={t(`${I18N_PREFIX}.title`)}
                subtitle={t(`${I18N_PREFIX}.subtitle`)}
                titleActions={
                    selectedTenantId ? (
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
            <TenantSelectorWithDetail
                value={selectedTenantId}
                onChange={setSelectedTenantId}
            />
            {selectedTenantId && (
                <MessageChannelManagerPanel
                    ref={panelRef}
                    scope={ResourceScope.TENANT}
                    scopeId={selectedTenantId}
                    i18nPrefix={I18N_PREFIX}
                />
            )}
        </>
    );
}
