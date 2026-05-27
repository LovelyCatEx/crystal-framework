import {useCallback, useEffect, useRef, useState, type HTMLAttributes} from 'react';
import {Badge, Button, Popover} from 'antd';
import {CheckOutlined, CloseOutlined, FilterOutlined, ReloadOutlined} from '@ant-design/icons';
import {useTranslation} from 'react-i18next';
import type {FilterableField, GroupNode} from './filter-builder.types.ts';
import {
  createEmptyGroup,
  fromGroupNode,
  getActiveConditionCount,
  hasActiveConditions,
  hasMissingValues,
  toGroupNode,
} from './filter-builder.types.ts';
import {FilterGroup} from './FilterGroup.tsx';

export interface FilterBuilderProps extends Omit<HTMLAttributes<HTMLDivElement>, 'defaultValue' | 'onChange'> {
  fields: FilterableField[];
  defaultValue?: GroupNode | null;
  onChange: (node: GroupNode | null) => void;
}

export function FilterBuilder({fields, defaultValue, onChange, className, style, ...divProps}: FilterBuilderProps) {
  const {t} = useTranslation();
  const [open, setOpen] = useState(false);
  const [root, setRoot] = useState(() =>
    defaultValue ? fromGroupNode(defaultValue) : createEmptyGroup('and'),
  );
  const prevActiveRef = useRef(false);

  // Notify parent when root state changes (only when popover is closed — user clicked Apply)
  const applyFilters = useCallback(() => {
    if (hasActiveConditions(root)) {
      onChange(toGroupNode(root));
    } else {
      onChange(null);
    }
    setOpen(false);
  }, [root, onChange]);

  const resetFilters = useCallback(() => {
    const empty = createEmptyGroup('and');
    setRoot(empty);
    onChange(null);
    setOpen(false);
  }, [onChange]);

  const count = getActiveConditionCount(root);
  const missingValues = hasMissingValues(root);
  // Update badge while editing
  useEffect(() => {
    const hasActive = hasActiveConditions(root);
    if (hasActive !== prevActiveRef.current) {
      prevActiveRef.current = hasActive;
    }
  }, [root]);

  return (
    <div className={className} style={style} {...divProps}>
    <Popover
      open={open}
      onOpenChange={setOpen}
      trigger="click"
      placement="bottomLeft"
      arrow={false}
      overlayClassName="min-w-[640px] max-w-[800px]"
      content={
        <div className="flex flex-col gap-3 py-1">
          <FilterGroup
            group={root}
            fields={fields}
            isRoot
            onUpdate={setRoot}
          />
          <div className="flex items-center justify-between border-t border-gray-100 pt-3">
            <Button size="middle" icon={<ReloadOutlined />} onClick={resetFilters}>
              {t('components.filterBuilder.reset')}
            </Button>
            <div className="flex items-center gap-2">
              {missingValues && (
                <span className="text-xs text-red-400">{t('components.filterBuilder.fillRequired')}</span>
              )}
              <Button size="middle" icon={<CloseOutlined />} onClick={() => setOpen(false)}>
                {t('components.filterBuilder.cancel')}
              </Button>
              <Button size="middle" type="primary" icon={<CheckOutlined />} disabled={missingValues} onClick={applyFilters}>
                {t('components.filterBuilder.apply')}
              </Button>
            </div>
          </div>
        </div>
      }
    >
      <Badge count={count} size="small" offset={[4, -4]}>
        <Button
          icon={<FilterOutlined />}
          className="rounded-xl"
          type={count > 0 ? 'primary' : 'default'}
          ghost={count > 0}
        >
          {t('components.filterBuilder.filters')}
        </Button>
      </Badge>
    </Popover>
    </div>
  );
}
