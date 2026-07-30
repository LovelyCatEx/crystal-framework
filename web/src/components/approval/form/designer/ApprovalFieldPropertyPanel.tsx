import {Button, Form, Input, InputNumber, Select, Space, Switch, Typography} from "antd";
import {DeleteOutlined, PlusOutlined} from "@ant-design/icons";
import {useTranslation} from "react-i18next";
import {useMemo} from "react";
import {ApprovalFieldType} from "@/types/approval/approval-enums.ts";
import type {
    ApprovalFieldOption,
    ApprovalFieldSchema,
    ApprovalFieldValidation,
    ApprovalFormSchema,
} from "@/types/approval/approval-form-schema.types.ts";
import {APPROVAL_FIELD_KEY_PATTERN} from "@/types/approval/approval-form-schema.types.ts";
import {getApprovalFieldType} from "@/i18n/enum-helpers.ts";

export interface ApprovalFieldPropertyPanelProps {
    field: ApprovalFieldSchema;
    /** All fields in the schema — needed to detect key collisions. */
    allFields: ApprovalFieldSchema[];
    groups: ApprovalFormSchema['groups'];
    onChange: (updated: ApprovalFieldSchema) => void;
}

/** Field type -> which validation subfields make sense to show. Keeps the panel type-aware. */
const VALIDATION_SLOTS: Record<ApprovalFieldType, Array<keyof ApprovalFieldValidation>> = {
    [ApprovalFieldType.TEXT]: ['maxLength', 'pattern'],
    [ApprovalFieldType.TEXTAREA]: ['maxLength'],
    [ApprovalFieldType.NUMBER]: ['min', 'max', 'precision'],
    [ApprovalFieldType.BOOLEAN]: [],
    [ApprovalFieldType.SELECT]: ['multiple'],
    [ApprovalFieldType.RADIO]: [],
    [ApprovalFieldType.CHECKBOX]: ['minCount', 'maxCount'],
    [ApprovalFieldType.DATE]: ['minDate', 'maxDate'],
    [ApprovalFieldType.DATETIME]: ['minDate', 'maxDate'],
};

const OPTION_TYPES: readonly ApprovalFieldType[] = [
    ApprovalFieldType.SELECT,
    ApprovalFieldType.RADIO,
    ApprovalFieldType.CHECKBOX,
];

function usesOptions(type: ApprovalFieldType): boolean {
    return OPTION_TYPES.includes(type);
}

