import {useSWRState} from "@/compositions/use-swr.ts";
import type {ManagerUpdateApprovalFlowGraphDTO} from "@/api/approval/approval-flow-definition.api.ts";
import {
    ApprovalFlowDefinitionManagerController,
    getApprovalFlowDefinitionDetails,
    updateApprovalFlowGraph,
} from "@/api/approval/approval-flow-definition.api.ts";
import {ApprovalFormDesigner} from "@/components/approval/form/designer/ApprovalFormDesigner.tsx";
import {NodeFormOverlayPanel} from "@/components/approval/form/designer/NodeFormOverlayPanel.tsx";
import {ConditionNodeInspector} from "@/components/approval/form/designer/ConditionNodeInspector.tsx";
import {ApprovalFormRenderer} from "@/components/approval/form/ApprovalFormRenderer.tsx";
import type {ApprovalFormSchema} from "@/types/approval/approval-form-schema.types.ts";
import {
    mergeFieldOverrides,
    parseFormSchema,
    stringifyFormSchema,
} from "@/utils/approval-form-utils.ts";
import {useCreateReteBaseGraphEditor} from "@/rete/rete-editor.tsx";
import {useRete} from "rete-react-plugin";
import {
    ApprovalFlowConnection,
    type ApprovalFlowGraphEditorContext,
    type ApprovalFlowGraphSchemes,
    type ApprovalFlowGraphSocket,
    BaseApprovalFlowGraphNode,
    BaseApprovalFlowGraphNodeControl
} from "./rete-typs.ts";
import {type ReactNode, useEffect, useRef, useState} from "react";
import {applyApprovalFlowEditorAreaBackground} from "@/components/approval/background.ts";
import {
    Button,
    Collapse,
    Descriptions,
    Divider,
    Empty,
    Form,
    Input,
    message,
    Modal,
    Select,
    Tabs,
    Tag,
    theme,
    Tooltip,
    Typography
} from "antd";
import {ApartmentOutlined, FullscreenExitOutlined, RedoOutlined, SaveOutlined, UndoOutlined} from "@ant-design/icons";
import {createApprovalFlowNode} from "./approval-graph-nodes.ts";
import {ApprovalEditorContext, type ApprovalEditorContextValue} from "./ApprovalEditorContext.tsx";
import {ApprovalNodeInspector} from "@/components/approval/ApprovalNodeInspector.tsx";
import {ContextMenuContainer} from "@/rete/ui/menu/ContextMenuContainer.tsx";
import {ContextMenuItem} from "@/rete/ui/menu/ContextMenuItem.tsx";
import {ContextMenuSubItem} from "@/rete/ui/menu/ContextMenuSubItem.tsx";
import {SquareFunction} from "lucide-react";
import type {ApprovalFlowNode} from "@/types/approval/approval-flow-node.types.ts";
import {ApprovalFlowNodeType} from "@/types/approval/approval-enums.ts";
import {ResourceScope} from "@/types/BaseScopedEntity.ts";
import {getApprovalFlowNodeType} from "@/i18n/enum-helpers.ts";
import {useTranslation} from "react-i18next";
import i18n from "@/i18n";
import {renderApprovalFlow, renderApprovalFlowNode} from "./utils/approval-graph-render.tsx";
import {useResizablePanel} from "./utils/use-resizable-panel.ts";
import {useGraphViewport} from "./utils/use-graph-viewport.ts";

