/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import type {BaseEntity} from "../BaseEntity.ts";
import type {BaseScopedEntity} from "../BaseScopedEntity.ts";

export interface CurrencyEntity extends BaseEntity {
    code: string;
    name: string;
    symbol: string;
    precision: number;
    symbolPosition: number;
    decimalSeparator: string;
    thousandsSeparator: string;
    description: string | null;
    enabled: boolean;
    sort: number;
}

export interface WalletEntity extends BaseScopedEntity {
    ownerId: string;
    currencyId: string;
    balance: string;
}

export interface EconomyTransactionEntity extends BaseScopedEntity {
    ownerId: string;
    requestId: string;
    type: number;
    currencyId: string;
    amount: string;
    balanceBefore: string;
    balanceAfter: string;
    referenceType: number;
    referenceId: string | null;
    remark: string | null;
}

export enum EconomyTransactionType {
    RECHARGE = 0,
    DEDUCT = 1,
}

export enum EconomyReferenceType {
    NONE = 0,
    AI_INVOCATION = 1,
}

export enum CurrencySymbolPosition {
    PREFIX = 0,
    SUFFIX = 1,
    REPLACE_DECIMAL = 2,
}
