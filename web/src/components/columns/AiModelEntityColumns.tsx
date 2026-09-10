import React, {type JSX, useEffect, useState} from "react";
import {Space, Spin, Tag} from "antd";
import type {EntityTableColumns} from "../table/entity-table.types.ts";
import type {AiModelEntity} from "@/types/ai/ai.types.ts";
import {CopyableToolTip} from "../CopyableToolTip.tsx";
import {useTranslation} from "react-i18next";
import {AiProviderManagerController} from "@/api/ai/ai-provider.api.ts";

function ProviderInfoDisplay({ providerId }: { providerId: string }): JSX.Element {
    const [providerName, setProviderName] = useState<string | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        AiProviderManagerController.getById(providerId)
            .then(res => {
                if (res) {
                    setProviderName(res.name);
                }
            })
            .catch(() => {
                setProviderName(null);
            })
            .finally(() => {
                setLoading(false);
            });
    }, [providerId]);

    if (loading) {
        return <Spin size="small" />;
    }

    if (!providerName) {
        return <Tag color="red">Unknown</Tag>;
    }

    return <Space direction="vertical" size={0}>
        <span className="text-xs">{providerName}</span>
        <CopyableToolTip title={providerId}>
            <Tag color="blue" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">ID: {providerId}</Tag>
        </CopyableToolTip>
    </Space>;
}

export function useAiModelTableColumns(): EntityTableColumns<AiModelEntity> {
    const { t } = useTranslation();

    return [
        {
            title: t('components.columns.aiModel.name'),
            dataIndex: "displayName",
            key: "displayName",
            render: function (_: unknown, row: AiModelEntity): React.ReactNode | JSX.Element {
                return <Space direction="vertical" size={0}>
                    <span className="text-sm font-medium">{row.displayName}</span>
                    <CopyableToolTip title={row.key}>
                        <span className="text-xs text-gray-500 font-mono">{row.key}</span>
                    </CopyableToolTip>
                    <CopyableToolTip title={row.id}>
                        <Tag color="blue" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">ID: {row.id}</Tag>
                    </CopyableToolTip>
                </Space>
            }
        },
        {
            title: t('components.columns.aiModel.providerId'),
            dataIndex: "providerId",
            key: "providerId",
            width: 180,
            render: function (_: unknown, row: AiModelEntity): React.ReactNode | JSX.Element {
                return <ProviderInfoDisplay providerId={row.providerId} />
            }
        },
        {
            title: t('components.columns.aiModel.maxOutputTokens'),
            dataIndex: "maxOutputTokens",
            key: "maxOutputTokens",
            width: 120,
            render: function (_: unknown, row: AiModelEntity): React.ReactNode | JSX.Element {
                return <span className="text-xs font-mono">{row.maxOutputTokens ?? 'N/A'}</span>
            }
        },
        {
            title: t('components.columns.aiModel.pricing'),
            dataIndex: "inputPricePerMillion",
            key: "pricing",
            render: function (_: unknown, row: AiModelEntity): React.ReactNode | JSX.Element {
                const formatPrice = (price: string | null | undefined): string => {
                    if (!price) return '0.00';
                    return parseFloat(price).toFixed(2);
                };

                return <div className="text-xs font-mono">
                    <div className="flex gap-2">
                        <span className="w-16">输入</span>
                        <span>{formatPrice(row.inputPricePerMillion)} {row.currency}/M</span>
                    </div>
                    <div className="flex gap-2">
                        <span className="w-16">缓存输入</span>
                        <span>{formatPrice(row.cacheReadPricePerMillion)} {row.currency}/M</span>
                    </div>
                    <div className="flex gap-2">
                        <span className="w-16">输出</span>
                        <span>{formatPrice(row.outputPricePerMillion)} {row.currency}/M</span>
                    </div>
                    <div className="flex gap-2">
                        <span className="w-16">缓存写入</span>
                        <span>{formatPrice(row.cacheWritePricePerMillion)} {row.currency}/M</span>
                    </div>
                </div>
            }
        },
        {
            title: t('components.columns.aiModel.enabled'),
            dataIndex: "enabled",
            key: "enabled",
            width: 100,
            render: function (_: unknown, row: AiModelEntity): React.ReactNode | JSX.Element {
                return row.enabled
                    ? <Tag color="green">{t('components.columns.aiModel.enabledStatus.enabled')}</Tag>
                    : <Tag color="red">{t('components.columns.aiModel.enabledStatus.disabled')}</Tag>
            }
        }
    ];
}
