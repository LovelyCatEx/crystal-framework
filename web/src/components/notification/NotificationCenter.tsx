import {type CSSProperties, useEffect, useLayoutEffect, useMemo, useRef, useState} from "react";
import {useTranslation} from "react-i18next";
import {Badge, Button, Empty, List, Segmented, Spin, Tag, theme, Typography} from "antd";
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
import type {
    ContactableTenantView,
    ContactableUserView,
    ConversationInboxVO,
} from "@/types/message/message.types.ts";
import type {UserTenantVO} from "@/types/tenant/tenant.types.ts";
import type {TenantMateVO} from "@/types/tenant/tenant-member.types.ts";
import {StartConversationModal} from "./StartConversationModal.tsx";
import {ConversationPanel, type ConversationTarget} from "./ConversationPanel.tsx";
import {useSystemIntegrated} from "@/context/SystemIntegratedContext.tsx";
import {SystemModuleKey} from "@/router/system-module-menu-paths.ts";

const {useToken} = theme;
const {Text, Paragraph, Title} = Typography;

// Scroll within this many px of the top triggers loading the previous (older) page.
const LOAD_MORE_THRESHOLD_PX = 8;

/** The single built-in read-diffusion conversation: system broadcasts. Also used as an active-key sentinel. */
enum NotificationConversationKind {
    SYSTEM_BROADCAST = 'system-broadcast',
}

const {SYSTEM_BROADCAST} = NotificationConversationKind;

