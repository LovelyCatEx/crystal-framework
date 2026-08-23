import type {BaseScopedEntity} from "../BaseScopedEntity.ts";

export enum ResourceVisibility {
    PUBLIC = 'PUBLIC',
    AUTHENTICATED = 'AUTHENTICATED',
    OWNER_ONLY = 'OWNER_ONLY',
    SCOPE_MEMBER = 'SCOPE_MEMBER',
}

export interface ResourceFileTypeDeclaration {
    typeId: number;
    key: string;
    displayName: string;
    description: string;
    supportedContentTypes: string[];
    supportedFileExtensions: string[];
    defaultVisibility: ResourceVisibility;
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
