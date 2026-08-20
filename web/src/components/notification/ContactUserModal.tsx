import {useCallback, useState} from "react";
import {useTranslation} from "react-i18next";
import {Empty, Input, List, Pagination, Spin, Typography} from "antd";
import {UserOutlined} from "@ant-design/icons";
import {queryContactableUsers} from "@/api/message/message.api.ts";
import type {ContactableUserView} from "@/types/message/message.types.ts";

const {Text} = Typography;

// Backend caps a page at 20; keep in lockstep so the pager never over-requests.
const PAGE_SIZE = 20;

/**
 * User picker for initiating a SYSTEM-scope peer conversation. Backed by the dedicated
 * `/message/contactable-users` endpoint, which is EXACT-MATCH only (username or email) — there is
 * deliberately no browsable directory, so nothing loads until the user searches. Table-modal UX
 * (search + paginated list ≤20/page) mirrors {@link ContactTenantPanel} for consistency.
 */
export function ContactUserPanel(props: {
    onSelect: (user: ContactableUserView) => void;
}) {
    const {onSelect} = props;
    const {t} = useTranslation();

    const [keyword, setKeyword] = useState("");
    const [page, setPage] = useState(1);
    const [records, setRecords] = useState<ContactableUserView[]>([]);
    const [total, setTotal] = useState(0);
    const [loading, setLoading] = useState(false);
    const [searched, setSearched] = useState(false);

    const load = useCallback(async (kw: string, p: number) => {
        setLoading(true);
        try {
            const data = (await queryContactableUsers({keyword: kw || undefined, page: p, pageSize: PAGE_SIZE})).data;
            setRecords(data?.records ?? []);
            setTotal(Number(data?.total ?? 0));
        } finally {
            setLoading(false);
        }
    }, []);

    const onSearch = (kw: string) => {
        const trimmed = kw.trim();
        setKeyword(trimmed);
        setPage(1);
        setSearched(true);
        void load(trimmed, 1);
    };

    const onPageChange = (p: number) => {
        setPage(p);
        void load(keyword, p);
    };

    return (
        <>
            <Input.Search
                allowClear
                placeholder={t('components.notification.contactUser.searchPlaceholder')}
                onSearch={onSearch}
                className="mb-3"
            />
            {loading ? (
                <div className="flex justify-center py-8"><Spin/></div>
            ) : !searched ? (
                <div className="flex justify-center py-8">
                    <Text type="secondary">{t('components.notification.contactUser.hint')}</Text>
                </div>
            ) : records.length === 0 ? (
                <Empty description={t('components.notification.contactUser.empty')}/>
            ) : (
                <List
                    dataSource={records}
                    style={{maxHeight: 320, overflow: 'auto'}}
                    renderItem={(user) => (
                        <List.Item
                            className="cursor-pointer"
                            onClick={() => onSelect(user)}
                        >
                            <List.Item.Meta
                                avatar={<UserOutlined/>}
                                title={user.nickname}
                                description={<Text type="secondary">@{user.username}</Text>}
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
