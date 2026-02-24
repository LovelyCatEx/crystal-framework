import type {BaseEntity} from "./BaseEntity.ts";

export interface UserRole extends BaseEntity {
    name: string;
    description: string | null;
}
