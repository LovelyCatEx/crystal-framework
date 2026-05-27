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

  const renderSingleValueInput = (value: unknown, onChange: (v: unknown) => void) => {
    if (fieldDef?.renderValue) {
      return fieldDef.renderValue({ value, onChange, operator: 'eq' });
    }

    switch (fieldDef?.type) {
      case 'text':
        return (
          <Input
            className="flex-1"
            value={(value as string) ?? ''}
            onChange={(e) => onChange(e.target.value)}
            placeholder={t('components.filterBuilder.valuePlaceholder')}
            allowClear
          />
        );
      case 'number':
        return (
          <InputNumber
            className="flex-1"
            value={(value as number | null) ?? null}
            onChange={(v) => onChange(v)}
            placeholder={t('components.filterBuilder.valuePlaceholder')}
          />
        );
      case 'select':
        return (
          <Select className="flex-1"
            value={(value as string | number | undefined) ?? undefined}
            onChange={(v) => onChange(v)}
            placeholder={t('components.filterBuilder.selectValue')}
            allowClear
            options={fieldDef.options ?? []}
          />
        );
      case 'date':
        return (
          <DatePicker
            className="flex-1"
            value={value ? dayjs(value as number) : null}
            onChange={(d) => onChange(d?.valueOf() ?? null)}
          />
        );
      case 'dateTime':
        return (
          <DatePicker
            className="flex-1"
            showTime
            value={value ? dayjs(value as number) : null}
            onChange={(d) => onChange(d?.valueOf() ?? null)}
          />
        );
      default:
        return (
          <Input className="flex-1"
            value={(value as string) ?? ''}
            onChange={(e) => onChange(e.target.value)}
            placeholder={t('components.filterBuilder.valuePlaceholder')}
            allowClear
          />
        );
    }
  };

  const renderValueInput = () => {
    if (!row.field) return null;

    if (row.operator === 'in') {
      const values = Array.isArray(row.value) ? (row.value as unknown[]) : [];
      return (
        <div className="flex flex-col gap-1 min-w-64">
          {values.map((v, i) => (
            <div key={i} className="flex items-center gap-1">
              {renderSingleValueInput(v, (newVal) => {
                const next = [...values];
                next[i] = newVal;
                handleValueChange(next);
              })}
              <Button type="text" size="small" danger icon={<CloseOutlined />}
                onClick={() => handleValueChange(values.filter((_, j) => j !== i))} />
            </div>
          ))}
          <Button type="dashed" size="small"
            onClick={() => handleValueChange([...values, null])}>
            {t('components.filterBuilder.addValue')}
          </Button>
        </div>
      );
    }

    if (fieldDef?.renderValue) {
      return fieldDef.renderValue({ value: row.value, onChange: handleValueChange, operator: row.operator });
    }

    return renderSingleValueInput(row.value, handleValueChange);
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