/** Which identity perspective the left list is showing. */
type IdentityTab = 'system' | 'tenant';

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
 * a Modal (header bell) and inline in a page.
 *
 * When the user has a current tenant (authenticated tenant session), the left column shows an
 * identity-tab switcher between «System User» and «Current Org»:
 *
 *   System tab  — SYSTEM-scope peer chats + service-desk contacts the user initiated (USER→TENANT).
 *   Org tab     — TENANT(currentTenant)-scope member chats + desk conversations the user staffs +
 *                 External section (SYSTEM-scope peer chats duplicated here, plus any TENANT-scope
 *                 conversations from other orgs addressed to the user).
 *
 * When there is no current tenant the switcher is hidden and only the system view is shown.
 *
 * [fillParent] makes it stretch to the parent's height (full-page use). [className] / [style] pass
 * through to the root so the caller decides frame and background.
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
    const {userProfile} = useLoggedUser();
    const {currentTenant} = useUserTenants();
    const {isModuleEnabled} = useSystemIntegrated();
    // Fold the currently-acting tenant into the fetch so `counterpartInCurrentOrg` is computed against
    // the org I am logged into; switching org identity refetches with the correct flags.
    const {conversations, refresh: refreshInbox} = useInboxConversations(currentTenant?.tenantId);

    const systemPeerEnabled = isModuleEnabled(SystemModuleKey.MESSAGE_SYSTEM_PEER);
    const tenantScopeEnabled = isModuleEnabled(SystemModuleKey.MESSAGE_TENANT_SCOPE);
    const tenantDeskEnabled = isModuleEnabled(SystemModuleKey.MESSAGE_TENANT_DESK);

    // Hide start conversation button if all message features are disabled
    const canStartConversation = systemPeerEnabled || tenantScopeEnabled || tenantDeskEnabled;
    // Show identity switcher if: (1) tenant scope is enabled, OR (2) tenant desk is enabled (members need to handle desk conversations)
    const showIdentitySwitcher = currentTenant != null && (tenantScopeEnabled || tenantDeskEnabled);

    const [activeKey, setActiveKey] = useState<string>(SYSTEM_BROADCAST);
    const [drafts, setDrafts] = useState<ConversationTarget[]>([]);
    const [startConversationOpen, setStartConversationOpen] = useState(false);
    // Identity tab: only meaningful when currentTenant is set.
    const [identityTab, setIdentityTab] = useState<IdentityTab>('system');

    // Switching the identity tab or acting tenant resets the right panel to blank so a conversation
    // opened under the previous identity can't keep sending under the wrong one.
    const firstRunRef = useRef(true);
    useEffect(() => {
        if (firstRunRef.current) { firstRunRef.current = false; return; }
        setActiveKey('');
    }, [currentTenant?.tenantId]);

    const inboxTargets: ConversationTarget[] = useMemo(
        () => conversations.map((c: ConversationInboxVO) => ({
            conversationId: c.conversationId,
            title: c.counterpartName ?? (c.counterpartId ?? ''),
            viewingPartyType: c.viewingPartyType,
            viewingPartyId: c.viewingPartyId,
            viewingPartyName: c.viewingPartyName,
            counterpartType: c.counterpartType,
            counterpartId: c.counterpartId,
            counterpartInCurrentOrg: c.counterpartInCurrentOrg,
            scopeType: c.scopeType,
            scopeId: c.scopeId,
            unreadCount: c.unreadCount,
        })),
        [conversations],
    );

    // Drafts not yet represented by a fetched conversation (dedup by viewing identity + counterpart).
    const materializedKeys = useMemo(() => new Set(inboxTargets.map(targetKey)), [inboxTargets]);
    const targets = [
        ...inboxTargets,
        ...drafts.filter((d) => !materializedKeys.has(targetKey(d))),
    ];
    const activeTarget = targets.find((it) => targetKey(it) === activeKey) ?? null;

    // ── System-tab partitions ──────────────────────────────────────────────────
    // Peer chats: SYSTEM scope, I am a plain USER.
    const systemPeerTargets = targets.filter(
        (it) => it.scopeType === ScopeType.SYSTEM && it.viewingPartyType === PartyType.USER,
    );
    // Service-desk contacts I initiated: TENANT scope, I am USER, counterpart is TENANT.
    const deskContactTargets = targets.filter(
        (it) => it.scopeType === ScopeType.TENANT
            && it.viewingPartyType === PartyType.USER
            && it.counterpartType === PartyType.TENANT,
    );

    // ── Org-tab partitions (only relevant when currentTenant is set) ───────────
    const myTenantId = currentTenant?.tenantId ?? null;

    // Internal member conversations: scoped to my current org, held as a USER, counterpart is a
    // member of my current org. Outside people I contacted as an org member (same scope but a
    // non-member counterpart) fall through to the External section below.
    const orgMemberTargets = targets.filter(
        (it) => it.scopeType === ScopeType.TENANT
            && it.scopeId === myTenantId
            && it.viewingPartyType === PartyType.USER
            && it.counterpartType === PartyType.USER
            && it.counterpartInCurrentOrg,
    );
    // Desk conversations I staff (I am the TENANT party).
    const deskByViewing = useMemo(() => {
        const map = new Map<string, {id: string; name: string; items: ConversationTarget[]}>();
        for (const it of targets) {
            if (it.viewingPartyType !== PartyType.TENANT || it.viewingPartyId == null) continue;
            const section = map.get(it.viewingPartyId)
                ?? {id: it.viewingPartyId, name: it.viewingPartyName ?? it.viewingPartyId, items: []};
            section.items.push(it);
            map.set(it.viewingPartyId, section);
        }
        return [...map.values()];
    }, [targets]);
    // No External section on the Org tab: a TENANT-scoped conversation only ever exists between two
    // members of that org (internal), so anything external is a SYSTEM-scope thread shown — tagged
    // "external" — under the System tab instead.

    const openTarget = (target: ConversationTarget) => {
        setActiveKey(targetKey(target));
        if (target.conversationId && target.unreadCount > 0) {
            void markConversationRead(target.conversationId).then(() => {
                refreshInbox();
                void revalidateTotalUnread();
            });
        }
    };

    // Materialize a draft into the list (or reopen it if a conversation already exists for its key).
    const openDraft = (draft: ConversationTarget) => {
        setStartConversationOpen(false);
        const key = targetKey(draft);
        const existing = inboxTargets.find((it) => targetKey(it) === key);
        if (existing) { openTarget(existing); return; }
        setDrafts((prev) => (prev.some((d) => targetKey(d) === key) ? prev : [...prev, draft]));
        setActiveKey(key);
    };

    const onTenantPicked = (tenant: ContactableTenantView) => {
        // Contacting a tenant desk is always a system-user identity act (I am the customer). A tenant
        // desk is never an org "member", so this draft lives under the System tab regardless.
        openDraft({
            conversationId: null,
            title: tenant.name,
            viewingPartyType: PartyType.USER,
            viewingPartyId: userProfile?.id ?? null,
            viewingPartyName: null,
            counterpartType: PartyType.TENANT,
            counterpartId: tenant.id,
            counterpartInCurrentOrg: false,
            scopeType: ScopeType.TENANT,
            scopeId: tenant.id,
            unreadCount: 0,
        });
    };

    // System identity → a SYSTEM-scope peer chat with any user found by exact search. Never carries
    // a tenant scope, so it stays isolated from org-identity threads. counterpartInCurrentOrg is left
    // false on the draft; the backend recomputes it when the first message materializes the thread.
    const onUserPicked = (user: ContactableUserView) => {
        openDraft({
            conversationId: null,
            title: user.nickname,
            viewingPartyType: PartyType.USER,
            viewingPartyId: userProfile?.id ?? null,
            viewingPartyName: null,
            counterpartType: PartyType.USER,
            counterpartId: user.id,
            counterpartInCurrentOrg: false,
            scopeType: ScopeType.SYSTEM,
            scopeId: null,
            unreadCount: 0,
        });
    };

    // Org identity → a TENANT-scope member chat within the current org. The picker only surfaces
    // members of the acting org, so scope is always the current tenant and both parties share it.
    const onOrgMemberPicked = (tenant: UserTenantVO, mate: TenantMateVO) => {
        openDraft({
            conversationId: null,
            title: mate.nickname,
            viewingPartyType: PartyType.USER,
            viewingPartyId: userProfile?.id ?? null,
            viewingPartyName: null,
            counterpartType: PartyType.USER,
            counterpartId: mate.userId,
            counterpartInCurrentOrg: true,
            scopeType: ScopeType.TENANT,
            scopeId: tenant.tenantId,
            unreadCount: 0,
        });
    };

    // First message materialized the draft — refetch so it folds into the fetched list.
    const onEstablished = () => {
        void refreshInbox();
        void revalidateTotalUnread();
    };

    const renderTargetItem = (target: ConversationTarget, showExternalTag = false) => {
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
                        {showExternalTag && (
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

    const systemBroadcastRow = (
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
    );

    /** Left-column contents for the System-identity tab. */
    const systemTabList = (
        <>
            {systemBroadcastRow}
            {systemPeerTargets.length > 0 && (
                <List
                    header={sectionHeader(t('components.notification.conversations.personal'))}
                    dataSource={systemPeerTargets}
                    // A peer whose counterpart is not a member of the org I'm logged into is an
                    // external contact — tag it so cross-org threads read clearly in the personal list.
                    renderItem={(it) => renderTargetItem(it, !it.counterpartInCurrentOrg)}
                />
            )}
            {deskContactTargets.length > 0 && (
                <List
                    header={sectionHeader(t('components.notification.conversations.deskContactTag'))}
                    dataSource={deskContactTargets}
                    renderItem={(it) => renderTargetItem(it)}
                />
            )}
        </>
    );

    /** Left-column contents for the Org-identity tab. */
    const orgTabList = (
        <>
            {orgMemberTargets.length > 0 && (
                <List
                    header={sectionHeader(t('components.notification.conversations.membersTag'))}
                    dataSource={orgMemberTargets}
                    renderItem={(it) => renderTargetItem(it)}
                />
            )}
            {deskByViewing.map((desk) => (
                <List
                    key={desk.id}
                    header={sectionHeader(
                        `${desk.name} · ${t('components.notification.conversations.deskTag')}`,
                    )}
                    dataSource={desk.items}
                    renderItem={(it) => renderTargetItem(it)}
                />
            ))}
        </>
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
                    {canStartConversation && (
                        <Button
                            type="text"
                            size="small"
                            icon={<PlusOutlined/>}
                            onClick={() => setStartConversationOpen(true)}
                        >
                            {t('components.notification.startConversation.start')}
                        </Button>
                    )}
                </div>
                {/* Identity switcher — only rendered when the user has an authenticated tenant and tenant scope is enabled. */}
                {showIdentitySwitcher && (
                    <div style={{padding: '0 12px 8px'}}>
                        <Segmented
                            block
                            size="small"
                            value={identityTab}
                            onChange={(v) => {
                                setIdentityTab(v as IdentityTab);
                                setActiveKey('');
                            }}
                            options={[
                                {label: t('components.notification.conversations.systemTab'), value: 'system'},
                                {label: currentTenant.tenantName, value: 'tenant'},
                            ]}
                        />
                    </div>
                )}
                <div className="flex-1 overflow-auto">
                    {identityTab === 'system' || !showIdentitySwitcher ? systemTabList : orgTabList}
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
                identity={currentTenant == null ? 'system' : identityTab}
                currentTenant={currentTenant}
                onClose={() => setStartConversationOpen(false)}
                onUserSelect={onUserPicked}
                onTenantSelect={onTenantPicked}
                onOrgMemberSelect={onOrgMemberPicked}
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
            el.scrollTop = el.scrollHeight;
            if (ordered.length > 0) initializedRef.current = true;
        } else if (prevScrollHeightRef.current > 0) {
            el.scrollTop = el.scrollHeight - prevScrollHeightRef.current;
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
