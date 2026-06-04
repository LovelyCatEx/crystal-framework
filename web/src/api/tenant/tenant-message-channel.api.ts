import {BaseManagerController} from "../BaseManagerController.ts";
import type {BaseManagerDeleteDTO, BaseManagerReadDTO, BaseManagerUpdateDTO} from "@/types/api.types.ts";
import type {TenantMessageChannel} from "@/types/tenant/tenant-message-channel.types.ts";

export interface ManagerCreateTenantMessageChannelDTO {
    tenantId: string;
    channelType: number;
    name: string;
    enabled: boolean;
    config: string;
}

export interface ManagerReadTenantMessageChannelDTO extends BaseManagerReadDTO {
    tenantId: string;
    channelType?: number;
}

export interface ManagerUpdateTenantMessageChannelDTO extends BaseManagerUpdateDTO {
    name?: string;
    enabled?: boolean;
    config?: string;
}

export interface ManagerDeleteTenantMessageChannelDTO extends BaseManagerDeleteDTO {
    ids: string[];
}

class TenantMessageChannelManagerControllerClass extends BaseManagerController<
    TenantMessageChannel,
    ManagerCreateTenantMessageChannelDTO,
    ManagerReadTenantMessageChannelDTO,
    ManagerUpdateTenantMessageChannelDTO,
    ManagerDeleteTenantMessageChannelDTO
> {
    constructor() {
        super('/manager/tenant/message-channel');
    }
}

export const TenantMessageChannelManagerController = new TenantMessageChannelManagerControllerClass();
