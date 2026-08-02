import {Button, Space} from "antd";
import {useState} from "react";
import {useTranslation} from "react-i18next";
import {EntitySelectorModal} from "./EntitySelector.tsx";
import {TenantDictTypeManagerController} from "@/api/tenant/tenant-dict-type.api.ts";
import {useTenantDictTypeTableColumns} from "@/components/columns/TenantDictTypeEntityColumns.tsx";
import {DictTypeStatus} from "@/types/tenant/tenant-dict-type.types.ts";
import type {TenantDictType} from "@/types/tenant/tenant-dict-type.types.ts";

export interface DictTypeCodeSelectorProps {
    /** Selected dict type `code` (not id). */
    value?: string | null;
    onChange?: (code: string | null) => void;
    /** Scope to filter dict types by (`ResourceScope` typeId). */
    scope: number;
    /** Scope id (tenantId for TENANT scope, 0 for SYSTEM scope). */
    scopeId: string;
    placeholder?: string;
    disabled?: boolean;
}

/**
 * Picks a dict type within a `(scope, scopeId)` and stores its stable `code`. Reuses
 * `EntitySelectorModal` (radio mode, page size capped by the modal) rather than a raw large-page
 * query, per the Entity-selection convention. Only ENABLED dict types are selectable.
 */
export function DictTypeCodeSelector({value, onChange, scope, scopeId, placeholder, disabled}: DictTypeCodeSelectorProps) {
    const {t} = useTranslation();
    const columns = useTenantDictTypeTableColumns();
    const [open, setOpen] = useState(false);

    const entityName = t('entityNames.tenantDictType');

    const handleOk = (selected: TenantDictType[]) => {
        onChange?.(selected.length > 0 ? selected[0].code : null);
        setOpen(false);
    };

    return (
        <>
            <Space>
                <Button className="h-10" onClick={() => setOpen(true)} disabled={disabled}>
                    {value
                        ? `${t('components.columns.tenantDictType.code')}: ${value}`
                        : (placeholder ?? t('components.selector.dictTypeCodeSelector.placeholder'))}
                </Button>
                {value && (
                    <Button type="link" danger onClick={() => onChange?.(null)} disabled={disabled}>
                        {t('components.selector.entityIdSelector.clear')}
                    </Button>
                )}
            </Space>

            <EntitySelectorModal
                type="radio"
                visible={open}
                title={t('components.selector.entitySelector.title', {entityName})}
                entityName={entityName}
                columns={columns}
                query={async (props) => (await TenantDictTypeManagerController.query({
                    ...props,
                    scope,
                    scopeId,
                })).data!}
                isRowDisabled={(row) => row.status !== DictTypeStatus.ENABLED}
                onCancel={() => setOpen(false)}
                onOk={handleOk}
            />
        </>
    );
}
