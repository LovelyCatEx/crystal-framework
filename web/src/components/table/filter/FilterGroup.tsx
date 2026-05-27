import {useCallback, useState, type ReactNode} from 'react';
import {Button, Select, Tag} from 'antd';
import {DeleteOutlined, PlusOutlined, ApartmentOutlined, HolderOutlined} from '@ant-design/icons';
import {useTranslation} from 'react-i18next';
import {DndContext, DragOverlay, closestCenter, PointerSensor, KeyboardSensor, useSensor, useSensors, type DragEndEvent, type DragStartEvent} from '@dnd-kit/core';
import {SortableContext, verticalListSortingStrategy, useSortable, arrayMove} from '@dnd-kit/sortable';
import {CSS} from '@dnd-kit/utilities';
import type {FilterableField, FilterGroupState} from './filter-builder.types.ts';
import {isGroupNode, addRowToGroup, addGroupToGroup} from './filter-builder.types.ts';
import {FilterConditionRow} from './FilterConditionRow.tsx';

// ── Sortable item wrapper with drag handle ──────────

function SortableItem({id, logicLabel, spacer, children}: {id: string; logicLabel?: string; spacer?: boolean; children: ReactNode}) {
  const {attributes, listeners, setNodeRef, setActivatorNodeRef, transform, transition, isDragging} = useSortable({id});

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0 : 1,
  };

  return (
    <div ref={setNodeRef} style={style} className="flex items-start gap-1">
      <div className="flex items-center gap-1 mt-[3px]">
        {logicLabel !== undefined ? (
          <span className="text-xs font-medium text-gray-400 min-w-[28px] text-center select-none">
            {logicLabel}
          </span>
        ) : spacer ? (
          <span className="min-w-[28px]" />
        ) : null}
        <button
          ref={setActivatorNodeRef}
          {...attributes}
          {...listeners}
          className="cursor-grab active:cursor-grabbing text-gray-300 hover:text-gray-500 p-0.5 rounded transition-colors"
        >
          <HolderOutlined />
        </button>
      </div>
      <div className="flex-1 min-w-0">
        {children}
      </div>
    </div>
  );
}

// ── Group component ─────────────────────────────────

interface FilterGroupProps {
  group: FilterGroupState;
  fields: FilterableField[];
  isRoot?: boolean;
  onUpdate: (group: FilterGroupState) => void;
  onDelete?: () => void;
}

