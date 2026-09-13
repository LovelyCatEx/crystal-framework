/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {BaseManagerController} from "@/api/BaseManagerController.ts";
import type {AiProviderEntity, DefaultProviderConfigsVO} from "@/types/ai/ai.types.ts";
import type {BaseManagerReadDTO, BaseManagerUpdateDTO} from "@/types/api.types.ts";
import {doGet} from "@/api/system-request.ts";

export interface ManagerCreateAiProviderDTO {
    name: string;
    key: string;
    description: string | null;
    protocolType: number;
    baseUrl: string;
    apiKey: string;
    requestConfig: string;
    responseConfig: string;
    enabled: boolean;
    sort: number;
}

export interface ManagerUpdateAiProviderDTO extends BaseManagerUpdateDTO {
    name?: string | null;
    key?: string | null;
    description?: string | null;
    protocolType?: number | null;
    baseUrl?: string | null;
    apiKey?: string | null;
    requestConfig?: string | null;
    responseConfig?: string | null;
    enabled?: boolean | null;
    sort?: number | null;
}

export interface ManagerReadAiProviderDTO extends BaseManagerReadDTO {
    // All filtering is done via query: GroupNode
}

export const AiProviderManagerController = new BaseManagerController<
    AiProviderEntity,
    ManagerCreateAiProviderDTO,
    ManagerReadAiProviderDTO,
    ManagerUpdateAiProviderDTO
>('/manager/ai-provider');

export const getDefaultProviderConfigs = async () => {
    return doGet<DefaultProviderConfigsVO>('/api/manager/ai-provider/default-configs');
};
