/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import type {BaseEntity} from "@/types/BaseEntity.ts";

export enum SessionType {
    USER = 0,
    PROMETHEUS = 1,
}

export interface SessionDescription extends BaseEntity {
    sessionId: string;
    remoteIp: string;
    userAgent: string;
    userId: string | null;
    tenantId: string | null;
    type: number;
}