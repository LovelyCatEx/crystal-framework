import {
    BaseApprovalFlowGraphNode,
    approvalFlowBaseInSocket,
    approvalFlowBaseOutSocket,
    approvalFlowMultiInSocket,
    approvalFlowMultiOutSocket
} from "./rete-typs.ts";
import type {ApprovalFlowNode} from "@/types/approval/approval-flow-node.types.ts";
import {ApprovalFlowNodeType} from "@/types/approval/approval-enums.ts";

/** Start node: only has output socket */
export class StartNode extends BaseApprovalFlowGraphNode {
    constructor(node: ApprovalFlowNode) {
        super(node);
        this.addOutputSocket('out', approvalFlowBaseOutSocket, 'out');
    }
}

/** End node: only has input socket */
export class EndNode extends BaseApprovalFlowGraphNode {
    constructor(node: ApprovalFlowNode) {
        super(node);
        this.addInputSocket('in', approvalFlowBaseInSocket, 'in');
    }
}

/** Approval node: single in, single out */
export class ApprovalNode extends BaseApprovalFlowGraphNode {
    constructor(node: ApprovalFlowNode) {
        super(node);
        this.addInputSocket('in', approvalFlowBaseInSocket, 'in');
        this.addOutputSocket('out', approvalFlowBaseOutSocket, 'out');
    }
}

/** Condition node: single in, multiple out */
export class ConditionNode extends BaseApprovalFlowGraphNode {
    constructor(node: ApprovalFlowNode) {
        super(node);
        this.addInputSocket('in', approvalFlowBaseInSocket, 'in');
        this.addOutputSocket('out', approvalFlowMultiOutSocket, 'out');
    }
}

/** CC node: single in, single out */
export class CcNode extends BaseApprovalFlowGraphNode {
    constructor(node: ApprovalFlowNode) {
        super(node);
        this.addInputSocket('in', approvalFlowBaseInSocket, 'in');
        this.addOutputSocket('out', approvalFlowBaseOutSocket, 'out');
    }
}

/** Fork node: single in, multiple out (parallel split) */
export class ForkNode extends BaseApprovalFlowGraphNode {
    constructor(node: ApprovalFlowNode) {
        super(node);
        this.addInputSocket('in', approvalFlowBaseInSocket, 'in');
        this.addOutputSocket('out', approvalFlowMultiOutSocket, 'out');
    }
}

/** Join node: multiple in, single out (parallel merge) */
export class JoinNode extends BaseApprovalFlowGraphNode {
    constructor(node: ApprovalFlowNode) {
        super(node);
        this.addInputSocket('in', approvalFlowMultiInSocket, 'in');
        this.addOutputSocket('out', approvalFlowBaseOutSocket, 'out');
    }
}

/** Factory: create the correct node class based on type */
export function createApprovalFlowNode(node: ApprovalFlowNode): BaseApprovalFlowGraphNode {
    switch (node.type) {
        case ApprovalFlowNodeType.START:
            return new StartNode(node);
        case ApprovalFlowNodeType.END:
            return new EndNode(node);
        case ApprovalFlowNodeType.APPROVAL:
            return new ApprovalNode(node);
        case ApprovalFlowNodeType.CONDITION:
            return new ConditionNode(node);
        case ApprovalFlowNodeType.CC:
            return new CcNode(node);
        case ApprovalFlowNodeType.FORK:
            return new ForkNode(node);
        case ApprovalFlowNodeType.JOIN:
            return new JoinNode(node);
        default:
            return new ApprovalNode(node);
    }
}
