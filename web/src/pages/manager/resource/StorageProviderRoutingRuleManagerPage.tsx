import {Col, Form, Input, message, Modal, Row, Select, Spin, Switch, Tag} from "antd";
import {DndContext, KeyboardSensor, PointerSensor, closestCenter, useSensor, useSensors, type DragEndEvent} from "@dnd-kit/core";
import {SortableContext, arrayMove, useSortable, verticalListSortingStrategy} from "@dnd-kit/sortable";
import {CSS} from "@dnd-kit/utilities";
import {HolderOutlined} from "@ant-design/icons";
import React, {useCallback, useRef, useState} from "react";
import {useTranslation} from "react-i18next";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {StorageProviderRoutingRuleManagerController, reorderStorageProviderRoutingRules, type ManagerCreateStorageProviderRoutingRuleDTO, type ManagerReadStorageProviderRoutingRuleDTO,} from "@/api/resource/storage-provider-routing-rule.api.ts";
import type {StorageProviderRoutingRule} from "@/types/resource/storage-provider-routing-rule.types.ts";
import {RuleDistributionType} from "@/types/resource/storage-provider-routing-rule.types.ts";
import {type StorageProvider} from "@/types/resource/storage-provider.types.ts";
import {StorageProviderManagerController} from "@/api/resource/storage-provider.api.ts";
import {useStorageProviderRoutingRuleTableColumns} from "@/components/columns/StorageProviderRoutingRuleEntityColumns.tsx";
import {useStorageProviderTableColumns} from "@/components/columns/StorageProviderEntityColumns.tsx";
import {FilterBuilder} from "@/components/table/filter/index.ts";
import type {GroupNode} from "@/components/table/filter/filter-builder.types.ts";
import {getRuleDistributionType} from "@/i18n/enum-helpers.ts";
import {EntityIdsSelector} from "@/components/selector/EntityIdsSelector.tsx";

// ─── Drag-sortable row for the reorder modal ─────────────────────────────────
interface SortableRuleItemProps { rule: StorageProviderRoutingRule }

function SortableRuleItem({rule}: SortableRuleItemProps) {
    const {attributes, listeners, setNodeRef, setActivatorNodeRef, transform, transition, isDragging} = useSortable({id: rule.id});
    const style: React.CSSProperties = {transform: CSS.Transform.toString(transform), transition, opacity: isDragging ? 0.4 : 1};
    const {t} = useTranslation();
    return (
        <div ref={setNodeRef} style={style} className="flex items-center gap-3 px-3 py-2 rounded border border-gray-200 bg-white">
            <span ref={setActivatorNodeRef} className="cursor-grab active:cursor-grabbing text-gray-400" onClick={(e) => e.stopPropagation()} {...attributes} {...listeners}>
                <HolderOutlined />
            </span>
            <div className="flex flex-col min-w-0 flex-1">
                <span className="text-sm font-semibold truncate">{rule.name}</span>
                <span className="text-xs text-gray-400 truncate">
                    {rule.conditionTree ? rule.conditionTree.substring(0, 60) + '…' : t('pages.storageProviderRoutingRuleManager.reorderModal.matchAll')}
                </span>
            </div>
            {!rule.enabled && <Tag color="default" className="text-xs">OFF</Tag>}
        </div>
    );
}

// ─── Condition tree fields available in the FilterBuilder popover ───────────
const ROUTING_FIELDS = [
    {field: 'fileType', label: 'fileType', type: 'number' as const},
    {field: 'fileName', label: 'fileName', type: 'text' as const},
    {field: 'fileExtension', label: 'fileExtension', type: 'text' as const},
    {field: 'fileContentType', label: 'fileContentType', type: 'text' as const},
    {field: 'fileSize', label: 'fileSize', type: 'number' as const},
    {field: 'userId', label: 'userId', type: 'text' as const},
    {field: 'hourOfDay', label: 'hourOfDay', type: 'number' as const},
    {field: 'dayOfWeek', label: 'dayOfWeek', type: 'number' as const},
];

// ─── Form.Item wrapper: serializes GroupNode | null <-> JSON string ─────────
interface ConditionTreeFieldProps { value?: string | null; onChange?: (value: string | null) => void }