async function save(ctx: ApprovalFlowGraphEditorContext, definitionId: string): Promise<{ success: boolean; errors?: string[] }> {
    const nodes = ctx.rete.editor.getNodes();
    const connections = ctx.rete.editor.getConnections();

    // Validate: nodeKey must not be empty
    for (const n of nodes) {
        if (!n.node.nodeKey || !n.node.nodeKey.trim()) {
            message.error(i18n.t('components.approvalEditor.validation.emptyNodeKey', { name: n.node.name || n.id }));
            return { success: false };
        }
    }

    // Validate: nodeKey must be unique
    const nodeKeySet = new Set<string>();
    for (const n of nodes) {
        if (nodeKeySet.has(n.node.nodeKey)) {
            message.error(i18n.t('components.approvalEditor.validation.duplicateNodeKey', { nodeKey: n.node.nodeKey }));
            return { success: false };
        }
        nodeKeySet.add(n.node.nodeKey);
    }

    const dto: ManagerUpdateApprovalFlowGraphDTO = {
        definitionId,
        nodes: nodes.map(n => ({
            nodeKey: n.node.nodeKey,
            type: n.node.type,
            name: n.node.name,
            config: n.node.config,
            formSchema: n.node.formSchema,
            positionX: Math.round(ctx.rete.area.nodeViews.get(n.id)?.position.x ?? n.node.positionX),
            positionY: Math.round(ctx.rete.area.nodeViews.get(n.id)?.position.y ?? n.node.positionY),
        })),
        edges: connections.map(c => {
            const sourceNode = nodes.find(n => n.id === c.source);
            const targetNode = nodes.find(n => n.id === c.target);
            return {
                sourceNodeKey: sourceNode!.node.nodeKey,
                targetNodeKey: targetNode!.node.nodeKey,
            };
        }),
    };

    try {
        const response = await updateApprovalFlowGraph(dto);
        const data = response.data as { success: boolean; errors: string[] } | null;
        if (data?.success) {
            return { success: true };
        }
        return { success: false, errors: data?.errors ?? [] };
    } catch {
        return { success: false };
    }
}

