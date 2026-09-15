/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

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
>('/manager/announcement');

export async function getPublishedAnnouncements() {
    return doGet<Announcement[]>('/api/announcements/list');
}
