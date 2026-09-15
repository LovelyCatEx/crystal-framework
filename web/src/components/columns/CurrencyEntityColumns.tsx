/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import React, {type JSX} from "react";
import {Space, Tag} from "antd";
import type {EntityTableColumns} from "../table/entity-table.types.ts";
import type {CurrencyEntity} from "@/types/economy/economy.types.ts";
import {CopyableToolTip} from "../CopyableToolTip.tsx";
import {useTranslation} from "react-i18next";

export function useCurrencyTableColumns(): EntityTableColumns<CurrencyEntity> {
    const { t } = useTranslation();

    return [
        {
            title: t('components.columns.currency.code'),
            dataIndex: "code",
            key: "code",
            render: function (_: unknown, row: CurrencyEntity): React.ReactNode | JSX.Element {
                return <Space direction="vertical" size={0}>
                    <span className="text-sm font-medium">{row.name}</span>
                    <CopyableToolTip title={row.code}>
                        <span className="text-xs text-gray-500 font-mono">{row.code}</span>
                    </CopyableToolTip>
                    <CopyableToolTip title={row.id}>
                        <Tag color="blue" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">ID: {row.id}</Tag>
                    </CopyableToolTip>
                </Space>
            }
        },
        {
            title: t('components.columns.currency.symbol'),
            dataIndex: "symbol",
            key: "symbol",
            width: 60,
            render: function (_: unknown, row: CurrencyEntity): React.ReactNode | JSX.Element {
                return <span>{row.symbol}</span>
            }
        },
        {
            title: t('components.columns.currency.precision'),
            dataIndex: "precision",
            key: "precision",
            width: 100,
            render: function (_: unknown, row: CurrencyEntity): React.ReactNode | JSX.Element {
                return <span>{row.precision}</span>
            }
        },
        {
            title: t('components.columns.currency.description'),
            dataIndex: "description",
            key: "description",
            width: 220,
            render: function (_: unknown, row: CurrencyEntity): React.ReactNode | JSX.Element {
                return <span className="text-gray-500">{row.description ?? '—'}</span>
            }
        },
        {
            title: t('components.columns.currency.enabled'),
            dataIndex: "enabled",
            key: "enabled",
            width: 100,
            render: function (_: unknown, row: CurrencyEntity): React.ReactNode | JSX.Element {
                return row.enabled
                    ? <Tag color="green">{t('components.columns.currency.enabledYes')}</Tag>
                    : <Tag color="red">{t('components.columns.currency.enabledNo')}</Tag>
            }
        },
        {
            title: t('components.columns.currency.sort'),
            dataIndex: "sort",
            key: "sort",
            width: 80,
            render: function (_: unknown, row: CurrencyEntity): React.ReactNode | JSX.Element {
                return <span>{row.sort}</span>
            }
        }
    ];
}
