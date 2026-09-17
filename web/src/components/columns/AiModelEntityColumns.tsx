/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import React, {type JSX} from "react";
import {Flex, Space, Spin, Tag} from "antd";
import type {EntityTableColumns} from "../table/entity-table.types.ts";
import type {AiModelEntity} from "@/types/ai/ai.types.ts";
import {CopyableToolTip} from "../CopyableToolTip.tsx";
import {useTranslation} from "react-i18next";
import {useAiProvider} from "@/compositions/use-ai-provider.ts";
import {getAiModelCapability} from "@/i18n/enum-helpers.ts";
import {CurrencyAmountCodeDisplay} from "@/components/economy/CurrencyAmountCodeDisplay.tsx";

function ProviderInfoDisplay({ providerId }: { providerId: string }): JSX.Element {
    const {provider, isLoading} = useAiProvider(providerId);

    if (isLoading) {
        return <Spin size="small" />;
    }

    if (!provider) {
        return <Tag color="red">Unknown</Tag>;
    }

    return <Space direction="vertical" size={0}>
        <span className="text-xs">{provider.name}</span>
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
            title: t('components.columns.aiModel.capabilities'),
            dataIndex: "capabilities",
            key: "capabilities",
            width: 200,
            render: function (_: unknown, row: AiModelEntity): React.ReactNode | JSX.Element {
                try {
                    const capabilities: number[] = JSON.parse(row.capabilities);
                    return (
                        <Flex gap={4} wrap style={{ maxWidth: '100%' }}>
                            {capabilities.map((capability, index) => (
                                <Tag key={index} color="blue" className="text-xs m-0">
                                    {getAiModelCapability(capability)}
                                </Tag>
                            ))}
                        </Flex>
                    );
                } catch {
                    return <Tag color="red" className="text-xs">Invalid</Tag>;
                }
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
                return <div className="text-xs font-mono">
                    <div className="flex gap-2">
                        <span className="w-16">{t('components.columns.aiModel.input')}</span>
                        <span><CurrencyAmountCodeDisplay currencyId={row.currencyId} amount={row.inputPricePerMillion} />/M</span>
                    </div>
                    <div className="flex gap-2">
                        <span className="w-16">{t('components.columns.aiModel.cacheInput')}</span>
                        <span><CurrencyAmountCodeDisplay currencyId={row.currencyId} amount={row.cacheReadPricePerMillion} />/M</span>
                    </div>
                    <div className="flex gap-2">
                        <span className="w-16">{t('components.columns.aiModel.output')}</span>
                        <span><CurrencyAmountCodeDisplay currencyId={row.currencyId} amount={row.outputPricePerMillion} />/M</span>
                    </div>
                    <div className="flex gap-2">
                        <span className="w-16">{t('components.columns.aiModel.cacheWrite')}</span>
                        <span><CurrencyAmountCodeDisplay currencyId={row.currencyId} amount={row.cacheWritePricePerMillion} />/M</span>
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
