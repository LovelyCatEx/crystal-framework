/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {BaseManagerController} from "../BaseManagerController.ts";
import type {
    UserBanRecord,
    ManagerReadUserBanRecordDTO
} from "@/types/user/user-ban-record.types.ts";

/**
 * Readonly manager controller for user ban records.
 * Backend: ReadonlyManagerController at /api/manager/user-ban-record ({list,query}).
 */
export const UserBanRecordManagerController = new BaseManagerController<
    UserBanRecord,
    Record<string, never>,
    ManagerReadUserBanRecordDTO
>('/manager/user-ban-record');
