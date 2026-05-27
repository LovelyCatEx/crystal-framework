import type {BaseEntity} from "@/types/BaseEntity.ts";
import React, {
    type ForwardedRef,
    forwardRef,
    type JSX,
    type ReactNode,
    useCallback,
    useEffect,
    useImperativeHandle,
    useMemo,
    useRef,
    useState
} from "react";
import {Button, Checkbox, Flex, Input, message, Popover, Space, Switch, Table, type TableProps} from "antd";
import type {ColumnGroupType, ColumnType} from "antd/es/table";
import {formatTimestamp} from "@/utils/datetime.utils.ts";
import {SearchOutlined, SettingOutlined} from "@ant-design/icons";
import type {EntityTableColumn, EntityTableColumns} from "./entity-table.types.ts";
import type {BaseManagerReadDTO, PaginatedResponseData} from "@/types/api.types.ts";
import type {RowSelectionType} from "antd/es/table/interface";
import {useTranslation} from "react-i18next";
import {useDebounce} from "@/compositions/use-debounce.ts";
import {FilterBuilder} from "@/components/table/filter/FilterBuilder.tsx";
import type {ConditionNode, FilterableField, GroupNode} from "@/components/table/filter/filter-builder.types.ts";

export interface EntityTableProps<ENTITY extends BaseEntity> {
    entityName: string;
    children?: React.ReactNode | JSX.Element;
    tablePrefixActions?: {
        label: React.ReactNode | JSX.Element,
        children: React.ReactNode | JSX.Element,
        queryParamsProvider?: () => object
    }[];
    tableActions?: {
        label: React.ReactNode | JSX.Element,
        children: React.ReactNode | JSX.Element,
        queryParamsProvider?: () => object
    }[];
    filterableFields?: FilterableField[];
    /**
     * Fields the global search box will OR against.
     * A single search input is rendered; the typed value generates a GroupNode
     * where each listed field gets a `contains` condition, all OR-ed together.
     *
     * This group is part of the "simple group" (AND-ed with simpleFilters).
     * The simple group and the FilterBuilder group are combined via the AND/OR switch.
     *
     * URL param: `searchKeyword=<value>`
     *
     * @example
     * searchKeywords={['username', 'email', 'nickname']}
     */
    searchKeywords?: string[];
    /**
     * Developer-provided per-field filter values that are AND-ed into the simple group.
     * Each non-empty entry becomes a `contains` condition on the specified field.
     *
     * Manage the values externally (e.g. with useManagerQueryParams schema mode) and
     * pass the current values here on every render. EntityTable re-fires the query
     * whenever this prop changes (by reference).
     *
     * URL params: each field is stored as a flat param (`username=xxx`), managed by
     * the caller via queryParamsSync / useManagerQueryParams.
     *
     * @example
     * simpleFilters={[
     *   { field: 'username', value: username },
     *   { field: 'nickname', value: nickname },
     * ]}
     */
    simpleFilters?: { field: string; value: string | undefined }[];
    hideRecordTimeColumn?: boolean;
    tableRowActionsRender?: (record: ENTITY) => ReactNode;
    columns: EntityTableColumns<ENTITY>;
    query: <T extends BaseManagerReadDTO>(props: T) => Promise<PaginatedResponseData<ENTITY>>;
    tableSelection?: {
        type: 'disabled' | RowSelectionType;
        onChange?: (records: ENTITY[]) => void;
        isDisabled?: (record: ENTITY) => boolean;
    };
    /** Callback to sync query params to URL. Provided by useManagerQueryParams().syncToUrl */
    queryParamsSync?: (params: Record<string, unknown>) => void;
    /**
     * Initial values for built-in query fields.
     * Typically provided from URL search params via useManagerQueryParams().
     */
    initialQueryValues?: {
        page?: number;
        pageSize?: number;
        searchKeyword?: string;
        startTime?: number | string;
        endTime?: number | string;
        query?: unknown;
    };
    /**
     * Extra query params merged into every request as-is (not folded into the query GroupNode).
     * Use for non-filter params like tenantId, etc.
     */
    extraQueryParams?: Record<string, unknown>;
}

