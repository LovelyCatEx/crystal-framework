/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

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
