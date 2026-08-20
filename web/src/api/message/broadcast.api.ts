import {BaseManagerController} from "../BaseManagerController.ts";
import type {BaseManagerDeleteDTO, BaseManagerReadScopedDTO, BaseManagerUpdateDTO} from "@/types/api.types.ts";
import type {Broadcast, BroadcastInboxItem} from "@/types/message/broadcast.types.ts";
import type {PaginatedResponseData} from "@/types/api.types.ts";
import {doGet, doPost, type ApiResponse} from "../system-request.ts";

/**
 * Frontend write-side client for {@link com.lovelycatv.crystalframework.message.controller.manager.broadcast.ManagerBroadcastController}
 * (StandardScopedManagerController, baseUrl `/manager/broadcast`). DTOs mirror the backend
 * Manager*BroadcastDTO exactly; every backend `Long` is transmitted as `string`.
 */

// Mirrors ManagerCreateBroadcastDTO. `scope + scopeId` drive scoped RBAC; sender identity is
// derived server-side from the scope, never sent by the client.
export interface ManagerCreateBroadcastDTO {
    scope: number;
    scopeId: string;
    category?: number;
    audienceType: number;
    audienceRef?: string;
    title: string;
    content: string;
    publishTime?: string;
    expireTime?: string;
}

// Mirrors ManagerReadBroadcastDTO (page/pageSize/id/query + scope/scopeId from BaseManagerReadScopedDTO).
export type ManagerReadBroadcastDTO = BaseManagerReadScopedDTO;

// Mirrors ManagerUpdateBroadcastDTO. Scope is fixed at creation and cannot be changed here.
export interface ManagerUpdateBroadcastDTO extends BaseManagerUpdateDTO {
    title?: string;
    content?: string;
    category?: number;
    audienceType?: number;
    audienceRef?: string;
    publishTime?: string;
    expireTime?: string;
}

// Mirrors ManagerDeleteBroadcastDTO.
export interface ManagerDeleteBroadcastDTO extends BaseManagerDeleteDTO {
    ids: string[];
}

class BroadcastManagerControllerClass extends BaseManagerController<
    Broadcast,
    ManagerCreateBroadcastDTO,
    ManagerReadBroadcastDTO,
    ManagerUpdateBroadcastDTO,
    ManagerDeleteBroadcastDTO
> {
    constructor() {
        super('/manager/broadcast');
    }
}

export const BroadcastManagerController = new BroadcastManagerControllerClass();

/**
 * End-user consumer endpoints for {@link com.lovelycatv.crystalframework.message.controller.BroadcastController}
 * (base `/broadcast`). Read-diffusion: `unread-count` returns the number of unread, non-expired
 * broadcasts (for the header badge); `history` returns every visible broadcast (read + expired
 * included) with a `read` flag; opening one records a lazy read marker.
 */

// GET /broadcast/unread-count → Long (serialized as string): number of unread broadcasts.
export async function getBroadcastUnreadCount(): Promise<ApiResponse<string>> {
    return doGet<string>('/api/broadcast/unread-count');
}

// POST /broadcast/mark-read (@RequestParam broadcastId) → records the read marker. broadcastId is a
// query param, not a body field: @RequestParam binds from the query string, and the request-body
// encryption layer would otherwise hide it.
export async function markBroadcastRead(broadcastId: string): Promise<ApiResponse<null>> {
    return doPost<null>(`/api/broadcast/mark-read?broadcastId=${encodeURIComponent(broadcastId)}`);
}

// GET /broadcast/history?page&pageSize → PaginatedResponseData<BroadcastInboxVO>: every broadcast
// visible to the caller (read + expired included), newest first, each tagged with `read`.
export async function listBroadcastHistory(
    page: number,
    pageSize: number,
): Promise<ApiResponse<PaginatedResponseData<BroadcastInboxItem>>> {
    return doGet<PaginatedResponseData<BroadcastInboxItem>>('/api/broadcast/history', {page, pageSize});
}
