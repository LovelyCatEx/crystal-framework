import {BaseManagerController} from "../BaseManagerController.ts";
import type {BaseManagerDeleteDTO, BaseManagerReadScopedDTO, BaseManagerUpdateDTO} from "@/types/api.types.ts";
import type {Broadcast} from "@/types/message/broadcast.types.ts";

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
