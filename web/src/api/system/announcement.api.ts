import { BaseManagerController } from '../BaseManagerController.ts';
import type { Announcement } from '@/types/system/announcement.types.ts';
import type { BaseManagerReadDTO, BaseManagerUpdateDTO } from '@/types/api.types.ts';
import { doGet } from '../system-request.ts';

export interface ManagerCreateAnnouncementDTO {
    title: string;
    content: string;
    status: number;
    target: number;
    priority: number;
}

export interface ManagerReadAnnouncementDTO extends BaseManagerReadDTO {
    status?: number;
    target?: number;
}

export interface ManagerUpdateAnnouncementDTO extends BaseManagerUpdateDTO {
    title?: string;
    content?: string;
    status?: number;
    target?: number;
    priority?: number;
}

export const AnnouncementManagerController = new BaseManagerController<
    Announcement,
    ManagerCreateAnnouncementDTO,
    ManagerReadAnnouncementDTO,
    ManagerUpdateAnnouncementDTO
>('/manager/announcements');

export async function getAnnouncementsForUser() {
    return doGet<Announcement[]>('/api/announcements/user/list');
}

export async function getAnnouncementsForManager() {
    return doGet<Announcement[]>('/api/announcements/manager/list');
}
