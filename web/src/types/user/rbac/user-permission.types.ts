/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import type {BaseEntity} from "../../BaseEntity.ts";

export enum PermissionType {
    ACTION = 0,
    MENU = 1,
    COMPONENT = 2
}

export interface UserPermission extends BaseEntity {
    name: string;
    type: number;
    description: string | null;
    path: string | null;
}