export function FilterGroup({group, fields, isRoot, onUpdate, onDelete}: FilterGroupProps) {
  const {t} = useTranslation();

  const sensors = useSensors(
    useSensor(PointerSensor, {activationConstraint: {distance: 5}}),
    useSensor(KeyboardSensor),
  );

  const handleLogicChange = useCallback((logic: 'and' | 'or') => {
    onUpdate({...group, logic});
  }, [group, onUpdate]);

  const handleChildUpdate = useCallback((index: number, child: typeof group.children[number]) => {
    const newChildren = [...group.children];
    newChildren[index] = child;
    onUpdate({...group, children: newChildren});
  }, [group, onUpdate]);

  const handleChildDelete = useCallback((index: number) => {
    const newChildren = group.children.filter((_, i) => i !== index);
    onUpdate({...group, children: newChildren});
  }, [group, onUpdate]);

  const handleAddCondition = useCallback(() => {
    onUpdate(addRowToGroup(group));
  }, [group, onUpdate]);

  const handleAddGroup = useCallback(() => {
    onUpdate(addGroupToGroup(group));
  }, [group, onUpdate]);

  const [activeDragId, setActiveDragId] = useState<string | null>(null);
  const activeDragChild = activeDragId ? group.children.find(c => c.id === activeDragId) ?? null : null;

  const handleDragStart = useCallback((event: DragStartEvent) => {
    setActiveDragId(String(event.active.id));
  }, []);

  const handleDragEnd = useCallback((event: DragEndEvent) => {
    setActiveDragId(null);
    const {active, over} = event;
    if (!over || active.id === over.id) return;
    const oldIndex = group.children.findIndex(c => c.id === active.id);
    const newIndex = group.children.findIndex(c => c.id === over.id);
    if (oldIndex !== -1 && newIndex !== -1) {
      onUpdate({...group, children: arrayMove(group.children, oldIndex, newIndex)});
    }
  }, [group, onUpdate]);

  const childIds = group.children.map(c => c.id);

  return (
    <div className={`rounded-lg border ${isRoot ? 'border-gray-200' : 'border-dashed border-gray-300 bg-gray-50'} p-3`}>
      {/* Group header */}
      <div className="flex items-center justify-between mb-2">
        <div className="flex items-center gap-2">
          {isRoot ? (
            <span className="text-sm font-medium text-gray-500">
              {t('components.filterBuilder.rootLogic')}
            </span>
          ) : (
            <span className="flex items-center gap-1 text-xs font-medium text-gray-400">
              <ApartmentOutlined />
              {t('components.filterBuilder.group')}
            </span>
          )}
          <Select
            size="small"
            className="min-w-[72px]"
            value={group.logic}
            onChange={handleLogicChange}
            options={[
              {value: 'and', label: t('components.filterBuilder.and')},
              {value: 'or', label: t('components.filterBuilder.or')},
            ]}
          />
        </div>

        <div className="flex items-center gap-1">
          <Button
            type="text"
            size="small"
            icon={<PlusOutlined />}
            onClick={handleAddCondition}
          >
            {t('components.filterBuilder.addCondition')}
          </Button>
          <Button
            type="text"
            size="small"
            icon={<ApartmentOutlined />}
            onClick={handleAddGroup}
          >
            {t('components.filterBuilder.addGroup')}
          </Button>
          {!isRoot && onDelete && (
            <Button
              type="text"
              size="small"
              danger
              icon={<DeleteOutlined />}
              onClick={onDelete}
            />
          )}
        </div>
      </div>

      {/* Group children */}
      {group.children.length === 0 && (
        <div className="text-xs text-gray-400 py-2 text-center">
          {t('components.filterBuilder.noConditions')}
        </div>
      )}

      <DndContext sensors={sensors} collisionDetection={closestCenter} onDragStart={handleDragStart} onDragEnd={handleDragEnd}>
        <SortableContext items={childIds} strategy={verticalListSortingStrategy}>
          <div className="flex flex-col gap-2">
            {group.children.map((child, index) => (
              <SortableItem
                key={child.id}
                id={child.id}
                logicLabel={index > 0 ? t(group.logic === 'and' ? 'components.filterBuilder.and' : 'components.filterBuilder.or') : undefined}
                spacer={index === 0 && group.children.length > 1}
              >
                {isGroupNode(child) ? (
                  <FilterGroup
                    group={child}
                    fields={fields}
                    onUpdate={(updated) => handleChildUpdate(index, updated)}
                    onDelete={() => handleChildDelete(index)}
                  />
                ) : (
                  <FilterConditionRow
                    row={child}
                    fields={fields}
                    onUpdate={(updated) => handleChildUpdate(index, updated)}
                    onDelete={() => handleChildDelete(index)}
                  />
                )}
              </SortableItem>
            ))}
          </div>
        </SortableContext>
        <DragOverlay>
          {activeDragChild && (
            <div className="bg-white rounded-lg shadow-xl border border-gray-200 px-4 py-2.5">
              {isGroupNode(activeDragChild) ? (
                <span className="text-sm text-gray-500">
                  <ApartmentOutlined className="mr-1" />
                  {t('components.filterBuilder.group')}
                </span>
              ) : (
                <div className="flex items-center gap-2 text-sm">
                  <Tag className="m-0">
                    {fields.find(f => f.field === activeDragChild.field)?.label ?? activeDragChild.field ?? t('components.filterBuilder.selectField')}
                  </Tag>
                  {activeDragChild.operator && (
                    <>
                      <span className="text-gray-400">{t(`components.filterBuilder.operators.${activeDragChild.operator}` as any)}</span>
                      <span className="text-gray-600">
                        {Array.isArray(activeDragChild.value) ? activeDragChild.value.join(' | ') : String(activeDragChild.value ?? '')}
                      </span>
                    </>
                  )}
                </div>
              )}
            </div>
          )}
        </DragOverlay>
      </DndContext>
    </div>
  );
}
