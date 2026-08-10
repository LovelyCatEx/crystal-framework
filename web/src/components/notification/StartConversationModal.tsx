import {Modal, Tabs} from "antd";
import {useTranslation} from "react-i18next";
import type {ContactableTenantView} from "@/types/message/message.types.ts";
import type {TenantMateVO} from "@/types/tenant/tenant-member.types.ts";
import type {UserTenantVO} from "@/types/tenant/tenant.types.ts";
import {ContactTenantPanel} from "./ContactTenantModal.tsx";
import {TenantMateSelectorPanel} from "./TenantMateSelectorModal.tsx";

export function StartConversationModal(props: {
    open: boolean;
    tenants: UserTenantVO[];
    onClose: () => void;
    onTenantSelect: (tenant: ContactableTenantView) => void;
    onTenantMateSelect: (tenant: UserTenantVO, mate: TenantMateVO) => void;
}) {
    const {open, tenants, onClose, onTenantSelect, onTenantMateSelect} = props;
    const {t} = useTranslation();

    return (
        <Modal
            open={open}
            onCancel={onClose}
            footer={null}
            title={t('components.notification.startConversation.title')}
            width={480}
            destroyOnHidden
        >
            <Tabs
                items={[
                    {
                        key: 'tenant',
                        label: t('components.notification.startConversation.tenantTab'),
                        children: <ContactTenantPanel onSelect={onTenantSelect}/>,
                    },
                    {
                        key: 'tenant-member',
                        label: t('components.notification.startConversation.tenantMemberTab'),
                        children: <TenantMateSelectorPanel tenants={tenants} onSelect={onTenantMateSelect}/>,
                    },
                ]}
            />
        </Modal>
    );
}