export function ApprovalFieldPropertyPanel(props: ApprovalFieldPropertyPanelProps) {
    const {field, allFields, groups, onChange} = props;
    const {t} = useTranslation();

    const patch = (partial: Partial<ApprovalFieldSchema>) => onChange({...field, ...partial});
    const patchValidation = (partial: Partial<ApprovalFieldValidation>) =>
        onChange({...field, validation: {...field.validation, ...partial}});

    // --- key validation ---
    const keyDuplicated = useMemo(
        () => allFields.some(f => f !== field && f.key === field.key),
        [allFields, field],
    );
    const keyEmpty = !field.key.trim();
    const keyPatternInvalid = !!field.key && !APPROVAL_FIELD_KEY_PATTERN.test(field.key);
    let keyError: string | null = null;
    if (keyEmpty) keyError = t('components.approvalFormDesigner.property.error.keyEmpty');
    else if (keyPatternInvalid) keyError = t('components.approvalFormDesigner.property.error.keyPattern');
    else if (keyDuplicated) keyError = t('components.approvalFormDesigner.property.error.keyDuplicated');

    const typeOptions = Object.values(ApprovalFieldType).map(v => ({
        value: v,
        label: getApprovalFieldType(v),
    }));

    const groupOptions = [
        {value: '', label: t('components.approvalFormDesigner.property.noGroup')},
        ...(groups ?? []).map(g => ({value: g.key, label: g.label})),
    ];

    const validationSlots = VALIDATION_SLOTS[field.type];

    return (
        <div className="flex flex-col gap-3">
            <Typography.Title level={5} className="!mb-0">
                {t('components.approvalFormDesigner.property.title')}
            </Typography.Title>

            <Form layout="vertical" size="small">
                <Form.Item
                    label={t('components.approvalFormDesigner.property.key')}
                    validateStatus={keyError ? 'error' : ''}
                    help={keyError ?? t('components.approvalFormDesigner.property.keyHint')}
                    required
                >
                    <Input
                        value={field.key}
                        onChange={(e) => patch({key: e.target.value})}
                        placeholder="camelCaseKey"
                    />
                </Form.Item>

                <Form.Item label={t('components.approvalFormDesigner.property.label')} required>
                    <Input value={field.label} onChange={(e) => patch({label: e.target.value})}/>
                </Form.Item>

                <Form.Item label={t('components.approvalFormDesigner.property.type')} required>
                    <Select
                        value={field.type}
                        options={typeOptions}
                        onChange={(v) => {
                            const nextType = v as ApprovalFieldType;
                            const nextOptions = usesOptions(nextType) ? (field.options ?? []) : null;
                            onChange({
                                ...field,
                                type: nextType,
                                options: nextOptions,
                                validation: {},
                                defaultValue: null,
                            });
                        }}
                    />
                </Form.Item>

                <Form.Item label={t('components.approvalFormDesigner.property.description')}>
                    <Input.TextArea
                        rows={2}
                        value={field.description ?? ''}
                        onChange={(e) => patch({description: e.target.value || null})}
                    />
                </Form.Item>

                <Form.Item label={t('components.approvalFormDesigner.property.placeholder')}>
                    <Input
                        value={field.placeholder ?? ''}
                        onChange={(e) => patch({placeholder: e.target.value || null})}
                    />
                </Form.Item>

                <Space size="middle" className="mb-3">
                    <Form.Item
                        label={t('components.approvalFormDesigner.property.required')}
                        className="!mb-0"
                    >
                        <Switch checked={field.required} onChange={(v) => patch({required: v})}/>
                    </Form.Item>
                    <Form.Item
                        label={t('components.approvalFormDesigner.property.visible')}
                        className="!mb-0"
                    >
                        <Switch checked={field.visible} onChange={(v) => patch({visible: v})}/>
                    </Form.Item>
                    <Form.Item
                        label={t('components.approvalFormDesigner.property.readonly')}
                        className="!mb-0"
                    >
                        <Switch checked={field.readonly} onChange={(v) => patch({readonly: v})}/>
                    </Form.Item>
                </Space>

                <Form.Item label={t('components.approvalFormDesigner.property.groupKey')}>
                    <Select
                        value={field.groupKey ?? ''}
                        options={groupOptions}
                        onChange={(v) => patch({groupKey: v || null})}
                    />
                </Form.Item>

                {validationSlots.length > 0 && (
                    <>
                        <Typography.Text strong className="!block !mb-2">
                            {t('components.approvalFormDesigner.property.validationTitle')}
                        </Typography.Text>
                        {validationSlots.includes('maxLength') && (
                            <Form.Item label="maxLength">
                                <InputNumber
                                    className="w-full"
                                    min={0}
                                    value={field.validation?.maxLength}
                                    onChange={(v) => patchValidation({maxLength: v ?? undefined})}
                                />
                            </Form.Item>
                        )}
                        {validationSlots.includes('pattern') && (
                            <Form.Item
                                label="pattern"
                                help={t('components.approvalFormDesigner.property.patternHint')}
                            >
                                <Input
                                    value={field.validation?.pattern ?? ''}
                                    onChange={(e) => patchValidation({pattern: e.target.value || undefined})}
                                />
                            </Form.Item>
                        )}
                        {validationSlots.includes('min') && (
                            <Form.Item label="min">
                                <InputNumber
                                    className="w-full"
                                    value={field.validation?.min}
                                    onChange={(v) => patchValidation({min: v ?? undefined})}
                                />
                            </Form.Item>
                        )}
                        {validationSlots.includes('max') && (
                            <Form.Item label="max">
                                <InputNumber
                                    className="w-full"
                                    value={field.validation?.max}
                                    onChange={(v) => patchValidation({max: v ?? undefined})}
                                />
                            </Form.Item>
                        )}
                        {validationSlots.includes('precision') && (
                            <Form.Item label="precision">
                                <InputNumber
                                    className="w-full"
                                    min={0}
                                    value={field.validation?.precision}
                                    onChange={(v) => patchValidation({precision: v ?? undefined})}
                                />
                            </Form.Item>
                        )}
                        {validationSlots.includes('minCount') && (
                            <Form.Item label="minCount">
                                <InputNumber
                                    className="w-full"
                                    min={0}
                                    value={field.validation?.minCount}
                                    onChange={(v) => patchValidation({minCount: v ?? undefined})}
                                />
                            </Form.Item>
                        )}
                        {validationSlots.includes('maxCount') && (
                            <Form.Item label="maxCount">
                                <InputNumber
                                    className="w-full"
                                    min={0}
                                    value={field.validation?.maxCount}
                                    onChange={(v) => patchValidation({maxCount: v ?? undefined})}
                                />
                            </Form.Item>
                        )}
                        {validationSlots.includes('minDate') && (
                            <Form.Item label="minDate (ISO)">
                                <Input
                                    value={field.validation?.minDate ?? ''}
                                    onChange={(e) => patchValidation({minDate: e.target.value || undefined})}
                                />
                            </Form.Item>
                        )}
                        {validationSlots.includes('maxDate') && (
                            <Form.Item label="maxDate (ISO)">
                                <Input
                                    value={field.validation?.maxDate ?? ''}
                                    onChange={(e) => patchValidation({maxDate: e.target.value || undefined})}
                                />
                            </Form.Item>
                        )}
                        {validationSlots.includes('multiple') && (
                            <Form.Item
                                label={t('components.approvalFormDesigner.property.multiple')}
                            >
                                <Switch
                                    checked={field.validation?.multiple ?? false}
                                    onChange={(v) => patchValidation({multiple: v})}
                                />
                            </Form.Item>
                        )}
                    </>
                )}

                {usesOptions(field.type) && (
                    <OptionsEditor
                        options={field.options ?? []}
                        onChange={(next) => patch({options: next})}
                        addLabel={t('components.approvalFormDesigner.property.addOption')}
                        deleteLabel={t('components.approvalFormDesigner.property.deleteOption')}
                        title={t('components.approvalFormDesigner.property.options')}
                    />
                )}
            </Form>
        </div>
    );
}

