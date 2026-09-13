/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import type {PaginatedResponseData} from "@/types/api.types.ts";
import type {SessionDescription} from "@/types/system/session.types.ts";
import {doGet} from "../system-request.ts";

export interface SessionSearchDTO {
    page: number;
    pageSize: number;
    sessionId?: string;
    type?: number;
}

export async function getOnlineSessions(dto: SessionSearchDTO): Promise<PaginatedResponseData<SessionDescription>> {
    const result = await doGet<PaginatedResponseData<SessionDescription>>(
        `/api/manager/monitor/session/online`,
        dto
    );
    return result.data!;
}