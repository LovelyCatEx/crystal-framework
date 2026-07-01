import React, {type JSX, useEffect, useState} from "react";
import {Space, Spin, Tag} from "antd";
import type {EntityTableColumns} from "../table/entity-table.types.ts";
import type {TenantTireBenefitFeature, TenantTireBenefitValue} from "@/types/tenant/tenant-benefit.types.ts";
import {TenantTireBenefitFeatureManagerController} from "@/api/tenant/tenant-benefit.api.ts";
import {TenantTireTypeManagerController} from "@/api/tenant/tenant-tire-type.api.ts";
import {getTenantBenefitType} from "@/i18n/enum-helpers.ts";
import {CopyableToolTip} from "../CopyableToolTip.tsx";
import {useTranslation} from "react-i18next";
import {useTenantBenefitKeyToTranslationMap} from "@/i18n/tenant-benefit.tsx";

export interface TenantTireBenefitValueColumnsOptions {
    useI18n?: boolean;
}

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

// ── Feature resolver: returns the full feature so columns can render name/desc/type independently ──

function useFeatureById(featureId: string): { feature: TenantTireBenefitFeature | null; loading: boolean } {
    const [feature, setFeature] = useState<TenantTireBenefitFeature | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        setLoading(true);
        TenantTireBenefitFeatureManagerController.getById(featureId)
            .then((data) => setFeature(data ?? null))
            .catch(() => setFeature(null))
            .finally(() => setLoading(false));
    }, [featureId]);

    return { feature, loading };
}

const FeatureNameDescDisplay: React.FC<{ featureId: string; useI18n: boolean }> = ({ featureId, useI18n }) => {
    const { feature, loading } = useFeatureById(featureId);
    const benefitKeyMap = useTenantBenefitKeyToTranslationMap();

    if (loading) return <Spin size="small" />;
    if (!feature) return <span className="text-red-500">Unknown</span>;

    const translation = useI18n ? benefitKeyMap.get(feature.featureKey) : undefined;
    const displayName = translation?.name ?? feature.name;
    const displayDesc = translation?.description ?? feature.description;

    return (
        <div className="inline-block align-top">
            <div>{displayName}</div>
            {displayDesc && (
                <div className="text-gray-400 text-xs leading-tight mt-0.5">
                    {displayDesc.length > 128
                        ? `${displayDesc.slice(0, 128)}...`
                        : displayDesc}
                </div>
            )}
        </div>
    );
};

const FeatureTypeDisplay: React.FC<{ featureId: string }> = ({ featureId }) => {
    const { feature, loading } = useFeatureById(featureId);
    if (loading) return <Spin size="small" />;
    if (!feature) return <span className="text-red-500">-</span>;
    return <Tag>{getTenantBenefitType(feature.featureType)}</Tag>;
};

// ── Columns ──

export function useTenantTireBenefitValueTableColumns(
    options: TenantTireBenefitValueColumnsOptions = {}
): EntityTableColumns<TenantTireBenefitValue> {
    const { t } = useTranslation();
    const { useI18n = false } = options;

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
                return <FeatureNameDescDisplay featureId={row.featureId} useI18n={useI18n} />;
            }
        },
        {
            title: t('components.columns.tenantTireBenefitValue.featureType'),
            dataIndex: "featureType",
            key: "featureType",
            width: 120,
            render: function (_: unknown, row: TenantTireBenefitValue): React.ReactNode | JSX.Element {
                return <FeatureTypeDisplay featureId={row.featureId} />;
            }
        },
        {
            title: t('components.columns.tenantTireBenefitValue.featureValue'),
            dataIndex: "featureValue",
            key: "featureValue",
            render: function (_: unknown, row: TenantTireBenefitValue): React.ReactNode | JSX.Element {
                return <span>{row.featureValue}</span>;
            }
        },
    ];
}
