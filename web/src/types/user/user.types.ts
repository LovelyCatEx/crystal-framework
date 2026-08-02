import type {BaseEntity} from "../BaseEntity.ts";

export interface UserProfileVO extends BaseEntity {
    nickname: string;
    avatar: string | null;
    username: string | null;
    email: string | null;
    registeredTime: string | null;
}

export interface User extends BaseEntity {
    username: string;
    email: string;
    nickname: string;
    avatar: string | null;
    /**
     * Backend UserEntity.getEnabledFlag() (isEnabled() is @JsonIgnore-d, so the
     * serialized property name is `enabledFlag`, not `enabled`).
     */
    enabledFlag: boolean;
    /**
     * Derived, non-persistent flag from UserEntity.getBanned(): true when the user currently has an
     * effective ban. Only populated on the manager user list (ManagerUserController query); the ban /
     * unban row action switches on it.
     */
    banned: boolean;
}

export interface UserAccessibleResourceVO {
    menus: string[];
    components: string[];
}