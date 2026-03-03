import {BaseManagerController} from "./BaseManagerController.ts";
import type {StorageProvider} from "../types/storage-provider.types.ts";
import type {BaseManagerReadDTO, BaseManagerUpdateDTO} from "../types/api.types.ts";

export const StorageProviderManagerController = new BaseManagerController<
    StorageProvider,
    ManagerCreateStorageProviderDTO,
    ManagerReadStorageProviderDTO,
    ManagerUpdateStorageProviderDTO
>('/manager/storage-provider');

export interface ManagerCreateStorageProviderDTO {
    name: string;
    description: string | null;
    type: number;
    baseUrl: string;
    properties: string;
}

export interface ManagerUpdateStorageProviderDTO extends BaseManagerUpdateDTO {
    name?: string | null;
    description?: string | null;
    type?: number | null;
    baseUrl?: string | null;
    properties?: string | null;
    active?: boolean | null;
}

export interface ManagerReadStorageProviderDTO extends BaseManagerReadDTO {
    type?: number | null;
}
