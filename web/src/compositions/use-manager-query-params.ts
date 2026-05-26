import {useCallback, useMemo, useRef} from 'react';
import {useSearchParams} from 'react-router-dom';

/**
 * Reserved URL param keys used internally by EntityTable.
 * These are handled separately via initialQueryValues.
 */
const RESERVED_KEYS: readonly string[] = ['page', 'pageSize', 'searchKeyword', 'startTime', 'endTime'];

export interface UseManagerQueryParamsOptions {
    /**
     * Whether to sync query params to URL search params.
     * Default: true
     */
    enabled?: boolean;
}

export interface UseManagerQueryParamsReturn {
    /**
     * Get the initial value of a query param from the URL.
     * Use this to initialize your filter state.
     *
     * @example
     * const [filterUserId, setFilterUserId] = useState<string | undefined>(getInitialParam('userId'));
     */
    getInitialParam: (key: string) => string | undefined;

    /**
     * Get all initial params from the URL (excluding reserved keys).
     * Useful for bulk initialization.
     */
    getAllInitialParams: () => Record<string, string>;

    /**
     * Initial values for EntityTable's built-in fields (page, pageSize, searchKeyword).
     * Pass this directly to ManagerPageContainer/EntityTable as `initialQueryValues`.
     */
    initialQueryValues: {
        page?: number;
        pageSize?: number;
        searchKeyword?: string;
    };

    /**
     * Sync the current query params to the URL.
     * Called by EntityTable internally after each query.
     * Pass this directly to ManagerPageContainer/EntityTable as `queryParamsSync`.
     */
    syncToUrl: (params: Record<string, unknown>) => void;

    /**
     * Whether URL sync is enabled.
     */
    enabled: boolean;
}

/**
 * Hook for syncing manager page query params with URL search params.
 *
 * This enables:
 * 1. Sharing URLs with query conditions pre-filled
 * 2. Restoring query state when navigating back to a page
 *
 * Usage in a manager page:
 * ```tsx
 * const { getInitialParam, syncToUrl, initialQueryValues } = useManagerQueryParams();
 *
 * // Initialize filter state from URL
 * const [filterUserId, setFilterUserId] = useState<string | undefined>(getInitialParam('userId'));
 *
 * // Pass to ManagerPageContainer
 * <ManagerPageContainer
 *     queryParamsSync={syncToUrl}
 *     initialQueryValues={initialQueryValues}
 *     ...
 * />
 * ```
 */
export function useManagerQueryParams(options?: UseManagerQueryParamsOptions): UseManagerQueryParamsReturn {
    const enabled = options?.enabled !== false;
    const [searchParams, setSearchParams] = useSearchParams();

    // Capture initial params on first render only
    const initialParamsRef = useRef<Record<string, string> | null>(null);
    const initialQueryValuesRef = useRef<{ page?: number; pageSize?: number; searchKeyword?: string; startTime?: number; endTime?: number } | null>(null);

    if (initialParamsRef.current === null) {
        const params: Record<string, string> = {};
        const queryValues: { page?: number; pageSize?: number; searchKeyword?: string; startTime?: number; endTime?: number } = {};

        searchParams.forEach((value, key) => {
            if (!RESERVED_KEYS.includes(key)) {
                params[key] = value;
                return;
            }
            if (key === 'page') {
                const num = Number.parseInt(value, 10);
                if (!Number.isNaN(num) && num > 0) queryValues.page = num;
            } else if (key === 'pageSize') {
                const num = Number.parseInt(value, 10);
                if (!Number.isNaN(num) && num > 0) queryValues.pageSize = num;
            } else if (key === 'searchKeyword') {
                if (value) queryValues.searchKeyword = value;
            } else if (key === 'startTime') {
                const num = Number(value);
                if (!Number.isNaN(num)) queryValues.startTime = num;
            } else if (key === 'endTime') {
                const num = Number(value);
                if (!Number.isNaN(num)) queryValues.endTime = num;
            }
        });

        initialParamsRef.current = params;
        initialQueryValuesRef.current = queryValues;
    }

    const getInitialParam = useCallback((key: string): string | undefined => {
        return initialParamsRef.current?.[key] ?? undefined;
    }, []);

    const getAllInitialParams = useCallback((): Record<string, string> => {
        return { ...(initialParamsRef.current ?? {}) };
    }, []);

    const syncToUrl = useCallback((params: Record<string, unknown>) => {
        if (!enabled) return;

        const newSearchParams = new URLSearchParams();

        for (const [key, value] of Object.entries(params)) {
            if (value === undefined || value === null || value === '') continue;
            newSearchParams.set(key, String(value));
        }

        // Avoid unnecessary setSearchParams calls that would trigger re-renders and infinite loops
        const newStr = newSearchParams.toString();
        const currentStr = new URLSearchParams(window.location.search).toString();
        if (newStr === currentStr) return;

        setSearchParams(newSearchParams, { replace: true });
    }, [enabled, setSearchParams]);

    const initialQueryValues = useMemo(() => initialQueryValuesRef.current ?? {}, []);

    return useMemo(() => ({
        getInitialParam,
        getAllInitialParams,
        initialQueryValues,
        syncToUrl,
        enabled,
    }), [getInitialParam, getAllInitialParams, initialQueryValues, syncToUrl, enabled]);
}