export default function ApprovalEditor(props: {
    definitionId: string;
    headerLeft?: () => ReactNode;
    headerRight?: { left?: () => ReactNode; right?: () => ReactNode };
}) {
    const { token } = theme.useToken();
    const { t } = useTranslation();

    const [definitionDetails, , , mutateDefinitionDetails] = useSWRState(
        `approval-definition-details:${props.definitionId}`,
        () => getApprovalFlowDefinitionDetails(props.definitionId)
    );

    const [ctx, setCtx] = useState<ApprovalFlowGraphEditorContext | null>(null);
    const [selectedNode, setSelectedNode] = useState<ApprovalFlowNode | null>(null);
    // Rete-assigned UID of the currently selected node. Used to look up the rete node
    // when applying inspector edits, because freshly created ApprovalFlowNode.id is
    // an empty string until the graph is saved — relying on it would conflate every
    // unsaved node into one.
    const [selectedReteNodeId, setSelectedReteNodeId] = useState<string | null>(null);

    // Form designer state. Owned by ApprovalEditor so switching to the Designer tab and
    // then away doesn't lose in-progress edits. Persisted separately from the graph via
    // ApprovalFlowDefinitionManagerController.update() (decision 3: definition-level schema
    // does NOT go through update-graph).
    const [formSchema, setFormSchema] = useState<ApprovalFormSchema | null>(null);
    const [selectedFieldKey, setSelectedFieldKey] = useState<string | null>(null);
    const [savingFormSchema, setSavingFormSchema] = useState(false);
    const [rightPanelTab, setRightPanelTab] = useState<string>('node');

    useEffect(() => {
        setFormSchema(parseFormSchema(definitionDetails?.definition.formSchema ?? null));
        setSelectedFieldKey(null);
    }, [definitionDetails?.definition.id, definitionDetails?.definition.formSchema]);

    const contextValueRef = useRef<ApprovalEditorContextValue>({
        scope: ResourceScope.SYSTEM,
        scopeId: '',
    });
    contextValueRef.current = {
        scope: definitionDetails?.definition.scope ?? ResourceScope.SYSTEM,
        scopeId: definitionDetails?.definition.scopeId ?? '',
    };

    const {width: panelWidth, handleResizeStart} = useResizablePanel(28);

    const [ref, baseCtx] = useRete(
        useCreateReteBaseGraphEditor<
            ApprovalFlowGraphSocket,
            BaseApprovalFlowGraphNodeControl,
            BaseApprovalFlowGraphNode,
            ApprovalFlowConnection,
            ApprovalFlowGraphSchemes
        >({
            canMakeConnection(): boolean {
                return true;
            },
            connectionFactory(fromNode, fromSocket, toNode, toSocket): ApprovalFlowGraphSchemes["Connection"] {
                return new ApprovalFlowConnection(fromNode, fromSocket, toNode, toSocket)
            },
            events: {
                onNodeSelected: (node) => {
                    setSelectedReteNodeId(node.id);
                    setSelectedNode(node.node);
                },
                onSelectedNodesChanged: (nodes) => {
                    if (nodes.length === 0) {
                        setSelectedReteNodeId(null);
                        setSelectedNode(null);
                    }
                },
            },
            render: {
                node: (_, node, emit) => (
                    <ApprovalEditorContext.Provider value={contextValueRef.current}>
                        {renderApprovalFlowNode(node, emit)}
                    </ApprovalEditorContext.Provider>
                ),
                contextMenu: {
                    main: () => ContextMenuContainer({
                        className: "p-2 rounded-lg shadow-2xl min-w-[256px] backdrop-blur-md",
                        style: {
                            background: token.colorBgElevated,
                            color: token.colorText,
                            border: `1px solid ${token.colorBorderSecondary}`,
                            borderRadius: token.borderRadiusLG,
                            boxShadow: token.boxShadowSecondary,
                        }
                    }),
                    item: (item) => ContextMenuItem(item, {
                        className: "flex flex-row justify-between items-center rounded-sm py-2 px-4 transition cursor-pointer hover:opacity-80",
                        style: {
                            color: token.colorText,
                            '--hover-bg': token.colorPrimaryBg,
                        } as React.CSSProperties
                    }, {
                        className: "flex flex-row justify-between items-center rounded-sm py-2 px-4 transition cursor-pointer hover:opacity-80",
                        style: {
                            color: token.colorErrorText,
                        }
                    }),
                    subitems: (_) => ContextMenuSubItem({
                        className: "p-2 rounded-lg shadow-2xl min-w-[256px]",
                        style: {
                            background: token.colorBgElevated,
                            color: token.colorText,
                            border: `1px solid ${token.colorBorderSecondary}`,
                            borderRadius: token.borderRadiusLG,
                            boxShadow: token.boxShadowSecondary,
                        }
                    }),
                    common: () => () =>
                        <div style={{ padding: 8, display: 'flex', alignItems: 'center', gap: 8, color: token.colorTextSecondary }}>
                            <SquareFunction size="20" />
                            <span>{i18n.t('components.approvalEditor.contextMenu.createNode')}</span>
                        </div>
                }
            },
            contextMenu: { renderDelay: 100 }
        })
    );
    // --- PART2_PLACEHOLDER ---

    useEffect(() => {
        if (!baseCtx) return;

        const getNodeById = (nodeId: string) => {
            return baseCtx.rete.editor.getNodes().find((it) => it.node.id == nodeId)
        }

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
        }

        setCtx({ ...baseCtx, getNodeById, deleteNodeById });

        // Prevent adding more than one Start node
        baseCtx.rete.editor.addPipe((context) => {
            if (context.type === 'nodecreate') {
                const newNode = context.data as BaseApprovalFlowGraphNode;
                if (newNode.node.type === ApprovalFlowNodeType.START) {
                    const hasStart = baseCtx.rete.editor.getNodes().some(n => n.node.type === ApprovalFlowNodeType.START);
                    if (hasStart) {
                        message.warning(i18n.t('components.approvalEditor.validation.duplicateStart'));
                        return undefined;
                    }
                }
            }
            return context;
        });

        const uid = () => String(Date.now()).slice(-6);
        const emptyNode = (type: number, nodeKey: string): ApprovalFlowNode => ({
            id: "", createdTime: "", modifiedTime: "",
            definitionId: "", definitionVersion: 0,
            nodeKey, type, name: getApprovalFlowNodeType(type),
            config: null, formSchema: null, positionX: 0, positionY: 0,
        });

        const presetConfigs: Record<number, string | null> = {
            [ApprovalFlowNodeType.START]: null,
            [ApprovalFlowNodeType.END]: null,
            [ApprovalFlowNodeType.APPROVAL]: JSON.stringify({ approveMode: 0, strategy: 0, strategyParams: {} }),
            [ApprovalFlowNodeType.CONDITION]: JSON.stringify({ routes: [] }),
            [ApprovalFlowNodeType.CC]: JSON.stringify({ userIds: [], roleIds: [], channelIds: [] }),
            [ApprovalFlowNodeType.FORK]: null,
            [ApprovalFlowNodeType.JOIN]: null,
        };

        baseCtx.registerContextMenu([
            [getApprovalFlowNodeType(ApprovalFlowNodeType.START), () =>
                createApprovalFlowNode({ ...emptyNode(ApprovalFlowNodeType.START, 'start'), config: presetConfigs[ApprovalFlowNodeType.START] })],
            [getApprovalFlowNodeType(ApprovalFlowNodeType.END), () =>
                createApprovalFlowNode({ ...emptyNode(ApprovalFlowNodeType.END, `end_${uid()}`), config: presetConfigs[ApprovalFlowNodeType.END] })],
            [getApprovalFlowNodeType(ApprovalFlowNodeType.APPROVAL), () =>
                createApprovalFlowNode({ ...emptyNode(ApprovalFlowNodeType.APPROVAL, `approval_${uid()}`), config: presetConfigs[ApprovalFlowNodeType.APPROVAL] })],
            [getApprovalFlowNodeType(ApprovalFlowNodeType.CONDITION), () =>
                createApprovalFlowNode({ ...emptyNode(ApprovalFlowNodeType.CONDITION, `condition_${uid()}`), config: presetConfigs[ApprovalFlowNodeType.CONDITION] })],
            [getApprovalFlowNodeType(ApprovalFlowNodeType.CC), () =>
                createApprovalFlowNode({ ...emptyNode(ApprovalFlowNodeType.CC, `cc_${uid()}`), config: presetConfigs[ApprovalFlowNodeType.CC] })],
            [getApprovalFlowNodeType(ApprovalFlowNodeType.FORK), () =>
                createApprovalFlowNode({ ...emptyNode(ApprovalFlowNodeType.FORK, `fork_${uid()}`), config: presetConfigs[ApprovalFlowNodeType.FORK] })],
            [getApprovalFlowNodeType(ApprovalFlowNodeType.JOIN), () =>
                createApprovalFlowNode({ ...emptyNode(ApprovalFlowNodeType.JOIN, `join_${uid()}`), config: presetConfigs[ApprovalFlowNodeType.JOIN] })],
        ]);

        baseCtx.rete.area.addPipe((context) => {
            if (context.type === 'nodetranslated') {
                const reteNode = baseCtx.rete.editor.getNode(context.data.id);
                if (reteNode) {
                    reteNode.node.positionX = Math.round(context.data.position.x);
                    reteNode.node.positionY = Math.round(context.data.position.y);
                }
            }
            return context;
        });

        applyApprovalFlowEditorAreaBackground(baseCtx.rete.area);
    }, [baseCtx]);

    useEffect(() => {
        if (!definitionDetails || !ctx) return;
        void renderApprovalFlow(ctx, definitionDetails);
    }, [definitionDetails, ctx]);

    const {mousePos, zoom} = useGraphViewport(baseCtx, ref);

    const performSaveFormSchema = async () => {
        if (!definitionDetails) return;
        if (!formSchema) {
            // Nothing configured yet — treat as clearing the schema (send empty).
            setSavingFormSchema(true);
            try {
                await ApprovalFlowDefinitionManagerController.update({
                    id: definitionDetails.definition.id,
                    formSchema: '',
                });
                message.success(t('components.approvalFormDesigner.save.success'));
                await mutateDefinitionDetails();
            } catch {
                message.error(t('components.approvalFormDesigner.save.failed'));
            } finally {
                setSavingFormSchema(false);
            }
            return;
        }
        // Refuse to save when any field has an invalid / duplicate key — mirrors the
        // per-field guards in ApprovalFieldPropertyPanel so the user gets a single, clear
        // error at the save boundary rather than silently persisting invalid schema.
        const keys = new Set<string>();
        for (const f of formSchema.fields) {
            if (!f.key.trim() || keys.has(f.key)) {
                message.error(t('components.approvalFormDesigner.save.hasInvalidFields'));
                return;
            }
            keys.add(f.key);
        }
        setSavingFormSchema(true);
        try {
            await ApprovalFlowDefinitionManagerController.update({
                id: definitionDetails.definition.id,
                formSchema: stringifyFormSchema(formSchema),
            });
            message.success(t('components.approvalFormDesigner.save.success'));
            await mutateDefinitionDetails();
        } catch {
            message.error(t('components.approvalFormDesigner.save.failed'));
        } finally {
            setSavingFormSchema(false);
        }
    };

    /**
     * User-facing save entry: guards the actual update behind an explicit confirm so the user
     * understands the semantics (see design record option C: changes take effect for newly
     * initiated instances only; in-flight instances keep their snapshot). No confirm when the
     * definition currently has no schema at all — that's a first-time setup, not an edit.
     */
    const confirmAndSaveFormSchema = () => {
        const hasExistingSchema = !!definitionDetails?.definition.formSchema;
        if (!hasExistingSchema) {
            void performSaveFormSchema();
            return;
        }
        Modal.confirm({
            title: t('components.approvalFormDesigner.save.confirmTitle'),
            content: t('components.approvalFormDesigner.save.confirmContent'),
            okText: t('components.approvalFormDesigner.save.confirmOk'),
            cancelText: t('components.approvalFormDesigner.save.confirmCancel'),
            onOk: () => performSaveFormSchema(),
        });
    };

    const previewFields = formSchema
        ? mergeFieldOverrides(formSchema, null, {nodeType: ApprovalFlowNodeType.START})
        : [];

    return (
        <div className="w-full h-[100vh] flex flex-col">
            {/* Header */}
            <div className="w-full flex flex-row items-center justify-between px-4 py-3 border-b" style={{ borderColor: token.colorBorder }}>
                <div className="flex items-center gap-3">
                    {props.headerLeft?.()}
                    <Typography.Text strong>
                        {definitionDetails?.definition.name ?? t('components.approvalEditor.header.loading')}
                    </Typography.Text>
                    {definitionDetails && (
                        <Tag color="blue">v{definitionDetails.definition.currentVersion}</Tag>
                    )}
                </div>
                <div className="flex items-center gap-2">
                    {props.headerRight?.left?.()}
                    <Button
                        type="primary"
                        icon={<SaveOutlined />}
                        onClick={async () => {
                            if (!ctx) return;
                            const result = await save(ctx, props.definitionId);
                            if (result.success) {
                                message.success(t('components.approvalEditor.header.saveSuccess'));
                                setSelectedReteNodeId(null);
                                setSelectedNode(null);
                                await mutateDefinitionDetails();
                            } else if (result.errors && result.errors.length > 0) {
                                Modal.error({
                                    title: t('components.approvalEditor.header.validationFailed'),
                                    content: (
                                        <ul style={{ paddingLeft: 16, marginTop: 8 }}>
                                            {result.errors.map((err, idx) => (
                                                <li key={idx} style={{ marginBottom: 4 }}>{err}</li>
                                            ))}
                                        </ul>
                                    ),
                                    width: 520,
                                });
                            }
                        }}
                    >
                        {t('components.approvalEditor.header.save')}
                    </Button>
                    {props.headerRight?.right?.()}
                </div>
            </div>

            {/* Body: Center / Right */}
            <div className="w-full flex-1 flex flex-row overflow-hidden">
                {/* Center: Editor */}
                <div className="flex-1 relative">
                    <div className="w-full h-full cursor-default active:cursor-grabbing" ref={ref} />

                    {/* Top Float Tools */}
                    <div className="absolute top-4 left-1/2 transform -translate-x-1/2 flex group">
                        <div className="backdrop-blur-sm rounded-lg border border-white/10 flex items-center font-mono">
                            <Tooltip title={t('components.approvalEditor.toolbar.undo')} placement="bottom">
                                <Button style={{ color: token.colorPrimary }} type="text" icon={<UndoOutlined />}
                                    onClick={() => { baseCtx?.historyUndo(); }} />
                            </Tooltip>
                            <Tooltip title={t('components.approvalEditor.toolbar.redo')} placement="bottom">
                                <Button style={{ color: token.colorPrimary }} type="text" icon={<RedoOutlined />}
                                    onClick={() => { baseCtx?.historyRedo(); }} />
                            </Tooltip>
                            <Divider orientation="vertical" className="bg-white/10 h-4 mx-1" />
                            <Tooltip title={t('components.approvalEditor.toolbar.fitViewport')} placement="bottom">
                                <Button style={{ color: token.colorPrimary }} type="text" icon={<FullscreenExitOutlined />}
                                    onClick={() => { baseCtx?.autoFitViewport(); }}>
                                    {t('components.approvalEditor.toolbar.fitViewport')}
                                </Button>
                            </Tooltip>
                            <Divider orientation="vertical" className="bg-white/10 h-4 mx-1" />
                            <Tooltip title={t('components.approvalEditor.toolbar.autoArrangeTooltip')} placement="bottom">
                                <Button style={{ color: token.colorPrimary }} type="text" icon={<ApartmentOutlined />}
                                    onClick={() => { baseCtx?.autoArrangeNodes(true); }}>
                                    {t('components.approvalEditor.toolbar.autoArrange')}
                                </Button>
                            </Tooltip>
                        </div>
                    </div>

                    {/* Bottom Float Panel */}
                    <div className="absolute bottom-4 left-1/2 transform -translate-x-1/2 flex gap-2 text-sm">
                        <div
                            className="backdrop-blur-sm px-3 py-1.5 rounded-lg border border-white/10 flex items-center gap-4 font-mono"
                            style={{ color: token.colorPrimary }}
                        >
                            <span>{t('components.approvalEditor.statusBar.position')}: {mousePos?.x ?? '0'}, {mousePos?.y ?? '0'}</span>
                            <span>{t('components.approvalEditor.statusBar.scale')}: {zoom?.toFixed(2) ?? '1'}x</span>
                        </div>
                    </div>
                </div>

                {/* Right Panel: Tabs (Node / Form Designer / Preview), resizable */}
                <div
                    className="relative border-l flex flex-row"
                    style={{ width: `${panelWidth}%`, minWidth: '20%', borderColor: token.colorBorder }}
                >
                    {/* Resize handle */}
                    <div
                        className="absolute left-0 top-0 bottom-0 w-1 cursor-col-resize hover:bg-blue-500/50 active:bg-blue-500/70 z-10"
                        onMouseDown={handleResizeStart}
                    />
                    {/*
                      * Split Tabs into "tab bar as selector" + "external scrollable container":
                      * antd Tabs' `items[].children` renders inside `.ant-tabs-content-holder`
                      * which does NOT participate in the outer flex column, so any inner
                      * `overflow-y-auto` never receives a bounded height and can't scroll.
                      * We render the tab bar only (items with `children: null`, and hide the
                      * default holder via a tailwind arbitrary variant) and drive the content
                      * area ourselves in the sibling flex-1 div below.
                      */}
                    <div className="flex-1 flex flex-col overflow-hidden pl-2 pr-2">
                        <Tabs
                            activeKey={rightPanelTab}
                            onChange={setRightPanelTab}
                            className="!px-3 !pt-2 !mb-0 [&_.ant-tabs-content-holder]:hidden"
                            items={[
                                {key: 'node', label: t('components.approvalFormDesigner.tabs.node'), children: null},
                                {key: 'designer', label: t('components.approvalFormDesigner.tabs.designer'), children: null},
                                {key: 'preview', label: t('components.approvalFormDesigner.tabs.preview'), children: null},
                            ]}
                        />
                        <div className="flex-1 overflow-y-auto p-3">
                            {rightPanelTab === 'node' && (
                                <NodeInspectorPanel
                                    key={selectedReteNodeId ?? ''}
                                    node={selectedNode}
                                    scope={definitionDetails?.definition.scope ?? ResourceScope.SYSTEM}
                                    scopeId={definitionDetails?.definition.scopeId ?? ''}
                                    definitionFormSchema={formSchema}
                                    graphNodes={ctx ? ctx.rete.editor.getNodes().map(n => n.node) : []}
                                    onNodeChange={(field, value) => {
                                        if (!ctx || !selectedReteNodeId) return;
                                        const reteNode = ctx.rete.editor.getNode(selectedReteNodeId);
                                        if (reteNode) {
                                            (reteNode.node as unknown as Record<string, unknown>)[field] = value;
                                            setSelectedNode({ ...reteNode.node });
                                        }
                                    }}
                                />
                            )}
                            {rightPanelTab === 'designer' && (
                                <ApprovalFormDesigner
                                    schema={formSchema}
                                    selectedKey={selectedFieldKey}
                                    onSelectedKeyChange={setSelectedFieldKey}
                                    onSchemaChange={setFormSchema}
                                    scope={definitionDetails?.definition.scope ?? ResourceScope.SYSTEM}
                                    scopeId={definitionDetails?.definition.scopeId ?? ''}
                                    headerRight={(
                                        <Button
                                            type="primary"
                                            icon={<SaveOutlined/>}
                                            loading={savingFormSchema}
                                            onClick={confirmAndSaveFormSchema}
                                        >
                                            {t('components.approvalFormDesigner.save.button')}
                                        </Button>
                                    )}
                                />
                            )}
                            {rightPanelTab === 'preview' && (
                                <>
                                    <Typography.Title level={5} className="!mb-0">
                                        {t('components.approvalFormDesigner.preview.title')}
                                    </Typography.Title>
                                    <Typography.Text type="secondary" className="text-xs">
                                        {t('components.approvalFormDesigner.preview.subtitle')}
                                    </Typography.Text>
                                    <div className="mt-3">
                                        {previewFields.length === 0 ? (
                                            <Empty description={t('components.approvalFormDesigner.preview.empty')}/>
                                        ) : (
                                            <ApprovalFormRenderer
                                                fields={previewFields}
                                                groups={formSchema?.groups}
                                                readonly
                                            />
                                        )}
                                    </div>
                                </>
                            )}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}

