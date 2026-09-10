import React, {
    type ForwardedRef,
    forwardRef,
    type JSX,
    type ReactNode,
    useCallback,
    useImperativeHandle,
    useMemo,
    useRef,
    useState
} from "react";
import {Button, Form, Input, message, Modal, Popconfirm, Select, Space} from "antd";
import {DeleteOutlined, EditOutlined, ExclamationCircleFilled, PlusOutlined} from "@ant-design/icons";
import {useTranslation} from "react-i18next";
import {ActionBarComponent, type ActionBarComponentProps} from "./ActionBarComponent.tsx";
import type {BaseManagerDeleteDTO, BaseManagerUpdateDTO} from "../types/api.types.ts";
import type {BaseEntity} from "../types/BaseEntity.ts";
import {
    EntityTable,
    type EntityTableProps,
    type EntityTableRef,
    type EntityTableRefreshOptions
} from "./table/EntityTable.tsx";
import {StandardCard} from "@/components/card/StandardCard.tsx";
import {useManagerQueryParams} from "@/compositions/use-manager-query-params.ts";

type DivHTMLAttributes = Omit<React.HTMLAttributes<HTMLDivElement>, 'title' | 'children'>;

export interface ManagerPageContainerBatchAction<ENTITY extends BaseEntity> {
    key: string;
    label: React.ReactNode;
    confirmTitle?: React.ReactNode;
    confirmContent?: React.ReactNode;
    successMessage?: string;
    failedMessage?: string;
    handler: (entities: ENTITY[]) => Promise<unknown>;
}

export interface ManagerPageContainerProps<ENTITY extends BaseEntity> extends ActionBarComponentProps, EntityTableProps<ENTITY>, DivHTMLAttributes {
    delete?: <T extends BaseManagerDeleteDTO>(props: T) => Promise<unknown>;
    update?: <T extends BaseManagerUpdateDTO>(props: T) => Promise<unknown>;
    create: <T extends object>(props: T) => Promise<unknown>;
    editModalFormChildren?: React.ReactNode | JSX.Element | ((editingItem: ENTITY | null) => React.ReactNode | JSX.Element);
    editModalInitialValues?: object;
    showActionBar?: boolean;
    readonlyMode?: boolean;
    showRowActions?: boolean;
    extraBatchActions?: ManagerPageContainerBatchAction<ENTITY>[];
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

    const { syncToUrl: defaultSyncToUrl, initialQueryValues: defaultInitialQueryValues } = useManagerQueryParams();
    const effectiveSyncToUrl = props.queryParamsSync ?? defaultSyncToUrl;
    const effectiveInitialQueryValues = props.initialQueryValues ?? defaultInitialQueryValues;

    // New / Edit Modal
    const [isModalVisible, setIsModalVisible] = useState(false);
    const [editingItem, setEditingItem] = useState<ENTITY | null>(null);
    const [submitting, setSubmitting] = useState(false);
    const [form] = Form.useForm();

    // Selector
    const [selectedEntities, setSelectedEntities] = useState<ENTITY[]>([]);
    const [batchOperationKey, setBatchOperationKey] = useState<string | null>(null);

    const runBatchAction = useCallback((action: ManagerPageContainerBatchAction<ENTITY>) => {
        modal.confirm({
            title: action.confirmTitle ?? action.label,
            icon: <ExclamationCircleFilled />,
            content: action.confirmContent,
            onOk() {
                return action
                    .handler(selectedEntities)
                    .then(() => {
                        if (action.successMessage) void message.success(action.successMessage);
                        setSelectedEntities([]);
                        entityTableRef?.current?.clearSelection();
                        entityTableRef?.current?.refreshData();
                    })
                    .catch(() => {
                        if (action.failedMessage) void message.error(action.failedMessage);
                    });
            },
        });
    }, [modal, selectedEntities]);

    const builtinBatchActions = useMemo<ManagerPageContainerBatchAction<ENTITY>[]>(() => [
        ...(props.delete !== undefined ? [{
            key: 'delete',
            label: t('components.managerPageContainer.batchDelete'),
            confirmTitle: t('components.managerPageContainer.batchDeleteTitle'),
            confirmContent: t('components.managerPageContainer.batchDeleteConfirm'),
            successMessage: t('components.managerPageContainer.batchDeleteSuccess'),
            failedMessage: t('components.managerPageContainer.batchDeleteFailed'),
            handler: (entities) => props.delete!({ ids: entities.map((entity) => entity.id) }),
        } as ManagerPageContainerBatchAction<ENTITY>] : []),
        ...(props.extraBatchActions ?? []),
    ], [t, props]);

    const handleOnBatchOperationClick = useCallback(() => {
        if (!batchOperationKey) return;
        if (selectedEntities.length <= 0) return;
        const action = builtinBatchActions.find((it) => it.key === batchOperationKey);
        if (!action) return;
        runBatchAction(action);
    }, [batchOperationKey, selectedEntities, builtinBatchActions, runBatchAction]);

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
        if (!props.delete) {
            return;
        }

