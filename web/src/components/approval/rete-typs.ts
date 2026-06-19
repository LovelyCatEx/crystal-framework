import { BaseGraphSocket } from "@/rete/socket/BaseGraphSocket";
import {ClassicPreset} from "rete";
import {BaseGraphNodeConnection} from "@/rete/types/connection.ts";
import type {BaseGraphSchemes} from "@/rete/types/schemes.ts";
import {BaseGraphControl} from "@/rete/control/BaseGraphControl.ts";
import {BaseGraphNode} from "@/rete/node/BaseGraphNode.ts";
import type {ApprovalFlowNode} from "@/types/approval/approval-flow-node.types.ts";
import type {GraphEditorContext} from "@/rete/rete-editor.tsx";

export type ApprovalFlowGraphSocket =
    | ApprovalFlowBaseInSocket
    | ApprovalFlowBaseOutSocket
    | ApprovalFlowMultiInSocket
    | ApprovalFlowMultiOutSocket

export class ApprovalFlowBaseInSocket extends BaseGraphSocket {
    constructor() {
        super("BaseIn");
    }

    isCompatibleWith(socket: ClassicPreset.Socket): boolean {
        return socket instanceof ApprovalFlowBaseOutSocket || socket instanceof ApprovalFlowMultiOutSocket;
    }
}

export class ApprovalFlowBaseOutSocket extends BaseGraphSocket {
    constructor() {
        super("BaseOut");
    }

    isCompatibleWith(socket: ClassicPreset.Socket): boolean {
        return socket instanceof ApprovalFlowBaseInSocket || socket instanceof ApprovalFlowMultiInSocket;
    }
}

export class ApprovalFlowMultiInSocket extends BaseGraphSocket {
    constructor() {
        super("MultiIn", true);
    }

    isCompatibleWith(socket: ClassicPreset.Socket): boolean {
        return socket instanceof ApprovalFlowBaseOutSocket || socket instanceof ApprovalFlowMultiOutSocket;
    }
}

export class ApprovalFlowMultiOutSocket extends BaseGraphSocket {
    constructor() {
        super("MultiOut", true);
    }

    isCompatibleWith(socket: ClassicPreset.Socket): boolean {
        return socket instanceof ApprovalFlowBaseInSocket || socket instanceof ApprovalFlowMultiInSocket;
    }
}

export const approvalFlowBaseInSocket = new ApprovalFlowBaseInSocket()
export const approvalFlowBaseOutSocket = new ApprovalFlowBaseOutSocket()
export const approvalFlowMultiInSocket = new ApprovalFlowMultiInSocket()
export const approvalFlowMultiOutSocket = new ApprovalFlowMultiOutSocket()

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