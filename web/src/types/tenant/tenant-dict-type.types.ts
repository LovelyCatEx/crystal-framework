/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import type {BaseEntity} from "@/types/BaseEntity.ts";

export interface TenantDictType extends BaseEntity {
    scope: number;
    scopeId: string;
    code: string;
    name: string;
    remark: string | null;
    status: number;
}

export enum DictTypeStatus {
    DISABLED = 0,
    ENABLED = 1,
}
