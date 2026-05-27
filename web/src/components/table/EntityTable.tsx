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
import {
    Button,
    Checkbox,
    DatePicker,
    Flex,
    Input,
    message,
    Popover,
    Space,
    Switch,
    Table,
    type TableProps,
    type TimeRangePickerProps
} from 'antd';
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
import type {Dayjs} from 'dayjs';
import dayjs from 'dayjs';

export interface EntityTableProps<ENTITY extends BaseEntity> {
    entityName: string;
    children?: React.ReactNode | JSX.Element;
    tablePrefixActions?: {
        label: React.ReactNode | JSX.Element,
        children: React.ReactNode | JSX.Element,
    }[];
    tableActions?: {
        label: React.ReactNode | JSX.Element,
        children: React.ReactNode | JSX.Element,
    }[];
    filterableFields?: FilterableField[];
    /**
     * Fields the global search box will OR against.
     * URL param: `searchKeyword=<value>`
     */
    searchKeywords?: string[];
    /**
     * External search control. When `onSearch` is provided,
     * EntityTable enters external-search mode:
     * - no internal search box is rendered
     * - `searchKeyword` is used as the current keyword value
     * - `onSearch` is called when the keyword changes
     * Used by ManagerPageContainer to render a unified search box.
     */
    searchKeyword?: string;
    onSearch?: (keyword: string) => void;
    /**
     * Developer-provided per-field filter values AND-ed into the simple group.
     * `urlKey` overrides the URL param name (defaults to `field`).
     */
    simpleFilters?: { field: string; operator?: string; value: unknown; urlKey?: string }[];
    /**
     * Whether to show the built-in time range picker. Default: true.
     * The selected range is AND-ed into the simple group as created_time GTE/LTE conditions.
     */
    showTimeRangeFilter?: boolean;
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
     * Initial values for built-in query fields (page, pageSize, searchKeyword, startTime, endTime, query).
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

    // Time range — initialised from URL params
    const [timeRange, setTimeRange] = useState<[number, number | null] | null>(() => {
        const st = props.initialQueryValues?.startTime;
        const et = props.initialQueryValues?.endTime;
        if (st !== undefined) {
            const start = typeof st === 'number' ? st : Number(st);
            const end = et !== undefined ? (typeof et === 'number' ? et : Number(et)) : null;
            if (!Number.isNaN(start)) return [start, end && !Number.isNaN(end) ? end : null];
        }
        return null;
    });
    // Keep a ref so fireQuery (debounced) always sees the latest value
    const timeRangeRef = useRef(timeRange);

    // AND/OR switch between the simple group and the FilterBuilder group.
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

    // FilterBuilder node
    const filterNodeRef = useRef<GroupNode | null>(
        (() => {
            const q = (props.initialQueryValues as { query?: unknown })?.query;
            return q ? q as GroupNode : null;
        })()
    );

    // Keep a stable ref to simpleFilters
    const simpleFiltersRef = useRef(props.simpleFilters);
    simpleFiltersRef.current = props.simpleFilters;

    // Built-in filterable fields (created_time, modified_time) with DatePicker.
    const mergedFields: FilterableField[] = useMemo(() => {
        const base = props.filterableFields ?? [];
        const timeRender = ({ value, onChange }: { value: unknown; onChange: (v: unknown) => void }) => (
            <DatePicker className="flex-1" showTime
                value={value ? dayjs(value as number) : null}
                onChange={(d) => onChange(d?.valueOf() ?? null)}
            />
        );
        return [
            ...base,
            { field: 'created_time',  type: 'number', label: t('components.entityTable.createdTime'),  renderValue: timeRender },
            { field: 'modified_time', type: 'number', label: t('components.entityTable.modifiedTime'), renderValue: timeRender },
        ];
    }, [props.filterableFields, t]);

    // ── Query node builders ──────────────────────────────────────────────────

