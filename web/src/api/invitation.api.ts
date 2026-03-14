import {BaseManagerController} from "./BaseManagerController.ts";
import type {BaseManagerDeleteDTO, BaseManagerUpdateDTO} from "../types/api.types.ts";
import type {Invitation, TenantInvitationVO} from "@/types/tenant-invitation.types.ts";
import {doGet, doPost} from "./system-request.ts";

export interface ManagerCreateInvitationDTO {
    tenantId: string;
    creatorMemberId: string;
    departmentId?: string;
    invitationCount: number;
    expiresTime?: string;
    requiresReviewing: boolean;
}

export interface ManagerUpdateInvitationDTO extends BaseManagerUpdateDTO {
    departmentId?: string;
    invitationCount?: number;
    expiresTime?: string;
    requiresReviewing?: boolean;
}

export interface ManagerReadInvitationDTO {
    tenantId: string;
    searchKeyword?: string;
    page: number;
    pageSize: number;
}

export interface ManagerDeleteInvitationDTO extends BaseManagerDeleteDTO {
    ids: string[];
}

export interface AcceptTenantInvitationDTO {
    invitationCode: string;
    realName: string;
    phoneNumber: string;
}

export function queryTenantInvitationByCode(code: string) {
    return doGet<TenantInvitationVO>("/api/tenant/invitation/query", { code });
}

export function acceptTenantInvitation(dto: AcceptTenantInvitationDTO) {
    return doPost<null>("/api/tenant/invitation/accept", dto);
}

class InvitationManagerControllerClass extends BaseManagerController<
    Invitation,
    ManagerCreateInvitationDTO,
    ManagerReadInvitationDTO,
    ManagerUpdateInvitationDTO,
    ManagerDeleteInvitationDTO
> {
    constructor() {
        super('/manager/tenant/invitation');
    }
}

export const InvitationManagerController = new InvitationManagerControllerClass();
