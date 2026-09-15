/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

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
