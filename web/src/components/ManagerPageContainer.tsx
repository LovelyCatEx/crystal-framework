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
import type {TimeRangePickerProps} from 'antd';
import {Button, DatePicker, Form, Input, message, Modal, Popconfirm, Select, Space} from "antd";
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
import type {Dayjs} from 'dayjs';
import dayjs from 'dayjs';
import {StandardCard} from "@/components/card/StandardCard.tsx";
import {useManagerQueryParams} from "@/compositions/use-manager-query-params.ts";
type DivHTMLAttributes = Omit<React.HTMLAttributes<HTMLDivElement>, 'title' | 'children'>;

export interface ManagerPageContainerProps<ENTITY extends BaseEntity> extends ActionBarComponentProps, EntityTableProps<ENTITY>, DivHTMLAttributes {
    delete: <T extends BaseManagerDeleteDTO>(props: T) => Promise<unknown>;
    update: <T extends BaseManagerUpdateDTO>(props: T) => Promise<unknown>;
    create: <T extends object>(props: T) => Promise<unknown>;
    editModalFormChildren?: React.ReactNode | JSX.Element | ((editingItem: ENTITY | null) => React.ReactNode | JSX.Element);
    editModalInitialValues?: object;
    showActionBar?: boolean;
    readonlyMode?: boolean;
    showRowActions?: boolean;
    showTimeRangeFilter?: boolean;
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

    // Default URL sync — handles searchKeyword, page, pageSize, startTime, endTime
    // Pages that use useManagerQueryParams({ schema }) will pass their own syncToUrl / initialQueryValues,
    // which take priority via the ?? fallback below.
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
    const [batchOperationType, setBatchOperationType] = useState(0);

    // Time range filter
    const [timeRange, setTimeRange] = useState<[number, number | null] | null>(() => {
        const startTime = effectiveInitialQueryValues?.startTime;
        const endTime = effectiveInitialQueryValues?.endTime;
        if (startTime !== undefined) {
            const start = typeof startTime === 'number' ? startTime : Number(startTime);
            const end = endTime !== undefined ? (typeof endTime === 'number' ? endTime : Number(endTime)) : null;
            if (!Number.isNaN(start)) return [start, end && !Number.isNaN(end) ? end : null];
        }
        return null;
    });

