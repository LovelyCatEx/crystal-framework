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
            title: t('pages.aiModelManager.modal.name.label'),
            dataIndex: "name",
            key: "name",
            render: function (_: unknown, row: AiModelEntity): React.ReactNode | JSX.Element {
                return <Space direction="vertical" size={0}>
                    <span className="text-sm font-medium">{row.name}</span>
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
            title: t('pages.aiModelManager.modal.providerId.label'),
            dataIndex: "providerId",
            key: "providerId",
            width: 180,
            render: function (_: unknown, row: AiModelEntity): React.ReactNode | JSX.Element {
                return <ProviderInfoDisplay providerId={row.providerId} />
            }
        },
        {
            title: t('pages.aiModelManager.modal.maxTokens.label'),
            dataIndex: "maxTokens",
            key: "maxTokens",
            width: 120,
            render: function (_: unknown, row: AiModelEntity): React.ReactNode | JSX.Element {
                return <span className="text-xs font-mono">{row.maxTokens ?? 'N/A'}</span>
            }
        },
        {
            title: t('pages.aiModelManager.modal.inputPricePerMillion.label'),
            dataIndex: "inputPricePerMillion",
            key: "inputPricePerMillion",
            width: 120,
            render: function (_: unknown, row: AiModelEntity): React.ReactNode | JSX.Element {
                return <span className="text-xs">{row.inputPricePerMillion}</span>
            }
        },
        {
            title: t('pages.aiModelManager.modal.enabled.label'),
            dataIndex: "enabled",
            key: "enabled",
            width: 100,
            render: function (_: unknown, row: AiModelEntity): React.ReactNode | JSX.Element {
                return row.enabled
                    ? <Tag color="green">{t('pages.aiModelManager.modal.enabled.enabled')}</Tag>
                    : <Tag color="red">{t('pages.aiModelManager.modal.enabled.disabled')}</Tag>
            }
        }
    ];
}
