import {useSWRState} from "@/compositions/use-swr.ts";
import {getApprovalFlowInstanceDetails} from "@/api/approval/approval-flow-instance.api.ts";
import {useCreateReteBaseGraphEditor} from "@/rete/rete-editor.tsx";
import {useRete} from "rete-react-plugin";
import {
    ApprovalFlowConnection,
    type ApprovalFlowGraphEditorContext,
    type ApprovalFlowGraphSchemes,
    type ApprovalFlowGraphSocket,
    BaseApprovalFlowGraphNode,
    BaseApprovalFlowGraphNodeControl
} from "../rete-typs.ts";
import {type ReactNode, useEffect, useMemo, useRef, useState} from "react";
import {applyApprovalFlowEditorAreaBackground} from "@/components/approval/background.ts";
import {Empty, Spin, Tag, theme, Typography} from "antd";
import {ApprovalEditorContext, type ApprovalEditorContextValue} from "../ApprovalEditorContext.tsx";
import {
    ApprovalNodeStatusProvider,
    type ApprovalNodeStatusContextValue
} from "./ApprovalNodeStatusContext.tsx";
import {ApprovalNodeRecordsPanel} from "./ApprovalNodeRecordsPanel.tsx";
import {ApprovalFlowInstanceStatus} from "@/types/approval/approval-enums.ts";
import {ResourceScope} from "@/types/BaseScopedEntity.ts";
import {getApprovalFlowInstanceStatus} from "@/i18n/enum-helpers.ts";
import {useTranslation} from "react-i18next";
import {renderApprovalFlow, renderApprovalFlowNode} from "../utils/approval-graph-render.tsx";
import {useResizablePanel} from "../utils/use-resizable-panel.ts";
import {useGraphViewport} from "../utils/use-graph-viewport.ts";

function getInstanceStatusColor(status: number): string | undefined {
    switch (status) {
        case ApprovalFlowInstanceStatus.APPROVED:
            return 'green';
        case ApprovalFlowInstanceStatus.REJECTED:
            return 'red';
        case ApprovalFlowInstanceStatus.IN_PROGRESS:
            return 'blue';
        case ApprovalFlowInstanceStatus.CANCELLED:
        default:
            return undefined;
    }
}

