import type {ReactElement} from "react";
import type {RenderEmit} from "rete-react-plugin";
import {
    ApprovalFlowConnection,
    type ApprovalFlowGraphEditorContext,
    type ApprovalFlowGraphSchemes,
    type BaseApprovalFlowGraphNode
} from "../rete-typs.ts";
import type {ApprovalFlowNode} from "@/types/approval/approval-flow-node.types.ts";
import type {ApprovalFlowEdge} from "@/types/approval/approval-flow-edge.types.ts";
import {createApprovalFlowNode} from "../approval-graph-nodes.ts";
import {ApprovalFlowNodeType} from "@/types/approval/approval-enums.ts";
import {StartNodeComponent} from "@/components/approval/node/StartNodeComponent.tsx";
import {EndNodeComponent} from "@/components/approval/node/EndNodeComponent.tsx";
import {ApprovalNodeComponent} from "@/components/approval/node/ApprovalNodeComponent.tsx";
import {ConditionNodeComponent} from "@/components/approval/node/ConditionNodeComponent.tsx";
import {CcNodeComponent} from "@/components/approval/node/CcNodeComponent.tsx";
import {ForkNodeComponent} from "@/components/approval/node/ForkNodeComponent.tsx";
import {JoinNodeComponent} from "@/components/approval/node/JoinNodeComponent.tsx";

export interface ApprovalFlowGraphSnapshot {
    nodes: ApprovalFlowNode[];
    edges: ApprovalFlowEdge[];
}

/**
 * Rebuild the rete editor to reflect the given graph snapshot.
 * Callers that need read-only mode should call ctx.enableReadonly() after this resolves.
 */
export async function renderApprovalFlow(
    ctx: ApprovalFlowGraphEditorContext,
    graph: ApprovalFlowGraphSnapshot
): Promise<void> {
    for (const conn of ctx.rete.editor.getConnections()) {
        await ctx.rete.editor.removeConnection(conn.id);
    }
    for (const node of ctx.rete.editor.getNodes()) {
        await ctx.rete.editor.removeNode(node.id);
    }

    // why: rete overwrites node.positionX/Y to 0 during addNode, snapshot beforehand
    const positionMap = new Map<string, { x: number; y: number }>();
    for (const node of graph.nodes) {
        positionMap.set(node.id, {x: node.positionX, y: node.positionY});
    }

    for (const node of graph.nodes) {
        await ctx.rete.editor.addNode(createApprovalFlowNode(node));
    }

    const reteNodes = ctx.rete.editor.getNodes();
    for (const reteNode of reteNodes) {
        const pos = positionMap.get(reteNode.node.id);
        if (!pos) continue;
        const view = ctx.rete.area.nodeViews.get(reteNode.id);
        if (view) {
            await view.translate(pos.x, pos.y);
        }
    }

    for (const edge of graph.edges) {
        const sourceNode = reteNodes.find(n => n.node.id === edge.sourceNodeId);
        const targetNode = reteNodes.find(n => n.node.id === edge.targetNodeId);
        if (sourceNode && targetNode) {
            await ctx.rete.editor.addConnection(
                new ApprovalFlowConnection(sourceNode, 'out', targetNode, 'in')
            );
        }
    }

    ctx.autoFitViewport();
}

/**
 * Dispatches to the correct node component by node.type. Callers wrap the
 * returned element in whatever context providers they need (editor context,
 * node-status context, etc.) — this function stays presentation-only.
 */
export function renderApprovalFlowNode(
    node: BaseApprovalFlowGraphNode,
    emit: RenderEmit<ApprovalFlowGraphSchemes>
): ReactElement {
    switch (node.node.type) {
        case ApprovalFlowNodeType.START:
            return <StartNodeComponent data={node} emit={emit}/>;
        case ApprovalFlowNodeType.END:
            return <EndNodeComponent data={node} emit={emit}/>;
        case ApprovalFlowNodeType.CONDITION:
            return <ConditionNodeComponent data={node} emit={emit}/>;
        case ApprovalFlowNodeType.CC:
            return <CcNodeComponent data={node} emit={emit}/>;
        case ApprovalFlowNodeType.FORK:
            return <ForkNodeComponent data={node} emit={emit}/>;
        case ApprovalFlowNodeType.JOIN:
            return <JoinNodeComponent data={node} emit={emit}/>;
        default:
            return <ApprovalNodeComponent data={node} emit={emit}/>;
    }
}
