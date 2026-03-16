import React, {
    type ForwardedRef,
    forwardRef,
    type JSX,
    type ReactNode,
    useImperativeHandle,
    useRef,
    useState
} from "react";
import {Button, Card, Form, Input, message, Modal, Popconfirm, Select, Space} from "antd";
import {DeleteOutlined, EditOutlined, ExclamationCircleFilled, PlusOutlined} from "@ant-design/icons";
import {ActionBarComponent, type ActionBarComponentProps} from "./ActionBarComponent.tsx";
import type {BaseManagerDeleteDTO, BaseManagerUpdateDTO} from "../types/api.types.ts";
import type {BaseEntity} from "../types/BaseEntity.ts";
import {EntityTable, type EntityTableProps, type EntityTableRef} from "./EntityTable.tsx";

type DivHTMLAttributes = Omit<React.HTMLAttributes<HTMLDivElement>, 'title' | 'children'>;

export interface ManagerPageContainerProps<ENTITY extends BaseEntity> extends ActionBarComponentProps, EntityTableProps<ENTITY>, DivHTMLAttributes {
    delete: <T extends BaseManagerDeleteDTO>(props: T) => Promise<unknown>;
    update: <T extends BaseManagerUpdateDTO>(props: T) => Promise<unknown>;
    create: <T extends object>(props: T) => Promise<unknown>;
    editModalFormChildren?: React.ReactNode | JSX.Element | ((editingItem: ENTITY | null) => React.ReactNode | JSX.Element);
    editModalInitialValues?: object;
    showActionBar?: boolean;
}

export interface ManagerPageContainerRef extends EntityTableRef {
    openModal: () => void;
}

export type ManagerPageContainerReturnType =
    <ENTITY extends BaseEntity>(
        props: ManagerPageContainerProps<ENTITY> & React.RefAttributes<ManagerPageContainerRef>
    ) => ReactNode;

export const ManagerPageContainer = forwardRef(ManagerPageContainerInner) as ManagerPageContainerReturnType

function ManagerPageContainerInner<ENTITY extends BaseEntity>(
    props: ManagerPageContainerProps<ENTITY>,
    ref: ForwardedRef<ManagerPageContainerRef>,
) {
    const [modal, contextHolder] = Modal.useModal();

    const isCustomTableSelector = props.tableSelection !== undefined && props.tableSelection !== null;

    const entityTableRef = useRef<EntityTableRef | null>(null);

    // New / Edit Modal
    const [isModalVisible, setIsModalVisible] = useState(false);
    const [editingItem, setEditingItem] = useState<ENTITY | null>(null);
    const [form] = Form.useForm();

    // Selector
    const [selectedEntities, setSelectedEntities] = useState<ENTITY[]>([]);
    const [batchOperationType, setBatchOperationType] = useState(0);

    const handleOnBatchOperationClick = () => {
        if (batchOperationType === 1) {
            if (selectedEntities.length <= 0) {
                return;
            }

            modal.confirm({
                title: '删除所有选中的项目',
                icon: <ExclamationCircleFilled />,
                content: '此操作不可恢复，请确认是否要继续？',
                onOk() {
                    props
                        .delete({ ids: selectedEntities.map((entity) => entity.id) })
                        .then(() => {
                            void message.success("批量删除成功");
                            entityTableRef?.current?.refreshData();
                        })
                        .finally(() => {
                            void message.error("批量删除失败");
                        })
                },
            });
        }
    };


    const openModal = (item: ENTITY | null = null) => {
        setEditingItem(item);

        if (item) {
            form.setFieldsValue(item);
        } else {
            form.resetFields();
            if (props.editModalInitialValues) {
                form.setFieldsValue(props.editModalInitialValues);
            }
        }

        setIsModalVisible(true);
    };

    const deleteModel = (id: string) => {
        props.delete({ ids: [id] })
            .then(() => {
                void message.success(`${props.entityName}已刪除`);
                entityTableRef?.current?.refreshData();
            })
            .catch(() => {
                void message.error(`${props.entityName}删除失败`);
            })
    };

    const handleAddOrUpdateEdit = (values: ENTITY) => {
        if (editingItem) {
            props.update(values).then(() => {
                entityTableRef?.current?.refreshData();
                void message.success(`${props.entityName}更新成功`);
            }).catch(() => {
                void message.error(`${props.entityName}更新失败`);
            })
        } else {
            props.create(values).then(() => {
                entityTableRef?.current?.refreshData();
                void message.success(`新增${props.entityName}成功`);
            }).catch(() => {
                void message.error(`新增${props.entityName}失败`);
            })
        }

        setIsModalVisible(false);
        setEditingItem(null);
        form.resetFields();
    };

    useImperativeHandle(ref, () => {
        return {
            refreshData: () => {
                entityTableRef?.current?.refreshData();
            },
            openModal: () => {
                openModal();
            }
        }
    });

    const showActionBar = props.showActionBar !== false;

    const { className, style, ...restProps } = props;

    return (
        <div className={className} style={style}>
            {showActionBar && (
                <ActionBarComponent
                    title={restProps.title}
                    subtitle={restProps.subtitle}
                    titleActions={<>
                        {restProps.titleActions}

                        <Button
                            type="primary"
                            icon={<PlusOutlined/>}
                            size="large"
                            className="rounded-xl h-12 shadow-lg"
                            onClick={() => openModal()}
                        >
                            新增{restProps.entityName}
                        </Button>
                    </>}
                />
            )}

            <Card className="border-none shadow-sm rounded-2xl overflow-hidden">
                <EntityTable
                    ref={entityTableRef}
                    entityName={restProps.entityName}
                    columns={restProps.columns}
                    query={restProps.query}
                    tablePrefixActions={isCustomTableSelector ? restProps.tablePrefixActions : [
                        ...[{
                            label: '批量操作',
                            children: <div className="flex flex-row items-center gap-2">
                                <Select
                                    className="min-w-32"
                                    style={{ width: 120 }}
                                    options={[
                                        { value: '1', label: '全部删除' },
                                    ]}
                                    onChange={(value) => setBatchOperationType(Number.parseInt(value))}
                                    placeholder="批量操作"
                                />

                                <Button
                                    type="primary"
                                    onClick={handleOnBatchOperationClick}
                                >
                                    执行
                                </Button>
                            </div>,
                        }],
                        ...(restProps.tablePrefixActions ?? []),
                    ]}
                    tableActions={restProps.tableActions}
                    tableRowActionsRender={(record) => (
                        <Space>
                            {restProps.tableRowActionsRender?.(record)}
                            <Button type="text" size="small" icon={<EditOutlined />} onClick={() => openModal(record)} />
                            <Popconfirm title={`确定要删除此${props.entityName}？`} onConfirm={() => deleteModel(record.id)} okText="确认" cancelText="取消">
                                <Button type="text" size="small" icon={<DeleteOutlined />} danger />
                            </Popconfirm>
                        </Space>
                    )}
                    tableSelection={isCustomTableSelector ? restProps.tableSelection : {
                        type: 'checkbox',
                        onChange: (entities) => {
                            setSelectedEntities(entities);
                        }
                    }}
                />
            </Card>

            <Modal
                title={(editingItem ? "编辑" : "新建") + restProps.entityName}
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

                    {typeof restProps.editModalFormChildren === 'function'
                        ? restProps.editModalFormChildren(editingItem)
                        : restProps.editModalFormChildren}
                </Form>
            </Modal>

            {contextHolder}
        </div>
    )
}
