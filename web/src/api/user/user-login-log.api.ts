/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {BaseManagerController} from "../BaseManagerController.ts";
import type {
    UserLoginLogEntity,
    ManagerCreateUserLoginLogDTO,
    ManagerReadUserLoginLogDTO,
    ManagerUpdateUserLoginLogDTO,
    ManagerDeleteUserLoginLogDTO
} from "@/types/user/user-login-log.types.ts";

export const UserLoginLogManagerController = new BaseManagerController<
    UserLoginLogEntity,
    ManagerCreateUserLoginLogDTO,
    ManagerReadUserLoginLogDTO,
    ManagerUpdateUserLoginLogDTO,
    ManagerDeleteUserLoginLogDTO
>('/manager/user-login-log');