import useSWR from "swr";
import {listInboxConversations} from "@/api/message/message.api.ts";
import type {ConversationInboxVO} from "@/types/message/message.types.ts";

const KEY_INBOX_CONVERSATIONS = 'inbox-conversations';

/**
 * The acting user's write-diffusion conversations (peer, tenant-desk, and customer sides alike),
 * each enriched with the counterpart party and unread count, newest-active first. Distinct from
 * {@link useBroadcastInbox}, which owns the read-diffusion announcement stream. Callers refresh
 * after sending or marking a conversation read.
 */
export function useInboxConversations() {
    const swr = useSWR(
        KEY_INBOX_CONVERSATIONS,
        async () => (await listInboxConversations()).data ?? [],
    );

    return {
        conversations: (swr.data ?? []) as ConversationInboxVO[],
        isLoading: !swr.data && swr.isLoading,
        refresh: () => void swr.mutate(),
    };
}
