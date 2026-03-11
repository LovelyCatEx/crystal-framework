import type {BaseEntity} from "@/types/BaseEntity.ts";

export interface TenantRole extends BaseEntity {
    tenantId: string;
    name: string;
    description: string | null;
    parentId: string | null;
}