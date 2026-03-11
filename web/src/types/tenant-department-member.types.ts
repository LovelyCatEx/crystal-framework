import type {BaseEntity} from "@/types/BaseEntity.ts";
import type {TenantMemberVO} from "@/types/tenant-member.types.ts";

export interface TenantDepartmentMemberVO extends BaseEntity {
    member: TenantMemberVO;
    roleType: number;
}