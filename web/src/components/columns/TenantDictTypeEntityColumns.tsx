import {Tag} from "antd";
import {useTranslation} from "react-i18next";
import {getDictTypeStatus} from "@/i18n/enum-helpers.ts";
import {DictTypeStatus} from "@/types/tenant/tenant-dict-type.types.ts";
import type {TenantDictType} from "@/types/tenant/tenant-dict-type.types.ts";
import type {EntityTableColumns} from "../table/entity-table.types.ts";
import {CopyableToolTip} from "@/components/CopyableToolTip.tsx";

export function useTenantDictTypeTableColumns(): EntityTableColumns<TenantDictType> {
    const {t} = useTranslation();

    return [
        {
            title: t('components.columns.tenantDictType.code'),
            dataIndex: "code",
            key: "code",
            render: (_: unknown, row: TenantDictType) => (
                <CopyableToolTip title={row.code}>
                    <Tag color="blue">{row.code}</Tag>
                </CopyableToolTip>
            )
        },
        {
            title: t('components.columns.tenantDictType.name'),
            dataIndex: "name",
            key: "name",
            render: (_: unknown, row: TenantDictType) => row.name
        },
        {
            title: t('components.columns.tenantDictType.remark'),
            dataIndex: "remark",
            key: "remark",
            render: (_: unknown, row: TenantDictType) => row.remark || '-'
        },
        {
            title: t('components.columns.tenantDictType.status'),
            dataIndex: "status",
            key: "status",
            render: (_: unknown, row: TenantDictType) => (
                <Tag color={row.status === DictTypeStatus.ENABLED ? 'green' : 'default'}>
                    {getDictTypeStatus(row.status)}
                </Tag>
            )
        },
    ];
}
