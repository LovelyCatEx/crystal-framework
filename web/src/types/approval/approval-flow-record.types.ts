/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import type {BaseScopedEntity} from "@/types/BaseScopedEntity.ts";

export interface ApprovalFlowRecord extends BaseScopedEntity {
    instanceId: string;
    nodeId: string;
    operatorId: string;
    action: number;
    comment: string | null;
}
