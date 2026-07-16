import {BaseManagerController} from "../BaseManagerController.ts";
import type {BaseManagerDeleteDTO, BaseManagerReadScopedDTO, BaseManagerUpdateDTO} from "@/types/api.types.ts";
import type {MessageChannel} from "@/types/message-channel/message-channel.types.ts";

export interface ManagerCreateMessageChannelDTO {
    scope: number;
    scopeId: string;
    channelType: number;
    name: string;
    enabled: boolean;
    config: string;
}

export interface ManagerReadMessageChannelDTO extends BaseManagerReadScopedDTO {
    channelType?: number;
}

export interface ManagerUpdateMessageChannelDTO extends BaseManagerUpdateDTO {
    name?: string;
    enabled?: boolean;
    config?: string;
}

export interface ManagerDeleteMessageChannelDTO extends BaseManagerDeleteDTO {
    ids: string[];
}

class MessageChannelManagerControllerClass extends BaseManagerController<
    MessageChannel,
    ManagerCreateMessageChannelDTO,
    ManagerReadMessageChannelDTO,
    ManagerUpdateMessageChannelDTO,
    ManagerDeleteMessageChannelDTO
> {
    constructor() {
        super('/manager/message-channel');
    }
}

export const MessageChannelManagerController = new MessageChannelManagerControllerClass();
