import type {BaseScopedEntity} from "../BaseScopedEntity.ts";

export enum ResourceFileType {
    USER_AVATAR = 0,
    TENANT_ICON = 1,
    TENANT_MEMBER_AVATAR = 2,
}

export enum FileResourceStatus {
    UPLOADING = 0,
    COMMITTED = 1,
    CLEANUP_PENDING = 2,
}

export interface FileResource extends BaseScopedEntity {
    userId: string;
    type: number;
    fileName: string;
    fileExtension: string;
    md5: string;
    fileSize: string;
    storageProviderId: string;
    objectKey: string;
    status: number;
}
