import {useLayoutEffect, useMemo, useRef, useState} from "react";
import {useTranslation} from "react-i18next";
import {Badge, Empty, List, Spin, Tag, theme, Typography} from "antd";
import {NotificationOutlined} from "@ant-design/icons";
import dayjs from "dayjs";
import {useBroadcastHistory, useBroadcastInbox} from "@/compositions/use-broadcast-inbox.ts";
import type {Broadcast, BroadcastInboxItem} from "@/types/message/broadcast.types.ts";

const {useToken} = theme;
const {Text, Paragraph, Title} = Typography;

// Scroll within this many px of the top triggers loading the previous (older) page.
const LOAD_MORE_THRESHOLD_PX = 8;

/** The single built-in conversation kind currently wired: read-diffusion system broadcasts. */
export enum NotificationConversationKind {
    SYSTEM_BROADCAST = 'system-broadcast',
}

const {SYSTEM_BROADCAST} = NotificationConversationKind;

/** True when a broadcast's expireTime is in the past. Expired items still appear in history. */
function isExpired(broadcast: Broadcast): boolean {
    return broadcast.expireTime != null && Number(broadcast.expireTime) <= Date.now();
}

/**
 * Embeddable notification center: a left conversation list + right content panel. Designed to work
 * both inside a Modal (header bell) and inline in a page. Today only the read-diffusion
 * "System Announcements" conversation exists; write-diffusion conversations (P2P / tenant) will slot
 * into the same left list later without changing the shell. The right panel is an IM-style history
 * (newest at bottom, scroll up to load older); opening an unread broadcast marks it read.
 */
export function NotificationCenter() {
    const {t} = useTranslation();
    const {token} = useToken();
    const {unreadCount} = useBroadcastInbox();
    const [activeKind, setActiveKind] = useState<NotificationConversationKind>(SYSTEM_BROADCAST);

    return (
        <div
            className="flex overflow-hidden"
            style={{
                height: 480,
                border: `1px solid ${token.colorBorderSecondary}`,
                borderRadius: token.borderRadiusLG,
            }}
        >
            {/* Left: conversation list */}
            <div
                className="flex flex-col"
                style={{width: 220, borderRight: `1px solid ${token.colorBorderSecondary}`}}
            >
                <List
                    dataSource={[SYSTEM_BROADCAST]}
                    renderItem={(kind) => (
                        <List.Item
                            className="cursor-pointer"
                            style={{
                                paddingInline: 16,
                                background: kind === activeKind ? token.colorFillTertiary : undefined,
                            }}
                            onClick={() => setActiveKind(kind)}
                        >
                            <List.Item.Meta
                                avatar={<NotificationOutlined style={{color: token.colorPrimary}}/>}
                                title={t('components.notification.conversations.systemBroadcast')}
                            />
                            <Badge count={unreadCount} size="small"/>
                        </List.Item>
                    )}
                />
            </div>

            {/* Right: content panel */}
            <div className="flex flex-1 flex-col overflow-hidden">
                {activeKind === SYSTEM_BROADCAST && <BroadcastHistoryPanel/>}
            </div>
        </div>
    );
}

/**
 * IM-style history list: every visible broadcast (read + expired included), oldest at top, newest at
 * bottom. Opens scrolled to the bottom; scrolling to the top loads the previous page and keeps the
 * viewport anchored so the list appears to grow upward. Unread items are bold and clickable to read.
 */
function BroadcastHistoryPanel() {
    const {t} = useTranslation();
    const {history, hasMore, isLoadingInitial, isLoadingMore, loadMore, markRead} = useBroadcastHistory();

    const containerRef = useRef<HTMLDivElement>(null);
    const prevScrollHeightRef = useRef(0);
    const initializedRef = useRef(false);

    // Backend returns newest-first; IM shows oldest→newest so the newest sits at the bottom.
    const ordered = useMemo(() => [...history].reverse(), [history]);

    useLayoutEffect(() => {
        const el = containerRef.current;
        if (!el) return;
        if (!initializedRef.current) {
            el.scrollTop = el.scrollHeight; // first content: land on the newest
            if (ordered.length > 0) initializedRef.current = true;
        } else if (prevScrollHeightRef.current > 0) {
            el.scrollTop = el.scrollHeight - prevScrollHeightRef.current; // older page prepended: hold position
            prevScrollHeightRef.current = 0;
        }
    }, [ordered]);

    const onScroll = () => {
        const el = containerRef.current;
        if (!el || !hasMore || isLoadingMore) return;
        if (el.scrollTop <= LOAD_MORE_THRESHOLD_PX) {
            prevScrollHeightRef.current = el.scrollHeight;
            loadMore();
        }
    };

    if (isLoadingInitial) {
        return <div className="flex justify-center py-8"><Spin/></div>;
    }
    if (ordered.length === 0) {
        return <div className="flex flex-1 items-center justify-center">
            <Empty description={t('components.notification.emptyHistory')}/>
        </div>;
    }

    return (
        <div ref={containerRef} onScroll={onScroll} className="flex-1 overflow-auto" style={{padding: 16}}>
            <div className="flex justify-center pb-2" style={{minHeight: 22}}>
                {isLoadingMore && <Spin size="small"/>}
                {!hasMore && !isLoadingMore && (
                    <Text type="secondary" className="text-xs">{t('components.notification.noMore')}</Text>
                )}
            </div>
            <List
                dataSource={ordered}
                renderItem={(item) => (
                    <BroadcastRow
                        broadcast={item}
                        read={item.read}
                        onOpen={item.read ? undefined : () => markRead(item.id)}
                    />
                )}
            />
        </div>
    );
}

/** One broadcast entry. Unread titles are bold and carry a dot; expired ones carry a tag. */
function BroadcastRow(props: {
    broadcast: BroadcastInboxItem;
    read: boolean;
    onOpen?: () => void;
}) {
    const {t} = useTranslation();
    const {token} = useToken();
    const {broadcast, read, onOpen} = props;
    const expired = isExpired(broadcast);

    return (
        <List.Item onClick={onOpen} className={onOpen ? "cursor-pointer" : undefined}>
            <div className="w-full">
                <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                        {!read && <Badge status="processing"/>}
                        <Title level={5} style={{margin: 0, fontWeight: read ? 400 : 600}}>
                            {broadcast.title}
                        </Title>
                        {expired && <Tag>{t('components.notification.expired')}</Tag>}
                    </div>
                    <Text type="secondary" className="text-xs">
                        {dayjs(Number(broadcast.publishTime)).format('YYYY-MM-DD HH:mm')}
                    </Text>
                </div>
                <Paragraph style={{marginBottom: 0, color: token.colorTextSecondary}}>
                    {broadcast.content}
                </Paragraph>
            </div>
        </List.Item>
    );
}
