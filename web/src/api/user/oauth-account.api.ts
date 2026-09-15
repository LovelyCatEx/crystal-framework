/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {BaseManagerController} from "../BaseManagerController.ts";
import type {OAuthAccount} from "@/types/user/oauth-account.types.ts";
import type {BaseManagerReadDTO, BaseManagerUpdateDTO} from "@/types/api.types.ts";

export const OAuthAccountManagerController = new BaseManagerController<
    OAuthAccount,
    ManagerCreateOAuthAccountDTO,
    ManagerReadOAuthAccountDTO,
    ManagerUpdateOAuthAccountDTO
>('/manager/oauth-account');

export interface ManagerCreateOAuthAccountDTO {
    userId: string | null;
    platform: number;
    identifier: string;
    nickname: string | null;
    avatar: string | null;
}

export interface ManagerUpdateOAuthAccountDTO extends BaseManagerUpdateDTO {
    userId?: string | null;
    platform?: number | null;
    identifier?: string | null;
    nickname?: string | null;
    avatar?: string | null;
}

export interface ManagerReadOAuthAccountDTO extends BaseManagerReadDTO {
    platform?: number | null;
}
