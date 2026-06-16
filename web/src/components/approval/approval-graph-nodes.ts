import {BaseApprovalFlowGraphNode, approvalFlowGraphDefaultSocket} from "./rete-typs.ts";
import type {ApprovalFlowNode} from "@/types/approval/approval-flow-node.types.ts";
import {ApprovalFlowNodeType} from "@/types/approval/approval-enums.ts";

/** Start node: only has output socket */
export class StartNode extends BaseApprovalFlowGraphNode {
    constructor(node: ApprovalFlowNode) {
        super(node);
        this.addOutputSocket('out', approvalFlowGraphDefaultSocket, 'out');
    }
}

/** End node: only has input socket */
export class EndNode extends BaseApprovalFlowGraphNode {
    constructor(node: ApprovalFlowNode) {
        super(node);
        this.addInputSocket('in', approvalFlowGraphDefaultSocket, 'in');
    }
}

/** Approval node: has input and output */
export class ApprovalNode extends BaseApprovalFlowGraphNode {
    constructor(node: ApprovalFlowNode) {
        super(node);
        this.addInputSocket('in', approvalFlowGraphDefaultSocket, 'in');
        this.addOutputSocket('out', approvalFlowGraphDefaultSocket, 'out');
    }
}

/** Condition node: has input and output */
export class ConditionNode extends BaseApprovalFlowGraphNode {
    constructor(node: ApprovalFlowNode) {
        super(node);
        this.addInputSocket('in', approvalFlowGraphDefaultSocket, 'in');
        this.addOutputSocket('out', approvalFlowGraphDefaultSocket, 'out');
    }
}

/** CC node: has input and output */
export class CcNode extends BaseApprovalFlowGraphNode {
    constructor(node: ApprovalFlowNode) {
        super(node);
        this.addInputSocket('in', approvalFlowGraphDefaultSocket, 'in');
        this.addOutputSocket('out', approvalFlowGraphDefaultSocket, 'out');
    }
}

/** Fork node: has input and multiple outputs (parallel split) */
export class ForkNode extends BaseApprovalFlowGraphNode {
    constructor(node: ApprovalFlowNode) {
        super(node);
        this.addInputSocket('in', approvalFlowGraphDefaultSocket, 'in');
        this.addOutputSocket('out', approvalFlowGraphDefaultSocket, 'out');
    }
}

/** Join node: has multiple inputs and single output (parallel merge) */
export class JoinNode extends BaseApprovalFlowGraphNode {
    constructor(node: ApprovalFlowNode) {
        super(node);
        this.addInputSocket('in', approvalFlowGraphDefaultSocket, 'in');
        this.addOutputSocket('out', approvalFlowGraphDefaultSocket, 'out');
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
