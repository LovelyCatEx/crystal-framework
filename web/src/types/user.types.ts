import type {BaseEntity} from "./BaseEntity.ts";

export interface UserProfileVO extends BaseEntity {
    nickname: string;
    username: string | null;
    email: string | null;
    registeredTime: string | null;
}

export interface User extends BaseEntity {
    username: string;
    email: string;
    nickname: string;
}