        props.delete!({ ids: [id] })
            .then(() => {
                void message.success(t('components.managerPageContainer.deleteSuccess', { entityName: props.entityName }));
                setSelectedEntities((prev) => prev.filter((it) => it.id !== id));
                entityTableRef?.current?.clearSelection();
                entityTableRef?.current?.refreshData();
            })
            .catch(() => {
                void message.error(t('components.managerPageContainer.deleteFailed', { entityName: props.entityName }));
            });
    };

    const handleAddOrUpdateEdit = (values: ENTITY) => {
        const isEditing = !!editingItem;
        if (isEditing && !props.update) {
            return;
        }
        const action = isEditing ? props.update!(values) : props.create(values);

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

    useImperativeHandle(ref, () => ({
        refreshData: (options?: EntityTableRefreshOptions) => {
            entityTableRef?.current?.refreshData?.(options);
        },
        openModal: () => openModal(),
        clearSelection: () => {
            entityTableRef?.current?.clearSelection();
        },
    }));

    const showActionBar = props.showActionBar !== false;
    const readonlyMode = props.readonlyMode === true;
    const showRowActions = props.showRowActions !== false;

    const { className, style } = props;

    const builtinTablePrefixActions = useMemo(() => {
        if (readonlyMode) return props.tablePrefixActions ?? [];
        if (isCustomTableSelector) return props.tablePrefixActions;
        return [
            {
                label: t('components.managerPageContainer.batchOperation'),
                children: (
                    <div className="flex flex-row items-center gap-2">
                        <Select
                            className="min-w-32"
                            style={{ width: 160 }}
                            options={builtinBatchActions.map((it) => ({ value: it.key, label: it.label }))}
                            onChange={(value) => setBatchOperationKey(value)}
                            placeholder={t('components.managerPageContainer.batchOperation')}
                        />
                        <Button type="primary" onClick={handleOnBatchOperationClick}>
                            {t('components.managerPageContainer.execute')}
                        </Button>
                    </div>
                ),
            },
            ...(props.tablePrefixActions ?? []),
        ];
    }, [readonlyMode, isCustomTableSelector, props.tablePrefixActions, t, builtinBatchActions, handleOnBatchOperationClick]);

    const builtinTableSelection = useMemo<EntityTableProps<ENTITY>['tableSelection']>(() => {
        if (readonlyMode) return { type: 'disabled' };
        if (isCustomTableSelector) return props.tableSelection;
        return {
            type: 'checkbox',
            onChange: (entities) => setSelectedEntities(entities),
        };
    }, [readonlyMode, isCustomTableSelector, props.tableSelection]);

    return (
        <div className={className} style={style}>
            {showActionBar && (
                <ActionBarComponent
                    title={props.title}
                    subtitle={props.subtitle}
                    titleActions={<>
                        {props.titleActions}
                        {!readonlyMode && (
                            <Button
                                type="primary"
                                icon={<PlusOutlined />}
                                size="large"
                                className="rounded-xl h-12 shadow-lg"
                                onClick={() => openModal()}
                            >
                                {t('components.managerPageContainer.addNew', { entityName: props.entityName })}
                            </Button>
                        )}
                    </>}
                />
            )}

            <StandardCard>
                <EntityTable
                    ref={entityTableRef}
                    {...props}
                    tablePrefixActions={builtinTablePrefixActions}
                    tableSelection={builtinTableSelection}
                    queryParamsSync={effectiveSyncToUrl}
                    initialQueryValues={effectiveInitialQueryValues}
                    tableRowActionsRender={showRowActions ? (record) => (
                        <Space>
                            {props.tableRowActionsRender?.(record)}
                            {!readonlyMode && <>
                                {props.update && <Button type="text" size="small" icon={<EditOutlined />} onClick={() => openModal(record)} />}
                                {props.delete && <Popconfirm
                                    title={t('components.managerPageContainer.deleteConfirm', { entityName: props.entityName })}
                                    onConfirm={() => deleteModel(record.id)}
                                    okText={t('components.managerPageContainer.confirm')}
                                    cancelText={t('components.managerPageContainer.cancel')}
                                >
                                    <Button type="text" size="small" icon={<DeleteOutlined />} danger />
                                </Popconfirm>}
                            </>}
                        </Space>
                    ) : undefined}
                />
            </StandardCard>

            <Modal
                title={(editingItem ? t('components.managerPageContainer.edit') : t('components.managerPageContainer.create')) + props.entityName}
                open={isModalVisible}
                onCancel={() => { if (!submitting) setIsModalVisible(false); }}
                onOk={() => form.submit()}
                width={800}
                centered
                confirmLoading={submitting}
                mask={{ closable: !submitting }}
                okButtonProps={{ className: "rounded-lg h-10 px-6" }}
                cancelButtonProps={{ className: "rounded-lg h-10 px-6", disabled: submitting }}
            >
                <Form form={form} layout="vertical" onFinish={handleAddOrUpdateEdit} className="mt-4">
                    <Form.Item name="id" hidden>
                        <Input />
                    </Form.Item>
                    {typeof props.editModalFormChildren === 'function'
                        ? props.editModalFormChildren(editingItem)
                        : props.editModalFormChildren}
                </Form>
            </Modal>

            {contextHolder}
        </div>
    );
}
