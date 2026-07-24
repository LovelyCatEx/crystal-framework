import {CheckCircleOutlined, CloseCircleOutlined} from "@ant-design/icons";
import {Space, Switch, Table, Tag, Typography} from "antd";
import {useEffect, useMemo, useState} from "react";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {StandardCard} from "@/components/card/StandardCard.tsx";
import {TenantTireTypeManagerController} from "@/api/tenant/tenant-tire-type.api.ts";
import {queryBenefitOverview} from "@/api/tenant/tenant-benefit.api.ts";
import type {TenantTireBenefitOverviewGroupVO, TenantTireBenefitOverviewItemVO} from "@/types/tenant/tenant-benefit.types.ts";
import {TenantBenefitType} from "@/types/tenant/tenant-benefit.types.ts";
import {useTranslation} from "react-i18next";
import {TenantTireBenefitValueEditCell, TenantTireBenefitValueEditProvider} from "./TenantTireBenefitValueEditCell.tsx";
import {
    getBenefitGroupKey,
    useTenantBenefitGroupToTranslationMap,
    useTenantBenefitKeyToTranslationMap,
} from "@/i18n/tenant-benefit.tsx";

interface MatrixRow {
    featureId: string;
    featureKey: string;
    name: string;
    description: string | null;
    featureType: number;
    defaultValue: string | null;
    tireValues: Record<string, string>;
    tireValueIds: Record<string, string | null>;
    tireCustomized: Record<string, boolean>;
}

interface GroupRow {
    rowKey: string;
    groupKey: string;
    groupLabel: string;
    groupIcon?: React.ReactNode;
    childrenCount: number;
    isGroup: true;
    children: TableRow[];
}

type FeatureRow = MatrixRow & {
    rowKey: string;
    isGroup: false;
};

type TableRow = GroupRow | FeatureRow;

