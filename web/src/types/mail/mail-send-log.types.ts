/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import type {BaseEntity} from "../BaseEntity.ts";
import type {BaseManagerReadDTO} from "../api.types.ts";

export interface MailSendLogEntity extends BaseEntity {
    fromEmail: string;
    toEmail: string;
    subject: string;
    content: string;
    success: boolean;
    errorMessage: string | null;
    userId: string | null;
    tenantId: string | null;
}

export interface ManagerReadMailSendLogDTO extends BaseManagerReadDTO {
    keyword?: string;
    toEmail?: string;
    success?: boolean;
    userId?: string;
    tenantId?: string;
    startTime?: string;
    endTime?: string;
}

export interface ManagerUpdateMailSendLogDTO {
    id: string;
}

export interface ManagerDeleteMailSendLogDTO {
    ids: string[];
}

export interface ManagerCreateMailSendLogDTO {
    placeholder?: string;
}
