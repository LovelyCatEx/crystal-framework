import {Input, InputNumber, message, Select, Space, Table, Tag, Typography} from "antd";
import {useEffect, useState} from "react";
import {useSearchParams} from "react-router-dom";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {StandardCard} from "@/components/card/StandardCard.tsx";
import {TenantTireTypeManagerController} from "@/api/tenant/tenant-tire-type.api.ts";
import {getBenefitOverview, TenantTireBenefitValueManagerController} from "@/api/tenant/tenant-benefit.api.ts";
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

    useEffect(() => {
        TenantTireTypeManagerController.list().then((res) => {
            setTireTypes(res.data || []);
        });
    }, []);

    useEffect(() => {
        if (!selectedTireId) return;
        setLoading(true);
        getBenefitOverview(selectedTireId)
            .then((res) => {
                setItems(res.data || []);
            })
            .finally(() => setLoading(false));
    }, [selectedTireId]);

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
            getBenefitOverview(selectedTireId).then((res) => setItems(res.data || []));
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
                    <Tag color="blue" className="cursor-pointer" onClick={() => saveValue(row)}>{t('pages.tenantTireBenefitValueManager.overview.save')}</Tag>
                    <Tag color="default" className="cursor-pointer" onClick={() => setEditingFeatureId(null)}>{t('pages.tenantTireBenefitValueManager.overview.cancel')}</Tag>
                </Space>
            );
        }
        if (row.featureType === TenantBenefitType.LIMIT) {
            return (
                <Space>
                    <InputNumber className="w-32" min={0} value={Number(editValue)} onChange={(v) => setEditValue(String(v ?? '0'))} />
                    <Tag color="blue" className="cursor-pointer" onClick={() => saveValue(row)}>{t('pages.tenantTireBenefitValueManager.overview.save')}</Tag>
                    <Tag color="default" className="cursor-pointer" onClick={() => setEditingFeatureId(null)}>{t('pages.tenantTireBenefitValueManager.overview.cancel')}</Tag>
                </Space>
            );
        }
        if (row.featureType === TenantBenefitType.ENUM) {
            const enumOptions = (row.defaultValue || '').split(',').filter(Boolean).map((opt: string) => ({ value: opt, label: opt }));
            return (
                <Space>
                    <Select className="w-32" value={editValue} onChange={setEditValue} options={enumOptions} />
                    <Tag color="blue" className="cursor-pointer" onClick={() => saveValue(row)}>{t('pages.tenantTireBenefitValueManager.overview.save')}</Tag>
                    <Tag color="default" className="cursor-pointer" onClick={() => setEditingFeatureId(null)}>{t('pages.tenantTireBenefitValueManager.overview.cancel')}</Tag>
                </Space>
            );
        }
        return (
            <Space>
                <Input className="w-32" value={editValue} onChange={(e) => setEditValue(e.target.value)} />
                <Tag color="blue" className="cursor-pointer" onClick={() => saveValue(row)}>{t('pages.tenantTireBenefitValueManager.overview.save')}</Tag>
                <Tag color="default" className="cursor-pointer" onClick={() => setEditingFeatureId(null)}>{t('pages.tenantTireBenefitValueManager.overview.cancel')}</Tag>
            </Space>
        );
    };

    const columns = [
        {
            title: t('pages.tenantTireBenefitValueManager.overview.name'),
            dataIndex: 'name',
            key: 'name',
            render: (_: unknown, row: TenantTireBenefitOverviewItemVO) => (
                <span>{row.name}</span>
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
                                setSearchParams(value ? { tireId: value } : {}, { replace: true });
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
            </StandardCard>
        </>
    );
}