export default function ApprovalFlowViewer(props: {
    instanceId: string;
    headerLeft?: () => ReactNode;
    headerRight?: { left?: () => ReactNode; right?: () => ReactNode };
}) {
    const {token} = theme.useToken();
    const {t} = useTranslation();

    const [details, , isLoading] = useSWRState(
        `approval-instance-details:${props.instanceId}`,
        () => getApprovalFlowInstanceDetails(props.instanceId)
    );

    const [ctx, setCtx] = useState<ApprovalFlowGraphEditorContext | null>(null);
    const [selectedNodeId, setSelectedNodeId] = useState<string | null>(null);

    // Editor context (scope / scopeId) — ref-updated so rete internals don't stale-read
    const editorContextRef = useRef<ApprovalEditorContextValue>({
        scope: ResourceScope.SYSTEM,
        scopeId: '',
    });
    editorContextRef.current = {
        scope: details?.instance.scope ?? ResourceScope.SYSTEM,
        scopeId: details?.instance.scopeId ?? '',
    };

    // Node status context — ref-updated for the same reason. useMemo the value object
    // so identity is stable across renders except when nodeStates change.
    const statusContextValue = useMemo<ApprovalNodeStatusContextValue>(() => ({
        getNodeStateById: (nodeId: string) => details?.nodeStates[nodeId] ?? null,
    }), [details]);
    const statusContextRef = useRef<ApprovalNodeStatusContextValue>(statusContextValue);
    statusContextRef.current = statusContextValue;

    const {width: panelWidth, handleResizeStart} = useResizablePanel(20);

    const [ref, baseCtx] = useRete(
        useCreateReteBaseGraphEditor<
            ApprovalFlowGraphSocket,
            BaseApprovalFlowGraphNodeControl,
            BaseApprovalFlowGraphNode,
            ApprovalFlowConnection,
            ApprovalFlowGraphSchemes
        >({
            canMakeConnection(): boolean {
                return false;
            },
            connectionFactory(fromNode, fromSocket, toNode, toSocket): ApprovalFlowGraphSchemes["Connection"] {
                return new ApprovalFlowConnection(fromNode, fromSocket, toNode, toSocket);
            },
            events: {
                onNodeSelected: (node) => {
                    setSelectedNodeId(node.node.id || null);
                },
                onSelectedNodesChanged: (nodes) => {
                    if (nodes.length === 0) {
                        setSelectedNodeId(null);
                    }
                },
            },
            render: {
                node: (_, node, emit) => (
                    <ApprovalEditorContext.Provider value={editorContextRef.current}>
                        <ApprovalNodeStatusProvider value={statusContextRef.current}>
                            {renderApprovalFlowNode(node, emit)}
                        </ApprovalNodeStatusProvider>
                    </ApprovalEditorContext.Provider>
                ),
            },
        })
    );

    useEffect(() => {
        if (!baseCtx) return;

        const getNodeById = (nodeId: string) => {
            return baseCtx.rete.editor.getNodes().find((it) => it.node.id == nodeId);
        };

        const deleteNodeById = async (nodeId: string) => {
            const node = getNodeById(nodeId);
            if (!node) return undefined;
            await baseCtx.rete.editor.removeNode(node.id);
            const connections = baseCtx.rete.editor
                .getConnections()
                .filter((conn) => conn.source == node.id || conn.target == node.id);
            for (const conn of connections) {
                await baseCtx.rete.editor.removeConnection(conn.id);
            }
            return node;
        };

        setCtx({...baseCtx, getNodeById, deleteNodeById});

        applyApprovalFlowEditorAreaBackground(baseCtx.rete.area);
    }, [baseCtx]);

    useEffect(() => {
        if (!details || !ctx) return;
        void (async () => {
            await renderApprovalFlow(ctx, details);
            // Freeze editing after nodes/edges are rendered
            ctx.enableReadonly();
        })();
    }, [details, ctx]);

    const {mousePos, zoom} = useGraphViewport(baseCtx, ref);

    const instance = details?.instance;
    const definition = details?.definition;

    return (
        <div className="w-full h-[100vh] flex flex-col">
            {/* Header */}
            <div
                className="w-full flex flex-row items-center justify-between px-4 py-3 border-b"
                style={{borderColor: token.colorBorder}}
            >
                <div className="flex items-center gap-3">
                    {props.headerLeft?.()}
                    <Typography.Text strong>
                        {definition?.name ?? t('components.approvalFlowViewer.header.loading')}
                    </Typography.Text>
                    {instance && (
                        <>
                            <Tag color="blue">v{instance.definitionVersion}</Tag>
                            <Tag color={getInstanceStatusColor(instance.status)}>
                                {getApprovalFlowInstanceStatus(instance.status)}
                            </Tag>
                        </>
                    )}
                </div>
                <div className="flex items-center gap-2">
                    {props.headerRight?.left?.()}
                    {props.headerRight?.right?.()}
                </div>
            </div>

            {/* Body: Center / Right */}
            <div className="w-full flex-1 flex flex-row overflow-hidden">
                {/* Center: Viewer graph */}
                <div className="flex-1 relative">
                    {isLoading && !details && (
                        <div className="absolute inset-0 flex items-center justify-center z-10">
                            <Spin size="large"/>
                        </div>
                    )}
                    {!isLoading && !details && (
                        <div className="absolute inset-0 flex items-center justify-center z-10">
                            <Empty description={t('components.approvalFlowViewer.error.loadFailed')}/>
                        </div>
                    )}
                    <div className="w-full h-full cursor-default active:cursor-grabbing" ref={ref}/>

                    {/* Bottom Float Panel */}
                    <div className="absolute bottom-4 left-1/2 transform -translate-x-1/2 flex gap-2 text-sm">
                        <div
                            className="backdrop-blur-sm px-3 py-1.5 rounded-lg border border-white/10 flex items-center gap-4 font-mono"
                            style={{color: token.colorPrimary}}
                        >
                            <span>{t('components.approvalFlowViewer.statusBar.position')}: {mousePos?.x ?? '0'}, {mousePos?.y ?? '0'}</span>
                            <span>{t('components.approvalFlowViewer.statusBar.scale')}: {zoom?.toFixed(2) ?? '1'}x</span>
                        </div>
                    </div>
                </div>

                {/* Right Panel: Records */}
                <div
                    className="relative border-l flex flex-row"
                    style={{
                        width: `${panelWidth}%`,
                        minWidth: 360,
                        maxWidth: 640,
                        borderColor: token.colorBorder,
                    }}
                >
                    {/* Resize handle */}
                    <div
                        className="absolute left-0 top-0 bottom-0 w-1 cursor-col-resize hover:bg-blue-500/50 active:bg-blue-500/70 z-10"
                        onMouseDown={handleResizeStart}
                    />
                    <div className="flex-1 p-4 overflow-y-auto">
                        {details ? (
                            <ApprovalNodeRecordsPanel
                                records={details.records}
                                selectedNodeId={selectedNodeId}
                                nodes={details.nodes}
                                scope={details.instance.scope}
                            />
                        ) : (
                            <Empty description={t('components.approvalFlowViewer.records.empty')}/>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}
