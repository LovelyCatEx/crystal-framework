import {CheckCircleOutlined, CloseCircleOutlined} from "@ant-design/icons";
import {Space, Switch, Table, Typography} from "antd";
import {useEffect, useMemo, useState} from "react";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {StandardCard} from "@/components/card/StandardCard.tsx";
import {TenantTireTypeManagerController} from "@/api/tenant/tenant-tire-type.api.ts";
import {queryBenefitOverview} from "@/api/tenant/tenant-benefit.api.ts";
import type {TenantTireBenefitOverviewGroupVO, TenantTireBenefitOverviewItemVO} from "@/types/tenant/tenant-benefit.types.ts";
import {TenantBenefitType} from "@/types/tenant/tenant-benefit.types.ts";
import {useTranslation} from "react-i18next";
import {BenefitValueEditCell, BenefitValueEditProvider} from "./BenefitValueEditCell.tsx";

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

export default function CrossTireOverviewPage() {
    const {t} = useTranslation();
    const [tireTypes, setTireTypes] = useState<{ id: string; name: string }[]>([]);
    const [groupData, setGroupData] = useState<TenantTireBenefitOverviewGroupVO[]>([]);
    const [loading, setLoading] = useState(false);
    const [page, setPage] = useState(1);
    const [pageSize, setPageSize] = useState(20);
    const [total, setTotal] = useState(0);
    const [readOnly, setReadOnly] = useState(true);
    const [showDefaultValue, setShowDefaultValue] = useState(true);

    // 1. Load all tire types
    useEffect(() => {
        TenantTireTypeManagerController.list().then((res) => {
            setTireTypes(res.data || []);
        });
    }, []);

    // 2. Load overview data for ALL tire types with pagination
    useEffect(() => {
        if (tireTypes.length === 0) return;
        setLoading(true);
        queryBenefitOverview({
            tireTypeIds: tireTypes.map((t) => t.id),
            page,
            pageSize,
        })
            .then((res) => {
                setGroupData(res.data?.records ?? []);
                setTotal(res.data?.total ?? 0);
            })
            .finally(() => setLoading(false));
    }, [tireTypes, page, pageSize]);

    // 3. Build matrix: featureId → row with per-tire values
    const matrixData: MatrixRow[] = useMemo(() => {
        const featureMap = new Map<string, MatrixRow>();

        groupData.forEach((group) => {
            group.items.forEach((item: TenantTireBenefitOverviewItemVO) => {
                if (!featureMap.has(item.featureId)) {
                    featureMap.set(item.featureId, {
                        featureId: item.featureId,
                        featureKey: item.featureKey,
                        name: item.name,
                        description: item.description,
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
    }, [groupData]);

    const refreshData = () => {
        if (tireTypes.length === 0) return;
        queryBenefitOverview({
            tireTypeIds: tireTypes.map((t) => t.id),
            page,
            pageSize,
        }).then((res) => {
            setGroupData(res.data?.records ?? []);
            setTotal(res.data?.total ?? 0);
        });
    };

    // 4. Build table columns
    const columns = [
        {
            title: t('pages.tenantTireBenefitValueManager.crossOverview.name'),
            dataIndex: 'name',
            key: 'name',
            fixed: 'left' as const,
            width: 240,
            render: (_: unknown, row: MatrixRow) => (
                <div>
                    <div>{row.name}</div>
                    {row.description && (
                        <div className="text-gray-400 text-xs leading-tight mt-0.5">
                            {row.description.length > 128
                                ? `${row.description.slice(0, 128)}...`
                                : row.description}
                        </div>
                    )}
                </div>
            ),
        },
        ...(readOnly ? [] : [
            {
                title: t('pages.tenantTireBenefitValueManager.crossOverview.featureKey'),
                dataIndex: 'featureKey',
                key: 'featureKey',
                width: 260,
                render: (_: unknown, row: MatrixRow) => (
                    <Typography.Text copyable>{row.featureKey}</Typography.Text>
                ),
            },
        ]),
        ...(showDefaultValue ? [
            {
                title: t('pages.tenantTireBenefitValueManager.overview.defaultValue'),
                key: 'defaultValue',
                width: 100,
                render: (_: unknown, row: MatrixRow) => {
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
            render: (_: unknown, row: MatrixRow) => {
                const value = row.tireValues[tire.id];
                if (value === undefined) return <span>-</span>;
                return (
                    <BenefitValueEditCell
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
                <BenefitValueEditProvider>
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
                <Table
                    rowKey="featureId"
                    loading={loading}
                    dataSource={matrixData}
                    columns={columns}
                    scroll={{ x: 'max-content' }}
                    pagination={{
                        showSizeChanger: true,
                        current: page,
                        pageSize,
                        total,
                        pageSizeOptions: [5, 10, 15, 20],
                        onChange: (p, ps) => {
                            setPage(p);
                            setPageSize(ps);
                        },
                    }}
                />
                </BenefitValueEditProvider>
            </StandardCard>
        </>
    );
}
