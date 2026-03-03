import type {BaseEntity} from "./BaseEntity.ts";

export enum ResourceFileType {
    USER_AVATAR = 0
}

export interface FileResource extends BaseEntity {
    userId: string;
    type: number;
    fileName: string;
    fileExtension: string;
    md5: string;
    fileSize: string;
    storageProviderId: string;
    objectKey: string;
}
