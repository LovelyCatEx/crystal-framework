import {Button, Empty, Tag, Typography} from "antd";
import {DeleteOutlined, HolderOutlined, PlusOutlined} from "@ant-design/icons";
import {DndContext, KeyboardSensor, PointerSensor, closestCenter, useSensor, useSensors, type DragEndEvent} from "@dnd-kit/core";
import {SortableContext, arrayMove, useSortable, verticalListSortingStrategy} from "@dnd-kit/sortable";
import {CSS} from "@dnd-kit/utilities";
import {useTranslation} from "react-i18next";
import type {ApprovalFieldSchema} from "@/types/approval/approval-form-schema.types.ts";
import {getApprovalFieldType} from "@/i18n/enum-helpers.ts";

export interface ApprovalFormFieldListProps {
    fields: ApprovalFieldSchema[];
    selectedKey: string | null;
    onReorder: (fields: ApprovalFieldSchema[]) => void;
    onSelect: (key: string) => void;
    onDelete: (key: string) => void;
    onAdd: () => void;
    /** Keys that fail validation (duplicate / invalid regex / empty). Highlighted with a red border. */
    invalidKeys?: Set<string>;
}

interface SortableFieldItemProps {
    field: ApprovalFieldSchema;
    isSelected: boolean;
    isInvalid: boolean;
    onSelect: () => void;
    onDelete: () => void;
    deleteAriaLabel: string;
}

function SortableFieldItem({field, isSelected, isInvalid, onSelect, onDelete, deleteAriaLabel}: SortableFieldItemProps) {
    const {attributes, listeners, setNodeRef, setActivatorNodeRef, transform, transition, isDragging} = useSortable({id: field.key});
    const style: React.CSSProperties = {
        transform: CSS.Transform.toString(transform),
        transition,
        opacity: isDragging ? 0.4 : 1,
    };

    return (
        <div
            ref={setNodeRef}
            style={style}
            className={[
                "flex items-center gap-2 px-2 py-2 rounded border cursor-pointer transition",
                isSelected ? "border-blue-500 bg-blue-50" : "border-gray-200 hover:border-gray-400",
                isInvalid ? "!border-red-500" : "",
            ].join(" ")}
            onClick={onSelect}
            {...attributes}
        >
            <span
                ref={setActivatorNodeRef}
                className="cursor-grab active:cursor-grabbing text-gray-400"
                onClick={(e) => e.stopPropagation()}
                {...listeners}
            >
                <HolderOutlined/>
            </span>
            <div className="flex-1 flex flex-col min-w-0">
                <Typography.Text ellipsis className="!m-0 !text-sm">
                    {field.label || field.key}
                </Typography.Text>
                <div className="flex items-center gap-1 mt-0.5">
                    <Typography.Text type="secondary" className="!text-xs font-mono">
                        {field.key}
                    </Typography.Text>
                    <Tag color="blue" className="!m-0 !text-[10px]">{getApprovalFieldType(field.type)}</Tag>
                    {field.required && <Tag color="red" className="!m-0 !text-[10px]">*</Tag>}
                </div>
            </div>
            <Button
                type="text"
                size="small"
                danger
                icon={<DeleteOutlined/>}
                onClick={(e) => {
                    e.stopPropagation();
                    onDelete();
                }}
                aria-label={deleteAriaLabel}
            />
        </div>
    );
}

export function ApprovalFormFieldList(props: ApprovalFormFieldListProps) {
    const {fields, selectedKey, onReorder, onSelect, onDelete, onAdd, invalidKeys} = props;
    const {t} = useTranslation();

    const sensors = useSensors(
        useSensor(PointerSensor, {activationConstraint: {distance: 4}}),
        useSensor(KeyboardSensor),
    );

    const handleDragEnd = (event: DragEndEvent) => {
        const {active, over} = event;
        if (!over || active.id === over.id) return;
        const oldIndex = fields.findIndex(f => f.key === active.id);
        const newIndex = fields.findIndex(f => f.key === over.id);
        if (oldIndex < 0 || newIndex < 0) return;
        onReorder(arrayMove(fields, oldIndex, newIndex));
    };

    const invalidSet = invalidKeys ?? new Set<string>();

    return (
        <div className="flex flex-col gap-2">
            <div className="flex items-center justify-between">
                <Typography.Text strong>{t('components.approvalFormDesigner.fieldList.title')}</Typography.Text>
                <Button type="primary" size="small" icon={<PlusOutlined/>} onClick={onAdd}>
                    {t('components.approvalFormDesigner.fieldList.addField')}
                </Button>
            </div>
            {fields.length === 0 ? (
                <Empty description={t('components.approvalFormDesigner.fieldList.empty')} className="!my-4"/>
            ) : (
                <DndContext sensors={sensors} collisionDetection={closestCenter} onDragEnd={handleDragEnd}>
                    <SortableContext items={fields.map(f => f.key)} strategy={verticalListSortingStrategy}>
                        <div className="flex flex-col gap-1">
                            {fields.map(field => (
                                <SortableFieldItem
                                    key={field.key}
                                    field={field}
                                    isSelected={selectedKey === field.key}
                                    isInvalid={invalidSet.has(field.key)}
                                    onSelect={() => onSelect(field.key)}
                                    onDelete={() => onDelete(field.key)}
                                    deleteAriaLabel={t('components.approvalFormDesigner.fieldList.deleteField')}
                                />
                            ))}
                        </div>
                    </SortableContext>
                </DndContext>
            )}
        </div>
    );
}
