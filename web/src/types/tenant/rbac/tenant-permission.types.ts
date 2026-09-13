/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import type {BaseEntity} from "@/types/BaseEntity.ts";

export interface TenantPermission extends BaseEntity {
    name: string;
    description: string | null;
    type: number;
    path: string | null;
}

export enum TenantPermissionType {
    ACTION = 0,
    MENU = 1
}