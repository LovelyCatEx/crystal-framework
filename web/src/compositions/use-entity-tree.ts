import type {ReactNode} from "react";
import {useCallback, useEffect, useRef, useState} from "react";
import type {DataNode} from "antd/es/tree";

/**
 * Minimal shape an item must have so `useEntityTree` can wire it into a tree.
 * `parentId` may be null / undefined for root nodes.
 */
export interface EntityTreeItem {
    id: string;
    parentId?: string | null;
}

export interface UseEntityTreeOptions<T extends EntityTreeItem> {
    /** Fetch the flat list of items. Called whenever `deps` change (and initially). */
    fetch: () => Promise<T[]>;
    /** Renders the `title` of each tree node. */
    renderNodeTitle: (item: T) => ReactNode;
    /**
     * Optional query-string key to sync the currently selected item id with the URL.
     * When set, the hook reads the initial id from `window.location.search` on mount /
     * after each successful fetch, and writes it back on `selectItem()`.
     */
    urlParamKey?: string;
    /** Dependencies that trigger a reload. Behaves like the deps array of `useEffect`. */
    deps: ReadonlyArray<unknown>;
    /** Called if `fetch` throws (typically to show a `message.error`). */
    onFetchError?: () => void;
}

export interface UseEntityTreeReturn<T extends EntityTreeItem> {
    /** The raw flat list returned by `fetch`. */
    items: T[];
    /** The tree structure ready to be passed to antd `<Tree treeData={...} />`. */
    treeData: DataNode[];
    /** Whether a `fetch` call is currently in flight. */
    loading: boolean;
    /** Currently selected item, or `null` when nothing is selected. */
    selectedItem: T | null;
    /**
     * Select or clear the current item. Also syncs to URL when `urlParamKey` is set.
     * Pass `null` to clear.
     */
    selectItem: (id: string | null) => void;
    /** Manually re-run `fetch()` (e.g. after a create/update/delete). */
    refresh: () => Promise<void>;
    /** Convenience shortcut for `selectItem(null)`. */
    clearSelection: () => void;
}

function buildTreeData<T extends EntityTreeItem>(
    items: T[],
    renderNodeTitle: (item: T) => ReactNode
): DataNode[] {
    const map = new Map<string, DataNode>();
    const roots: DataNode[] = [];

    // First pass: create every node.
    items.forEach(item => {
        map.set(item.id, {
            key: item.id,
            title: renderNodeTitle(item),
            children: []
        });
    });

    // Second pass: build parent/child relationships.
    items.forEach(item => {
        const node = map.get(item.id)!;
        if (item.parentId && map.has(item.parentId)) {
            const parent = map.get(item.parentId)!;
            if (!parent.children) parent.children = [];
            parent.children.push(node);
        } else {
            roots.push(node);
        }
    });

    return roots;
}

function readIdFromUrl(paramKey: string | undefined): string | null {
    if (!paramKey) return null;
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get(paramKey);
}

function writeIdToUrl(paramKey: string | undefined, id: string | null): void {
    if (!paramKey) return;
    const url = new URL(window.location.href);
    if (id) {
        url.searchParams.set(paramKey, id);
    } else {
        url.searchParams.delete(paramKey);
    }
    window.history.replaceState({}, '', url.toString());
}

/**
 * Generic hook that turns a flat list of `{ id, parentId }` items into an antd-ready
 * tree, tracks the currently selected item, and optionally keeps the selection in
 * sync with a URL query parameter.
 *
 * @example
 * const { treeData, loading, selectedItem, selectItem, refresh } = useEntityTree({
 *     fetch: () => TenantDepartmentManagerController.list({ tenantId }).then(r => r.data ?? []),
 *     renderNodeTitle: (d) => <span>{d.name}</span>,
 *     urlParamKey: 'departmentId',
 *     deps: [tenantId],
 *     onFetchError: () => message.error(t('...'))
 * });
 */
export function useEntityTree<T extends EntityTreeItem>(
    options: UseEntityTreeOptions<T>
): UseEntityTreeReturn<T> {
    const {fetch, renderNodeTitle, urlParamKey, deps, onFetchError} = options;

    const [items, setItems] = useState<T[]>([]);
    const [treeData, setTreeData] = useState<DataNode[]>([]);
    const [loading, setLoading] = useState(false);
    const [selectedItem, setSelectedItem] = useState<T | null>(null);

    // Latest callbacks captured via refs so the `refresh` identity stays stable even
    // when the caller passes inline arrow functions each render.
    const fetchRef = useRef(fetch);
    const renderNodeTitleRef = useRef(renderNodeTitle);
    const onFetchErrorRef = useRef(onFetchError);
    const urlParamKeyRef = useRef(urlParamKey);
    fetchRef.current = fetch;
    renderNodeTitleRef.current = renderNodeTitle;
    onFetchErrorRef.current = onFetchError;
    urlParamKeyRef.current = urlParamKey;

    const refresh = useCallback(async (): Promise<void> => {
        setLoading(true);
        try {
            const list = await fetchRef.current();
            setItems(list);
            setTreeData(buildTreeData(list, renderNodeTitleRef.current));

            // Re-apply URL-driven selection whenever fresh data arrives. Only auto-
            // selects when the id currently lives in the URL; users can always clear
            // it explicitly via `selectItem(null)`.
            const idFromUrl = readIdFromUrl(urlParamKeyRef.current);
            if (idFromUrl) {
                const found = list.find(it => it.id === idFromUrl) ?? null;
                if (found) setSelectedItem(found);
            }
        } catch {
            onFetchErrorRef.current?.();
        } finally {
            setLoading(false);
        }
    }, []);

    const selectItem = useCallback((id: string | null) => {
        if (!id) {
            setSelectedItem(null);
            writeIdToUrl(urlParamKeyRef.current, null);
            return;
        }
        // Read the latest items via a functional-style lookup on the current state.
        setItems(prev => {
            const found = prev.find(it => it.id === id) ?? null;
            setSelectedItem(found);
            writeIdToUrl(urlParamKeyRef.current, found ? found.id : null);
            return prev;
        });
    }, []);

    const clearSelection = useCallback(() => selectItem(null), [selectItem]);

    // Load on mount + whenever `deps` change. Clear selection first so stale items
    // from a previous scope (e.g. previous tenant) don't leak into the new fetch.
    useEffect(() => {
        setSelectedItem(null);
        void refresh();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, deps);

    return {items, treeData, loading, selectedItem, selectItem, refresh, clearSelection};
}
