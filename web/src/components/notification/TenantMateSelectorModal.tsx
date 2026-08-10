import {useCallback, useEffect, useState} from "react";
import {useTranslation} from "react-i18next";
import {Empty, List, Pagination, Select, Spin} from "antd";
import {UserOutlined} from "@ant-design/icons";
import {queryTenantMates} from "@/api/tenant/tenant-member.api.ts";
import type {UserTenantVO} from "@/types/tenant/tenant.types.ts";
import type {TenantMateVO} from "@/types/tenant/tenant-member.types.ts";

const PAGE_SIZE = 20;

export function TenantMateSelectorPanel(props: {
    tenants: UserTenantVO[];
    onSelect: (tenant: UserTenantVO, mate: TenantMateVO) => void;
}) {
    const {tenants, onSelect} = props;
    const {t} = useTranslation();
    const [tenantId, setTenantId] = useState<string>();
    const [page, setPage] = useState(1);
    const [records, setRecords] = useState<TenantMateVO[]>([]);
    const [total, setTotal] = useState(0);
    const [loading, setLoading] = useState(false);

    const load = useCallback(async (id: string, targetPage: number) => {
        setLoading(true);
        try {
            const data = (await queryTenantMates({tenantId: id, page: targetPage, pageSize: PAGE_SIZE})).data;
            setRecords(data?.records ?? []);
            setTotal(Number(data?.total ?? 0));
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        const initialTenantId = tenants[0]?.tenantId;
        setTenantId(initialTenantId);
        setPage(1);
        if (initialTenantId) void load(initialTenantId, 1);
    }, [tenants, load]);

    const onTenantChange = (id: string) => {
        setTenantId(id);
        setPage(1);
        void load(id, 1);
    };

    const onPageChange = (targetPage: number) => {
        if (!tenantId) return;
        setPage(targetPage);
        void load(tenantId, targetPage);
    };

    const selectedTenant = tenants.find((tenant) => tenant.tenantId === tenantId);

    return (
        <>
            <Select
                className="mb-3 w-full"
                value={tenantId}
                options={tenants.map((tenant) => ({value: tenant.tenantId, label: tenant.tenantName}))}
                onChange={onTenantChange}
                placeholder={t('components.notification.tenantChat.tenantPlaceholder')}
            />
            {loading ? (
                <div className="flex justify-center py-8"><Spin/></div>
            ) : records.length === 0 ? (
                <Empty description={t('components.notification.tenantChat.empty')}/>
            ) : (
                <List
                    dataSource={records}
                    style={{maxHeight: 320, overflow: 'auto'}}
                    renderItem={(mate) => (
                        <List.Item
                            className="cursor-pointer"
                            onClick={() => selectedTenant && onSelect(selectedTenant, mate)}
                        >
                            <List.Item.Meta avatar={<UserOutlined/>} title={mate.nickname}/>
                        </List.Item>
                    )}
                />
            )}
            {total > PAGE_SIZE && (
                <div className="mt-3 flex justify-end">
                    <Pagination
                        current={page}
                        pageSize={PAGE_SIZE}
                        total={total}
                        showSizeChanger={false}
                        onChange={onPageChange}
                    />
                </div>
            )}
        </>
    );
}
