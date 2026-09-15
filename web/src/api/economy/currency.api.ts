/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {BaseManagerController} from "@/api/BaseManagerController.ts";
import type {CurrencyEntity} from "@/types/economy/economy.types.ts";
import type {BaseManagerReadDTO, BaseManagerUpdateDTO} from "@/types/api.types.ts";

export interface ManagerCreateCurrencyDTO {
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

export interface ManagerUpdateCurrencyDTO extends BaseManagerUpdateDTO {
    code?: string | null;
    name?: string | null;
    symbol?: string | null;
    precision?: number | null;
    symbolPosition?: number | null;
    decimalSeparator?: string | null;
    thousandsSeparator?: string | null;
    description?: string | null;
    enabled?: boolean | null;
    sort?: number | null;
}

export interface ManagerReadCurrencyDTO extends BaseManagerReadDTO {
    // All filtering is done via query: GroupNode
}

export const CurrencyManagerController = new BaseManagerController<
    CurrencyEntity,
    ManagerCreateCurrencyDTO,
    ManagerReadCurrencyDTO,
    ManagerUpdateCurrencyDTO
>('/manager/currency');
