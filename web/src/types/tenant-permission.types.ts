import type {BaseEntity} from "@/types/BaseEntity.ts";

export interface TenantPermission extends BaseEntity{
    name: string;
    description: string | null;
    type: number;
    path: string | null;
    preserved1: number | null;
    preserved2: number | null;
}