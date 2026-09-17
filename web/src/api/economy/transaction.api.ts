/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {BaseManagerController} from "@/api/BaseManagerController.ts";
import type {EconomyTransactionEntity} from "@/types/economy/economy.types.ts";
import type {BaseManagerReadDTO, BaseManagerUpdateDTO} from "@/types/api.types.ts";

export interface ManagerCreateEconomyTransactionDTO {
    placeholder?: string;
}

export interface ManagerReadEconomyTransactionDTO extends BaseManagerReadDTO {
    scope: number;
    scopeId?: string;
}

export interface ManagerUpdateEconomyTransactionDTO extends BaseManagerUpdateDTO {
    id: string;
}

export const EconomyTransactionManagerController = new BaseManagerController<
    EconomyTransactionEntity,
    ManagerCreateEconomyTransactionDTO,
    ManagerReadEconomyTransactionDTO,
    ManagerUpdateEconomyTransactionDTO
>('/manager/transaction');
