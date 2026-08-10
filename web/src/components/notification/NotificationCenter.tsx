import {type CSSProperties, useLayoutEffect, useMemo, useRef, useState} from "react";
import {useTranslation} from "react-i18next";
import {Badge, Button, Empty, List, Spin, Tag, theme, Typography} from "antd";
import {MessageOutlined, NotificationOutlined, PlusOutlined, ShopOutlined, UserOutlined} from "@ant-design/icons";
import dayjs from "dayjs";
import {useBroadcastHistory, useBroadcastInbox} from "@/compositions/use-broadcast-inbox.ts";
import {useInboxConversations} from "@/compositions/use-inbox-conversations.ts";
import {useLoggedUser} from "@/compositions/use-logged-user.ts";
import {revalidateTotalUnread} from "@/compositions/use-total-unread.ts";
import {markConversationRead} from "@/api/message/message.api.ts";
import {useUserTenants} from "@/compositions/use-tenant.ts";
import type {Broadcast, BroadcastInboxItem} from "@/types/message/broadcast.types.ts";
import {PartyType, ScopeType} from "@/types/message/broadcast.types.ts";
import type {ContactableTenantView, ConversationInboxVO} from "@/types/message/message.types.ts";
import type {UserTenantVO} from "@/types/tenant/tenant.types.ts";
import type {TenantMateVO} from "@/types/tenant/tenant-member.types.ts";
import {StartConversationModal} from "./StartConversationModal.tsx";
import {ConversationPanel, type ConversationTarget} from "./ConversationPanel.tsx";

const {useToken} = theme;
const {Text, Paragraph, Title} = Typography;

// Scroll within this many px of the top triggers loading the previous (older) page.
const LOAD_MORE_THRESHOLD_PX = 8;

/** The single built-in read-diffusion conversation: system broadcasts. Also used as an active-key sentinel. */
enum NotificationConversationKind {
    SYSTEM_BROADCAST = 'system-broadcast',
}

const {SYSTEM_BROADCAST} = NotificationConversationKind;

/**
 * Left-list / active-key / dedup identity: the viewing identity plus the counterpart. Stable across
 * a draft materializing (conversationId is data, not identity), and distinct across scopes and each
 * side of a self-conversation — where the user both staffs and contacted one desk — since their
 * viewing and counterpart parties are swapped. Assumes at most one conversation per scoped party set,
 * which the backend guarantees by reusing the conversation for a given dedupe key.
 */
function targetKey(target: ConversationTarget): string {
    return `${target.scopeType}:${target.scopeId}|${target.viewingPartyType}:${target.viewingPartyId}|${target.counterpartType}:${target.counterpartId}`;
}

/** True when a broadcast's expireTime is in the past. Expired items still appear in history. */
function isExpired(broadcast: Broadcast): boolean {
    return broadcast.expireTime != null && Number(broadcast.expireTime) <= Date.now();
}

/**
 * Embeddable notification center: a left conversation list + right content panel. Works both inside
 * a Modal (header bell) and inline in a page. The left list merges the built-in read-diffusion
 * "System Announcements" row with the caller's write-diffusion conversations (peer / tenant-desk /
 * customer) fetched from the inbox. A newly started tenant contact is held locally as a virtual draft
 * until its first message materializes it server-side, at which point it folds into the fetched list.
 *
 * The component carries no outer frame of its own (only the inner column divider); the caller supplies
 * the border — the header bell's Modal, or the page container. [fillParent] makes it stretch to the
 * parent's height (full-page use) instead of the fixed modal height. [className] / [style] pass
 * through to the root so the caller decides visuals like background (merged after the built-in
 * height, so caller styles win).
 */
