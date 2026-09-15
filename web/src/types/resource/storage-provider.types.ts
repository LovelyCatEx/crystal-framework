/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import type {BaseEntity} from "../BaseEntity.ts";

export interface StorageProviderTypeDeclaration {
    typeId: number;
    key: string;
    displayName: string;
    description: string;
}

export interface StorageProvider extends BaseEntity {
    name: string;
    description: string | null;
    type: number;
    baseUrl: string;
    properties: string;
    active: boolean;
}
