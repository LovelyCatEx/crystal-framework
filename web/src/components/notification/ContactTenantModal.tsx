import {useCallback, useEffect, useState} from "react";
import {useTranslation} from "react-i18next";
import {Empty, Input, List, Pagination, Spin} from "antd";
import {ShopOutlined} from "@ant-design/icons";
import {queryContactableTenants} from "@/api/message/message.api.ts";
import type {ContactableTenantView} from "@/types/message/message.types.ts";

// Backend caps a page at 20; keep in lockstep so the pager never over-requests.
const PAGE_SIZE = 20;

/**
 * Tenant picker for initiating a customer-service conversation. Deliberately NOT built on
 * EntitySelector / BaseManagerController — it queries the dedicated `/message/contactable-tenants`
 * endpoint so future per-user visibility rules live server-side in one place. Table-modal UX
 * (search + paginated list ≤20/page) mirrors EntitySelector for consistency.
 */
export function ContactTenantPanel(props: {
    onSelect: (tenant: ContactableTenantView) => void;
}) {
    const {onSelect} = props;
    const {t} = useTranslation();

    const [keyword, setKeyword] = useState("");
    const [page, setPage] = useState(1);
    const [records, setRecords] = useState<ContactableTenantView[]>([]);
    const [total, setTotal] = useState(0);
    const [loading, setLoading] = useState(false);

    const load = useCallback(async (kw: string, p: number) => {
        setLoading(true);
        try {
            const data = (await queryContactableTenants({keyword: kw || undefined, page: p, pageSize: PAGE_SIZE})).data;
            setRecords(data?.records ?? []);
            setTotal(Number(data?.total ?? 0));
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        void load("", 1);
    }, [load]);

    const onSearch = (kw: string) => {
        setKeyword(kw);
        setPage(1);
        void load(kw, 1);
    };

    const onPageChange = (p: number) => {
        setPage(p);
        void load(keyword, p);
    };

    return (
        <>
            <Input.Search
                allowClear
                placeholder={t('components.notification.contact.searchPlaceholder')}
                onSearch={onSearch}
                className="mb-3"
            />
            {loading ? (
                <div className="flex justify-center py-8"><Spin/></div>
            ) : records.length === 0 ? (
                <Empty description={t('components.notification.contact.empty')}/>
            ) : (
                <List
                    dataSource={records}
                    style={{maxHeight: 320, overflow: 'auto'}}
                    renderItem={(tenant) => (
                        <List.Item
                            className="cursor-pointer"
                            onClick={() => onSelect(tenant)}
                        >
                            <List.Item.Meta
                                avatar={<ShopOutlined/>}
                                title={tenant.name}
                            />
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
