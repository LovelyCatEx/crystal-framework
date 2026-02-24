import type {BaseEntity} from "./BaseEntity.ts";

export interface UserRoleRelation extends BaseEntity {
    userId: string;
    roleId: string;
}
