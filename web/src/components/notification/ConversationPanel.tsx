import {useCallback, useEffect, useRef, useState} from "react";
import {useTranslation} from "react-i18next";
import {Button, Empty, Input, Spin, theme} from "antd";
import {SendOutlined} from "@ant-design/icons";
import dayjs from "dayjs";
import {queryConversationMessages, send, sendAsTenant, sendToTenant} from "@/api/message/message.api.ts";
import type {ApiResponse} from "@/api/system-request.ts";
import type {MsgMessage} from "@/types/message/message.types.ts";
import {PartyType} from "@/types/message/broadcast.types.ts";

const {useToken} = theme;

// Matches backend MAX_CONVERSATION_PAGE_SIZE.
const PAGE_SIZE = 20;

/**
 * One conversation the acting user participates in, in either state:
 *  - virtual (conversationId == null): no backend row yet; sending the first message
 *    materializes it and `onEstablished` hands the real id back.
 *  - real (conversationId set): history is pulled from `/conversation-messages`.
 * The reply route is derived purely from the counterpart party and scope (see {@link routeSend}),
 * so the same panel serves peer, tenant-desk, and customer conversations.
 */
export interface ConversationTarget {
    conversationId: string | null;
    // Display title (counterpart's resolved name, or a virtual contact's tenant name).
    title: string;
    // PartyType typeId of the identity I act as here (0=USER personal, 2=TENANT desk).
    viewingPartyType: number;
    viewingPartyId: string | null;
    // Resolved name of the viewing party — the desk's tenant name; null for personal identity.
    viewingPartyName: string | null;
    // PartyType typeId of the other side (0=USER, 1=SYSTEM, 2=TENANT).
    counterpartType: number;
    counterpartId: string | null;
    // ScopeType typeId of the conversation's isolation boundary (0=SYSTEM, 1=TENANT).
    scopeType: number;
    scopeId: string | null;
    // Unread count for the left-list badge; unused by the panel body.
    unreadCount: number;
}

/**
 * Route a reply from the identity I act as (the viewing party) — unambiguous even when I both
 * staff and contacted the same desk:
 *  - viewing TENANT               → I am the tenant desk → `/send-as-tenant`
 *  - viewing USER, counterpart TENANT → I am the customer → `/send-to-tenant`
 *  - viewing USER, counterpart USER   → peer chat → `/send`
 */
function routeSend(target: ConversationTarget, content: string): Promise<ApiResponse<MsgMessage>> {
    if (target.viewingPartyType === PartyType.TENANT) {
        return sendAsTenant({tenantId: target.viewingPartyId!, targetUserId: target.counterpartId!, content});
    }
    if (target.counterpartType === PartyType.TENANT) {
        return sendToTenant({tenantId: target.counterpartId!, content});
    }
    return send({targetUserId: target.counterpartId!, content});
}

export function ConversationPanel(props: {
    target: ConversationTarget;
    onEstablished: (conversationId: string) => void;
}) {
    const {target, onEstablished} = props;
    const {conversationId} = target;
    const {t} = useTranslation();
    const {token} = useToken();

    const [messages, setMessages] = useState<MsgMessage[]>([]);
    const [loading, setLoading] = useState(false);
    const [draft, setDraft] = useState("");
    const [sending, setSending] = useState(false);
    const bottomRef = useRef<HTMLDivElement>(null);

    const loadMessages = useCallback(async (cid: string) => {
        setLoading(true);
        try {
            const data = (await queryConversationMessages(cid, 1, PAGE_SIZE)).data;
            // Backend returns newest-first; IM shows oldest→newest.
            setMessages([...(data?.records ?? [])].reverse());
        } finally {
            setLoading(false);
        }
    }, []);

    // Virtual conversation starts empty; a real one pulls its history.
    useEffect(() => {
        if (conversationId) {
            void loadMessages(conversationId);
        } else {
            setMessages([]);
        }
    }, [conversationId, loadMessages]);

    useEffect(() => {
        bottomRef.current?.scrollIntoView({behavior: 'auto'});
    }, [messages]);

    const onSend = async () => {
        const content = draft.trim();
        if (!content || sending) return;
        setSending(true);
        try {
            const sent = (await routeSend(target, content)).data;
            setDraft("");
            if (sent) {
                // Virtual → real: first message materialized the conversation.
                if (!conversationId) onEstablished(sent.conversationId);
                await loadMessages(sent.conversationId);
            }
        } finally {
            setSending(false);
        }
    };

    return (
        <div className="flex flex-1 flex-col overflow-hidden">
            {/* Message history */}
            <div className="flex-1 overflow-auto" style={{padding: 16}}>
                {loading ? (
                    <div className="flex justify-center py-8"><Spin/></div>
                ) : messages.length === 0 ? (
                    <div className="flex h-full items-center justify-center">
                        <Empty description={t('components.notification.contact.startHint')}/>
                    </div>
                ) : (
                    messages.map((msg) => {
                        // "Mine" = sent as the identity I'm viewing this conversation as, so a fellow
                        // receptionist's reply (also sent as the tenant) sits on my side in the desk view.
                        const mine = msg.senderPartyType === target.viewingPartyType
                            && (msg.senderPartyId ?? null) === target.viewingPartyId;
                        return (
                            <div key={msg.id} className={`mb-3 flex ${mine ? 'justify-end' : 'justify-start'}`}>
                                <div style={{maxWidth: '70%'}}>
                                    <div
                                        style={{
                                            padding: '8px 12px',
                                            borderRadius: token.borderRadiusLG,
                                            background: mine ? token.colorPrimary : 'transparent',
                                            color: mine ? token.colorWhite : token.colorText,
                                            border: mine ? undefined : `1px solid ${token.colorPrimary}`,
                                            wordBreak: 'break-word',
                                        }}
                                    >
                                        {msg.content}
                                    </div>
                                    <div
                                        className="mt-1 text-xs"
                                        style={{color: token.colorTextTertiary, textAlign: mine ? 'right' : 'left'}}
                                    >
                                        {dayjs(Number(msg.createdTime)).format('YYYY-MM-DD HH:mm')}
                                    </div>
                                </div>
                            </div>
                        );
                    })
                )}
                <div ref={bottomRef}/>
            </div>

            {/* Composer */}
            <div className="flex gap-2" style={{padding: 12, borderTop: `1px solid ${token.colorBorderSecondary}`}}>
                <Input.TextArea
                    value={draft}
                    onChange={(e) => setDraft(e.target.value)}
                    placeholder={t('components.notification.contact.composerPlaceholder')}
                    autoSize={{minRows: 1, maxRows: 3}}
                    onPressEnter={(e) => {
                        if (!e.shiftKey) {
                            e.preventDefault();
                            void onSend();
                        }
                    }}
                />
                <Button
                    type="primary"
                    icon={<SendOutlined/>}
                    loading={sending}
                    disabled={!draft.trim()}
                    onClick={onSend}
                >
                    {t('components.notification.contact.send')}
                </Button>
            </div>
        </div>
    );
}
