/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import type {BaseScopedEntity} from "@/types/BaseScopedEntity.ts";
import type {ApprovalFlowNode} from "@/types/approval/approval-flow-node.types.ts";
import type {ApprovalFlowEdge} from "@/types/approval/approval-flow-edge.types.ts";

export {ApprovalFlowDefinitionStatus, ApprovalFlowScope, ResourceScope} from "./approval-enums.ts";

export interface ApprovalFlowDefinition extends BaseScopedEntity {
    name: string;
    description: string | null;
    currentVersion: number;
    status: number;
    formSchema: string | null;
}

export interface ApprovalFlowDefinitionDetailsVO {
    definition: ApprovalFlowDefinition;
    nodes: ApprovalFlowNode[];
    edges: ApprovalFlowEdge[];
}