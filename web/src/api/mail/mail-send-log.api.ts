/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {BaseManagerController} from "../BaseManagerController.ts";
import type {
    MailSendLogEntity,
    ManagerCreateMailSendLogDTO,
    ManagerReadMailSendLogDTO,
    ManagerUpdateMailSendLogDTO,
    ManagerDeleteMailSendLogDTO
} from "@/types/mail/mail-send-log.types.ts";

export const MailSendLogManagerController = new BaseManagerController<
    MailSendLogEntity,
    ManagerCreateMailSendLogDTO,
    ManagerReadMailSendLogDTO,
    ManagerUpdateMailSendLogDTO,
    ManagerDeleteMailSendLogDTO
>('/manager/mail-send-log');
