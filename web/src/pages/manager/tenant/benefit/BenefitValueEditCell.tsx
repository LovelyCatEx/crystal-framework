import {createContext, useContext, useState, type ReactNode} from "react";
import {message} from "antd";
import {useTranslation} from "react-i18next";
import {TenantTireBenefitValueManagerController} from "@/api/tenant/tenant-benefit.api.ts";
import {BenefitValueCellEditor} from "./BenefitValueCellEditor.tsx";

// ── Context ──────────────────────────────────────────────

interface EditContextValue {
    editingKey: string | null;
    editValue: string;
    startEdit: (key: string, initialValue: string) => void;
    setEditValue: (value: string) => void;
    cancelEdit: () => void;
}

const EditContext = createContext<EditContextValue>({
    editingKey: null,
    editValue: '',
    startEdit: () => {},
    setEditValue: () => {},
    cancelEdit: () => {},
});

export function BenefitValueEditProvider({children}: { children: ReactNode }) {
    const [editingKey, setEditingKey] = useState<string | null>(null);
    const [editValue, setEditValue] = useState('');

    const startEdit = (key: string, initialValue: string) => {
        setEditingKey(key);
        setEditValue(initialValue);
    };

    const cancelEdit = () => {
        setEditingKey(null);
        setEditValue('');
    };

    return (
        <EditContext.Provider value={{editingKey, editValue, startEdit, setEditValue, cancelEdit}}>
            {children}
        </EditContext.Provider>
    );
}

// ── Cell Component ───────────────────────────────────────

export interface BenefitValueEditCellProps {
    cellKey: string;
    featureType: number;
    /** Display value (caller should pass the effective value: customized || default) */
    value: string | null;
    /** Feature default (for enum options in editor) */
    defaultValue: string | null;
    valueId: string | null;
    createPayload: { tireTypeId: string; featureId: string };
    editable?: boolean;
    onSaved: () => void;
}

export function BenefitValueEditCell(props: BenefitValueEditCellProps) {
    const ctx = useContext(EditContext);
    const {t} = useTranslation();
    const isEditing = ctx.editingKey === props.cellKey;

    const handleSave = async () => {
        try {
            if (props.valueId) {
                await TenantTireBenefitValueManagerController.update({
                    id: props.valueId,
                    featureValue: ctx.editValue,
                } as never);
            } else {
                await TenantTireBenefitValueManagerController.create({
                    ...props.createPayload,
                    featureValue: ctx.editValue,
                });
            }
            message.success(t('components.managerPageContainer.updateSuccess', {entityName: t('entityNames.tenantTireBenefitValue')}));
            ctx.cancelEdit();
            props.onSaved();
        } catch {
            message.error(t('components.managerPageContainer.updateFailed', {entityName: t('entityNames.tenantTireBenefitValue')}));
        }
    };

    return (
        <BenefitValueCellEditor
            featureType={props.featureType}
            value={props.value}
            defaultValue={props.defaultValue}
            editable={props.editable ?? true}
            editing={isEditing}
            editValue={ctx.editValue}
            onStartEdit={() => ctx.startEdit(props.cellKey, props.value ?? props.defaultValue ?? '')}
            onEditValueChange={ctx.setEditValue}
            onSave={handleSave}
            onCancel={ctx.cancelEdit}
        />
    );
}
