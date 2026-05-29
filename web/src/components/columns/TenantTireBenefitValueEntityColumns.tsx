import React, {type JSX} from "react";
import {Tag} from "antd";
import type {EntityTableColumns} from "../table/entity-table.types.ts";
import type {TenantTireBenefitValue} from "@/types/tenant/tenant-benefit.types.ts";
import {CopyableToolTip} from "../CopyableToolTip.tsx";
import {useTranslation} from "react-i18next";

export function useTenantTireBenefitValueTableColumns(): EntityTableColumns<TenantTireBenefitValue> {
    const { t } = useTranslation();

    return [
        {
            title: t('components.columns.tenantTireBenefitValue.id'),
            dataIndex: "id",
            key: "id",
            render: function (_: unknown, row: TenantTireBenefitValue): React.ReactNode | JSX.Element {
                return <CopyableToolTip title={row.id}>
                    <Tag color="blue" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">ID: {row.id}</Tag>
                </CopyableToolTip>
            }
        },
        {
            title: t('components.columns.tenantTireBenefitValue.tireTypeId'),
            dataIndex: "tireTypeId",
            key: "tireTypeId",
            render: function (_: unknown, row: TenantTireBenefitValue): React.ReactNode | JSX.Element {
                return <span className="text-xs font-mono">{row.tireTypeId}</span>
            }
        },
        {
            title: t('components.columns.tenantTireBenefitValue.featureId'),
            dataIndex: "featureId",
            key: "featureId",
            render: function (_: unknown, row: TenantTireBenefitValue): React.ReactNode | JSX.Element {
                return <span className="text-xs font-mono">{row.featureId}</span>
            }
        },
        {
            title: t('components.columns.tenantTireBenefitValue.featureValue'),
            dataIndex: "featureValue",
            key: "featureValue",
            render: function (_: unknown, row: TenantTireBenefitValue): React.ReactNode | JSX.Element {
                return <span className="text-xs font-mono font-bold">{row.featureValue}</span>
            }
        },
    ];
}