export interface EntityTableRefreshOptions {
    resetPage?: boolean;
}

export interface EntityTableRef {
    refreshData: (options?: EntityTableRefreshOptions) => void;
    clearSelection: () => void;
}

export type EntityTableReturnType =
    <ENTITY extends BaseEntity>(
        props: EntityTableProps<ENTITY> & React.RefAttributes<EntityTableRef>
    ) => ReactNode;

export const EntityTable = forwardRef(EntityTableInner) as EntityTableReturnType

function EntityTableInner<ENTITY extends BaseEntity>(
    props: EntityTableProps<ENTITY>,
    ref: ForwardedRef<EntityTableRef>,
) {
    const { t } = useTranslation();

    // Table
    const [data, setData] = useState<ENTITY[]>([]);
    const [refreshing, setRefreshing] = useState(false);

    // Pagination
    const [currentPage, setCurrentPage] = useState(props.initialQueryValues?.page ?? 1);
    const [currentPageSize, setCurrentPageSize] = useState(props.initialQueryValues?.pageSize ?? 20);
    const [total, setTotal] = useState(0);

    // Global search keyword
    const [searchKeyword, setSearchKeyword] = useState(props.initialQueryValues?.searchKeyword ?? '');

    // AND/OR switch between the simple group and the FilterBuilder group.
    // Only shown when both searchKeywords/simpleFilters and filterableFields are configured.
    const [combineWithAnd, setCombineWithAnd] = useState(true);
    const combineWithAndRef = useRef(true);

    // Selection
    const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);

    // Column visibility
    const [visibleColumns, setVisibleColumns] = useState<Set<string>>(() => {
        const allKeys = new Set<string>();
        props.columns.forEach(col => allKeys.add(col.key));
        allKeys.add('createdTime');
        if (props.tableRowActionsRender) allKeys.add('action');
        return allKeys;
    });
    const [columnFilterOpen, setColumnFilterOpen] = useState(false);

    const skipNextPageEffectRef = useRef(false);

    // FilterBuilder node (advanced filter UI) — stored in a ref so it survives re-renders
    // without causing extra effect triggers.
    const filterNodeRef = useRef<GroupNode | null>(
        (() => {
            const q = (props.initialQueryValues as { query?: unknown })?.query;
            return q ? q as GroupNode : null;
        })()
    );

    // Keep a stable ref to simpleFilters so the debounced fireQuery always sees the latest.
    const simpleFiltersRef = useRef(props.simpleFilters);
    simpleFiltersRef.current = props.simpleFilters;

    // ── Query node builders ──────────────────────────────────────────────────

    /**
     * Build the keyword GroupNode from the global search box.
     * All searchKeywords fields are OR-ed with the same value.
     */
    const buildKeywordGroup = (keyword: string): GroupNode | null => {
        const fields = props.searchKeywords;
        if (!fields || fields.length === 0 || keyword.trim() === '') return null;
        const conditions: ConditionNode[] = fields.map(field => ({
            type: 'condition',
            field,
            operator: 'contains',
            value: keyword.trim(),
        }));
        return { type: 'group', logic: 'or', children: conditions };
    };

    /**
     * Build the simple group from keyword + simpleFilters, all AND-ed together.
     *
     * Structure:
     *   AND(
     *     OR(username contains kw, email contains kw, ...),  ← keyword group (if any)
     *     username contains xxx,                              ← simpleFilters (if any)
     *     nickname contains yyy,
     *     ...
     *   )
     *
     * Returns null when nothing is active.
     */
    const buildSimpleGroup = (keyword: string, simpleFilters: typeof props.simpleFilters): GroupNode | null => {
        const parts: (GroupNode | ConditionNode)[] = [];

        const keywordGroup = buildKeywordGroup(keyword);
        if (keywordGroup) parts.push(keywordGroup);

        simpleFilters?.forEach(({ field, value }) => {
            if (value && value.trim() !== '') {
                parts.push({ type: 'condition', field, operator: 'contains', value: value.trim() });
            }
        });

        if (parts.length === 0) return null;
        if (parts.length === 1) return parts[0] as GroupNode;
        return { type: 'group', logic: 'and', children: parts };
    };

    /**
     * Merge the simple group and the FilterBuilder group into the final query node.
     *
     * - Only simple  → simple group
     * - Only filter  → filter group
     * - Both         → top-level group with AND/OR switch logic
     * - Neither      → undefined (no query param sent)
     */
    const buildQueryNode = (
        keyword: string,
        simpleFilters: typeof props.simpleFilters,
        filterNode: GroupNode | null,
        useAnd: boolean,
    ): GroupNode | undefined => {
        const simpleGroup = buildSimpleGroup(keyword, simpleFilters);
        const hasSimple = simpleGroup !== null;
        const hasFilter = filterNode !== null && filterNode.children.length > 0;

        if (hasSimple && hasFilter) {
            return { type: 'group', logic: useAnd ? 'and' : 'or', children: [simpleGroup, filterNode] };
        }
        if (hasSimple) return simpleGroup;
        if (hasFilter) return filterNode;
        return undefined;
    };

    // ── Fire query ───────────────────────────────────────────────────────────

    const fireQuery = useDebounce((
        page: number,
        pageSize: number,
        keyword: string,
        useAnd: boolean,
    ) => {
        setRefreshing(true);

        const actionParams = Object.assign(
            {},
            ...[...(props.tableActions ?? []), ...(props.tablePrefixActions ?? [])]
                .mapNotNull(action => action.queryParamsProvider?.())
        );

        const queryNode = buildQueryNode(keyword, simpleFiltersRef.current, filterNodeRef.current, useAnd);

        // ── URL params ──
        // searchKeyword and simpleFilter fields are stored as flat params.
        // FilterBuilder is stored as `query` (base64 JSON).
        // The merged queryNode is NOT written to the URL — it's only sent in the request.
        const urlParams: Record<string, unknown> = {
            page,
            pageSize,
            ...(keyword.trim() ? { searchKeyword: keyword.trim() } : {}),
            // simpleFilters: each field as a flat URL param
            ...Object.fromEntries(
                (simpleFiltersRef.current ?? [])
                    .filter(f => f.value && f.value.trim() !== '')
                    .map(f => [f.field, f.value])
            ),
            ...(props.extraQueryParams ?? {}),
            ...actionParams,
            // FilterBuilder stored separately — not merged
            ...(filterNodeRef.current && filterNodeRef.current.children.length > 0
                ? { query: filterNodeRef.current }
                : {}),
        };

        // Sync query params to URL
        props.queryParamsSync?.(urlParams);

        // ── Request params ──
        // Send the fully merged queryNode as `query`.
        const requestParams = {
            page,
            pageSize,
            ...(props.extraQueryParams ?? {}),
            ...actionParams,
            ...(queryNode ? { query: queryNode } : {}),
        };

        props.query(requestParams).then((res) => {
            setTotal(res.total);
            setData(res.records);
        }).catch(() => {
            void message.error(t('components.entityTable.fetchError', { entityName: props.entityName }));
        }).finally(() => {
            setRefreshing(false);
        });
    });

    // ── Refresh helpers ──────────────────────────────────────────────────────

    const refreshData = useCallback((options?: EntityTableRefreshOptions & { overrideKeyword?: string }) => {
        const targetPage = options?.resetPage ? 1 : currentPage;
        if (options?.resetPage && currentPage !== 1) {
            skipNextPageEffectRef.current = true;
            setCurrentPage(1);
        }
        const effectiveKeyword = options?.overrideKeyword !== undefined ? options.overrideKeyword : searchKeyword;
        fireQuery(targetPage, currentPageSize, effectiveKeyword, combineWithAndRef.current);
    }, [currentPage, currentPageSize, searchKeyword, fireQuery]);

    const handleSearch = (keyword: string) => {
        setSearchKeyword(keyword);
        refreshData({ resetPage: true, overrideKeyword: keyword });
    };

    // Re-fire when pagination changes
    useEffect(() => {
        if (skipNextPageEffectRef.current) {
            skipNextPageEffectRef.current = false;
            return;
        }
        // eslint-disable-next-line react-hooks/set-state-in-effect
        fireQuery(currentPage, currentPageSize, searchKeyword, combineWithAndRef.current);
    }, [currentPage, currentPageSize]);

    // Re-fire when simpleFilters change (caller updates them externally)
    const prevSimpleFiltersRef = useRef(props.simpleFilters);
    useEffect(() => {
        if (prevSimpleFiltersRef.current === props.simpleFilters) return;
        prevSimpleFiltersRef.current = props.simpleFilters;
        refreshData({ resetPage: true });
    }, [props.simpleFilters]);

    // ── Columns ──────────────────────────────────────────────────────────────

    const allColumns: EntityTableColumn<ENTITY, unknown>[] = [
        ...props.columns,
        ...(!!!props.hideRecordTimeColumn ? [{
            title: t('components.entityTable.recordTime'),
            dataIndex: 'createdTime',
            key: 'createdTime',
            width: 240,
            render: (_: unknown, row: ENTITY) => (
                <Space orientation="vertical" size={0}>
                    <span className="text-xs">{t('components.entityTable.createdTime')} {formatTimestamp(row.createdTime)}</span>
                    <span className="text-xs">{t('components.entityTable.modifiedTime')} {formatTimestamp(row.modifiedTime)}</span>
                </Space>
            ),
        }] : []),
        ...(props.tableRowActionsRender !== undefined ? [{
            title: t('components.entityTable.action'),
            dataIndex: 'action',
            key: 'action',
            width: 120,
            render: (_: unknown, record: ENTITY) => props.tableRowActionsRender!(record),
        }] : []),
    ];

    // ── Table actions (FilterBuilder + Refresh) ──────────────────────────────

    const tableActions = useMemo(() => {
        const actions = [...(props.tableActions ?? [])];

        if (props.filterableFields) {
            actions.push({
                label: <span>{t('components.filterBuilder.filters')}</span>,
                children: (
                    <FilterBuilder
                        fields={props.filterableFields}
                        defaultValue={filterNodeRef.current}
                        onChange={(node) => {
                            filterNodeRef.current = node;
                            setTimeout(() => refreshData({ resetPage: true }), 0);
                        }}
                    />
                ),
            });
        }

        actions.push({
            label: t('components.managerPageContainer.action'),
            children: (
                <Button type="primary" onClick={() => refreshData()}>
                    {t('components.managerPageContainer.refresh')}
                </Button>
            ),
        });

        return actions;
    }, [props.tableActions, props.filterableFields, t, refreshData]);

    // ── Render helpers ───────────────────────────────────────────────────────

    const tableColumns: (ColumnGroupType<ENTITY> | ColumnType<ENTITY>)[] = allColumns
        .filter(col => visibleColumns.has(col.key))
        .map(col => ({
            title: col.title,
            dataIndex: col.dataIndex,
            key: col.key,
            fixed: col.fixed,
            width: col.width,
            render: (data: unknown, row: ENTITY) => col.render(data, row),
        }));

    useImperativeHandle(ref, () => ({
        refreshData,
        clearSelection: () => {
            setSelectedRowKeys([]);
            props?.tableSelection?.onChange?.([]);
        },
    }));

    const rowSelection: TableProps<ENTITY>['rowSelection'] = {
        selectedRowKeys,
        onChange: (keys, selectedRows: ENTITY[]) => {
            setSelectedRowKeys(keys);
            props?.tableSelection?.onChange?.(selectedRows);
        },
        getCheckboxProps: (record: ENTITY) => ({
            disabled: props?.tableSelection?.isDisabled?.(record) ?? false,
            name: record.id,
        }),
    };

    const handleColumnToggle = (columnKey: string, checked: boolean) => {
        setVisibleColumns(prev => {
            const next = new Set(prev);
            checked ? next.add(columnKey) : next.delete(columnKey);
            return next;
        });
    };

    const columnFilterContent = (
        <div className="w-64">
            <div className="mb-2 flex justify-between items-center">
                <span className="font-medium">{t('components.entityTable.columnFilter.title')}</span>
                <Button
                    type="link"
                    size="small"
                    onClick={() => {
                        const allKeys = new Set<string>();
                        allColumns.forEach(col => allKeys.add(col.key));
                        setVisibleColumns(allKeys);
                    }}
                >
                    {t('components.entityTable.columnFilter.selectAll')}
                </Button>
            </div>
            <Space orientation="vertical" className="w-full">
                {allColumns.map(col => (
                    <Checkbox
                        key={col.key}
                        checked={visibleColumns.has(col.key)}
                        onChange={(e) => handleColumnToggle(col.key, e.target.checked)}
                    >
                        {col.title}
                    </Checkbox>
                ))}
            </Space>
        </div>
    );

    // Whether the AND/OR switch should be shown:
    // only when there's something in the simple group AND filterableFields is configured.
    const hasSimpleInputs = (props.searchKeywords && props.searchKeywords.length > 0)
        || (props.simpleFilters && props.simpleFilters.length > 0);
    const showCombineSwitch = hasSimpleInputs && !!props.filterableFields;

    // ── JSX ──────────────────────────────────────────────────────────────────

    return (
        <>
            <Flex className="mb-6 flex-wrap gap-4">
                {props.tablePrefixActions?.map((action, index) => (
                    <div key={index} className="flex flex-col space-y-2">
                        <span>{action.label}</span>
                        {action.children}
                    </div>
                ))}

                {/* Global search box — shown only when searchKeywords is configured */}
                {props.searchKeywords && props.searchKeywords.length > 0 && (
                    <div className="flex flex-col space-y-2">
                        <span>{t('components.entityTable.search')}</span>
                        <Input
                            placeholder={t('components.entityTable.searchPlaceholder', { entityName: props.entityName })}
                            prefix={<SearchOutlined className="text-gray-400" />}
                            className="w-full rounded-xl"
                            defaultValue={props.initialQueryValues?.searchKeyword}
                            onPressEnter={(e) => handleSearch((e.target as HTMLInputElement).value)}
                            allowClear
                            onChange={(e) => {
                                const value = e.target.value;
                                setSearchKeyword(value);
                                if (value === '') handleSearch('');
                            }}
                        />
                    </div>
                )}

                {tableActions.map((action, index) => (
                    <div key={index} className="flex flex-col space-y-2">
                        <span>{action.label}</span>
                        {action.children}
                    </div>
                ))}

                {/* AND/OR switch between simple group and FilterBuilder group */}
                {showCombineSwitch && (
                    <div className="flex flex-col space-y-2">
                        <span>{t('components.entityTable.combineLogic')}</span>
                        <Switch
                            checked={combineWithAnd}
                            checkedChildren={t('components.entityTable.combineAnd')}
                            unCheckedChildren={t('components.entityTable.combineOr')}
                            onChange={(checked) => {
                                combineWithAndRef.current = checked;
                                setCombineWithAnd(checked);
                                setTimeout(() => refreshData({ resetPage: true }), 0);
                            }}
                        />
                    </div>
                )}

                <div className="flex flex-col space-y-2">
                    <span>{t('components.entityTable.columnFilter.label')}</span>
                    <Popover
                        content={columnFilterContent}
                        trigger="click"
                        open={columnFilterOpen}
                        onOpenChange={setColumnFilterOpen}
                        placement="bottomRight"
                    >
                        <Button icon={<SettingOutlined />} className="rounded-xl">
                            {t('components.entityTable.columnFilter.button')}
                        </Button>
                    </Popover>
                </div>
            </Flex>

            <Table<ENTITY>
                columns={tableColumns}
                dataSource={data}
                rowKey="id"
                scroll={{ x: 1200 }}
                pagination={{
                    showSizeChanger: true,
                    defaultPageSize: 20,
                    className: "pr-6",
                    current: currentPage,
                    total,
                    pageSize: currentPageSize,
                    pageSizeOptions: [5, 10, 15, 20],
                    onChange: (page, pageSize) => {
                        setCurrentPage(page);
                        setCurrentPageSize(pageSize);
                    },
                    showTotal: (total) => <>{t('components.entityTable.pagination.total', { total })}</>,
                }}
                loading={refreshing}
                rowSelection={
                    props.tableSelection && props.tableSelection.type !== 'disabled'
                        ? { type: props.tableSelection.type, ...rowSelection }
                        : undefined
                }
            />

            {props.children}
        </>
    );
}
