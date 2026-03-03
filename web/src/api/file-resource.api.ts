import {BaseManagerController} from "./BaseManagerController.ts";
import type {FileResource} from "../types/file-resource.types.ts";
import type {BaseManagerReadDTO, BaseManagerUpdateDTO} from "../types/api.types.ts";
import {doGet} from "./system-request.ts";

export const FileResourceManagerController = new BaseManagerController<
    FileResource,
    ManagerCreateFileResourceDTO,
    ManagerReadFileResourceDTO,
    ManagerUpdateFileResourceDTO
>('/manager/file-resource');

export interface ManagerCreateFileResourceDTO {
    userId: string;
    type: number;
    fileName: string;
    fileExtension: string;
    md5: string;
    fileSize: number;
    storageProviderId: string;
    objectKey: string;
}

export interface ManagerUpdateFileResourceDTO extends BaseManagerUpdateDTO {
    userId?: string | null;
    type?: number | null;
    fileName?: string | null;
    fileExtension?: string | null;
    md5?: string | null;
    fileSize?: number | null;
    storageProviderId?: string | null;
    objectKey?: string | null;
}

export interface ManagerReadFileResourceDTO extends BaseManagerReadDTO {
    type?: number | null;
}

export function managerGetFileDownloadUrl(fileEntityId: string) {
    return doGet<string | null>('/api/manager/file-resource/downloadUrl', { id: fileEntityId });
}