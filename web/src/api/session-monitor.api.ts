import type {PaginatedResponseData} from "../types/api.types.ts";
import type {SessionDescription} from "../types/session.types.ts";
import {doGet} from "./system-request.ts";

export interface SessionSearchDTO {
    page: number;
    pageSize: number;
    sessionId?: string;
}

export async function getOnlineSessions(dto: SessionSearchDTO): Promise<PaginatedResponseData<SessionDescription>> {
    const result = await doGet<PaginatedResponseData<SessionDescription>>(
        `/api/manager/monitor/session/online`,
        dto
    );
    return result.data!;
}