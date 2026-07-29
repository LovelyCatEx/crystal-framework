import {CheckCircleOutlined, CloseCircleOutlined} from "@ant-design/icons";
import {Button, Input, InputNumber, Select, Space} from "antd";
import {TenantBenefitType} from "@/types/tenant/tenant-benefit.types.ts";
import {useTranslation} from "react-i18next";

export interface TenantTireBenefitValueCellEditorProps {
    featureType: number;
    /** Pre-computed display value — for booleans pass the raw "true"/"false" string */
    value: string | null;
    /** Feature default value (for enum options in editor) */
    defaultValue: string | null;
    editable: boolean;
    editing: boolean;
    editValue: string;
    onStartEdit: () => void;
    onEditValueChange: (value: string) => void;
    onSave: () => void;
    onCancel: () => void;
}

export function TenantTireBenefitValueCellEditor(props: TenantTireBenefitValueCellEditorProps) {
    const {t} = useTranslation();

    if (!props.editing) {
        // === Non-editing display ===
        const displayNode = props.featureType === TenantBenefitType.BOOLEAN
            ? props.value === 'true'
                ? <CheckCircleOutlined className="text-green-500 text-base" />
                : <CloseCircleOutlined className="text-red-500 text-base" />
            : <span>{props.value ?? '-'}</span>;

        if (props.editable) {
            return (
                <span
                    className="cursor-pointer hover:text-blue-500 transition-colors"
                    onClick={props.onStartEdit}
                >
                    {displayNode}
                </span>
            );
        }
        return <>{displayNode}</>;
    }

    // === Editing mode ===
    const enumOptions = (props.defaultValue || '')
        .split(',')
        .filter(Boolean)
        .map((opt) => ({value: opt, label: opt}));
    const booleanTrueLabel = t('pages.tenantTireBenefitValueManager.modal.featureValue.booleanTrue');
    const booleanFalseLabel = t('pages.tenantTireBenefitValueManager.modal.featureValue.booleanFalse');

    switch (props.featureType) {
        case TenantBenefitType.BOOLEAN:
            return (
                <Space>
                    <Select
                        className="w-32"
                        value={props.editValue}
                        onChange={props.onEditValueChange}
                        options={[
                            {value: 'true', label: booleanTrueLabel},
                            {value: 'false', label: booleanFalseLabel},
                        ]}
                    />
                    <Button type="primary" size="small" onClick={props.onSave}>{t('pages.tenantTireBenefitValueManager.overview.save')}</Button>
                    <Button size="small" onClick={props.onCancel}>{t('pages.tenantTireBenefitValueManager.overview.cancel')}</Button>
                </Space>
            );
        case TenantBenefitType.LIMIT:
            return (
                <Space>
                    <InputNumber className="w-32" min={0} value={Number(props.editValue)} onChange={(v) => props.onEditValueChange(String(v ?? '0'))} />
                    <Button type="primary" size="small" onClick={props.onSave}>{t('pages.tenantTireBenefitValueManager.overview.save')}</Button>
                    <Button size="small" onClick={props.onCancel}>{t('pages.tenantTireBenefitValueManager.overview.cancel')}</Button>
                </Space>
            );
        case TenantBenefitType.ENUM:
            return (
                <Space>
                    <Select className="w-32" value={props.editValue} onChange={props.onEditValueChange} options={enumOptions} />
                    <Button type="primary" size="small" onClick={props.onSave}>{t('pages.tenantTireBenefitValueManager.overview.save')}</Button>
                    <Button size="small" onClick={props.onCancel}>{t('pages.tenantTireBenefitValueManager.overview.cancel')}</Button>
                </Space>
            );
        default:
            return (
                <Space>
                    <Input className="w-32" value={props.editValue} onChange={(e) => props.onEditValueChange(e.target.value)} />
                    <Button type="primary" size="small" onClick={props.onSave}>{t('pages.tenantTireBenefitValueManager.overview.save')}</Button>
                    <Button size="small" onClick={props.onCancel}>{t('pages.tenantTireBenefitValueManager.overview.cancel')}</Button>
                </Space>
            );
    }
}
