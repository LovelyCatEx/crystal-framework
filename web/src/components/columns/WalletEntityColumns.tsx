/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import React, {type JSX} from "react";
import type {EntityTableColumns} from "../table/entity-table.types.ts";
import type {WalletEntity} from "@/types/economy/economy.types.ts";
import {ResourceScope} from "@/types/BaseScopedEntity.ts";
import {ScopedUserDisplay} from "@/components/ScopedUserDisplay.tsx";
import {CopyableToolTip} from "../CopyableToolTip.tsx";
import {CurrencyCodeDisplay} from "../economy/CurrencyCodeDisplay.tsx";
import {CurrencyAmountDisplay} from "../economy/CurrencyAmountDisplay.tsx";
import {useTranslation} from "react-i18next";

export function useWalletTableColumns(): EntityTableColumns<WalletEntity> {
    const { t } = useTranslation();

    return [
        {
            title: t('components.columns.wallet.id'),
            dataIndex: "id",
            key: "id",
            width: 170,
            render: function (_: unknown, row: WalletEntity): React.ReactNode | JSX.Element {
                return <CopyableToolTip title={row.id}>
                    <span className="font-mono text-xs text-gray-500">{row.id}</span>
                </CopyableToolTip>
            }
        },
        {
            title: t('components.columns.wallet.ownerId'),
            dataIndex: "ownerId",
            key: "ownerId",
            render: function (_: unknown, row: WalletEntity): React.ReactNode | JSX.Element {
                return <ScopedUserDisplay scope={row.scope as ResourceScope} resourceId={row.ownerId}/>;
            }
        },
        {
            title: t('components.columns.wallet.currency'),
            dataIndex: "currencyId",
            key: "currencyId",
            width: 140,
            render: function (_: unknown, row: WalletEntity): React.ReactNode | JSX.Element {
                return <CurrencyCodeDisplay currencyId={row.currencyId} />
            }
        },
        {
            title: t('components.columns.wallet.balance'),
            dataIndex: "balance",
            key: "balance",
            width: 160,
            render: function (_: unknown, row: WalletEntity): React.ReactNode | JSX.Element {
                return <CurrencyAmountDisplay currencyId={row.currencyId} units={row.balance}/>
            }
        }
    ];
}