    /**
     * Build the keyword GroupNode from the global search box.
     * All searchKeywords fields are OR-ed with the same value.
     */
    const buildKeywordGroup = (keyword: string): GroupNode | null => {
        const fields = props.searchKeywords;
        if (!fields || fields.length === 0 || keyword.trim() === '') return null;
        const conditions: ConditionNode[] = fields.map(field => ({
            type: 'condition', field, operator: 'contains', value: keyword.trim(),
        }));
        return { type: 'group', logic: 'or', children: conditions };
    };

    /**
     * Build the simple group from keyword + simpleFilters + time range, all AND-ed together.
     *
     * Structure:
     *   AND(
     *     OR(username contains kw, email contains kw, ...),  ← keyword group (if any)
     *     username contains xxx,                              ← simpleFilters (if any)
     *     nickname contains yyy,
     *     created_time GTE startTime,                         ← time range (if any)
     *     created_time LTE endTime,
     *   )
     *
     * Returns null when nothing is active.
     */
    const buildSimpleGroup = (
        keyword: string,
        simpleFilters: typeof props.simpleFilters,
        startTime?: number,
        endTime?: number,
    ): GroupNode | null => {
        const parts: (GroupNode | ConditionNode)[] = [];

        const keywordGroup = buildKeywordGroup(keyword);
        if (keywordGroup) parts.push(keywordGroup);

        simpleFilters?.forEach(({ field, operator, value }) => {
            if (value !== undefined && value !== null && value !== '') {
                parts.push({ type: 'condition', field, operator: operator ?? 'contains', value });
            }
        });

        if (startTime) parts.push({ type: 'condition', field: 'created_time', operator: 'gte', value: startTime });
        if (endTime)   parts.push({ type: 'condition', field: 'created_time', operator: 'lte', value: endTime });

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
        startTime?: number,
        endTime?: number,
    ): GroupNode | undefined => {
        const simpleGroup = buildSimpleGroup(keyword, simpleFilters, startTime, endTime);
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

        const tr = timeRangeRef.current;
        const startTime = tr ? tr[0] : undefined;
        const endTime   = tr ? (tr[1] ?? Date.now()) : undefined;

        const queryNode = buildQueryNode(
            keyword, simpleFiltersRef.current, filterNodeRef.current, useAnd,
            startTime, endTime,
        );

        // ── URL params ──
        // searchKeyword, simpleFilter fields, startTime, endTime stored as flat params.
        // FilterBuilder stored as `query` (base64 JSON).
        const urlParams: Record<string, unknown> = {
            page,
            pageSize,
            ...(keyword.trim() ? { searchKeyword: keyword.trim() } : {}),
            ...Object.fromEntries(
                (simpleFiltersRef.current ?? [])
                    .filter(f => f.value !== undefined && f.value !== null && f.value !== '')
                    .map(f => [f.urlKey ?? f.field, f.value])
            ),
            ...(startTime ? { startTime } : {}),
            ...(endTime   ? { endTime }   : {}),
            ...(filterNodeRef.current && filterNodeRef.current.children.length > 0
                ? { query: filterNodeRef.current }
                : {}),
        };

        props.queryParamsSync?.(urlParams);

        // ── Request params ──
        // Send the fully merged queryNode as `query`.
        const requestParams = {
            page,
            pageSize,
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

    useEffect(() => {
        if (skipNextPageEffectRef.current) {
            skipNextPageEffectRef.current = false;
            return;
        }
        // eslint-disable-next-line react-hooks/set-state-in-effect
        fireQuery(currentPage, currentPageSize, searchKeyword, combineWithAndRef.current);
    }, [currentPage, currentPageSize]);

    const prevSimpleFiltersRef = useRef(props.simpleFilters);
    useEffect(() => {
        if (prevSimpleFiltersRef.current === props.simpleFilters) return;
        prevSimpleFiltersRef.current = props.simpleFilters;
        refreshData({ resetPage: true });
    }, [props.simpleFilters]);

    // ── Columns ──────────────────────────────────────────────────────────────

    const allColumns: EntityTableColumn<ENTITY, unknown>[] = [
        ...props.columns,
        ...(!props.hideRecordTimeColumn ? [{
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

    // ── Time range presets ───────────────────────────────────────────────────

    const rangePresets: TimeRangePickerProps['presets'] = useMemo(() => {
        const now = dayjs();
        return [
            { label: t('components.managerPageContainer.todayToNow'), value: () => [dayjs().startOf('day'), dayjs()] as [Dayjs, Dayjs] },
            { label: t('components.managerPageContainer.last5Minutes'),  value: [now.add(-5,  'minute'), now] },
            { label: t('components.managerPageContainer.last10Minutes'), value: [now.add(-10, 'minute'), now] },
            { label: t('components.managerPageContainer.last15Minutes'), value: [now.add(-15, 'minute'), now] },
            { label: t('components.managerPageContainer.last30Minutes'), value: [now.add(-30, 'minute'), now] },
            { label: t('components.managerPageContainer.last1Hour'),     value: [now.add(-1,  'hour'),   now] },
            { label: t('components.managerPageContainer.last2Hours'),    value: [now.add(-2,  'hour'),   now] },
            { label: t('components.managerPageContainer.last3Hours'),    value: [now.add(-3,  'hour'),   now] },
            { label: t('components.managerPageContainer.last4Hours'),    value: [now.add(-4,  'hour'),   now] },
            { label: t('components.managerPageContainer.last8Hours'),    value: [now.add(-8,  'hour'),   now] },
            { label: t('components.managerPageContainer.last12Hours'),   value: [now.add(-12, 'hour'),   now] },
            { label: t('components.managerPageContainer.last1Day'),      value: [now.add(-1,  'day'),    now] },
            { label: t('components.managerPageContainer.last3Days'),     value: [now.add(-3,  'day'),    now] },
            { label: t('components.managerPageContainer.last5Days'),     value: [now.add(-5,  'day'),    now] },
            { label: t('components.managerPageContainer.last7Days'),     value: [now.add(-7,  'day'),    now] },
            { label: t('components.managerPageContainer.last14Days'),    value: [now.add(-14, 'day'),    now] },
            { label: t('components.managerPageContainer.last30Days'),    value: [now.add(-30, 'day'),    now] },
        ];
    }, [t]);

    // ── Table actions ────────────────────────────────────────────────────────

    const showTimeRangeFilter = props.showTimeRangeFilter !== false;

    const hasSimpleInputs = (props.searchKeywords && props.searchKeywords.length > 0)
        || (props.simpleFilters && props.simpleFilters.length > 0);
    const showCombineSwitch = hasSimpleInputs && !!props.filterableFields;

    const tableActions = useMemo(() => {
        const actions = [...(props.tableActions ?? [])];

        if (showTimeRangeFilter) {
            actions.push({
                label: <span>{t('components.managerPageContainer.timeRange')}</span>,
                children: (
                    <DatePicker.RangePicker
                        showTime
                        allowClear
                        presets={rangePresets}
                        defaultValue={timeRange ? [dayjs(timeRange[0]), timeRange[1] ? dayjs(timeRange[1]) : null] : undefined}
                        placeholder={[t('components.managerPageContainer.startTime'), t('components.managerPageContainer.tillNow')]}
                        allowEmpty={[false, true]}
                        onChange={(dates) => {
                            const next: [number, number | null] | null = dates && dates[0]
                                ? [dates[0].valueOf(), dates[1]?.valueOf() ?? null]
                                : null;
                            timeRangeRef.current = next;
                            setTimeRange(next);
                            setTimeout(() => refreshData({ resetPage: true }), 0);
                        }}
                    />
                ),
            });
        }

        if (props.filterableFields) {
            if (showCombineSwitch) {
                actions.push({
                    label: <span>{t('components.entityTable.combineLogic')}</span>,
                    children: (
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
                    ),
                });
            }
            actions.push({
                label: <span>{t('components.filterBuilder.filters')}</span>,
                children: (
                    <FilterBuilder
                        fields={mergedFields}
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
    }, [props.tableActions, props.filterableFields, showTimeRangeFilter, showCombineSwitch, combineWithAnd, timeRange, rangePresets, t, refreshData]);

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
