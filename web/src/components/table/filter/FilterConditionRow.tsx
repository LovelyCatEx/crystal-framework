import {useCallback} from 'react';
import {Button, DatePicker, Input, InputNumber, Select} from 'antd';
import {CloseOutlined} from '@ant-design/icons';
import {useTranslation} from 'react-i18next';
import type {FilterableField, FilterRowState} from './filter-builder.types.ts';
import {findFieldDef, getFieldOperators} from './filter-builder.types.ts';
import dayjs from 'dayjs';

interface FilterConditionRowProps {
  row: FilterRowState;
  fields: FilterableField[];
  onUpdate: (row: FilterRowState) => void;
  onDelete: () => void;
}

export function FilterConditionRow({row, fields, onUpdate, onDelete}: FilterConditionRowProps) {
  const {t} = useTranslation();

  const fieldDef = findFieldDef(fields, row.field);
  const operators = getFieldOperators(fieldDef);

  const handleFieldChange = useCallback((fieldName: string | null) => {
    const def = findFieldDef(fields, fieldName);
    const ops = getFieldOperators(def);
    const defaultOp = ops.length > 0 ? ops[0].value : null;
    onUpdate({...row, field: fieldName, operator: defaultOp, value: null});
  }, [fields, onUpdate, row]);

  const handleOperatorChange = useCallback((op: string | null) => {
    onUpdate({...row, operator: op});
  }, [onUpdate, row]);

  const handleValueChange = useCallback((v: unknown) => {
    onUpdate({...row, value: v});
  }, [onUpdate, row]);

  const renderValueInput = () => {
    if (!row.field) return null;

    switch (fieldDef?.type) {
      case 'text':
        if (row.operator === 'in') {
          return (
            <Select
              className="flex-1"
              mode="tags"
              value={(row.value as unknown[]) ?? []}
              onChange={(v) => handleValueChange(v)}
              placeholder={t('components.filterBuilder.valuePlaceholder')}
            />
          );
        }
        return (
          <Input
            className="flex-1"
            value={(row.value as string) ?? ''}
            onChange={(e) => handleValueChange(e.target.value)}
            placeholder={t('components.filterBuilder.valuePlaceholder')}
            allowClear
          />
        );
      case 'number':
        if (row.operator === 'in') {
          return (
            <Select
              className="flex-1"
              mode="tags"
              value={(row.value as string[]) ?? []}
              onChange={(v) => handleValueChange(v.map(Number).filter(n => !Number.isNaN(n)))}
              placeholder={t('components.filterBuilder.valuePlaceholder')}
            />
          );
        }
        return (
          <InputNumber
            className="flex-1"
            value={(row.value as number | null) ?? null}
            onChange={(v) => handleValueChange(v)}
            placeholder={t('components.filterBuilder.valuePlaceholder')}
          />
        );
      case 'select':
        if (row.operator === 'in') {
          return (
            <Select
              className="flex-1"
              mode="multiple"
              value={(row.value as (string | number)[]) ?? []}
              onChange={(v) => handleValueChange(v)}
              placeholder={t('components.filterBuilder.selectValue')}
              options={fieldDef.options ?? []}
            />
          );
        }
        return (
          <Select
            className="flex-1"
            value={(row.value as string | number | undefined) ?? undefined}
            onChange={(v) => handleValueChange(v)}
            placeholder={t('components.filterBuilder.selectValue')}
            allowClear
            options={fieldDef.options ?? []}
          />
        );
      case 'date':
        return (
          <DatePicker
            className="flex-1"
            value={row.value ? dayjs(row.value as number) : null}
            onChange={(d) => handleValueChange(d?.valueOf() ?? null)}
          />
        );
      case 'dateTime':
        return (
          <DatePicker
            className="flex-1"
            showTime
            value={row.value ? dayjs(row.value as number) : null}
            onChange={(d) => handleValueChange(d?.valueOf() ?? null)}
          />
        );
      default:
        return (
          <Input
            className="flex-1"
            value={(row.value as string) ?? ''}
            onChange={(e) => handleValueChange(e.target.value)}
            placeholder={t('components.filterBuilder.valuePlaceholder')}
            allowClear
          />
        );
    }
  };

  return (
    <div className="flex items-center gap-2">
      <Select
        className="min-w-32"
        value={row.field}
        onChange={handleFieldChange}
        placeholder={t('components.filterBuilder.selectField')}
        options={fields.map(f => ({value: f.field, label: f.label}))}
        allowClear
      />
      {row.field && (
        <Select
          className="min-w-28"
          value={row.operator}
          onChange={handleOperatorChange}
          placeholder={t('components.filterBuilder.selectOperator')}
          options={operators.map(op => ({value: op.value, label: t(op.label)}))}
        />
      )}
      {row.field && row.operator && renderValueInput()}
      <Button
        type="text"
        size="small"
        danger
        icon={<CloseOutlined />}
        onClick={onDelete}
      />
    </div>
  );
}
