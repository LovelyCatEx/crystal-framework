import React, {type JSX, useEffect, useState} from "react";
import {Space, Spin, Tag} from "antd";
import type {EntityTableColumns} from "../table/entity-table.types.ts";
import type {TenantTireBenefitValue} from "@/types/tenant/tenant-benefit.types.ts";
import {TenantTireBenefitFeatureManagerController} from "@/api/tenant/tenant-benefit.api.ts";
import {TenantTireTypeManagerController} from "@/api/tenant/tenant-tire-type.api.ts";
import {getTenantBenefitType} from "@/i18n/enum-helpers.ts";
import {CopyableToolTip} from "../CopyableToolTip.tsx";
import {useTranslation} from "react-i18next";
import type {TenantTireBenefitFeature} from "@/types/tenant/tenant-benefit.types.ts";

// ── TireType resolver ──

const TireTypeNameDisplay: React.FC<{ tireTypeId: string }> = ({ tireTypeId }) => {
    const [name, setName] = useState<string | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        setLoading(true);
        TenantTireTypeManagerController.getById(tireTypeId)
            .then((data) => setName(data?.name ?? null))
            .catch(() => setName(null))
            .finally(() => setLoading(false));
    }, [tireTypeId]);

    if (loading) return <Spin size="small" />;

    return (
        <Tag color="blue">{name ?? 'Unknown'}</Tag>
    );
};

// ── Feature resolver ──

const FeatureInfoDisplay: React.FC<{ featureId: string }> = ({ featureId }) => {
    const [feature, setFeature] = useState<TenantTireBenefitFeature | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        setLoading(true);
        TenantTireBenefitFeatureManagerController.getById(featureId)
            .then((data) => setFeature(data ?? null))
            .catch(() => setFeature(null))
            .finally(() => setLoading(false));
    }, [featureId]);

    if (loading) return <Spin size="small" />;

    if (!feature) return <Tag color="red">Unknown</Tag>;

    return (
        <Space size={4}>
            <Tag color="purple">{feature.name}</Tag>
            <Tag color="cyan">{getTenantBenefitType(feature.featureType)}</Tag>
        </Space>
    );
};

// ── Columns ──

export function useTenantTireBenefitValueTableColumns(): EntityTableColumns<TenantTireBenefitValue> {
    const { t } = useTranslation();

    return [
        {
            title: t('components.columns.tenantTireBenefitValue.recordInfo'),
            dataIndex: "id",
            key: "id",
            render: function (_: unknown, row: TenantTireBenefitValue): React.ReactNode | JSX.Element {
                return <Space orientation='vertical' size={0}>
                    <CopyableToolTip title={row.id}>
                        <Tag color="blue" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">ID: {row.id}</Tag>
                    </CopyableToolTip>
                    <CopyableToolTip title={row.tireTypeId}>
                        <Tag color="orange" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">{t('components.columns.tenantTireBenefitValue.tireType')}: {row.tireTypeId}</Tag>
                    </CopyableToolTip>
                    <CopyableToolTip title={row.featureId}>
                        <Tag color="green" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">{t('components.columns.tenantTireBenefitValue.feature')}: {row.featureId}</Tag>
                    </CopyableToolTip>
                </Space>
            }
        },
        {
            title: t('components.columns.tenantTireBenefitValue.tireType'),
            dataIndex: "tireTypeId",
            key: "tireTypeId",
            render: function (_: unknown, row: TenantTireBenefitValue): React.ReactNode | JSX.Element {
                return <TireTypeNameDisplay tireTypeId={row.tireTypeId} />;
            }
        },
        {
            title: t('components.columns.tenantTireBenefitValue.feature'),
            dataIndex: "featureId",
            key: "featureId",
            render: function (_: unknown, row: TenantTireBenefitValue): React.ReactNode | JSX.Element {
                return <FeatureInfoDisplay featureId={row.featureId} />;
            }
        },
        {
            title: t('components.columns.tenantTireBenefitValue.featureValue'),
            dataIndex: "featureValue",
            key: "featureValue",
            render: function (_: unknown, row: TenantTireBenefitValue): React.ReactNode | JSX.Element {
                return <Tag color="green">{row.featureValue}</Tag>;
            }
        },
    ];
}
