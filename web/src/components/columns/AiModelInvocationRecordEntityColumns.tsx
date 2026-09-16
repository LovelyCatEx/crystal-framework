/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import React, {type JSX} from "react";
import {Space, Spin, Tag, Tooltip} from "antd";
import type {EntityTableColumns} from "../table/entity-table.types.ts";
import {AiModelInvocationStatus, type AiModelInvocationRecordEntity} from "@/types/ai/ai.types.ts";
import {CopyableToolTip} from "../CopyableToolTip.tsx";
import {UserChipById} from "@/components/chip/UserChipById.tsx";
import {AiModelChip} from "@/components/chip/AiModelChip.tsx";
import {CurrencyAmountCodeDisplay} from "@/components/economy/CurrencyAmountCodeDisplay.tsx";
import {useAiUserGroup} from "@/compositions/use-ai-user-group.ts";
import {getAiModelInvocationStatus} from "@/i18n/enum-helpers.ts";
import {formatTimestamp} from "@/utils/datetime.utils.ts";
import {useTranslation} from "react-i18next";

function GroupInfoDisplay({ groupId, groupMultiplier }: { groupId: string | null; groupMultiplier: number }): JSX.Element {
    const {group, isLoading} = useAiUserGroup(groupId);

    if (isLoading) {
        return <Spin size="small" />;
    }

    if (!groupId) {
        return <span className="text-xs text-gray-400">—</span>;
    }

    return <Space direction="vertical" size={0}>
        {group
            ? <span className="text-xs">{group.name}</span>
            : <Tag color="red">Unknown</Tag>}
        <span className="text-xs text-gray-500">×{groupMultiplier}</span>
    </Space>;
}

