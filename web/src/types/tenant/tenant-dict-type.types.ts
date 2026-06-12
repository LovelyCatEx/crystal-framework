import type {BaseEntity} from "@/types/BaseEntity.ts";

export interface TenantDictType extends BaseEntity {
    scope: number;
    scopeId: string;
    code: string;
    name: string;
    remark: string | null;
    status: number;
}

export enum DictTypeStatus {
    DISABLED = 0,
    ENABLED = 1,
}
