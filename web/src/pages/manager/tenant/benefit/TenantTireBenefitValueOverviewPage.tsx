import {CheckCircleOutlined, CloseCircleOutlined} from "@ant-design/icons";
import {Select, Space, Table, Typography} from "antd";
import {useEffect, useState} from "react";
import {useSearchParams} from "react-router-dom";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {StandardCard} from "@/components/card/StandardCard.tsx";
import {TenantTireTypeManagerController} from "@/api/tenant/tenant-tire-type.api.ts";
import {queryBenefitOverview} from "@/api/tenant/tenant-benefit.api.ts";
import type {TenantTireBenefitOverviewItemVO} from "@/types/tenant/tenant-benefit.types.ts";
import {TenantBenefitType} from "@/types/tenant/tenant-benefit.types.ts";
import {getTenantBenefitType} from "@/i18n/enum-helpers.ts";
import {useTenantBenefitKeyToTranslationMap} from "@/i18n/tenant-benefit.tsx";
import {useTranslation} from "react-i18next";
import {TenantTireBenefitValueEditCell, TenantTireBenefitValueEditProvider} from "./TenantTireBenefitValueEditCell.tsx";

export default function BenefitOverviewPage() {
    const {t} = useTranslation();
    const benefitKeyMap = useTenantBenefitKeyToTranslationMap();
    const [searchParams, setSearchParams] = useSearchParams();
    const [tireTypes, setTireTypes] = useState<{ id: string; name: string }[]>([]);
    const [selectedTireId, setSelectedTireId] = useState<string | null>(searchParams.get('tireId') || null);
    const [items, setItems] = useState<TenantTireBenefitOverviewItemVO[]>([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        TenantTireTypeManagerController.list().then((res) => {
            setTireTypes(res.data || []);
        });
    }, []);

    const refreshData = () => {
        if (!selectedTireId) return;
        setLoading(true);
        queryBenefitOverview({ tireTypeIds: [selectedTireId] })
            .then((res) => {
                setItems(res.data?.[0]?.items || []);
            })
            .finally(() => setLoading(false));
    };

    useEffect(() => {
        refreshData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [selectedTireId]);

    const columns = [
        {
            title: t('pages.tenantTireBenefitValueManager.overview.name'),
            dataIndex: 'name',
            key: 'name',
            render: (_: unknown, row: TenantTireBenefitOverviewItemVO) => {
                const translation = benefitKeyMap.get(row.featureKey);
                const displayName = translation?.name ?? row.name;
                const displayDesc = translation?.description ?? row.description;
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
            },
        },
        {
            title: t('pages.tenantTireBenefitValueManager.overview.featureKey'),
            dataIndex: 'featureKey',
            key: 'featureKey',
            render: (_: unknown, row: TenantTireBenefitOverviewItemVO) => (
                <Typography.Text copyable>{row.featureKey}</Typography.Text>
            ),
        },
        {
            title: t('pages.tenantTireBenefitValueManager.overview.featureType'),
            dataIndex: 'featureType',
            key: 'featureType',
            width: 120,
            render: (_: unknown, row: TenantTireBenefitOverviewItemVO) => (
                <span>{getTenantBenefitType(row.featureType)}</span>
            ),
        },
        {
            title: t('pages.tenantTireBenefitValueManager.overview.defaultValue'),
            dataIndex: 'defaultValue',
            key: 'defaultValue',
            width: 120,
            render: (_: unknown, row: TenantTireBenefitOverviewItemVO) => {
                if (row.defaultValue === null) return <span>-</span>;
                if (row.featureType === TenantBenefitType.BOOLEAN) {
                    return row.defaultValue === 'true'
                        ? <CheckCircleOutlined className="text-green-500 text-base" />
                        : <CloseCircleOutlined className="text-red-500 text-base" />;
                }
                return <span>{row.defaultValue}</span>;
            },
        },
        {
            title: t('pages.tenantTireBenefitValueManager.overview.currentValue'),
            key: 'currentValue',
            width: 220,
            render: (_: unknown, row: TenantTireBenefitOverviewItemVO) => (
                <TenantTireBenefitValueEditCell
                    cellKey={`overview_${row.featureId}`}
                    featureType={row.featureType}
                    value={row.value ?? row.defaultValue}
                    defaultValue={row.defaultValue}
                    valueId={row.valueId}
                    createPayload={{ tireTypeId: selectedTireId!, featureId: row.featureId }}
                    onSaved={refreshData}
                />
            ),
        },
    ];

    return (
        <>
            <ActionBarComponent
                title={t('pages.tenantTireBenefitValueManager.overview.title')}
                subtitle={t('pages.tenantTireBenefitValueManager.overview.subtitle')}
            />
            <StandardCard className="mt-4">
                <TenantTireBenefitValueEditProvider>
                <div className="mb-4">
                    <Space>
                        <span className="font-medium">{t('pages.tenantTireBenefitValueManager.overview.selectTireType')}</span>
                        <Select
                            className="w-56"
                            placeholder={t('pages.tenantTireBenefitValueManager.overview.selectTireTypePlaceholder')}
                            allowClear
                            value={selectedTireId}
                            onChange={(value) => {
                                setSelectedTireId(value || null);
                                const params = new URLSearchParams();
                                if (value) params.set('tireId', value);
                                setSearchParams(params, { replace: true });
                            }}
                            options={(tireTypes ?? []).map((t) => ({ value: t.id, label: t.name }))}
                        />
                    </Space>
                </div>
                <Table
                    rowKey="featureId"
                    loading={loading}
                    dataSource={items}
                    columns={columns}
                    pagination={false}
                />
                </TenantTireBenefitValueEditProvider>
            </StandardCard>
        </>
    );
}
