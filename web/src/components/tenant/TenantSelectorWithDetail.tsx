import {Button, Card, Descriptions, Space, Tag} from "antd";
import {TenantIdSelector} from "@/components/selector/TenantIdSelector.tsx";
import type {Tenant} from "@/types/tenant.types.ts";
import {getTenantStatus} from "@/i18n/enum-helpers.ts";
import {formatTimestamp} from "@/utils/datetime.utils.ts";
import {useEffect, useRef, useState} from "react";
import {useTranslation} from "react-i18next";
import {RedoOutlined, ShopOutlined} from "@ant-design/icons";
import {TenantManagerController} from "@/api/tenant.api.ts";
import type {EntityIdSelectorRef} from "@/components/selector/EntityIdSelector.tsx";
import {AvatarResource} from "@/components/AvatarResource.tsx";
import {theme} from "antd";
const { useToken } = theme;

const TENANT_ID_STORAGE_KEY = 'selected_tenant_id';

interface TenantSelectorWithDetailProps {
    value?: string | null;
    onChange?: (tenantId: string | null) => void;
    onTenantChange?: (tenant: Tenant | null) => void;
}

export function TenantSelectorWithDetail({
    value,
    onChange,
    onTenantChange
}: TenantSelectorWithDetailProps) {
    const { token } = useToken();
    const { t } = useTranslation();

    const selectorRef = useRef<EntityIdSelectorRef | null>(null);
    const [selectedTenant, setSelectedTenant] = useState<Tenant | null>(null);
    const [initialChecked, setInitialChecked] = useState(false);

    const getTenantIdFromStorage = (): string | null => {
        return sessionStorage.getItem(TENANT_ID_STORAGE_KEY);
    };

    const saveTenantId = (tenantId: string | null) => {
        if (tenantId) {
            sessionStorage.setItem(TENANT_ID_STORAGE_KEY, tenantId);
            const url = new URL(window.location.href);
            url.searchParams.set('tenantId', tenantId);
            window.history.replaceState({}, '', url.toString());
        } else {
            sessionStorage.removeItem(TENANT_ID_STORAGE_KEY);
            const url = new URL(window.location.href);
            url.searchParams.delete('tenantId');
            window.history.replaceState({}, '', url.toString());
        }
    };

    useEffect(() => {
        if (initialChecked) return;
        
        const urlParams = new URLSearchParams(window.location.search);
        const tenantIdFromUrl = urlParams.get('tenantId');
        const tenantIdFromStorage = getTenantIdFromStorage();
        const tenantIdToUse = tenantIdFromUrl || tenantIdFromStorage;
        
        if (tenantIdToUse) {
            TenantManagerController.getById(tenantIdToUse).then((tenant) => {
                if (tenant) {
                    setSelectedTenant(tenant);
                    onTenantChange?.(tenant);
                    onChange?.(tenantIdToUse);
                    saveTenantId(tenantIdToUse);
                }
                setInitialChecked(true);
            }).catch(() => {
                saveTenantId(null);
                setInitialChecked(true);
            });
        } else {
            setInitialChecked(true);
        }
    }, [initialChecked, onChange, onTenantChange]);

    useEffect(() => {
        if (!value) {
            setSelectedTenant(null);
        }
    }, [value]);

    const handleTenantEntityChange = (tenant: Tenant | null) => {
        setSelectedTenant(tenant);
        onTenantChange?.(tenant);
        onChange?.(tenant?.id || null);
        
        saveTenantId(tenant?.id || null);
    };

    const handleReselect = () => {
        selectorRef.current?.openModal();
    };

    if (!selectedTenant) {
        return (
            <Card className="border-none shadow-sm rounded-2xl overflow-hidden">
                <div className="p-4">
                    <label className="block text-sm font-medium mb-2" style={{ color: token.colorTextHeading }}>{t('components.tenantSelectorWithDetail.label')}</label>
                    <TenantIdSelector
                        ref={selectorRef}
                        value={value}
                        onChange={onChange}
                        onEntityChange={handleTenantEntityChange}
                    />
                </div>
            </Card>
        );
    }

    const statusColors: Record<number, string> = {
        0: 'orange',
        1: 'green',
        2: 'red'
    };
    const statusColor = statusColors[selectedTenant.status] || 'default';

    return (
        <Card
            className="border-none shadow-sm rounded-2xl overflow-hidden"
            title={
                <div className="flex items-center justify-between">
                    <Space orientation="horizontal" size={12}>
                        <AvatarResource
                            fileEntityId={selectedTenant?.icon}
                            defaultIcon={<ShopOutlined />}
                        />
                        <span className="font-bold" style={{ color: token.colorTextHeading }}>{selectedTenant.name}</span>
                    </Space>
                    <Button
                        type="link"
                        size="small"
                        icon={<RedoOutlined />}
                        onClick={handleReselect}
                    >
                        {t('components.tenantSelectorWithDetail.reselect')}
                    </Button>
                </div>
            }
        >
            <div style={{ display: 'none' }}>
                <TenantIdSelector
                    ref={selectorRef}
                    value={value}
                    onChange={onChange}
                    onEntityChange={handleTenantEntityChange}
                />
            </div>

            <Descriptions column={2} size="small" className="text-xs">
                <Descriptions.Item label={t('components.tenantSelectorWithDetail.tenantId')}>
                    <Tag color="blue" className="text-xs">{selectedTenant.id}</Tag>
                </Descriptions.Item>
                <Descriptions.Item label={t('components.tenantSelectorWithDetail.status')}>
                    <Tag color={statusColor} className="text-xs">{getTenantStatus(selectedTenant.status)}</Tag>
                </Descriptions.Item>
                <Descriptions.Item label={t('components.tenantSelectorWithDetail.contactName')}>
                    {selectedTenant.contactName}
                </Descriptions.Item>
                <Descriptions.Item label={t('components.tenantSelectorWithDetail.contactPhone')}>
                    {selectedTenant.contactPhone}
                </Descriptions.Item>
                <Descriptions.Item label={t('components.tenantSelectorWithDetail.contactEmail')}>
                    {selectedTenant.contactEmail}
                </Descriptions.Item>
                <Descriptions.Item label={t('components.tenantSelectorWithDetail.address')}>
                    {selectedTenant.address}
                </Descriptions.Item>
                <Descriptions.Item label={t('components.tenantSelectorWithDetail.subscribedTime')}>
                    {formatTimestamp(selectedTenant.subscribedTime)}
                </Descriptions.Item>
                <Descriptions.Item label={t('components.tenantSelectorWithDetail.expiresTime')}>
                    {formatTimestamp(selectedTenant.expiresTime)}
                </Descriptions.Item>
                {selectedTenant.description && (
                    <Descriptions.Item label={t('components.tenantSelectorWithDetail.description')} span={2}>
                        {selectedTenant.description}
                    </Descriptions.Item>
                )}
            </Descriptions>
        </Card>
    );
}
