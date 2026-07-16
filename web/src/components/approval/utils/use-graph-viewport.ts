import {type RefObject, useEffect, useState} from "react";
import type {GraphEditorContext} from "@/rete/rete-editor.tsx";
import type {
    ApprovalFlowConnection,
    ApprovalFlowGraphSchemes,
    ApprovalFlowGraphSocket,
    BaseApprovalFlowGraphNode,
    BaseApprovalFlowGraphNodeControl,
} from "../rete-typs.ts";

export interface GraphViewportState {
    mousePos: { x: number; y: number } | null;
    zoom: number;
}

/**
 * Tracks the graph-local mouse position (accounting for pan/zoom) and the current
 * zoom factor of the rete area. The two are bundled because both consumers render
 * them together in the bottom status pill.
 *
 * Accepts the base GraphEditorContext (not the augmented ApprovalFlowGraphEditorContext)
 * so callers can pass the raw `baseCtx` from useRete before `getNodeById` / `deleteNodeById`
 * are attached — this hook only needs `rete.area`.
 */
export function useGraphViewport(
    ctx: GraphEditorContext<
        ApprovalFlowGraphSocket,
        BaseApprovalFlowGraphNodeControl,
        BaseApprovalFlowGraphNode,
        ApprovalFlowConnection,
        ApprovalFlowGraphSchemes
    > | null,
    containerRef: RefObject<HTMLDivElement | null>
): GraphViewportState {
    const [mousePos, setMousePos] = useState<{ x: number; y: number } | null>(null);
    const [zoom, setZoom] = useState(1);

    useEffect(() => {
        if (!ctx) return;
        const el = containerRef.current;
        if (!el) return;
        const onMove = (e: MouseEvent) => {
            const rect = el.getBoundingClientRect();
            const {x, y, k} = ctx.rete.area.area.transform;
            setMousePos({
                x: Math.round((e.clientX - rect.left - x) / k),
                y: Math.round((e.clientY - rect.top - y) / k),
            });
        };
        el.addEventListener('mousemove', onMove);
        return () => el.removeEventListener('mousemove', onMove);
    }, [ctx, containerRef]);

    useEffect(() => {
        if (!ctx) return;
        const area = ctx.rete.area;
        const update = () => setZoom(area.area.transform.k);
        update();
        area.signal.addPipe((context) => {
            update();
            return context;
        });
    }, [ctx]);

    return {mousePos, zoom};
}
