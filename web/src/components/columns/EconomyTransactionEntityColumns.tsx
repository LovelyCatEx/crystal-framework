/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import React, {type JSX} from "react";
import {Space, Tag} from "antd";
import type {EntityTableColumns} from "../table/entity-table.types.ts";
import type {EconomyTransactionEntity} from "@/types/economy/economy.types.ts";
import {ResourceScope} from "@/types/BaseScopedEntity.ts";
import {ScopedUserDisplay} from "@/components/ScopedUserDisplay.tsx";
import {CopyableToolTip} from "../CopyableToolTip.tsx";
import {CurrencyCodeDisplay} from "../economy/CurrencyCodeDisplay.tsx";
import {CurrencyAmountDisplay} from "../economy/CurrencyAmountDisplay.tsx";
import {useTranslation} from "react-i18next";
import {getEconomyReferenceType, getEconomyTransactionType} from "@/i18n/enum-helpers.ts";

export function useEconomyTransactionTableColumns(): EntityTableColumns<EconomyTransactionEntity> {
    const { t } = useTranslation();

    return [
        {
            title: t('components.columns.transaction.requestId'),
            dataIndex: "requestId",
            key: "requestId",
            width: 280,
            render: function (_: unknown, row: EconomyTransactionEntity): React.ReactNode | JSX.Element {
                return <Space direction="vertical" size={0}>
                    <CopyableToolTip title={row.requestId}>
                        <span className="font-mono text-xs text-gray-500">{row.requestId}</span>
                    </CopyableToolTip>
                    <CopyableToolTip title={row.id}>
                        <Tag color="blue" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">ID: {row.id}</Tag>
                    </CopyableToolTip>
                </Space>
            }
        },
        {
            title: t('components.columns.transaction.type'),
            dataIndex: "type",
            key: "type",
            width: 120,
            render: function (_: unknown, row: EconomyTransactionEntity): React.ReactNode | JSX.Element {
                return <Tag color="purple">{getEconomyTransactionType(row.type)}</Tag>
            }
        },
        {
            title: t('components.columns.transaction.ownerId'),
            dataIndex: "ownerId",
            key: "ownerId",
            width: 220,
            render: function (_: unknown, row: EconomyTransactionEntity): React.ReactNode | JSX.Element {
                return <ScopedUserDisplay scope={row.scope as ResourceScope} resourceId={row.ownerId}/>;
            }
        },
        {
            title: t('components.columns.transaction.currency'),
            dataIndex: "currencyId",
            key: "currencyId",
            width: 140,
            render: function (_: unknown, row: EconomyTransactionEntity): React.ReactNode | JSX.Element {
                return <CurrencyCodeDisplay currencyId={row.currencyId} />
            }
        },
        {
            title: t('components.columns.transaction.amount'),
            dataIndex: "amount",
            key: "amount",
            width: 140,
            render: function (_: unknown, row: EconomyTransactionEntity): React.ReactNode | JSX.Element {
                const negative = Number(row.amount) < 0;
                const color = negative ? 'text-red-600' : 'text-green-600';
                const sign = negative ? '' : '+';
                return <span className={`font-mono font-semibold ${color}`}>{sign}<CurrencyAmountDisplay currencyId={row.currencyId} units={row.amount}/></span>
            }
        },
        {
            title: t('components.columns.transaction.balance'),
            dataIndex: "balanceAfter",
            key: "balanceAfter",
            width: 180,
            render: function (_: unknown, row: EconomyTransactionEntity): React.ReactNode | JSX.Element {
                return <span className="font-mono text-xs">
                    <CurrencyAmountDisplay currencyId={row.currencyId} units={row.balanceBefore}/> → <CurrencyAmountDisplay currencyId={row.currencyId} units={row.balanceAfter}/>
                </span>
            }
        },
        {
            title: t('components.columns.transaction.reference'),
            dataIndex: "referenceId",
            key: "referenceId",
            width: 200,
            render: function (_: unknown, row: EconomyTransactionEntity): React.ReactNode | JSX.Element {
                if (!row.referenceId && row.referenceType === 0) return <span className="text-gray-400">—</span>;
                return <Space direction="vertical" size={0}>
                    {row.referenceType !== 0 && <Tag color="cyan" className="m-0">{getEconomyReferenceType(row.referenceType)}</Tag>}
                    {row.referenceId && <CopyableToolTip title={row.referenceId}>
                        <span className="font-mono text-xs text-gray-500">{row.referenceId}</span>
                    </CopyableToolTip>}
                </Space>
            }
        },
        {
            title: t('components.columns.transaction.remark'),
            dataIndex: "remark",
            key: "remark",
            width: 200,
            render: function (_: unknown, row: EconomyTransactionEntity): React.ReactNode | JSX.Element {
                return <span className="text-gray-500">{row.remark ?? '—'}</span>
            }
        }
    ];
}
