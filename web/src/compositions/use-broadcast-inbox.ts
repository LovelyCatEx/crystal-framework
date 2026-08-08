import useSWR from "swr";
import {useCallback} from "react";
import {
    getBroadcastUnreadCount,
    listUnreadBroadcasts,
    markBroadcastRead,
} from "@/api/message/broadcast.api.ts";
import type {Broadcast} from "@/types/message/broadcast.types.ts";

// Poll cadence for the unread red dot. 30s balances freshness against request volume for a
// header badge that every logged-in user renders.
const UNREAD_REFRESH_INTERVAL_MS = 30_000;

const KEY_UNREAD_LIST = 'broadcast-unread-list';
const KEY_UNREAD_COUNT = 'broadcast-unread-count';

/**
 * Consumer-side broadcast inbox (read-diffusion). Exposes the caller's unread broadcasts and unread
 * count with SWR polling, plus a markRead that revalidates both keys so the list and the header
 * badge stay in sync. Only unread broadcasts are available — the backend has no history endpoint by
 * design. Uses raw useSWR (not the shared useSWRState) because polling needs refreshInterval, which
 * the shared helper does not forward.
 */
export function useBroadcastInbox() {
    const listSwr = useSWR(
        KEY_UNREAD_LIST,
        async () => (await listUnreadBroadcasts()).data ?? [],
        {refreshInterval: UNREAD_REFRESH_INTERVAL_MS},
    );

    const countSwr = useSWR(
        KEY_UNREAD_COUNT,
        async () => Number((await getBroadcastUnreadCount()).data ?? '0'),
        {refreshInterval: UNREAD_REFRESH_INTERVAL_MS},
    );

    const markRead = useCallback(async (broadcastId: string) => {
        await markBroadcastRead(broadcastId);
        await Promise.all([listSwr.mutate(), countSwr.mutate()]);
    }, [listSwr, countSwr]);

    const unreadBroadcasts: Broadcast[] = listSwr.data ?? [];
    const unreadCount: number = countSwr.data ?? 0;

    return {
        unreadBroadcasts,
        unreadCount,
        isLoading: listSwr.isLoading,
        markRead,
        refresh: () => {
            void listSwr.mutate();
            void countSwr.mutate();
        },
    };
}
