import type {BaseEntity} from "../types/BaseEntity.ts";
import React, {
    type ForwardedRef,
    forwardRef,
    type JSX,
    type ReactNode,
    useCallback,
    useEffect,
    useImperativeHandle,
    useRef,
    useState
} from "react";
import {Flex, Input, message, Space, Table, type TableProps} from "antd";
import type {ColumnGroupType, ColumnType} from "antd/es/table";
import {formatTimestamp} from "../utils/datetime.utils.ts";
import {SearchOutlined} from "@ant-design/icons";
import type {EntityTableColumn, EntityTableColumns} from "./types/entity-table.types.ts";
import type {BaseManagerReadDTO, PaginatedResponseData} from "../types/api.types.ts";
import type {RowSelectionType} from "antd/es/table/interface";
import {useTranslation} from "react-i18next";

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
    tableRowActionsRender?: (record: ENTITY) => ReactNode;
    columns: EntityTableColumns<ENTITY>;
    query: <T extends BaseManagerReadDTO>(props: T) => Promise<PaginatedResponseData<ENTITY>>;
    tableSelection?: {
        type: 'disabled' | RowSelectionType;
        onChange?: (records: ENTITY[]) => void;
        isDisabled?: (record: ENTITY) => boolean;
    }
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
    const [currentPage, setCurrentPage] = useState(1);
    const [currentPageSize, setCurrentPageSize] = useState(20);
    const [total, setTotal] = useState(0);

    // Search
    const [searchKeyword, setSearchKeyword] = useState('');

    // Selection (controlled, so parent can reset visually)
    const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);

    // When refreshData explicitly resets page, suppress the immediate effect re-fire that
    // would otherwise be triggered by the setCurrentPage(1) state change.
    const skipNextPageEffectRef = useRef(false);

    const fireQuery = useCallback((page: number, pageSize: number, keyword: string) => {
        setRefreshing(true);

        props.query({
            page: page,
            pageSize: pageSize,
            searchKeyword: keyword.length > 0 ? keyword : undefined,
            ...Object.assign(
                {},
                ...([
                        ...(props.tableActions ?? []),
                        ...(props.tablePrefixActions ?? [])
                    ].mapNotNull((action) => action.queryParamsProvider?.())
                )
            )
        }).then((res) => {
            setTotal(res.total);
            setData(res.records);
        }).catch(() => {
            void message.error(t('components.entityTable.fetchError', { entityName: props.entityName }))
        }).finally(() => {
            setRefreshing(false);
        })
    }, [props, t]);

    const refreshData = useCallback((options?: EntityTableRefreshOptions & { overrideKeyword?: string }) => {
        const targetPage = options?.resetPage ? 1 : currentPage;
        if (options?.resetPage && currentPage !== 1) {
            skipNextPageEffectRef.current = true;
            setCurrentPage(1);
        }
        const effectiveKeyword = options?.overrideKeyword !== undefined ? options.overrideKeyword : searchKeyword;
        fireQuery(targetPage, currentPageSize, effectiveKeyword);
    }, [currentPage, currentPageSize, searchKeyword, fireQuery]);

    const handleSearch = (keyword: string) => {
        setSearchKeyword(keyword);
        refreshData({ resetPage: true, overrideKeyword: keyword });
    }

    useEffect(() => {
        if (skipNextPageEffectRef.current) {
            skipNextPageEffectRef.current = false;
            return;
        }
        // eslint-disable-next-line react-hooks/set-state-in-effect
        fireQuery(currentPage, currentPageSize, searchKeyword);
    }, [currentPage, currentPageSize])

    const tableColumns: (ColumnGroupType<ENTITY> | ColumnType<ENTITY>)[] = [
        ...props.columns,
        ...[
            ...[{
                title: t('components.entityTable.recordTime'),
                dataIndex: "createdTime",
                key: "createdTime",
                width: 240,
                render: (_: unknown, row: ENTITY) => {
                    return <Space orientation='vertical' size={0}>
                        <span className="text-xs">{t('components.entityTable.createdTime')} {formatTimestamp(row.createdTime)}</span>
                        <span className="text-xs">{t('components.entityTable.modifiedTime')} {formatTimestamp(row.modifiedTime)}</span>
                    </Space>
                }
            }],
            ...(props.tableRowActionsRender !== undefined ? [{
                title: t('components.entityTable.action'),
                dataIndex: "action",
                key: 'action',
                width: 120,
                render: (_: unknown, record: ENTITY) => (
                    props.tableRowActionsRender!(record)
                ),
            }] : []),
        ] as EntityTableColumn<ENTITY, unknown>[]
    ].map((column) => {
        return {
            title: column.title,
            dataIndex: column.dataIndex,
            key: column.key,
            fixed: column.fixed,
            width: column.width,
            render: (data: unknown, row: ENTITY) => {
                return column.render(data, row)
            }
        }
    });

    useImperativeHandle(ref, () => {
        return {
            refreshData: refreshData,
            clearSelection: () => {
                setSelectedRowKeys([]);
                props?.tableSelection?.onChange?.([]);
            },
        }
    });

    const rowSelection: TableProps<ENTITY>['rowSelection'] = {
        selectedRowKeys: selectedRowKeys,
        onChange: (keys, selectedRows: ENTITY[]) => {
            setSelectedRowKeys(keys);
            props?.tableSelection?.onChange?.(selectedRows);
        },
        getCheckboxProps: (record: ENTITY) => ({
            disabled: props?.tableSelection?.isDisabled?.(record) ?? false,
            name: record.id,
        }),
    };

    return (
        <>
            <Flex className="mb-6 flex-wrap gap-4">
                {props.tablePrefixActions?.map((action, index) => (
                    <div key={index} className="flex flex-col space-y-2">
                        <span>{action.label}</span>
                        {action.children}
                    </div>
                ))}

                <div className="flex flex-col space-y-2">
                    <span>{t('components.entityTable.search')}</span>
                    <Input
                        placeholder={t('components.entityTable.searchPlaceholder', { entityName: props.entityName })}
                        prefix={<SearchOutlined className="text-gray-400" />}
                        className="w-full rounded-xl"
                        onPressEnter={(e) => handleSearch((e.target as HTMLInputElement).value)}
                        allowClear
                        onChange={(e) => {
                            const value = e.target.value;
                            setSearchKeyword(value);
                            if (value === '') {
                                handleSearch('');
                            }
                        }}
                    />
                </div>

                {props.tableActions?.map((action, index) => (
                    <div key={index} className="flex flex-col space-y-2">
                        <span>{action.label}</span>
                        {action.children}
                    </div>
                ))}
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
                    total: total,
                    pageSize: currentPageSize,
                    pageSizeOptions: [5, 10, 15, 20],
                    onChange: (page: number, pageSize: number) => {
                        setCurrentPage(page);
                        setCurrentPageSize(pageSize);
                    },
                    showTotal: (total: number) => {
                        return <>{t('components.entityTable.pagination.total', { total })}</>
                    }
                }}
                loading={refreshing}
                rowSelection={props.tableSelection && props.tableSelection.type !== 'disabled'
                    ? { type: props.tableSelection.type, ...rowSelection }
                    : undefined
                }
            />

            {props.children}
        </>
    )
}