function ConditionTreeField({value, onChange}: ConditionTreeFieldProps) {
    let parsed: GroupNode | null = null;
    try {
        parsed = value ? (JSON.parse(value) as GroupNode) : null;
    } catch {
        parsed = null;
    }
    return <FilterBuilder fields={ROUTING_FIELDS} defaultValue={parsed} onChange={(node) => onChange?.(node ? JSON.stringify(node) : null)} />;
}

// ─── Form.Item wrapper: serializes string[] <-> JSON array string ───────────
interface TargetProviderIdsFieldProps { value?: string; onChange?: (value: string) => void }

function TargetProviderIdsField({value, onChange}: TargetProviderIdsFieldProps) {
    const {t} = useTranslation();
    const columns = useStorageProviderTableColumns();
    let ids: string[] = [];
    try {
        ids = value ? (JSON.parse(value) as unknown[]).map((id) => String(id)) : [];
    } catch {
        ids = [];
    }
    return (
        <EntityIdsSelector<StorageProvider>
            value={ids}
            onChange={(newIds) => onChange?.(JSON.stringify(newIds))}
            entityName={t('entityNames.storageProvider')}
            columns={columns}
            query={async (params) => (await StorageProviderManagerController.query(params)).data!}
            getById={(id) => StorageProviderManagerController.getById(id)}
            renderItem={(provider) => <Tag color="blue">{provider.name}</Tag>}
        />
    );
}

