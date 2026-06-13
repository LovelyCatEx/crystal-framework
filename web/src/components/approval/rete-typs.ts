import { BaseGraphSocket } from "@/rete/socket/BaseGraphSocket";
import {ClassicPreset} from "rete";
import {BaseGraphNodeConnection} from "@/rete/types/connection.ts";
import type {BaseGraphSchemes} from "@/rete/types/schemes.ts";
import {BaseGraphControl} from "@/rete/control/BaseGraphControl.ts";
import {BaseGraphNode} from "@/rete/node/BaseGraphNode.ts";
import type {ApprovalFlowNode} from "@/types/approval/approval-flow-node.types.ts";
import type {GraphEditorContext} from "@/rete/rete-editor.tsx";

export type ApprovalFlowGraphSocket = ApprovalFlowGraphDefaultSocket

export class ApprovalFlowGraphDefaultSocket extends BaseGraphSocket {
    constructor() {
        super("Default");
    }

    isCompatibleWith(socket: ClassicPreset.Socket): boolean {
        return socket instanceof ApprovalFlowGraphDefaultSocket;
    }
}

export const approvalFlowGraphDefaultSocket = new ApprovalFlowGraphDefaultSocket()

export class BaseApprovalFlowGraphNodeControl extends BaseGraphControl<ApprovalFlowGraphSocket> {}

export class BaseApprovalFlowGraphNode extends BaseGraphNode<ApprovalFlowGraphSocket, BaseApprovalFlowGraphNodeControl> {
    public readonly node: ApprovalFlowNode;

    constructor(node: ApprovalFlowNode) {
        super(node.id);
        this.node = node;
    }
}

export class ApprovalFlowConnection extends BaseGraphNodeConnection<
    ApprovalFlowGraphSocket,
    BaseApprovalFlowGraphNodeControl,
    BaseApprovalFlowGraphNode,
    BaseApprovalFlowGraphNode
> {}

export type ApprovalFlowGraphSchemes = BaseGraphSchemes<
    ApprovalFlowGraphSocket,
    BaseApprovalFlowGraphNodeControl,
    BaseApprovalFlowGraphNode,
    ApprovalFlowConnection
>;

export interface ApprovalFlowGraphEditorContext extends GraphEditorContext<
    ApprovalFlowGraphSocket,
    BaseApprovalFlowGraphNodeControl,
    BaseApprovalFlowGraphNode,
    ApprovalFlowConnection,
    ApprovalFlowGraphSchemes
> {
    getNodeById: (nodeId: string) => BaseApprovalFlowGraphNode | undefined;
    deleteNodeById: (nodeId: string) => Promise<BaseApprovalFlowGraphNode | undefined>;
}