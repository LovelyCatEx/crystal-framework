import type { BaseEntity } from '../BaseEntity.ts';

export interface Announcement extends BaseEntity {
    title: string;
    content: string;
    /** 0=draft, 1=published, 2=offline */
    status: number;
    /** 0=user-side only, 1=manager-side only, 2=both */
    target: number;
    priority: number;
}

export const AnnouncementStatus = {
    DRAFT: 0,
    PUBLISHED: 1,
    OFFLINE: 2,
} as const;

export const AnnouncementTarget = {
    USER_ONLY: 0,
    MANAGER_ONLY: 1,
    BOTH: 2,
} as const;

export const ANNOUNCEMENT_STATUS_COLORS: Record<number, string> = {
    [AnnouncementStatus.DRAFT]: 'default',
    [AnnouncementStatus.PUBLISHED]: 'success',
    [AnnouncementStatus.OFFLINE]: 'warning',
};

export const ANNOUNCEMENT_TARGET_COLORS: Record<number, string> = {
    [AnnouncementTarget.USER_ONLY]: 'blue',
    [AnnouncementTarget.MANAGER_ONLY]: 'purple',
    [AnnouncementTarget.BOTH]: 'cyan',
};