export function useAiModelInvocationRecordTableColumns(): EntityTableColumns<AiModelInvocationRecordEntity> {
    const { t } = useTranslation();

    return [
        {
            title: t('components.columns.aiModelInvocationRecord.record'),
            dataIndex: "createdTime",
            key: "record",
            width: 190,
            render: function (_: unknown, row: AiModelInvocationRecordEntity): React.ReactNode | JSX.Element {
                return (
                    <Space direction="vertical" size={0}>
                        <span className="text-xs">{formatTimestamp(row.createdTime)}</span>
                        <CopyableToolTip title={row.id}>
                            <Tag color="blue" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">ID: {row.id}</Tag>
                        </CopyableToolTip>
                    </Space>
                );
            }
        },
        {
            title: t('components.columns.aiModelInvocationRecord.user'),
            dataIndex: "userId",
            key: "user",
            width: 160,
            render: function (_: unknown, row: AiModelInvocationRecordEntity): React.ReactNode | JSX.Element {
                return <UserChipById userId={row.userId} />;
            }
        },
        {
            title: t('components.columns.aiModelInvocationRecord.group'),
            dataIndex: "groupId",
            key: "group",
            width: 140,
            render: function (_: unknown, row: AiModelInvocationRecordEntity): React.ReactNode | JSX.Element {
                return <GroupInfoDisplay groupId={row.groupId} groupMultiplier={row.groupMultiplier} />;
            }
        },
        {
            title: t('components.columns.aiModelInvocationRecord.status'),
            dataIndex: "status",
            key: "status",
            width: 90,
            render: function (_: unknown, row: AiModelInvocationRecordEntity): React.ReactNode | JSX.Element {
                const tag = (
                    <Tag color={row.status === AiModelInvocationStatus.SUCCESS ? 'green' : 'red'}>
                        {getAiModelInvocationStatus(row.status)}
                    </Tag>
                );
                return row.errorMessage ? <Tooltip title={row.errorMessage}>{tag}</Tooltip> : tag;
            }
        },
        {
            title: t('components.columns.aiModelInvocationRecord.model'),
            dataIndex: "modelId",
            key: "model",
            width: 170,
            render: function (_: unknown, row: AiModelInvocationRecordEntity): React.ReactNode | JSX.Element {
                return <AiModelChip modelId={row.modelId} />;
            }
        },
        {
            title: t('components.columns.aiModelInvocationRecord.performance'),
            dataIndex: "totalDurationMs",
            key: "performance",
            width: 150,
            render: function (_: unknown, row: AiModelInvocationRecordEntity): React.ReactNode | JSX.Element {
                return (
                    <Space direction="vertical" size={0}>
                        <span className="text-xs font-mono">{t('components.columns.aiModelInvocationRecord.duration')}: {row.totalDurationMs}ms</span>
                        <span className="text-xs font-mono">{t('components.columns.aiModelInvocationRecord.firstToken')}: {row.timeToFirstTokenMs}ms</span>
                        {row.isStreaming && (
                            <Tag color="cyan" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">{t('components.columns.aiModelInvocationRecord.streaming')}</Tag>
                        )}
                    </Space>
                );
            }
        },
        {
            title: t('components.columns.aiModelInvocationRecord.inputTokens'),
            dataIndex: "promptTokens",
            key: "inputTokens",
            width: 110,
            render: function (_: unknown, row: AiModelInvocationRecordEntity): React.ReactNode | JSX.Element {
                return (
                    <Space direction="vertical" size={0}>
                        <span className="text-xs font-mono">{row.promptTokens}</span>
                        {row.cachedPromptTokens > 0 && (
                            <span className="text-xs text-gray-400">{t('components.columns.aiModelInvocationRecord.cache')} {row.cachedPromptTokens}</span>
                        )}
                    </Space>
                );
            }
        },
        {
            title: t('components.columns.aiModelInvocationRecord.outputTokens'),
            dataIndex: "completionTokens",
            key: "outputTokens",
            width: 110,
            render: function (_: unknown, row: AiModelInvocationRecordEntity): React.ReactNode | JSX.Element {
                const cell = (
                    <Space direction="vertical" size={0}>
                        <span className="text-xs font-mono">{row.completionTokens}</span>
                        {row.cacheCreationTokens > 0 && (
                            <span className="text-xs text-gray-400">{t('components.columns.aiModelInvocationRecord.cache')} {row.cacheCreationTokens}</span>
                        )}
                    </Space>
                );
                return row.reasoningTokens > 0
                    ? <Tooltip title={`${t('components.columns.aiModelInvocationRecord.reasoningTokens')}: ${row.reasoningTokens}`}>{cell}</Tooltip>
                    : cell;
            }
        },
        {
            title: t('components.columns.aiModelInvocationRecord.cost'),
            dataIndex: "finalCost",
            key: "cost",
            width: 130,
            render: function (_: unknown, row: AiModelInvocationRecordEntity): React.ReactNode | JSX.Element {
                return (
                    <Space direction="vertical" size={0}>
                        <span className="text-xs font-mono"><CurrencyAmountCodeDisplay currencyId={row.currencyId} amount={row.finalCost} /></span>
                        <span className="text-xs text-gray-400 line-through"><CurrencyAmountCodeDisplay currencyId={row.currencyId} amount={row.rawCost} /></span>
                    </Space>
                );
            }
        },
        {
            title: t('components.columns.aiModelInvocationRecord.pricing'),
            dataIndex: "promptUnitPrice",
            key: "pricing",
            width: 150,
            render: function (_: unknown, row: AiModelInvocationRecordEntity): React.ReactNode | JSX.Element {
                return (
                    <Tooltip title={
                        <div>
                            {t('components.columns.aiModelInvocationRecord.input')}: <CurrencyAmountCodeDisplay currencyId={row.currencyId} amount={row.promptUnitPrice} />/M
                            <br />
                            {t('components.columns.aiModelInvocationRecord.output')}: <CurrencyAmountCodeDisplay currencyId={row.currencyId} amount={row.completionUnitPrice} />/M
                            <br />
                            {t('components.columns.aiModelInvocationRecord.cacheRead')}: <CurrencyAmountCodeDisplay currencyId={row.currencyId} amount={row.cacheReadUnitPrice} />/M
                            <br />
                            {t('components.columns.aiModelInvocationRecord.cacheWrite')}: <CurrencyAmountCodeDisplay currencyId={row.currencyId} amount={row.cacheWriteUnitPrice} />/M
                        </div>
                    }>
                        <Space direction="vertical" size={0}>
                            <span className="text-xs font-mono">{t('components.columns.aiModelInvocationRecord.input')}: <CurrencyAmountCodeDisplay currencyId={row.currencyId} amount={row.promptUnitPrice} />/M</span>
                            <span className="text-xs font-mono">{t('components.columns.aiModelInvocationRecord.output')}: <CurrencyAmountCodeDisplay currencyId={row.currencyId} amount={row.completionUnitPrice} />/M</span>
                        </Space>
                    </Tooltip>
                );
            }
        }
    ];
}
