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
import {useTranslation} from "react-i18next";
import {ActionBarComponent, type ActionBarComponentProps} from "./ActionBarComponent.tsx";
import type {BaseManagerDeleteDTO, BaseManagerUpdateDTO} from "../types/api.types.ts";
import type {BaseEntity} from "../types/BaseEntity.ts";
import {EntityTable, type EntityTableProps, type EntityTableRef, type EntityTableRefreshOptions} from "./EntityTable.tsx";

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
    const { t } = useTranslation();
    const [modal, contextHolder] = Modal.useModal();

    const isCustomTableSelector = props.tableSelection !== undefined && props.tableSelection !== null;

    const entityTableRef = useRef<EntityTableRef | null>(null);

    // New / Edit Modal
    const [isModalVisible, setIsModalVisible] = useState(false);
    const [editingItem, setEditingItem] = useState<ENTITY | null>(null);
    const [submitting, setSubmitting] = useState(false);
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
                title: t('components.managerPageContainer.batchDeleteTitle'),
                icon: <ExclamationCircleFilled />,
                content: t('components.managerPageContainer.batchDeleteConfirm'),
                onOk() {
                    return props
                        .delete({ ids: selectedEntities.map((entity) => entity.id) })
                        .then(() => {
                            void message.success(t('components.managerPageContainer.batchDeleteSuccess'));
                            entityTableRef?.current?.refreshData();
                        })
                        .catch(() => {
                            void message.error(t('components.managerPageContainer.batchDeleteFailed'));
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
                void message.success(t('components.managerPageContainer.deleteSuccess', { entityName: props.entityName }));
                entityTableRef?.current?.refreshData();
            })
            .catch(() => {
                void message.error(t('components.managerPageContainer.deleteFailed', { entityName: props.entityName }));
            })
    };

    const handleAddOrUpdateEdit = (values: ENTITY) => {
        const isEditing = !!editingItem;
        const action = isEditing ? props.update(values) : props.create(values);

        setSubmitting(true);
        action.then(() => {
            entityTableRef?.current?.refreshData();
            void message.success(t(
                isEditing ? 'components.managerPageContainer.updateSuccess' : 'components.managerPageContainer.createSuccess',
                { entityName: props.entityName }
            ));
            setIsModalVisible(false);
            setEditingItem(null);
            form.resetFields();
        }).catch(() => {
            void message.error(t(
                isEditing ? 'components.managerPageContainer.updateFailed' : 'components.managerPageContainer.createFailed',
                { entityName: props.entityName }
            ));
        }).finally(() => {
            setSubmitting(false);
        });
    };

    useImperativeHandle(ref, () => {
        return {
            refreshData: (options?: EntityTableRefreshOptions) => {
                entityTableRef?.current?.refreshData?.(options);
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
                            {t('components.managerPageContainer.addNew', { entityName: restProps.entityName })}
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
                            label: t('components.managerPageContainer.batchOperation'),
                            children: <div className="flex flex-row items-center gap-2">
                                <Select
                                    className="min-w-32"
                                    style={{ width: 120 }}
                                    options={[
                                        { value: '1', label: t('components.managerPageContainer.batchDelete') },
                                    ]}
                                    onChange={(value) => setBatchOperationType(Number.parseInt(value))}
                                    placeholder={t('components.managerPageContainer.batchOperation')}
                                />

                                <Button
                                    type="primary"
                                    onClick={handleOnBatchOperationClick}
                                >
                                    {t('components.managerPageContainer.execute')}
                                </Button>
                            </div>,
                        }],
                        ...(restProps.tablePrefixActions ?? []),
                    ]}
                    tableActions={[
                        ...(restProps.tableActions ?? []),
                        ...[{
                            label: t('components.managerPageContainer.action'),
                            children: <Button
                                type="primary"
                                onClick={() => entityTableRef.current?.refreshData()}
                            >
                                {t('components.managerPageContainer.refresh')}
                            </Button>
                        }]
                    ]}
                    tableRowActionsRender={(record) => (
                        <Space>
                            {restProps.tableRowActionsRender?.(record)}
                            <Button type="text" size="small" icon={<EditOutlined />} onClick={() => openModal(record)} />
                            <Popconfirm 
                                title={t('components.managerPageContainer.deleteConfirm', { entityName: props.entityName })} 
                                onConfirm={() => deleteModel(record.id)} 
                                okText={t('components.managerPageContainer.confirm')} 
                                cancelText={t('components.managerPageContainer.cancel')}
                            >
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
                title={(editingItem ? t('components.managerPageContainer.edit') : t('components.managerPageContainer.create')) + restProps.entityName}
                open={isModalVisible}
                onCancel={() => {
                    if (submitting) return;
                    setIsModalVisible(false);
                }}
                onOk={() => form.submit()}
                width={800}
                centered
                confirmLoading={submitting}
                maskClosable={!submitting}
                okButtonProps={{ className: "rounded-lg h-10 px-6" }}
                cancelButtonProps={{ className: "rounded-lg h-10 px-6", disabled: submitting }}
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
