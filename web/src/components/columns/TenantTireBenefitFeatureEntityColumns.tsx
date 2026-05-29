import React, {type JSX} from "react";
import {Space, Tag} from "antd";
import type {EntityTableColumns} from "../table/entity-table.types.ts";
import type {TenantTireBenefitFeature} from "@/types/tenant/tenant-benefit.types.ts";
import {CopyableToolTip} from "../CopyableToolTip.tsx";
import {useTranslation} from "react-i18next";

export function useTenantTireBenefitFeatureTableColumns(): EntityTableColumns<TenantTireBenefitFeature> {
    const { t } = useTranslation();

    return [
        {
            title: t('components.columns.tenantTireBenefitFeature.featureKey'),
            dataIndex: "featureKey",
            key: "featureKey",
            render: function (_: unknown, row: TenantTireBenefitFeature): React.ReactNode | JSX.Element {
                return <Space orientation='vertical' size={0}>
                    <CopyableToolTip title={row.featureKey}>
                        <span className="text-xs font-mono font-bold">{row.featureKey}</span>
                    </CopyableToolTip>
                    <CopyableToolTip title={row.id}>
                        <Tag color="blue" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">ID: {row.id}</Tag>
                    </CopyableToolTip>
                </Space>
            }
        },
        {
            title: t('components.columns.tenantTireBenefitFeature.name'),
            dataIndex: "name",
            key: "name",
            render: function (_: unknown, row: TenantTireBenefitFeature): React.ReactNode | JSX.Element {
                return <span className="text-xs font-mono font-bold">{row.name}</span>
            }
        },
        {
            title: t('components.columns.tenantTireBenefitFeature.description'),
            dataIndex: "description",
            key: "description",
            width: 300,
            render: function (_: unknown, row: TenantTireBenefitFeature): React.ReactNode | JSX.Element {
                return <span className="text-xs font-mono">{row.description ?? '-'}</span>
            }
        },
        {
            title: t('components.columns.tenantTireBenefitFeature.featureType'),
            dataIndex: "featureType",
            key: "featureType",
            width: 120,
            render: function (_: unknown, row: TenantTireBenefitFeature): React.ReactNode | JSX.Element {
                const typeMap: Record<number, string> = {
                    0: t('components.columns.tenantTireBenefitFeature.typeBoolean'),
                    1: t('components.columns.tenantTireBenefitFeature.typeLimit'),
                    2: t('components.columns.tenantTireBenefitFeature.typeEnum'),
                };
                return <Tag>{typeMap[row.featureType] ?? row.featureType}</Tag>
            }
        },
        {
            title: t('components.columns.tenantTireBenefitFeature.defaultValue'),
            dataIndex: "defaultValue",
            key: "defaultValue",
            width: 120,
            render: function (_: unknown, row: TenantTireBenefitFeature): React.ReactNode | JSX.Element {
                return <span className="text-xs font-mono">{row.defaultValue ?? '-'}</span>
            }
        },
    ];
}