export default function StorageProviderRoutingRuleManagerPage() {
    const {t} = useTranslation();
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const baseColumns = useStorageProviderRoutingRuleTableColumns();

    const [reorderModalOpen, setReorderModalOpen] = useState(false);
    const [loadingRules, setLoadingRules] = useState(false);
    const [savingReorder, setSavingReorder] = useState(false);
    const [orderedRules, setOrderedRules] = useState<StorageProviderRoutingRule[]>([]);

    const sensors = useSensors(
        useSensor(PointerSensor, {activationConstraint: {distance: 4}}),
        useSensor(KeyboardSensor),
    );

    const handleToggleEnabled = useCallback((enabled: boolean, row: StorageProviderRoutingRule) => {
        StorageProviderRoutingRuleManagerController.update({id: row.id, enabled})
            .then(() => {
                void message.success(t('pages.storageProviderRoutingRuleManager.messages.enabledToggleSuccess'));
                pageRef.current?.refreshData();
            })
            .catch(() => void message.error(t('pages.storageProviderRoutingRuleManager.messages.enabledToggleFailed')));
    }, [t]);

    const columns = [
        ...baseColumns,
        {
            title: t('pages.storageProviderRoutingRuleManager.columns.enabled'),
            dataIndex: 'enabled',
            key: 'enabled',
            width: 100,
            render: (_: unknown, row: StorageProviderRoutingRule) => (
                <Switch value={row.enabled} onChange={(v) => handleToggleEnabled(v, row)} />
            ),
        },
    ];

    const openReorderModal = useCallback(() => {
        setLoadingRules(true);
        setReorderModalOpen(true);
        StorageProviderRoutingRuleManagerController.list()
            .then((res) => {
                const sorted = [...(res.data ?? [])].sort((a, b) => a.priority - b.priority);
                setOrderedRules(sorted);
            })
            .finally(() => setLoadingRules(false));
    }, []);

    const handleDragEnd = useCallback((event: DragEndEvent) => {
        const {active, over} = event;
        if (!over || active.id === over.id) return;
        setOrderedRules((prev) => {
            const oldIndex = prev.findIndex((r) => r.id === active.id);
            const newIndex = prev.findIndex((r) => r.id === over.id);
            if (oldIndex < 0 || newIndex < 0) return prev;
            return arrayMove(prev, oldIndex, newIndex);
        });
    }, []);

    const handleSaveReorder = useCallback(() => {
        setSavingReorder(true);
        reorderStorageProviderRoutingRules(orderedRules.map((r) => r.id))
            .then(() => {
                void message.success(t('pages.storageProviderRoutingRuleManager.messages.reorderSuccess'));
                setReorderModalOpen(false);
                pageRef.current?.refreshData({resetPage: true});
            })
            .catch(() => void message.error(t('pages.storageProviderRoutingRuleManager.messages.reorderFailed')))
            .finally(() => setSavingReorder(false));
    }, [orderedRules, t]);

    return (
        <>
            <ManagerPageContainer
                ref={pageRef}
                entityName={t('entityNames.storageProviderRoutingRule')}
                title={t('pages.storageProviderRoutingRuleManager.title')}
                subtitle={t('pages.storageProviderRoutingRuleManager.subtitle')}
                columns={columns}
                searchKeywords={['name']}
                titleActions={
                    <Select
                        className="rounded-xl"
                        style={{minWidth: 160}}
                        value="reorder"
                        onSelect={openReorderModal}
                        options={[{value: 'reorder', label: t('pages.storageProviderRoutingRuleManager.action.reorder')}]}
                    />
                }
                query={async (props: ManagerReadStorageProviderRoutingRuleDTO) => {
                    return (await StorageProviderRoutingRuleManagerController.query(props)).data!;
                }}
                delete={async (props) => (await StorageProviderRoutingRuleManagerController.delete(props)).data!}
                update={async (props) => (await StorageProviderRoutingRuleManagerController.update(props)).data!}
                create={async (props) => (await StorageProviderRoutingRuleManagerController.create(props as ManagerCreateStorageProviderRoutingRuleDTO)).data!}
                editModalFormChildren={
                    <>
                        <Row gutter={24}>
                            <Col span={16}>
                                <Form.Item name="name" label={t('pages.storageProviderRoutingRuleManager.modal.name.label')} rules={[{required: true, message: t('pages.storageProviderRoutingRuleManager.modal.name.required')}]}>
                                    <Input placeholder={t('pages.storageProviderRoutingRuleManager.modal.name.placeholder')} maxLength={64} showCount />
                                </Form.Item>
                            </Col>
                            <Col span={8}>
                                <Form.Item name="distributionType" label={t('pages.storageProviderRoutingRuleManager.modal.distributionType.label')} initialValue={RuleDistributionType.FIRST_AVAILABLE}>
                                    <Select options={[
                                        {value: RuleDistributionType.FIRST_AVAILABLE, label: getRuleDistributionType(RuleDistributionType.FIRST_AVAILABLE)},
                                        {value: RuleDistributionType.RANDOM, label: getRuleDistributionType(RuleDistributionType.RANDOM)},
                                    ]} />
                                </Form.Item>
                            </Col>
                        </Row>
                        <Form.Item name="enabled" label={t('pages.storageProviderRoutingRuleManager.modal.enabled.label')} valuePropName="checked" initialValue={true}>
                            <Switch />
                        </Form.Item>
                        <Form.Item name="targetProviderIds" label={t('pages.storageProviderRoutingRuleManager.modal.targetProviders.label')} rules={[{required: true, message: t('pages.storageProviderRoutingRuleManager.modal.targetProviders.required')}]} initialValue="[]">
                            <TargetProviderIdsField />
                        </Form.Item>
                        <Form.Item name="conditionTree" label={t('pages.storageProviderRoutingRuleManager.modal.conditionTree.label')} extra={t('pages.storageProviderRoutingRuleManager.modal.conditionTree.description')}>
                            <ConditionTreeField />
                        </Form.Item>
                    </>
                }
            />
            <Modal
                title={t('pages.storageProviderRoutingRuleManager.reorderModal.title')}
                open={reorderModalOpen}
                onCancel={() => setReorderModalOpen(false)}
                onOk={handleSaveReorder}
                okText={t('pages.storageProviderRoutingRuleManager.reorderModal.save')}
                cancelText={t('pages.storageProviderRoutingRuleManager.reorderModal.cancel')}
                confirmLoading={savingReorder}
                width={600}
            >
                <p className="text-sm text-gray-500 mb-3">{t('pages.storageProviderRoutingRuleManager.reorderModal.description')}</p>
                {loadingRules ? (
                    <div className="flex justify-center py-8"><Spin /></div>
                ) : (
                    <DndContext sensors={sensors} collisionDetection={closestCenter} onDragEnd={handleDragEnd}>
                        <SortableContext items={orderedRules.map((r) => r.id)} strategy={verticalListSortingStrategy}>
                            <div className="flex flex-col gap-2 max-h-96 overflow-y-auto">
                                {orderedRules.map((rule) => <SortableRuleItem key={rule.id} rule={rule} />)}
                            </div>
                        </SortableContext>
                    </DndContext>
                )}
            </Modal>
        </>
    );
}
