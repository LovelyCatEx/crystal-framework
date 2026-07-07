import {Button} from "antd";
import React from "react";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {MessageChannelManagerPanel, type MessageChannelManagerPanelRef} from "@/components/tenant/MessageChannelManagerPanel.tsx";
import {PlusOutlined} from "@ant-design/icons";
import {useTranslation} from "react-i18next";
import {ResourceScope} from "@/types/BaseScopedEntity.ts";

const I18N_PREFIX = 'pages.systemMessageChannelManager';

export default function SystemMessageChannelManagerPage() {
    const {t} = useTranslation();
    const panelRef = React.useRef<MessageChannelManagerPanelRef | null>(null);

    return (
        <>
            <ActionBarComponent
                title={t(`${I18N_PREFIX}.title`)}
                subtitle={t(`${I18N_PREFIX}.subtitle`)}
                titleActions={
                    <Button
                        type="primary"
                        icon={<PlusOutlined/>}
                        size="large"
                        className="rounded-xl h-12 shadow-lg"
                        onClick={() => panelRef.current?.openCreateModal()}
                    >
                        {t(`${I18N_PREFIX}.addChannel`)}
                    </Button>
                }
            />
            <MessageChannelManagerPanel
                ref={panelRef}
                scope={ResourceScope.SYSTEM}
                scopeId="0"
                i18nPrefix={I18N_PREFIX}
            />
        </>
    );
}