function NodeInspectorPanel({ node, scope, scopeId, definitionFormSchema, graphNodes, onNodeChange }: {
    node: ApprovalFlowNode | null;
    scope: number;
    scopeId: string;
    definitionFormSchema: ApprovalFormSchema | null;
    graphNodes: ApprovalFlowNode[];
    onNodeChange: (field: keyof ApprovalFlowNode, value: unknown) => void;
}) {
    const { t } = useTranslation();

    if (!node) {
        return <Empty description={t('components.approvalEditor.inspector.emptyHint')} />;
    }

    const formatJson = (str: string | null) => {
        if (!str) return t('components.approvalEditor.inspector.none');
        try { return JSON.stringify(JSON.parse(str), null, 2); }
        catch { return str; }
    };

    const nodeTypeOptions = Object.values(ApprovalFlowNodeType)
        .filter((v): v is number => typeof v === 'number')
        .map(v => ({ value: v, label: getApprovalFlowNodeType(v) }));

    const updateConfig = (patch: Record<string, unknown>) => {
        const current = node.config ? JSON.parse(node.config) : {};
        onNodeChange('config', JSON.stringify({ ...current, ...patch }));
    };

    return (
        <div className="flex flex-col gap-4">
            <Typography.Title level={5} className="!mb-0">
                {t('components.approvalEditor.inspector.title')}
            </Typography.Title>

            <Form layout="vertical" className="!mb-0">
                <Form.Item label={t('components.approvalEditor.inspector.id')}>
                    <Typography.Text copyable className="font-mono">
                        {node.id || t('components.approvalEditor.inspector.newNode')}
                    </Typography.Text>
                </Form.Item>
                <Form.Item label={t('components.approvalEditor.inspector.nodeKey')}>
                    <Input
                        value={node.nodeKey}
                        placeholder={t('components.approvalEditor.inspector.emptyValue')}
                        onChange={(e) => onNodeChange('nodeKey', e.target.value)}
                    />
                </Form.Item>
                <Form.Item label={t('components.approvalEditor.inspector.name')}>
                    <Input
                        value={node.name}
                        placeholder={t('components.approvalEditor.inspector.unnamed')}
                        onChange={(e) => onNodeChange('name', e.target.value)}
                    />
                </Form.Item>
                <Form.Item label={t('components.approvalEditor.inspector.type')}>
                    <Select
                        value={node.type}
                        disabled
                        options={nodeTypeOptions}
                    />
                </Form.Item>

                {/* Type-specific inspector */}
                {(node.type === ApprovalFlowNodeType.APPROVAL || node.type === ApprovalFlowNodeType.CC) && (
                    <ApprovalNodeInspector
                        node={node}
                        scope={scope}
                        scopeId={scopeId}
                        onConfigChange={updateConfig}
                    />
                )}
            </Form>

            {/* Form-field overlay: only meaningful for APPROVAL / CC nodes (the two node types
                that render a form to their operator). Changes are pushed to node.formSchema via
                onNodeChange and persisted along with the graph on Save. */}
            {(node.type === ApprovalFlowNodeType.APPROVAL || node.type === ApprovalFlowNodeType.CC) && (
                <NodeFormOverlayPanel
                    nodeType={node.type}
                    definitionFormSchema={definitionFormSchema}
                    nodeFormSchemaRaw={node.formSchema}
                    onOverlayChange={(nextRaw) => onNodeChange('formSchema', nextRaw || null)}
                />
            )}

            {/* CONDITION inspector: edits `node.config` (routes + condition tree). Target dropdown
                lists all saved nodes; unsaved nodes surface as a warning banner so the user knows
                to Save the graph before pointing a route at them. */}
            {node.type === ApprovalFlowNodeType.CONDITION && (
                <ConditionNodeInspector
                    node={node}
                    definitionFormSchema={definitionFormSchema}
                    graphNodes={graphNodes}
                    onConfigChange={(nextConfigJson) => onNodeChange('config', nextConfigJson)}
                />
            )}

            <Collapse
                items={[
                    {
                        key: 'config',
                        label: t('components.approvalEditor.inspector.config'),
                        children: (
                            <pre className="whitespace-pre-wrap break-all text-xs font-mono m-0">
                                {formatJson(node.config)}
                            </pre>
                        ),
                    },
                    {
                        key: 'formSchema',
                        label: t('components.approvalEditor.inspector.formSchema'),
                        children: (
                            <pre className="whitespace-pre-wrap break-all text-xs font-mono m-0">
                                {formatJson(node.formSchema)}
                            </pre>
                        ),
                    },
                ]}
            />

            <Descriptions column={1} bordered>
                <Descriptions.Item label={t('components.approvalEditor.inspector.position')}>
                    <Typography.Text className="font-mono text-xs">
                        x: {node.positionX}, y: {node.positionY}
                    </Typography.Text>
                </Descriptions.Item>
            </Descriptions>
        </div>
    );
}