/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import type {BaseEntity} from "@/types/BaseEntity.ts";

export interface TenantDictItem extends BaseEntity {
    typeId: string;
    itemCode: string;
    itemValue: string;
    parentId: string | null;
    sortOrder: number;
    isDefault: boolean;
    status: number;
}

export interface TenantDictItemTreeNode {
    id: string;
    itemCode: string;
    itemValue: string;
    parentId: string | null;
    sortOrder: number;
    isDefault: boolean;
    status: number;
    createdTime: string;
    modifiedTime: string;
    children: TenantDictItemTreeNode[];
}

export enum DictItemStatus {
    DISABLED = 0,
    ENABLED = 1,
}
