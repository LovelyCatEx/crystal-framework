import {BaseManagerController} from "./BaseManagerController.ts";
import type {User, UserProfileVO} from "../types/user.types.ts";
import type {BaseManagerReadDTO, BaseManagerUpdateDTO} from "../types/api.types.ts";
import {doGet, doPost} from "./system-request.ts";

export const UserManagerController = new BaseManagerController<
    User,
    ManagerCreateUserDTO,
    BaseManagerReadDTO,
    ManagerUpdateUserDTO
>('/manager/user');

export interface ManagerCreateUserDTO {
    username: string;
    password: string;
    email: string;
    nickname: string;
}

export interface ManagerUpdateUserDTO extends BaseManagerUpdateDTO {
    email?: string | null;
    nickname?: string | null;
}

export async function getUserProfile() {
    return doGet<UserProfileVO>('/api/user/profile');
}

export async function getUserAccessibleMenus() {
    return doGet<string[]>('/api/user/menus/list');
}

export async function uploadUserAvatar(file: File) {
    const formData = new FormData();
    formData.append('file', file);

    return doPost('/api/user/uploadAvatar', {file: file}, {'Content-Type': 'multipart/form-data'});
}

export async function updateUserProfile(dto: UpdateUserProfileDTO) {
    return doPost('/api/user/profile', {...dto})
}

export interface UpdateUserProfileDTO {
    nickname?: string | null;
}