import {Modal, Tabs} from "antd";
import {useTranslation} from "react-i18next";
import type {ContactableTenantView, ContactableUserView} from "@/types/message/message.types.ts";
import type {TenantMateVO} from "@/types/tenant/tenant-member.types.ts";
import type {UserTenantVO} from "@/types/tenant/tenant.types.ts";
import {ContactTenantPanel} from "./ContactTenantModal.tsx";
import {ContactUserPanel} from "./ContactUserModal.tsx";
import {TenantMateSelectorPanel} from "./TenantMateSelectorModal.tsx";
import {useSystemIntegrated} from "@/context/SystemIntegratedContext.tsx";
import {SystemModuleKey} from "@/router/system-module-menu-paths.ts";
import type {JSX} from "react";

/**
 * Identity-driven initiation dialog. The acting identity — not a free choice inside the dialog —
 * decides what you may start and in which scope, so the two identities stay isolated:
 *
 *   system identity → search a user (exact match) for a SYSTEM-scope peer chat, or contact a
 *                      tenant service desk (also a system-user act).
 *   org identity    → pick a member of the current org for a TENANT-scope member chat. No user
 *                      search and no desk here: acting as the org you only talk to its members.
 *
 * [currentTenant] is required for the org identity (it is the locked scope); it is null only when
 * the user has no authenticated tenant, in which case [identity] is always 'system'.
 */
export function StartConversationModal(props: {
    open: boolean;
    identity: 'system' | 'tenant';
    currentTenant?: UserTenantVO | null;
    onClose: () => void;
    onUserSelect: (user: ContactableUserView) => void;
    onTenantSelect: (tenant: ContactableTenantView) => void;
    onOrgMemberSelect: (tenant: UserTenantVO, mate: TenantMateVO) => void;
}) {
    const {open, identity, currentTenant, onClose, onUserSelect, onTenantSelect, onOrgMemberSelect} = props;
    const {t} = useTranslation();
    const {isModuleEnabled} = useSystemIntegrated();

    const isOrg = identity === 'tenant' && currentTenant != null;

    const systemPeerEnabled = isModuleEnabled(SystemModuleKey.MESSAGE_SYSTEM_PEER);
    const tenantScopeEnabled = isModuleEnabled(SystemModuleKey.MESSAGE_TENANT_SCOPE);
    const tenantDeskEnabled = isModuleEnabled(SystemModuleKey.MESSAGE_TENANT_DESK);

    // Build tabs dynamically based on enabled features
    const systemTabs = [
        systemPeerEnabled && {
            key: 'user',
            label: t('components.notification.startConversation.userTab'),
            children: <ContactUserPanel onSelect={onUserSelect}/>,
        },
        tenantDeskEnabled && {
            key: 'tenant',
            label: t('components.notification.startConversation.tenantTab'),
            children: <ContactTenantPanel onSelect={onTenantSelect}/>,
        },
    ].filter((tab): tab is {key: string; label: string; children: JSX.Element} => Boolean(tab));

    return (
        <Modal
            open={open}
            onCancel={onClose}
            footer={null}
            title={t('components.notification.startConversation.title')}
            width={480}
            destroyOnHidden
        >
            {isOrg ? (
                // Org identity: show member selector if tenantScope is enabled, otherwise show disabled message
                tenantScopeEnabled ? (
                    <TenantMateSelectorPanel
                        tenants={[currentTenant]}
                        lockedTenant={currentTenant}
                        onSelect={onOrgMemberSelect}
                    />
                ) : (
                    <div style={{padding: 16, textAlign: 'center', color: '#999'}}>
                        {t('components.notification.startConversation.tenantScopeDisabled')}
                    </div>
                )
            ) : (
                // System identity: show available tabs based on enabled features
                systemTabs.length > 0 ? (
                    <Tabs items={systemTabs} />
                ) : (
                    <div style={{padding: 16, textAlign: 'center', color: '#999'}}>
                        {t('components.notification.startConversation.allFeaturesDisabled')}
                    </div>
                )
            )}
        </Modal>
    );
}
