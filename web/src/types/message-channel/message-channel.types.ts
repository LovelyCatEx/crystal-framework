/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import type {BaseEntity} from "@/types/BaseEntity.ts";

export enum ChannelType {
    EMAIL = 1,
    LARK = 2
}

export interface MessageChannel extends BaseEntity {
    scope: number;
    scopeId: string;
    channelType: number;
    name: string;
    enabled: boolean;
    config: string;
}