    const handleOnBatchOperationClick = useCallback(() => {
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
                            setSelectedEntities([]);
                            entityTableRef?.current?.clearSelection();
                            entityTableRef?.current?.refreshData();
                        })
                        .catch(() => {
                            void message.error(t('components.managerPageContainer.batchDeleteFailed'));
                        })
                },
            });
        }
    }, [batchOperationType, selectedEntities, modal, props, t]);


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
                setSelectedEntities((prev) => prev.filter((it) => it.id !== id));
                entityTableRef?.current?.clearSelection();
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
            },
            clearSelection: () => {
                entityTableRef?.current?.clearSelection();
            }
        }
    });

    const showActionBar = props.showActionBar !== false;
    const readonlyMode = props.readonlyMode === true;
    const showRowActions = props.showRowActions !== false;

    const { className, style, ...restProps } = props;

    const builtinTablePrefixActions = useMemo(() => {
        if (readonlyMode) return restProps.tablePrefixActions ?? [];
        if (isCustomTableSelector) return restProps.tablePrefixActions;
        return [
            {
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
            },
            ...(restProps.tablePrefixActions ?? []),
        ];
    }, [readonlyMode, isCustomTableSelector, restProps.tablePrefixActions, t, handleOnBatchOperationClick]);

    const showTimeRangeFilter = props.showTimeRangeFilter !== false;

    const rangePresets: TimeRangePickerProps['presets'] = useMemo(() => {
        const now = dayjs();
        return [
            {
                label: t('components.managerPageContainer.todayToNow'),
                value: () => [dayjs().startOf('day'), dayjs()] as [Dayjs, Dayjs]
            },
            { label: t('components.managerPageContainer.last5Minutes'), value: [now.add(-5, 'minute'), now] },
            { label: t('components.managerPageContainer.last10Minutes'), value: [now.add(-10, 'minute'), now] },
            { label: t('components.managerPageContainer.last15Minutes'), value: [now.add(-15, 'minute'), now] },
            { label: t('components.managerPageContainer.last30Minutes'), value: [now.add(-30, 'minute'), now] },
            { label: t('components.managerPageContainer.last1Hour'), value: [now.add(-1, 'hour'), now] },
            { label: t('components.managerPageContainer.last2Hours'), value: [now.add(-2, 'hour'), now] },
            { label: t('components.managerPageContainer.last3Hours'), value: [now.add(-3, 'hour'), now] },
            { label: t('components.managerPageContainer.last4Hours'), value: [now.add(-4, 'hour'), now] },
            { label: t('components.managerPageContainer.last8Hours'), value: [now.add(-8, 'hour'), now] },
            { label: t('components.managerPageContainer.last12Hours'), value: [now.add(-12, 'hour'), now] },
            { label: t('components.managerPageContainer.last1Day'), value: [now.add(-1, 'day'), now] },
            { label: t('components.managerPageContainer.last3Days'), value: [now.add(-3, 'day'), now] },
            { label: t('components.managerPageContainer.last5Days'), value: [now.add(-5, 'day'), now] },
            { label: t('components.managerPageContainer.last7Days'), value: [now.add(-7, 'day'), now] },
            { label: t('components.managerPageContainer.last14Days'), value: [now.add(-14, 'day'), now] },
            { label: t('components.managerPageContainer.last30Days'), value: [now.add(-30, 'day'), now] },
        ];
    }, [t]);

    const builtinTableActions = useMemo(() => {
        const actions = [
            ...(restProps.tableActions ?? []),
        ];

        if (showTimeRangeFilter) {
            actions.push({
                label: <span>{t('components.managerPageContainer.timeRange')}</span>,
                children: <DatePicker.RangePicker
                    showTime
                    allowClear
                    presets={rangePresets}
                    defaultValue={timeRange ? [dayjs(timeRange[0]), timeRange[1] ? dayjs(timeRange[1]) : null] : undefined}
                    placeholder={[t('components.managerPageContainer.startTime'), t('components.managerPageContainer.tillNow')]}
                    allowEmpty={[false, true]}
                    onChange={(dates) => {
                        if (dates && dates[0]) {
                            setTimeRange([dates[0].valueOf(), dates[1]?.valueOf() ?? null]);
                        } else {
                            setTimeRange(null);
                        }
                        setTimeout(() => entityTableRef.current?.refreshData({ resetPage: true }), 0);
                    }}
                />,
                queryParamsProvider() {
                    if (!timeRange) return {};
                    return {
                        startTime: timeRange[0],
                        endTime: timeRange[1] ?? Date.now()
                    };
                }
            });
        }


        return actions;
    }, [restProps.tableActions, t, showTimeRangeFilter, timeRange, rangePresets]);

    const builtinTableSelection = useMemo<EntityTableProps<ENTITY>['tableSelection']>(() => {
        if (readonlyMode) return { type: 'disabled' };
        if (isCustomTableSelector) return restProps.tableSelection;
        return {
            type: 'checkbox',
            onChange: (entities) => {
                setSelectedEntities(entities);
            }
        };
    }, [readonlyMode, isCustomTableSelector, restProps.tableSelection]);

    return (
        <div className={className} style={style}>
            {showActionBar && (
                <ActionBarComponent
                    title={restProps.title}
                    subtitle={restProps.subtitle}
                    titleActions={<>
                        {restProps.titleActions}

                        {!readonlyMode && (
                            <Button
                                type="primary"
                                icon={<PlusOutlined/>}
                                size="large"
                                className="rounded-xl h-12 shadow-lg"
                                onClick={() => openModal()}
                            >
                                {t('components.managerPageContainer.addNew', { entityName: restProps.entityName })}
                            </Button>
                        )}
                    </>}
                />
            )}

            <StandardCard>
                <EntityTable
                    ref={entityTableRef}
                    entityName={restProps.entityName}
                    columns={restProps.columns}
                    query={restProps.query}
                    tablePrefixActions={builtinTablePrefixActions}
                    tableActions={builtinTableActions}
                    extraQueryParams={restProps.extraQueryParams}
                    filterableFields={restProps.filterableFields}
                    tableRowActionsRender={showRowActions ? (record) => (
                        <Space>
                            {restProps.tableRowActionsRender?.(record)}
                            {!readonlyMode && <>
                                <Button type="text" size="small" icon={<EditOutlined />} onClick={() => openModal(record)} />
                                <Popconfirm 
                                    title={t('components.managerPageContainer.deleteConfirm', { entityName: props.entityName })} 
                                    onConfirm={() => deleteModel(record.id)} 
                                    okText={t('components.managerPageContainer.confirm')} 
                                    cancelText={t('components.managerPageContainer.cancel')}
                                >
                                    <Button type="text" size="small" icon={<DeleteOutlined />} danger />
                                </Popconfirm>
                            </>}
                        </Space>
                    ) : undefined}
                    tableSelection={builtinTableSelection}
                    queryParamsSync={effectiveSyncToUrl}
                    initialQueryValues={effectiveInitialQueryValues}
                />
            </StandardCard>

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
                mask={{ closable: !submitting }}
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
