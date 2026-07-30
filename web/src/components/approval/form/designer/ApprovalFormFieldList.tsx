import {Button, Empty, Tag, theme, Tooltip, Typography} from "antd";
import {DeleteOutlined, HolderOutlined, PlusOutlined} from "@ant-design/icons";
import {DndContext, KeyboardSensor, PointerSensor, closestCenter, useSensor, useSensors, type DragEndEvent} from "@dnd-kit/core";
import {SortableContext, arrayMove, useSortable, verticalListSortingStrategy} from "@dnd-kit/sortable";
import {CSS} from "@dnd-kit/utilities";
import {AlignLeft, Calendar, CalendarClock, CircleDot, Hash, List, SquareCheck, ToggleLeft, Type} from "lucide-react";
import type {ComponentType} from "react";
import {useTranslation} from "react-i18next";
import type {ApprovalFieldSchema} from "@/types/approval/approval-form-schema.types.ts";
import {ApprovalFieldType} from "@/types/approval/approval-enums.ts";
import {getApprovalFieldType} from "@/i18n/enum-helpers.ts";

const FIELD_TYPE_ICON: Record<ApprovalFieldType, ComponentType<{size?: number; className?: string}>> = {
    [ApprovalFieldType.TEXT]: Type,
    [ApprovalFieldType.TEXTAREA]: AlignLeft,
    [ApprovalFieldType.NUMBER]: Hash,
    [ApprovalFieldType.BOOLEAN]: ToggleLeft,
    [ApprovalFieldType.SELECT]: List,
    [ApprovalFieldType.RADIO]: CircleDot,
    [ApprovalFieldType.CHECKBOX]: SquareCheck,
    [ApprovalFieldType.DATE]: Calendar,
    [ApprovalFieldType.DATETIME]: CalendarClock,
};

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
    const {token} = theme.useToken();
    const style: React.CSSProperties = {
        transform: CSS.Transform.toString(transform),
        transition,
        opacity: isDragging ? 0.4 : 1,
        borderColor: isInvalid
            ? token.colorError
            : isSelected
                ? token.colorPrimary
                : token.colorBorder,
        background: isSelected ? token.colorPrimaryBg : undefined,
    };

    const TypeIcon = FIELD_TYPE_ICON[field.type];

    return (
        <div
            ref={setNodeRef}
            style={style}
            className={[
                "flex items-center gap-2 px-2 py-2 rounded border cursor-pointer transition",
                isSelected ? "" : "hover:border-gray-400",
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
            <Tooltip title={getApprovalFieldType(field.type)}>
                <TypeIcon size={16} className="text-gray-500 shrink-0"/>
            </Tooltip>
            <div className="flex-1 flex flex-col min-w-0 ml-2">
                <Typography.Text ellipsis className="!m-0 !text-sm">
                    {field.label || field.key}
                </Typography.Text>
                <div className="flex items-center gap-1 mt-0.5">
                    <Typography.Text type="secondary" className="!text-xs font-mono">
                        {field.key}
                    </Typography.Text>
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
