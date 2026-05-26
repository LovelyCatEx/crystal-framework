import {useCallback, useMemo, useRef, useState} from 'react';
import {useSearchParams} from 'react-router-dom';

/**
 * Reserved URL param keys used internally by EntityTable.
 * These are handled separately via initialQueryValues.
 */
const RESERVED_KEYS: readonly string[] = ['page', 'pageSize', 'searchKeyword', 'startTime', 'endTime'];

// ---------------------------------------------------------------------------
// Schema-based filter types
// ---------------------------------------------------------------------------

/** Supported field types in the filter schema. */
export type FilterFieldType = 'string' | 'number' | 'boolean';

/** Maps a FilterFieldType to its corresponding TypeScript type. */
type InferFieldType<T extends FilterFieldType> =
    T extends 'string' ? string :
    T extends 'number' ? number :
    T extends 'boolean' ? boolean :
    never;

/** A schema definition: record of field name → field type. */
export type FilterSchema = Record<string, FilterFieldType>;

/** The resulting filters object derived from a schema — all fields are optional. */
export type FiltersFromSchema<S extends FilterSchema> = {
    [K in keyof S]?: InferFieldType<S[K]>
};

// ---------------------------------------------------------------------------
// Options & return types
// ---------------------------------------------------------------------------

export interface UseManagerQueryParamsOptions<S extends FilterSchema = FilterSchema> {
    /**
     * Whether to sync query params to URL search params.
     * Default: true
     */
    enabled?: boolean;

    /**
     * Optional schema for typed filter fields.
     * When provided, the hook manages filter state and returns `filters` / `setFilter`.
     *
     * @example
     * const { filters, setFilter, syncToUrl, initialQueryValues } = useManagerQueryParams({
     *     schema: { userId: 'string', loginMethod: 'number', success: 'string' }
     * });
     */
    schema?: S;
}

export interface UseManagerQueryParamsReturn<S extends FilterSchema = FilterSchema> {
    /**
     * Get the initial value of a query param from the URL.
     * Use this to initialize your own filter state when not using schema mode.
     *
     * @example
     * const [filterUserId, setFilterUserId] = useState<string | undefined>(getInitialParam('userId'));
     */
    getInitialParam: (key: string) => string | undefined;

    /**
     * Get all initial params from the URL (excluding reserved keys).
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
        startTime?: number;
        endTime?: number;
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

    // --- Schema mode only ---

    /**
     * Typed filter values derived from the schema.
     * Only available when `schema` is provided.
     * Pass this directly to ManagerPageContainer/EntityTable as `extraQueryParams`.
     *
     * @example
     * <ManagerPageContainer extraQueryParams={filters} ... />
     */
    filters: FiltersFromSchema<S>;

    /**
     * Update a single filter field.
     * Passing `undefined` clears the field.
     * Only available when `schema` is provided.
     *
     * @example
     * setFilter('userId', 'abc123');
     * setFilter('userId', undefined); // clear
     */
    setFilter: <K extends keyof S>(key: K, value: InferFieldType<S[K]> | undefined) => void;
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function parseValue(raw: string, type: FilterFieldType): string | number | boolean | undefined {
    if (type === 'number') {
        const n = Number(raw);
        return Number.isNaN(n) ? undefined : n;
    }
    if (type === 'boolean') {
        if (raw === 'true') return true;
        if (raw === 'false') return false;
        return undefined;
    }
    return raw || undefined;
}

// ---------------------------------------------------------------------------
// Hook
// ---------------------------------------------------------------------------

/**
 * Hook for syncing manager page query params with URL search params.
 *
 * **Schema mode (recommended)** — declare your filter fields once, get typed state back:
 * ```tsx
 * const { filters, setFilter, syncToUrl, initialQueryValues } = useManagerQueryParams({
 *     schema: { userId: 'string', loginMethod: 'number', success: 'string' }
 * });
 *
 * // Use filters directly in controls
 * <Input defaultValue={filters.userId} onChange={(e) => setFilter('userId', e.target.value || undefined)} />
 *
 * // Pass to ManagerPageContainer — no queryParamsProvider needed
 * <ManagerPageContainer
 *     extraQueryParams={filters}
 *     queryParamsSync={syncToUrl}
 *     initialQueryValues={initialQueryValues}
 * />
 * ```
 *
 * **Manual mode (legacy / advanced)** — manage your own state and use queryParamsProvider:
 * ```tsx
 * const { getInitialParam, syncToUrl, initialQueryValues } = useManagerQueryParams();
 * const [filterUserId, setFilterUserId] = useState(getInitialParam('userId'));
 * ```
 */
export function useManagerQueryParams<S extends FilterSchema = FilterSchema>(
    options?: UseManagerQueryParamsOptions<S>
): UseManagerQueryParamsReturn<S> {
    const enabled = options?.enabled !== false;
    const schema = options?.schema;
    const [searchParams, setSearchParams] = useSearchParams();

    // Capture initial params on first render only (ref = no re-render on URL change)
    const initialParamsRef = useRef<Record<string, string> | null>(null);
    const initialQueryValuesRef = useRef<{
        page?: number; pageSize?: number; searchKeyword?: string; startTime?: number; endTime?: number;
    } | null>(null);

    if (initialParamsRef.current === null) {
        const params: Record<string, string> = {};
        const queryValues: typeof initialQueryValuesRef.current = {};

        searchParams.forEach((value, key) => {
            if (!RESERVED_KEYS.includes(key)) {
                params[key] = value;
                return;
            }
            if (key === 'page') {
                const n = Number.parseInt(value, 10);
                if (!Number.isNaN(n) && n > 0) queryValues!.page = n;
            } else if (key === 'pageSize') {
                const n = Number.parseInt(value, 10);
                if (!Number.isNaN(n) && n > 0) queryValues!.pageSize = n;
            } else if (key === 'searchKeyword') {
                if (value) queryValues!.searchKeyword = value;
            } else if (key === 'startTime') {
                const n = Number(value);
                if (!Number.isNaN(n)) queryValues!.startTime = n;
            } else if (key === 'endTime') {
                const n = Number(value);
                if (!Number.isNaN(n)) queryValues!.endTime = n;
            }
        });

        initialParamsRef.current = params;
        initialQueryValuesRef.current = queryValues;
    }

    // --- Schema mode: initialize filter state from URL snapshot ---
    const [filters, setFilters] = useState<FiltersFromSchema<S>>(() => {
        if (!schema) return {} as FiltersFromSchema<S>;
        const initial: Record<string, unknown> = {};
        for (const [key, type] of Object.entries(schema)) {
            const raw = initialParamsRef.current?.[key];
            if (raw !== undefined) {
                const parsed = parseValue(raw, type);
                if (parsed !== undefined) initial[key] = parsed;
            }
        }
        return initial as FiltersFromSchema<S>;
    });

    const setFilter = useCallback(<K extends keyof S>(
        key: K,
        value: InferFieldType<S[K]> | undefined
    ) => {
        setFilters(prev => {
            const next = { ...prev };
            if (value === undefined) {
                delete next[key];
            } else {
                next[key] = value as FiltersFromSchema<S>[K];
            }
            return next;
        });
    }, []);

    // --- Shared helpers ---

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

        // Skip if nothing changed — prevents re-render loops
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
        filters,
        setFilter,
    }), [getInitialParam, getAllInitialParams, initialQueryValues, syncToUrl, enabled, filters, setFilter]);
}
