import type {BaseEntity} from "../BaseEntity.ts";
import type {BaseManagerReadDTO} from "../api.types.ts";

/**
 * Corresponds to backend UserBanRecordEntity.
 * All Long fields are serialized to string via ToStringSerializer.
 */
export interface UserBanRecord extends BaseEntity {
    userId: string;
    reason: string;
    /** Millisecond timestamp string; null means permanent ban. */
    banUntil: string | null;
    /** Millisecond timestamp string; null means not yet lifted. */
    liftedTime: string | null;
    operatorUserId: string;
}

export interface ManagerReadUserBanRecordDTO extends BaseManagerReadDTO {
    userId?: string;
    operatorUserId?: string;
}
