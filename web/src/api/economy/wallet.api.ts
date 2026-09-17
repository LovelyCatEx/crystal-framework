/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {BaseManagerController} from "@/api/BaseManagerController.ts";
import type {EconomyTransactionEntity, WalletEntity} from "@/types/economy/economy.types.ts";
import type {BaseManagerReadDTO, BaseManagerUpdateDTO} from "@/types/api.types.ts";
import {doPost} from "@/api/system-request.ts";

export interface ManagerCreateWalletDTO {
    placeholder?: string;
}

export interface ManagerReadWalletDTO extends BaseManagerReadDTO {
    scope: number;
    scopeId?: string;
}

export interface ManagerUpdateWalletDTO extends BaseManagerUpdateDTO {
    id: string;
}

export const WalletManagerController = new BaseManagerController<
    WalletEntity,
    ManagerCreateWalletDTO,
    ManagerReadWalletDTO,
    ManagerUpdateWalletDTO
>('/manager/wallet');

export interface ManagerAdjustWalletDTO {
    scope: number;
    scopeId: string;
    ownerId: string;
    currencyId: string;
    amount: number;
    type: number;
    referenceId?: string | null;
    remark?: string | null;
}

export const adjustWallet = async (dto: ManagerAdjustWalletDTO) => {
    return doPost<EconomyTransactionEntity>('/api/manager/wallet/adjust', dto, {'Content-Type': 'application/json'});
};
