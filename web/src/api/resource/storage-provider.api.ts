import {BaseManagerController} from "../BaseManagerController.ts";
import type {StorageProvider, StorageProviderTypeDeclaration} from "@/types/resource/storage-provider.types.ts";
import type {BaseManagerReadDTO, BaseManagerUpdateDTO} from "@/types/api.types.ts";
import {doGet} from "@/api/system-request.ts";

export const StorageProviderManagerController = new BaseManagerController<
    StorageProvider,
    ManagerCreateStorageProviderDTO,
    ManagerReadStorageProviderDTO,
    ManagerUpdateStorageProviderDTO
>('/manager/storage-provider');

export function getStorageProviderTypes(): Promise<StorageProviderTypeDeclaration[]> {
    return doGet<StorageProviderTypeDeclaration[]>('/api/manager/storage-provider/types')
        .then(r => r.data ?? []);
}

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
