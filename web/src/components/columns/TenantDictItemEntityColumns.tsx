import {Tag} from "antd";
import {useTranslation} from "react-i18next";
import {getDictItemStatus} from "@/i18n/enum-helpers.ts";
import {DictItemStatus} from "@/types/tenant/tenant-dict-item.types.ts";
import type {TenantDictItem} from "@/types/tenant/tenant-dict-item.types.ts";
import type {EntityTableColumns} from "../table/entity-table.types.ts";
import {CopyableToolTip} from "@/components/CopyableToolTip.tsx";

export function useTenantDictItemTableColumns(): EntityTableColumns<TenantDictItem> {
    const {t} = useTranslation();

    return [
        {
            title: t('components.columns.tenantDictItem.itemCode'),
            dataIndex: "itemCode",
            key: "itemCode",
            render: (_: unknown, row: TenantDictItem) => (
                <CopyableToolTip title={row.itemCode}>
                    <Tag color="blue">{row.itemCode}</Tag>
                </CopyableToolTip>
            )
        },
        {
            title: t('components.columns.tenantDictItem.itemValue'),
            dataIndex: "itemValue",
            key: "itemValue",
            render: (_: unknown, row: TenantDictItem) => row.itemValue
        },
        {
            title: t('components.columns.tenantDictItem.sortOrder'),
            dataIndex: "sortOrder",
            key: "sortOrder",
            render: (_: unknown, row: TenantDictItem) => row.sortOrder
        },
        {
            title: t('components.columns.tenantDictItem.isDefault'),
            dataIndex: "isDefault",
            key: "isDefault",
            render: (_: unknown, row: TenantDictItem) => (
                row.isDefault
                    ? <Tag color="green">{t('components.columns.tenantDictItem.yes')}</Tag>
                    : <Tag>{t('components.columns.tenantDictItem.no')}</Tag>
            )
        },
        {
            title: t('components.columns.tenantDictItem.status'),
            dataIndex: "status",
            key: "status",
            render: (_: unknown, row: TenantDictItem) => (
                <Tag color={row.status === DictItemStatus.ENABLED ? 'green' : 'default'}>
                    {getDictItemStatus(row.status)}
                </Tag>
            )
        },
    ];
}
