/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import type {BaseEntity} from "@/types/BaseEntity.ts";

export interface ApprovalFlowEdge extends BaseEntity {
    definitionId: string;
    definitionVersion: number;
    sourceNodeId: string;
    targetNodeId: string;
}
