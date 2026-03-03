import type {BaseEntity} from "../types/BaseEntity.ts";
import React, {
    type ForwardedRef,
    forwardRef,
    type JSX,
    type ReactNode,
    useCallback,
    useEffect,
    useImperativeHandle,
    useState
} from "react";
import {Card, Flex, Input, message, Space, Table, type TableProps} from "antd";
import type {ColumnGroupType, ColumnType} from "antd/es/table";
import {formatTimestamp} from "../utils/datetime.utils.ts";
import {SearchOutlined} from "@ant-design/icons";
import type {EntityTableColumn, EntityTableColumns} from "./types/entity-table.types.ts";
import type {BaseManagerReadDTO, PaginatedResponseData} from "../types/api.types.ts";
import type {RowSelectionType} from "antd/es/table/interface";

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
        onChange?: (record: ENTITY[]) => void;
    }
}

export interface EntityTableRef {
    refreshData: () => void;
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
    // Table
    const [data, setData] = useState<ENTITY[]>([]);
    const [refreshing, setRefreshing] = useState(false);

    // Pagination
    const [currentPage, setCurrentPage] = useState(1);
    const [currentPageSize, setCurrentPageSize] = useState(20);
    const [total, setTotal] = useState(0);

    // Search
    const [searchKeyword, setSearchKeyword] = useState('');

    const refreshData = useCallback(()=> {
        setRefreshing(true);

        props.query({
            page: currentPage,
            pageSize: currentPageSize,
            searchKeyword: searchKeyword.length > 0 ? searchKeyword : undefined,
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
            void message.error(`无法获取${props.entityName}列表`)
        }).finally(() => {
            setRefreshing(false);
        })
    }, [currentPage, currentPageSize, props, searchKeyword])

    const handleSearch = (keyword: string) => {
        setSearchKeyword(keyword);
        refreshData();
    }

    useEffect(() => {
        // eslint-disable-next-line react-hooks/set-state-in-effect
        refreshData();
    }, [currentPage, currentPageSize])

    const tableColumns: (ColumnGroupType<ENTITY> | ColumnType<ENTITY>)[] = [
        ...props.columns,
        ...[
            ...[{
                title: "记录时间",
                dataIndex: "createdTime",
                key: "createdTime",
                width: 240,
                render: (_: unknown, row: ENTITY) => {
                    return <Space orientation='vertical' size={0}>
                        <span className="text-xs">创建时间 {formatTimestamp(row.createdTime)}</span>
                        <span className="text-xs">修改时间 {formatTimestamp(row.modifiedTime)}</span>
                    </Space>
                }
            }],
            ...(props.tableRowActionsRender !== undefined ? [{
                title: '操作',
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
        }
    });

    const rowSelection: TableProps<ENTITY>['rowSelection'] = {
        onChange: (_, selectedRows: ENTITY[]) => {
            props?.tableSelection?.onChange?.(selectedRows);
        },
        getCheckboxProps: (record: ENTITY) => ({
            disabled: false,
            name: record.id,
        }),
    };

    return (
        <>
            <Card className="border-none shadow-sm rounded-2xl overflow-hidden">
                <Flex className="mb-6 flex-wrap gap-4">
                    {props.tablePrefixActions?.map((action, index) => (
                        <div key={index} className="flex flex-col space-y-2">
                            <span>{action.label}</span>
                            {action.children}
                        </div>
                    ))}

                    <div className="flex flex-col space-y-2">
                        <span>搜索</span>
                        <Input
                            placeholder={`搜索${props.entityName}...`}
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
                    scroll={{ x: 1000 }}
                    className="custom-table"
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
                        }
                    }}
                    loading={refreshing}
                    rowSelection={props.tableSelection && props.tableSelection.type !== 'disabled'
                        ? { type: props.tableSelection.type, ...rowSelection }
                        : undefined
                    }
                />
            </Card>

            {props.children}
        </>
    )
}