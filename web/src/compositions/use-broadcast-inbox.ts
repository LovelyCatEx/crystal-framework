import useSWR, {mutate as globalMutate} from "swr";
import useSWRInfinite from "swr/infinite";
import {revalidateTotalUnread} from "@/compositions/use-total-unread.ts";
import {useCallback} from "react";
import {
    getBroadcastUnreadCount,
    listBroadcastHistory,
    markBroadcastRead,
} from "@/api/message/broadcast.api.ts";
import type {BroadcastInboxItem} from "@/types/message/broadcast.types.ts";

// Poll cadence for the unread red dot. 30s balances freshness against request volume for a
// header badge that every logged-in user renders.
const UNREAD_REFRESH_INTERVAL_MS = 30_000;

// Page size for the history "load more" list; matches backend MAX_BROADCAST_PAGE_SIZE (20).
const HISTORY_PAGE_SIZE = 20;

const KEY_UNREAD_COUNT = 'broadcast-unread-count';
const KEY_HISTORY = 'broadcast-history';

/** Revalidate the header unread badge. Shared so history's markRead also drops the header dot. */
function revalidateUnread(): Promise<unknown> {
    return globalMutate(KEY_UNREAD_COUNT);
}

/**
 * Consumer-side unread badge (read-diffusion). Polls only the unread *count* — the full unread list
 * is not fetched here because nothing renders it; {@link useBroadcastHistory} owns list display
 * (read + expired included). Uses raw useSWR (not the shared useSWRState) because polling needs
 * refreshInterval, which the shared helper does not forward.
 */
export function useBroadcastInbox() {
    const countSwr = useSWR(
        KEY_UNREAD_COUNT,
        async () => Number((await getBroadcastUnreadCount()).data ?? '0'),
        {refreshInterval: UNREAD_REFRESH_INTERVAL_MS},
    );

    return {
        unreadCount: countSwr.data ?? 0,
        refresh: () => void countSwr.mutate(),
    };
}

/**
 * Consumer-side broadcast history (read-diffusion). Paginated "load more" list of every broadcast
 * visible to the caller — read and expired included, newest first — each tagged with `read`. Its
 * markRead revalidates the shared unread list + badge so reading a broadcast here also clears the
 * header dot. Uses useSWRInfinite for accumulate-on-scroll paging.
 */
export function useBroadcastHistory() {
    const swr = useSWRInfinite(
        (index: number, previous: {hasMore: boolean} | null) => {
            if (previous && !previous.hasMore) return null;
            return [KEY_HISTORY, index + 1];
        },
        async ([, page]: [string, number]) => {
            const data = (await listBroadcastHistory(page, HISTORY_PAGE_SIZE)).data;
            return {
                records: data?.records ?? [],
                hasMore: page < (data?.totalPages ?? 0),
            };
        },
    );

    const pages = swr.data ?? [];
    const records: BroadcastInboxItem[] = pages.flatMap((it) => it.records);
    const hasMore: boolean = pages.length > 0 ? pages[pages.length - 1].hasMore : false;
    const isLoadingInitial: boolean = !swr.data && swr.isLoading;
    const isLoadingMore: boolean = swr.isValidating && pages.length > 0;

    const markRead = useCallback(async (broadcastId: string) => {
        await markBroadcastRead(broadcastId);
        await Promise.all([swr.mutate(), revalidateUnread(), revalidateTotalUnread()]);
    }, [swr]);

    return {
        history: records,
        hasMore,
        isLoadingInitial,
        isLoadingMore,
        loadMore: () => void swr.setSize(swr.size + 1),
        markRead,
    };
}
