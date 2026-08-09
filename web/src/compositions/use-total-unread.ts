import useSWR, {mutate as globalMutate} from "swr";
import {getInboxUnreadCount} from "@/api/message/message.api.ts";

// Poll cadence for the aggregate header badge. Matches the broadcast badge's 30s.
const UNREAD_REFRESH_INTERVAL_MS = 30_000;

const KEY_TOTAL_UNREAD = 'total-unread';

/** Revalidate the aggregate header badge from anywhere a read happens (broadcast or conversation). */
export function revalidateTotalUnread(): Promise<unknown> {
    return globalMutate(KEY_TOTAL_UNREAD);
}

/**
 * The header bell's aggregate unread count: conversation messages (personal + every staffed desk)
 * plus announcements across all scopes. Backed by `/message/inbox-unread-count`, whose Long is
 * serialized as a String — parsed with Number here. Distinct from {@link useBroadcastInbox}, which
 * counts announcements only (used for the "System Announcements" row inside the center).
 */
export function useTotalUnread() {
    const countSwr = useSWR(
        KEY_TOTAL_UNREAD,
        async () => Number((await getInboxUnreadCount()).data ?? 0),
        {refreshInterval: UNREAD_REFRESH_INTERVAL_MS},
    );

    return {
        unreadCount: countSwr.data ?? 0,
        refresh: () => void countSwr.mutate(),
    };
}
