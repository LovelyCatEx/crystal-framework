import type {BaseEntity} from "./BaseEntity.ts";

export interface Tenant extends BaseEntity {
    ownerUserId: string;
    name: string;
    description: string | null;
    icon: string | null;
    status: number;
    tireTypeId: string;
    subscribedTime: string;
    expiresTime: string;
    contactName: string;
    settings: string | null;
    contactEmail: string;
    contactPhone: string;
    address: string;
}

export interface TenantTireType extends BaseEntity {
    name: string;
    description: string | null;
}

export enum TenantStatus {
    REVIEWING = 0,
    ACTIVE = 1,
    CLOSED = 2
}

export const TenantStatusMap: Record<number, { label: string; color: string }> = {
    [TenantStatus.REVIEWING]: { label: '审核中', color: 'orange' },
    [TenantStatus.ACTIVE]: { label: '活跃', color: 'green' },
    [TenantStatus.CLOSED]: { label: '已关闭', color: 'red' }
};

export interface UserTenantVO {
    tenantId: string;
    tenantName: string;
    tenantAvatar: string | null;
    authenticated: boolean;
}

export interface TenantProfileVO {
    tenantId: string;
    ownerUserId: string | null;
    name: string;
    description: string | null;
    icon: string | null;
    status: number;
    tireTypeId: string | null;
    subscribedTime: string | null;
    expiresTime: string | null;
    contactName: string | null;
    contactEmail: string | null;
    contactPhone: string | null;
    address: string;
    createdTime: string;
    modifiedTime: string;
}