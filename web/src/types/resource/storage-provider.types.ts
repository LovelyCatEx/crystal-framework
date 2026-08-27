import type {BaseEntity} from "../BaseEntity.ts";

export interface StorageProviderTypeDeclaration {
    typeId: number;
    key: string;
    displayName: string;
    description: string;
}

export interface StorageProvider extends BaseEntity {
    name: string;
    description: string | null;
    type: number;
    baseUrl: string;
    properties: string;
    active: boolean;
}