export default function CrossTireOverviewPage() {
    const {t} = useTranslation();
    const benefitKeyMap = useTenantBenefitKeyToTranslationMap();
    const benefitGroupMap = useTenantBenefitGroupToTranslationMap();
    const [tireTypes, setTireTypes] = useState<{ id: string; name: string }[]>([]);
    const [groupData, setGroupData] = useState<TenantTireBenefitOverviewGroupVO[]>([]);
    const [loading, setLoading] = useState(false);
    const [readOnly, setReadOnly] = useState(true);
    const [showDefaultValue, setShowDefaultValue] = useState(true);

    // 1. Load all tire types
    useEffect(() => {
        TenantTireTypeManagerController.list().then((res) => {
            setTireTypes(res.data || []);
        });
    }, []);

    // 2. Load overview data for ALL tire types in a single call (no pagination)
    useEffect(() => {
        if (tireTypes.length === 0) return;
        setLoading(true);
        queryBenefitOverview({tireTypeIds: tireTypes.map((t) => t.id)})
            .then((res) => {
                setGroupData(res.data ?? []);
            })
            .finally(() => setLoading(false));
    }, [tireTypes]);

    // 3. Build matrix: featureId → row with per-tire values, with i18n translations
    const matrixData: MatrixRow[] = useMemo(() => {
        const featureMap = new Map<string, MatrixRow>();

        groupData.forEach((group) => {
            group.items.forEach((item: TenantTireBenefitOverviewItemVO) => {
                if (!featureMap.has(item.featureId)) {
                    const translation = benefitKeyMap.get(item.featureKey);
                    featureMap.set(item.featureId, {
                        featureId: item.featureId,
                        featureKey: item.featureKey,
                        name: translation?.name ?? item.name,
                        description: translation?.description ?? item.description,
                        featureType: item.featureType,
                        defaultValue: item.defaultValue,
                        tireValues: {},
                        tireValueIds: {},
                        tireCustomized: {},
                    });
                }
                const row = featureMap.get(item.featureId)!;
                const effectiveValue = item.isCustomized
                    ? (item.value ?? item.defaultValue ?? '-')
                    : (item.defaultValue ?? '-');
                row.tireValues[group.tireTypeId] = effectiveValue;
                row.tireValueIds[group.tireTypeId] = item.valueId;
                row.tireCustomized[group.tireTypeId] = item.isCustomized;
            });
        });

        return Array.from(featureMap.values());
    }, [groupData, benefitKeyMap]);

    // 4. Group flat matrix rows by featureKey first segment for two-level table display
    const groupedTableData: TableRow[] = useMemo(() => {
        const buckets = new Map<string, FeatureRow[]>();
        matrixData.forEach((row) => {
            const groupKey = getBenefitGroupKey(row.featureKey);
            const featureRow: FeatureRow = {...row, rowKey: `feature_${row.featureId}`, isGroup: false};
            if (!buckets.has(groupKey)) buckets.set(groupKey, []);
            buckets.get(groupKey)!.push(featureRow);
        });

        const result: TableRow[] = [];
        buckets.forEach((children, groupKey) => {
            const translation = benefitGroupMap.get(groupKey);
            result.push({
                rowKey: `group_${groupKey}`,
                groupKey,
                groupLabel: translation?.label ?? groupKey,
                groupIcon: translation?.icon,
                childrenCount: children.length,
                isGroup: true,
                children,
            });
        });
        return result;
    }, [matrixData, benefitGroupMap]);

    const refreshData = async () => {
        if (tireTypes.length === 0) return;
        const res = await queryBenefitOverview({tireTypeIds: tireTypes.map((t) => t.id)});
        setGroupData(res.data ?? []);
    };

    // 4. Build table columns (handles both group parent rows and feature child rows)
    const columns = [
        {
            title: t('pages.tenantTireBenefitValueManager.crossOverview.name'),
            dataIndex: 'name',
            key: 'name',
            fixed: 'left' as const,
            width: 240,
            render: (_: unknown, row: TableRow) => {
                if (row.isGroup) {
                    return (
                        <Space>
                            {row.groupIcon}
                            <span className="font-medium">{row.groupLabel}</span>
                            <Tag color="blue">{row.childrenCount}</Tag>
                        </Space>
                    );
                }
                return (
                    <div className="inline-block align-top">
                        <div>{row.name}</div>
                        {row.description && (
                            <div className="text-gray-400 text-xs leading-tight mt-0.5">
                                {row.description.length > 128
                                    ? `${row.description.slice(0, 128)}...`
                                    : row.description}
                            </div>
                        )}
                    </div>
                );
            },
        },
        ...(readOnly ? [] : [
            {
                title: t('pages.tenantTireBenefitValueManager.crossOverview.featureKey'),
                dataIndex: 'featureKey',
                key: 'featureKey',
                width: 260,
                render: (_: unknown, row: TableRow) => {
                    if (row.isGroup) return null;
                    return <Typography.Text copyable>{row.featureKey}</Typography.Text>;
                },
            },
        ]),
        ...(showDefaultValue ? [
            {
                title: t('pages.tenantTireBenefitValueManager.overview.defaultValue'),
                key: 'defaultValue',
                width: 100,
                render: (_: unknown, row: TableRow) => {
                    if (row.isGroup) return null;
                    if (row.defaultValue === null) return <span>-</span>;
                    if (row.featureType === TenantBenefitType.BOOLEAN) {
                        return row.defaultValue === 'true'
                            ? <CheckCircleOutlined className="text-green-500 text-base" />
                            : <CloseCircleOutlined className="text-red-500 text-base" />;
                    }
                    return <span>{row.defaultValue}</span>;
                },
            },
        ] : []),
        ...tireTypes.map((tire) => ({
            title: tire.name,
            key: tire.id,
            width: readOnly ? 120 : 240,
            render: (_: unknown, row: TableRow) => {
                if (row.isGroup) return null;
                const value = row.tireValues[tire.id];
                if (value === undefined) return <span>-</span>;
                return (
                    <TenantTireBenefitValueEditCell
                        cellKey={`cross_${tire.id}_${row.featureId}`}
                        featureType={row.featureType}
                        value={value}
                        defaultValue={row.defaultValue}
                        valueId={row.tireValueIds[tire.id]}
                        createPayload={{ tireTypeId: tire.id, featureId: row.featureId }}
                        editable={!readOnly}
                        onSaved={refreshData}
                    />
                );
            },
        })),
    ];

    return (
        <>
            <ActionBarComponent
                title={t('pages.tenantTireBenefitValueManager.crossOverview.title')}
                subtitle={t('pages.tenantTireBenefitValueManager.crossOverview.subtitle')}
            />
            <StandardCard className="mt-4">
                <TenantTireBenefitValueEditProvider>
                <div className="flex justify-between mb-4">
                    <Space>
                        <Switch
                            checked={showDefaultValue}
                            onChange={setShowDefaultValue}
                        />
                        <span className="text-sm">{t('pages.tenantTireBenefitValueManager.crossOverview.showDefaultValue')}</span>
                    </Space>
                    <Space>
                        <span className="text-sm">{t('pages.tenantTireBenefitValueManager.crossOverview.readOnly')}</span>
                        <Switch
                            checked={!readOnly}
                            onChange={(checked) => setReadOnly(!checked)}
                        />
                        <span className="text-sm">{t('pages.tenantTireBenefitValueManager.crossOverview.edit')}</span>
                    </Space>
                </div>
                <Table<TableRow>
                    rowKey="rowKey"
                    loading={loading}
                    dataSource={groupedTableData}
                    columns={columns}
                    expandable={{
                        defaultExpandAllRows: true,
                        rowExpandable: (row) => row.isGroup === true,
                    }}
                    scroll={{ x: 'max-content' }}
                    pagination={false}
                />
                </TenantTireBenefitValueEditProvider>
            </StandardCard>
        </>
    );
}
