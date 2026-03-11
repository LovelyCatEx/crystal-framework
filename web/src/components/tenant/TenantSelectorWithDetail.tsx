import {Button, Card, Descriptions, Tag} from "antd";
import {TenantIdSelector} from "@/components/selector/TenantIdSelector.tsx";
import type {Tenant} from "@/types/tenant.types.ts";
import {TenantStatusMap} from "@/types/tenant.types.ts";
import {formatTimestamp} from "@/utils/datetime.utils.ts";
import {useEffect, useState} from "react";
import {RedoOutlined} from "@ant-design/icons";
import {TenantManagerController} from "@/api/tenant.api.ts";

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
    const [selectedTenant, setSelectedTenant] = useState<Tenant | null>(null);
    const [initialChecked, setInitialChecked] = useState(false);

    // 初始化时检查 URL 参数
    useEffect(() => {
        if (initialChecked) return;
        
        const urlParams = new URLSearchParams(window.location.search);
        const tenantIdFromUrl = urlParams.get('tenantId');
        
        if (tenantIdFromUrl) {
            // 从 URL 获取到 tenantId，自动加载租户信息
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

    // 当外部 value 清空时，同步清空内部状态
    useEffect(() => {
        if (!value) {
            setSelectedTenant(null);
        }
    }, [value]);

    const handleTenantEntityChange = (tenant: Tenant | null) => {
        setSelectedTenant(tenant);
        onTenantChange?.(tenant);
        onChange?.(tenant?.id || null);
        
        // 更新 URL query 参数
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
        setSelectedTenant(null);
        onChange?.(null);
        onTenantChange?.(null);
        
        // 清除 URL query 参数
        const url = new URL(window.location.href);
        url.searchParams.delete('tenantId');
        window.history.replaceState({}, '', url.toString());
    };

    if (!selectedTenant) {
        return (
            <Card className="border-none shadow-sm rounded-2xl overflow-hidden">
                <div className="p-4">
                    <label className="block text-sm font-medium text-gray-700 mb-2">选择租户</label>
                    <TenantIdSelector
                        value={value}
                        onChange={onChange}
                        onEntityChange={handleTenantEntityChange}
                    />
                </div>
            </Card>
        );
    }

    const statusInfo = TenantStatusMap[selectedTenant.status] || { label: '未知', color: 'default' };

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
