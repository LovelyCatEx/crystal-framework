import type {BaseEntity} from "@/types/BaseEntity.ts";

export interface TenantDepartment extends BaseEntity {
    tenantId: string;
    name: string;
    description: string | null;
    parentId: string | null;
}