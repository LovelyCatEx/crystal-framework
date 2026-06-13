import type {BaseEntity} from "./BaseEntity.ts";

/** Resource scope — corresponds to backend ResourceScope.kt */
export enum ResourceScope {
    SYSTEM = 0,
    TENANT = 1,
}

export interface BaseScopedEntity extends BaseEntity {
    scope: number;
    scopeId: string;
}
