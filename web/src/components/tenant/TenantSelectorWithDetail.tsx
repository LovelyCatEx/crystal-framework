import {Button, Card, Descriptions, Tag} from "antd";
import {TenantIdSelector} from "@/components/selector/TenantIdSelector.tsx";
import type {Tenant} from "@/types/tenant.types.ts";
import {TenantStatusMap} from "@/types/tenant.types.ts";
import {formatTimestamp} from "@/utils/datetime.utils.ts";
import {useEffect, useRef, useState} from "react";
import {RedoOutlined} from "@ant-design/icons";
import {TenantManagerController} from "@/api/tenant.api.ts";
import type {EntityIdSelectorRef} from "@/components/selector/EntityIdSelector.tsx";

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
    const selectorRef = useRef<EntityIdSelectorRef | null>(null);
    const [selectedTenant, setSelectedTenant] = useState<Tenant | null>(null);
    const [initialChecked, setInitialChecked] = useState(false);

    useEffect(() => {
        if (initialChecked) return;
        
        const urlParams = new URLSearchParams(window.location.search);
        const tenantIdFromUrl = urlParams.get('tenantId');
        
        if (tenantIdFromUrl) {
            TenantManagerController.getById(tenantIdFromUrl).then((tenant) => {
                if (tenant) {
                    setSelectedTenant(tenant);
                    onTenantChange?.(tenant);
                    onChange?.(tenantIdFromUrl);
                }
                setInitialChecked(true);
            }).catch(() => {
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
        
        if (tenant?.id) {
            const url = new URL(window.location.href);
            url.searchParams.set('tenantId', tenant.id);
            window.history.replaceState({}, '', url.toString());
        } else {
            const url = new URL(window.location.href);
            url.searchParams.delete('tenantId');
            window.history.replaceState({}, '', url.toString());
        }
    };

    const handleReselect = () => {
        // 直接打开弹窗，不重置状态
        selectorRef.current?.openModal();
    };

    // 如果没有选择租户，显示选择器
    if (!selectedTenant) {
        return (
            <Card className="border-none shadow-sm rounded-2xl overflow-hidden">
                <div className="p-4">
                    <label className="block text-sm font-medium text-gray-700 mb-2">选择租户</label>
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

    const statusInfo = TenantStatusMap[selectedTenant.status] || { label: '未知', color: 'default' };

    // 选择了租户后，显示详情卡片，但保留隐藏的 TenantIdSelector 用于弹窗
    return (
        <Card
            className="border-none shadow-sm rounded-2xl overflow-hidden"
            title={
                <div className="flex items-center justify-between">
                    <span className="font-bold">{selectedTenant.name}</span>
                    <Button
                        type="link"
                        size="small"
                        icon={<RedoOutlined />}
                        onClick={handleReselect}
                    >
                        重新选择
                    </Button>
                </div>
            }
        >
            {/* 隐藏的 TenantIdSelector，用于重新选择时打开弹窗 */}
            <div style={{ display: 'none' }}>
                <TenantIdSelector
                    ref={selectorRef}
                    value={value}
                    onChange={onChange}
                    onEntityChange={handleTenantEntityChange}
                />
            </div>

            <Descriptions column={2} size="small" className="text-xs">
                <Descriptions.Item label="租户ID">
                    <Tag color="blue" className="text-xs">{selectedTenant.id}</Tag>
                </Descriptions.Item>
                <Descriptions.Item label="状态">
                    <Tag color={statusInfo.color} className="text-xs">{statusInfo.label}</Tag>
                </Descriptions.Item>
                <Descriptions.Item label="联系人">
                    {selectedTenant.contactName}
                </Descriptions.Item>
                <Descriptions.Item label="联系电话">
                    {selectedTenant.contactPhone}
                </Descriptions.Item>
                <Descriptions.Item label="联系邮箱">
                    {selectedTenant.contactEmail}
                </Descriptions.Item>
                <Descriptions.Item label="地址">
                    {selectedTenant.address}
                </Descriptions.Item>
                <Descriptions.Item label="订阅时间">
                    {formatTimestamp(selectedTenant.subscribedTime)}
                </Descriptions.Item>
                <Descriptions.Item label="过期时间">
                    {formatTimestamp(selectedTenant.expiresTime)}
                </Descriptions.Item>
                {selectedTenant.description && (
                    <Descriptions.Item label="描述" span={2}>
                        {selectedTenant.description}
                    </Descriptions.Item>
                )}
            </Descriptions>
        </Card>
    );
}
