import type {BaseEntity} from "./BaseEntity.ts";

export enum StorageProviderType {
    LOCAL_FILE_SYSTEM = 0,
    ALIYUN_OSS = 1,
    TENCENT_COS = 2
}

export interface StorageProvider extends BaseEntity {
    name: string;
    description: string | null;
    type: number;
    baseUrl: string;
    properties: string;
    active: boolean;
}
