import type {BaseEntity} from "./BaseEntity.ts";

export enum StorageProviderType {
    ALIYUN_OSS = 0,
    TENCENT_COS = 1
}

export interface StorageProvider extends BaseEntity {
    name: string;
    description: string | null;
    type: number;
    baseUrl: string;
    properties: string;
}