export function NotificationCenter(props: {
    fillParent?: boolean;
    className?: string;
    style?: CSSProperties;
} = {}) {
    const {fillParent = false, className, style} = props;
    const {t} = useTranslation();
    const {token} = useToken();
    const {unreadCount} = useBroadcastInbox();
    const {conversations, refresh: refreshInbox} = useInboxConversations();
    const {userProfile} = useLoggedUser();
    const {joinedTenants, currentTenant} = useUserTenants();
    const hasTenants = (joinedTenants?.length ?? 0) > 0;

    const [activeKey, setActiveKey] = useState<string>(SYSTEM_BROADCAST);
    const [drafts, setDrafts] = useState<ConversationTarget[]>([]);
    const [startConversationOpen, setStartConversationOpen] = useState(false);

    const inboxTargets: ConversationTarget[] = useMemo(
        () => conversations.map((c: ConversationInboxVO) => ({
            conversationId: c.conversationId,
            title: c.counterpartName ?? (c.counterpartId ?? ''),
            viewingPartyType: c.viewingPartyType,
            viewingPartyId: c.viewingPartyId,
            viewingPartyName: c.viewingPartyName,
            counterpartType: c.counterpartType,
            counterpartId: c.counterpartId,
            scopeType: c.scopeType,
            scopeId: c.scopeId,
            unreadCount: c.unreadCount,
        })),
        [conversations],
    );

    // Drafts not yet represented by a fetched conversation (dedup by viewing identity + counterpart).
    const materializedKeys = useMemo(
        () => new Set(inboxTargets.map(targetKey)),
        [inboxTargets],
    );
    const targets = [
        ...inboxTargets,
        ...drafts.filter((d) => !materializedKeys.has(targetKey(d))),
    ];
    const activeTarget = targets.find((it) => targetKey(it) === activeKey) ?? null;

    // Partition by scope and viewing identity: personal, organization members, then staffed desks.
    const personalTargets = targets.filter((it) =>
        it.scopeType === ScopeType.SYSTEM && it.viewingPartyType === PartyType.USER,
    );
    const organizationByScope = new Map<string, {id: string; name: string; items: ConversationTarget[]}>();
    const deskByViewing = new Map<string, {id: string; name: string; items: ConversationTarget[]}>();
    const tenantNameById = new Map((joinedTenants ?? []).map((tenant) => [tenant.tenantId, tenant.tenantName]));
    for (const it of targets) {
        if (it.scopeType !== ScopeType.TENANT) continue;
        if (it.viewingPartyType === PartyType.USER && it.scopeId != null) {
            const section = organizationByScope.get(it.scopeId)
                ?? {id: it.scopeId, name: tenantNameById.get(it.scopeId) ?? it.scopeId, items: []};
            section.items.push(it);
            organizationByScope.set(it.scopeId, section);
        }
        if (it.viewingPartyType === PartyType.TENANT && it.viewingPartyId != null) {
            const section = deskByViewing.get(it.viewingPartyId)
                ?? {id: it.viewingPartyId, name: it.viewingPartyName ?? it.viewingPartyId, items: []};
            section.items.push(it);
            deskByViewing.set(it.viewingPartyId, section);
        }
    }
    const organizationSections = [...organizationByScope.values()];
    const deskSections = [...deskByViewing.values()];

    const openTarget = (target: ConversationTarget) => {
        setActiveKey(targetKey(target));
        if (target.conversationId && target.unreadCount > 0) {
            void markConversationRead(target.conversationId).then(() => {
                refreshInbox();
                void revalidateTotalUnread();
            });
        }
    };

    const onTenantPicked = (tenant: ContactableTenantView) => {
        setStartConversationOpen(false);
        // Contacting a tenant is always a personal-identity conversation (I am the customer).
        const draft: ConversationTarget = {
            conversationId: null,
            title: tenant.name,
            viewingPartyType: PartyType.USER,
            viewingPartyId: userProfile?.id ?? null,
            viewingPartyName: null,
            counterpartType: PartyType.TENANT,
            counterpartId: tenant.id,
            scopeType: ScopeType.TENANT,
            scopeId: tenant.id,
            unreadCount: 0,
        };
        const key = targetKey(draft);
        // Prefer an existing conversation with this tenant over spawning a fresh draft.
        const existing = inboxTargets.find((it) => targetKey(it) === key);
        if (existing) {
            openTarget(existing);
            return;
        }
        setDrafts((prev) => (prev.some((d) => targetKey(d) === key) ? prev : [...prev, draft]));
        setActiveKey(key);
    };

    const onTenantMatePicked = (tenant: UserTenantVO, mate: TenantMateVO) => {
        setStartConversationOpen(false);
        const draft: ConversationTarget = {
            conversationId: null,
            title: mate.nickname,
            viewingPartyType: PartyType.USER,
            viewingPartyId: userProfile?.id ?? null,
            viewingPartyName: null,
            counterpartType: PartyType.USER,
            counterpartId: mate.userId,
            scopeType: ScopeType.TENANT,
            scopeId: tenant.tenantId,
            unreadCount: 0,
        };
        const key = targetKey(draft);
        const existing = inboxTargets.find((it) => targetKey(it) === key);
        if (existing) {
            openTarget(existing);
            return;
        }
        setDrafts((prev) => (prev.some((d) => targetKey(d) === key) ? prev : [...prev, draft]));
        setActiveKey(key);
    };

    // First message materialized the draft — refetch so it folds into the fetched list. The active
    // key is the viewing/counterpart slot, unchanged by materialization, so selection is preserved.
    const onEstablished = () => {
        void refreshInbox();
        void revalidateTotalUnread();
    };

    const renderTargetItem = (target: ConversationTarget) => {
        const key = targetKey(target);
        const isTenant = target.counterpartType === PartyType.TENANT;
        return (
            <List.Item
                className="cursor-pointer"
                style={{
                    paddingInline: 16,
                    background: key === activeKey ? token.colorFillTertiary : undefined,
                }}
                onClick={() => openTarget(target)}
            >
                <List.Item.Meta
                    avatar={isTenant
                        ? <ShopOutlined style={{color: token.colorPrimary}}/>
                        : <UserOutlined style={{color: token.colorPrimary}}/>}
                    title={<div className="flex items-center gap-1">
                        <span>{target.title}</span>
                        {target.scopeType === ScopeType.TENANT && target.scopeId !== currentTenant?.tenantId && (
                            <Tag color="default">{t('components.notification.conversations.external')}</Tag>
                        )}
                    </div>}
                    description={target.conversationId == null
                        ? <Tag icon={<MessageOutlined/>} color="default">
                            {t('components.notification.contact.draftTag')}
                        </Tag>
                        : undefined}
                />
                <Badge count={target.unreadCount} size="small"/>
            </List.Item>
        );
    };

    const sectionHeader = (label: string) => (
        <div style={{padding: '6px 16px'}}>
            <Text type="secondary" style={{fontSize: 12}}>{label}</Text>
        </div>
    );

    return (
        <div
            className={`flex overflow-hidden${className ? ` ${className}` : ''}`}
            style={{height: fillParent ? '100%' : 480, ...style}}
        >
            {/* Left: conversation list */}
            <div
                className="flex flex-col"
                style={{width: 220, borderRight: `1px solid ${token.colorBorderSecondary}`}}
            >
                <div className="flex items-center justify-between" style={{padding: '8px 12px'}}>
                    <Text strong>{t('components.notification.conversations.title')}</Text>
                    <Button
                        type="text"
                        size="small"
                        icon={<PlusOutlined/>}
                        onClick={() => setStartConversationOpen(true)}
                    >
                        {t('components.notification.startConversation.start')}
                    </Button>
                </div>
                <div className="flex-1 overflow-auto">
                    <List
                        dataSource={[SYSTEM_BROADCAST]}
                        renderItem={(kind) => (
                            <List.Item
                                className="cursor-pointer"
                                style={{
                                    paddingInline: 16,
                                    background: kind === activeKey ? token.colorFillTertiary : undefined,
                                }}
                                onClick={() => setActiveKey(kind)}
                            >
                                <List.Item.Meta
                                    avatar={<NotificationOutlined style={{color: token.colorPrimary}}/>}
                                    title={t('components.notification.conversations.systemBroadcast')}
                                />
                                <Badge count={unreadCount} size="small"/>
                            </List.Item>
                        )}
                    />
                    {personalTargets.length > 0 && (
                        <List
                            header={sectionHeader(t('components.notification.conversations.personal'))}
                            dataSource={personalTargets}
                            renderItem={renderTargetItem}
                        />
                    )}
                    {hasTenants && organizationSections.map((organization) => (
                        <List
                            key={organization.id}
                            header={sectionHeader(
                                `${organization.name} · ${t('components.notification.conversations.organizationTag')}`,
                            )}
                            dataSource={organization.items}
                            renderItem={renderTargetItem}
                        />
                    ))}
                    {hasTenants && deskSections.map((desk) => (
                        <List
                            key={desk.id}
                            header={sectionHeader(
                                `${desk.name} · ${t('components.notification.conversations.deskTag')}`,
                            )}
                            dataSource={desk.items}
                            renderItem={renderTargetItem}
                        />
                    ))}
                </div>
            </div>

            {/* Right: content panel */}
            <div className="flex flex-1 flex-col overflow-hidden">
                {activeKey === SYSTEM_BROADCAST && <BroadcastHistoryPanel/>}
                {activeTarget && (
                    <ConversationPanel
                        key={targetKey(activeTarget)}
                        target={activeTarget}
                        onEstablished={onEstablished}
                    />
                )}
            </div>

            <StartConversationModal
                open={startConversationOpen}
                tenants={joinedTenants ?? []}
                onClose={() => setStartConversationOpen(false)}
                onTenantSelect={onTenantPicked}
                onTenantMateSelect={onTenantMatePicked}
            />
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
