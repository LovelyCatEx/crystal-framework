import useSWR from "swr";
import {listInboxConversations} from "@/api/message/message.api.ts";
import type {ConversationInboxVO} from "@/types/message/message.types.ts";
import {useLoggedUser} from "@/compositions/use-logged-user.ts";
import {MESSAGE_SWR_KEY_PREFIX} from "@/utils/message-swr-cache.ts";

const KEY_INBOX_CONVERSATIONS = `${MESSAGE_SWR_KEY_PREFIX}inbox-conversations`;

/**
 * The acting user's write-diffusion conversations (peer, tenant-desk, and customer sides alike),
 * each enriched with the counterpart party and unread count, newest-active first. Distinct from
 * {@link useBroadcastInbox}, which owns the read-diffusion announcement stream. Callers refresh
 * after sending or marking a conversation read.
 *
 * [currentTenantId] is the tenant the caller is currently acting as; it is folded into the SWR key so
 * switching org identity refetches with the correct `counterpartInCurrentOrg` flags. Omit it for a
 * plain system-user session.
 */
export function useInboxConversations(currentTenantId?: string) {
    const {userProfile} = useLoggedUser();
    const swr = useSWR(
        userProfile?.id ? [KEY_INBOX_CONVERSATIONS, userProfile.id, currentTenantId ?? null] : null,
        async () => (await listInboxConversations(currentTenantId)).data ?? [],
        {
            revalidateOnFocus: true,
        },
    );

    return {
        conversations: (swr.data ?? []) as ConversationInboxVO[],
        isLoading: !swr.data && swr.isLoading,
        refresh: () => void swr.mutate(),
    };
}
