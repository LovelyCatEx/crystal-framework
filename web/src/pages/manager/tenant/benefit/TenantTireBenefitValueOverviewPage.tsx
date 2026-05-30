import {Button, Input, InputNumber, message, Select, Space, Table, Tag, Typography} from "antd";
import {useEffect, useState} from "react";
import {useSearchParams} from "react-router-dom";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {StandardCard} from "@/components/card/StandardCard.tsx";
import {TenantTireTypeManagerController} from "@/api/tenant/tenant-tire-type.api.ts";
import {queryBenefitOverview, TenantTireBenefitValueManagerController} from "@/api/tenant/tenant-benefit.api.ts";
import type {TenantTireBenefitOverviewItemVO} from "@/types/tenant/tenant-benefit.types.ts";
import {TenantBenefitType} from "@/types/tenant/tenant-benefit.types.ts";
import {getTenantBenefitType} from "@/i18n/enum-helpers.ts";
import {useTranslation} from "react-i18next";

export default function BenefitOverviewPage() {
    const {t} = useTranslation();
    const [searchParams, setSearchParams] = useSearchParams();
    const [tireTypes, setTireTypes] = useState<{ id: string; name: string }[]>([]);
    const [selectedTireId, setSelectedTireId] = useState<string | null>(searchParams.get('tireId') || null);
    const [items, setItems] = useState<TenantTireBenefitOverviewItemVO[]>([]);
    const [loading, setLoading] = useState(false);
    const [editingFeatureId, setEditingFeatureId] = useState<string | null>(null);
    const [editValue, setEditValue] = useState<string>('');
    const [page, setPage] = useState(Number(searchParams.get('page')) || 1);
    const [pageSize, setPageSize] = useState(Number(searchParams.get('pageSize')) || 20);
    const [total, setTotal] = useState(0);

    useEffect(() => {
        TenantTireTypeManagerController.list().then((res) => {
            setTireTypes(res.data || []);
        });
    }, []);

    useEffect(() => {
        if (!selectedTireId) return;
        setLoading(true);
        queryBenefitOverview({ page, pageSize, tireTypeId: selectedTireId })
            .then((res) => {
                setItems(res.data?.records || []);
                setTotal(res.data?.total ?? 0);
            })
            .finally(() => setLoading(false));
    }, [selectedTireId, page, pageSize]);

    const saveValue = async (row: TenantTireBenefitOverviewItemVO) => {
        if (!selectedTireId) return;
        try {
            if (row.isCustomized && row.valueId) {
                await TenantTireBenefitValueManagerController.update({
                    id: row.valueId,
                    featureValue: editValue,
                } as never);
            } else {
                await TenantTireBenefitValueManagerController.create({
                    tireTypeId: selectedTireId,
                    featureId: row.featureId,
                    featureValue: editValue,
                });
            }
            message.success(t('components.managerPageContainer.updateSuccess', { entityName: t('entityNames.tenantTireBenefitValue') }));
            setEditingFeatureId(null);
            queryBenefitOverview({ page, pageSize, tireTypeId: selectedTireId })
                .then((res) => {
                    setItems(res.data?.records || []);
                    setTotal(res.data?.total ?? 0);
                });
        } catch {
            message.error(t('components.managerPageContainer.updateFailed', { entityName: t('entityNames.tenantTireBenefitValue') }));
        }
    };

    const renderValueEditor = (row: TenantTireBenefitOverviewItemVO) => {
        if (editingFeatureId !== row.featureId) {
            return (
                <span
                    className="cursor-pointer hover:text-blue-500 transition-colors"
                    onClick={() => {
                        setEditingFeatureId(row.featureId);
                        setEditValue(row.value ?? row.defaultValue ?? '');
                    }}
                >
                    {row.isCustomized
                        ? <Tag color="green" className="cursor-pointer">{row.value}</Tag>
                        : <Tag color="default" className="cursor-pointer">{t('pages.tenantTireBenefitValueManager.overview.default')}</Tag>
                    }
                </span>
            );
        }

        if (row.featureType === TenantBenefitType.BOOLEAN) {
            return (
                <Space>
                    <Select
                        className="w-32"
                        value={editValue}
                        onChange={setEditValue}
                        options={[
                            { value: 'true', label: t('pages.tenantTireBenefitValueManager.modal.featureValue.booleanTrue') },
                            { value: 'false', label: t('pages.tenantTireBenefitValueManager.modal.featureValue.booleanFalse') },
                        ]}
                    />
                    <Button type="primary" size="small" onClick={() => saveValue(row)}>{t('pages.tenantTireBenefitValueManager.overview.save')}</Button>
                    <Button size="small" onClick={() => setEditingFeatureId(null)}>{t('pages.tenantTireBenefitValueManager.overview.cancel')}</Button>
                </Space>
            );
        }
        if (row.featureType === TenantBenefitType.LIMIT) {
            return (
                <Space>
                    <InputNumber className="w-32" min={0} value={Number(editValue)} onChange={(v) => setEditValue(String(v ?? '0'))} />
                    <Button type="primary" size="small" onClick={() => saveValue(row)}>{t('pages.tenantTireBenefitValueManager.overview.save')}</Button>
                    <Button size="small" onClick={() => setEditingFeatureId(null)}>{t('pages.tenantTireBenefitValueManager.overview.cancel')}</Button>
                </Space>
            );
        }
        if (row.featureType === TenantBenefitType.ENUM) {
            const enumOptions = (row.defaultValue || '').split(',').filter(Boolean).map((opt: string) => ({ value: opt, label: opt }));
            return (
                <Space>
                    <Select className="w-32" value={editValue} onChange={setEditValue} options={enumOptions} />
                    <Button type="primary" size="small" onClick={() => saveValue(row)}>{t('pages.tenantTireBenefitValueManager.overview.save')}</Button>
                    <Button size="small" onClick={() => setEditingFeatureId(null)}>{t('pages.tenantTireBenefitValueManager.overview.cancel')}</Button>
                </Space>
            );
        }
        return (
            <Space>
                <Input className="w-32" value={editValue} onChange={(e) => setEditValue(e.target.value)} />
                <Button type="primary" size="small" onClick={() => saveValue(row)}>{t('pages.tenantTireBenefitValueManager.overview.save')}</Button>
                <Button size="small" onClick={() => setEditingFeatureId(null)}>{t('pages.tenantTireBenefitValueManager.overview.cancel')}</Button>
            </Space>
        );
    };

    const columns = [
        {
            title: t('pages.tenantTireBenefitValueManager.overview.name'),
            dataIndex: 'name',
            key: 'name',
            render: (_: unknown, row: TenantTireBenefitOverviewItemVO) => (
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
        {
            title: t('pages.tenantTireBenefitValueManager.overview.featureKey'),
            dataIndex: 'featureKey',
            key: 'featureKey',
            render: (_: unknown, row: TenantTireBenefitOverviewItemVO) => (
                <Typography.Text copyable className="font-mono">{row.featureKey}</Typography.Text>
            ),
        },
        {
            title: t('pages.tenantTireBenefitValueManager.overview.featureType'),
            dataIndex: 'featureType',
            key: 'featureType',
            width: 120,
            render: (_: unknown, row: TenantTireBenefitOverviewItemVO) => (
                <Tag>{getTenantBenefitType(row.featureType)}</Tag>
            ),
        },
        {
            title: t('pages.tenantTireBenefitValueManager.overview.defaultValue'),
            dataIndex: 'defaultValue',
            key: 'defaultValue',
            width: 120,
            render: (_: unknown, row: TenantTireBenefitOverviewItemVO) => (
                <span className="text-gray-400 font-mono">{row.defaultValue ?? '-'}</span>
            ),
        },
        {
            title: t('pages.tenantTireBenefitValueManager.overview.currentValue'),
            key: 'currentValue',
            width: 220,
            render: (_: unknown, row: TenantTireBenefitOverviewItemVO) => renderValueEditor(row),
        },
    ];

    return (
        <>
            <ActionBarComponent
                title={t('pages.tenantTireBenefitValueManager.overview.title')}
                subtitle={t('pages.tenantTireBenefitValueManager.overview.subtitle')}
            />
            <StandardCard className="mt-4">
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
                                setPage(1);
                                const params = new URLSearchParams();
                                if (value) params.set('tireId', value);
                                params.set('page', '1');
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
                    pagination={{
                        showSizeChanger: true,
                        current: page,
                        pageSize,
                        total,
                        pageSizeOptions: [5, 10, 15, 20],
                        onChange: (p, ps) => {
                            setPage(p);
                            setPageSize(ps);
                            const params = new URLSearchParams(searchParams.toString());
                            params.set('page', String(p));
                            params.set('pageSize', String(ps));
                            setSearchParams(params, { replace: true });
                        },
                    }}
                />
            </StandardCard>
        </>
    );
}
