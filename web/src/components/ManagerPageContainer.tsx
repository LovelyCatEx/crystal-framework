import React, {type JSX, useCallback, useEffect, useState} from "react";
import {Button, Card, Form, Input, message, Modal, Popconfirm, Space, Table} from "antd";
import {DeleteOutlined, EditOutlined, PlusOutlined, SearchOutlined} from "@ant-design/icons";
import type {ColumnGroupType, ColumnType} from "antd/es/table";
import type {ManagerPageTableColumn} from "./types/manager-page-container.types.ts";
import {ActionBarComponent, type ActionBarComponentProps} from "./ActionBarComponent.tsx";
import type {
    BaseManagerDeleteDTO,
    BaseManagerReadDTO,
    BaseManagerUpdateDTO,
    PaginatedResponseData
} from "../types/api.types.ts";
import type {BaseEntity} from "../types/BaseEntity.ts";
import {formatTimestamp} from "../utils/datetime.utils.ts";

export function ManagerPageContainer<ENTITY extends BaseEntity>(props: ActionBarComponentProps & {
    entityName: string;
    children?: React.ReactNode | JSX.Element;
    columns: ManagerPageTableColumn<ENTITY, unknown>[];
    query: <T extends BaseManagerReadDTO>(props: T) => Promise<PaginatedResponseData<ENTITY>>;
    delete: <T extends BaseManagerDeleteDTO>(props: T) => Promise<unknown>;
    update: <T extends BaseManagerUpdateDTO>(props: T) => Promise<unknown>;
    create: <T extends object>(props: T) => Promise<unknown>;
    editModalFormChildren?: React.ReactNode | JSX.Element;
}) {
    // Table
    const [data, setData] = useState<ENTITY[]>([]);
    const [refreshing, setRefreshing] = useState(false);

    // Pagination
    const [currentPage, setCurrentPage] = useState(1);
    const [currentPageSize, setCurrentPageSize] = useState(20);
    const [total, setTotal] = useState(0);

    // Search
    const [searchKeyword, setSearchKeyword] = useState('');

    // New / Edit Modal
    const [isModalVisible, setIsModalVisible] = useState(false);
    const [editingItem, setEditingItem] = useState<ENTITY | null>(null);
    const [form] = Form.useForm();

    const refreshData = useCallback(()=> {
        setRefreshing(true);

        props.query({
            page: currentPage,
            pageSize: currentPageSize,
            searchKeyword: searchKeyword.length > 0 ? searchKeyword : undefined
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

    const openModal = (item: ENTITY | null = null) => {
        setEditingItem(item);

        if (item) {
            form.setFieldsValue(item);
        } else {
            form.resetFields();
        }

        setIsModalVisible(true);
    };

    const deleteModel = (id: string) => {
        props.delete({ id: id })
            .then(() => {
                void message.success(`${props.entityName}已刪除`);
                refreshData();
            })
            .catch(() => {
                void message.error(`${props.entityName}删除失败`);
            })
    };

    const handleAddOrUpdateEdit = (values: ENTITY) => {
        if (editingItem) {
            props.update(values).then(() => {
                refreshData();
                void message.success(`${props.entityName}更新成功`);
            }).catch(() => {
                void message.error(`${props.entityName}更新失败`);
            })
        } else {
            props.create(values).then(() => {
                refreshData();
                void message.success(`新增${props.entityName}成功`);
            }).catch(() => {
                void message.error(`新增${props.entityName}失败`);
            })
        }

        setIsModalVisible(false);
        setEditingItem(null);
        form.resetFields();
    };

    useEffect(() => {
        // eslint-disable-next-line react-hooks/set-state-in-effect
        refreshData();
    }, [currentPage, currentPageSize])

    const tableColumns: (ColumnGroupType<ENTITY> | ColumnType<ENTITY>)[] = [
        ...props.columns,
        ...[
            {
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
            },
            {
                title: '操作',
                dataIndex: "action",
                key: 'action',
                fixed: 'end',
                width: 120,
                render: (_: unknown, record: ENTITY) => (
                    <Space>
                        <Button type="text" size="small" icon={<EditOutlined />} onClick={() => openModal(record)} />
                        <Popconfirm title="确定要删除此模型？" onConfirm={() => deleteModel(record.id)} okText="确认" cancelText="取消">
                            <Button type="text" size="small" icon={<DeleteOutlined />} danger />
                        </Popconfirm>
                    </Space>
                ),
            },
        ] as ManagerPageTableColumn<ENTITY, unknown>[]
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

    return (
        <>
            <ActionBarComponent
                title={props.title}
                subtitle={props.subtitle}
                titleActions={<>
                    {props.titleActions}

                    <Button
                        type="primary"
                        icon={<PlusOutlined/>}
                        size="large"
                        className="rounded-xl h-12 shadow-lg"
                        onClick={() => openModal()}
                    >
                        新增{props.entityName}
                    </Button>
                </>}
            />

            <Card className="border-none shadow-sm rounded-2xl overflow-hidden">
                <div className="mb-6 flex gap-4">
                    <Input
                        placeholder={`搜索${props.entityName}...`}
                        prefix={<SearchOutlined className="text-gray-400" />}
                        className="max-w-xs rounded-xl h-10"
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
                <Table
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
                />
            </Card>

            {props.children}

            <Modal
                title={(editingItem ? "编辑" : "新建") + props.entityName}
                open={isModalVisible}
                onCancel={() => setIsModalVisible(false)}
                onOk={() => form.submit()}
                width={800}
                centered
                okButtonProps={{ className: "rounded-lg h-10 px-6" }}
                cancelButtonProps={{ className: "rounded-lg h-10 px-6" }}
            >
                <Form form={form} layout="vertical" onFinish={handleAddOrUpdateEdit} className="mt-4">
                    {/* Hidden Id field */}
                    <Form.Item name="id" hidden>
                        <Input />
                    </Form.Item>

                    {props.editModalFormChildren}
                </Form>
            </Modal>
        </>
    )
}