interface OptionsEditorProps {
    options: ApprovalFieldOption[];
    onChange: (options: ApprovalFieldOption[]) => void;
    title: string;
    addLabel: string;
    deleteLabel: string;
}

function OptionsEditor({options, onChange, title, addLabel, deleteLabel}: OptionsEditorProps) {
    const addOption = () => onChange([...options, {value: '', label: ''}]);
    const removeAt = (index: number) => onChange(options.filter((_, i) => i !== index));
    const patchAt = (index: number, partial: Partial<ApprovalFieldOption>) =>
        onChange(options.map((opt, i) => i === index ? {...opt, ...partial} : opt));

    return (
        <div className="flex flex-col gap-2">
            <div className="flex items-center justify-between">
                <Typography.Text strong>{title}</Typography.Text>
                <Button type="dashed" size="small" icon={<PlusOutlined/>} onClick={addOption}>
                    {addLabel}
                </Button>
            </div>
            {options.map((opt, i) => (
                <div key={i} className="flex items-center gap-1">
                    <Input
                        size="small"
                        placeholder="label"
                        value={opt.label}
                        onChange={(e) => patchAt(i, {label: e.target.value})}
                    />
                    <Input
                        size="small"
                        placeholder="value"
                        value={opt.value}
                        onChange={(e) => patchAt(i, {value: e.target.value})}
                    />
                    <Button
                        type="text"
                        size="small"
                        danger
                        icon={<DeleteOutlined/>}
                        onClick={() => removeAt(i)}
                        aria-label={deleteLabel}
                    />
                </div>
            ))}
        </div>
    );
}
