import type {BaseEntity} from "./BaseEntity.ts";

export interface UserRolePermissionRelation extends BaseEntity {
    roleId: string;
    permissionId: string;
}
