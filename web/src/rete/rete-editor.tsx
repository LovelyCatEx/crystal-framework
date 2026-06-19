/**
 * Copyright 2025 lovelycat
 *
 * Use of this source code is governed by the Apache License, Version 2.0,
 * that can be found in the LICENSE file.
 */
import {Presets, type ReactArea2D, ReactPlugin, type RenderEmit} from "rete-react-plugin";
import {NodeEditor} from "rete";
import {AreaExtensions, AreaPlugin} from "rete-area-plugin";
import {ClassicFlow, ConnectionPlugin, getSourceTarget, type SocketData} from "rete-connection-plugin";
import {createRoot} from "react-dom/client";
import type {BaseGraphSchemes} from "./types/schemes.ts";
import type {BaseGraphSocket} from "./socket/BaseGraphSocket.ts";
import {BaseGraphNode} from "./node/BaseGraphNode.ts";
import {BaseGraphNodeConnection} from "./types/connection.ts";
import {getConnectionSockets} from "./utils/socket-utils.ts";
import {type ReactElement, useCallback} from "react";
import {type ContextMenuExtra, ContextMenuPlugin, Presets as ContextMenuPresets} from "rete-context-menu-plugin";
import type {EditorContextMenuItem, GraphNodeFactory} from "@/rete/types/context-menu.ts";
import type {ItemDefinition} from "rete-context-menu-plugin/_types/presets/classic/types";
import type {ComponentType, Item} from "rete-react-plugin/_types/presets/context-menu/types";
import {ContextMenuContainer} from "@/rete/ui/menu/ContextMenuContainer.tsx";
import {ContextMenuItem} from "@/rete/ui/menu/ContextMenuItem.tsx";
import {ContextMenuSubItem} from "@/rete/ui/menu/ContextMenuSubItem.tsx";
import {ArrangeAppliers, AutoArrangePlugin, Presets as ArrangePresets} from "rete-auto-arrange-plugin";
import {setupPanningBoundary} from "@/rete/ui/boundary";
import {HistoryExtensions, HistoryPlugin, Presets as HistoryPresets} from "rete-history-plugin";
import {SquareFunction} from "lucide-react";
import {ReadonlyPlugin} from "rete-readonly-plugin";
import type {BaseGraphControl} from "@/rete/control/BaseGraphControl.ts";

export interface GraphEditorContext<
  S extends BaseGraphSocket,
  CTRL extends BaseGraphControl<S>,
  N extends BaseGraphNode<S, CTRL>,
  C extends BaseGraphNodeConnection<S, CTRL, N, N>,
  SCHEMES extends BaseGraphSchemes<S, CTRL, N, C>
> {
  rete: {
    editor: NodeEditor<SCHEMES>;
    area: AreaPlugin<SCHEMES, ReactArea2D<SCHEMES> | ContextMenuExtra>;
    connection: ConnectionPlugin<SCHEMES, ReactArea2D<SCHEMES> | ContextMenuExtra>;
  };
  autoFitViewport(): void;
  autoArrangeNodes(animated: boolean): Promise<void>;
  historyUndo: () => Promise<void>;
  historyRedo: () => Promise<void>;
  getSelectedNodes: () => N[];
  enableReadonly: () => void;
  disableReadonly: () => void;
  registerContextMenu: (items: EditorContextMenuItem<S, CTRL>[]) => void;
  destroy(): void;
}

export interface CreateGraphEditorPropsEvents<
  S extends BaseGraphSocket,
  CTRL extends BaseGraphControl<S>,
  N extends BaseGraphNode<S, CTRL>,
  C extends BaseGraphNodeConnection<S, CTRL, N, N>
> {
  onInvalidConnection?: (
    reason: 'readonly'
      | 'incapable-socket'
      | 'already-connected'
      | 'additional-validation-failed'
  ) => void,
  onNodeSelected?: (node: N) => void,
  onSelectedNodesChanged?: (nodes: N[]) => void,
  onNodesChanged?: (type: 'created' | 'removed', currentNodes: N[]) => void,
  onNodeCreated?: (node: N) => void,
  onNodeRemoved?: (node: N) => void,
  onConnectionsChanged?: (type: 'created' | 'removed', currentConnections: C[]) => void,
  onConnectionCreated?: (conn: C) => void,
  onConnectionRemoved?: (conn: C) => void,
}

export interface CreateGraphEditorProps<
  S extends BaseGraphSocket,
  CTRL extends BaseGraphControl<S>,
  N extends BaseGraphNode<S, CTRL>,
  C extends BaseGraphNodeConnection<S, CTRL, N, N>,
  SCHEMES extends BaseGraphSchemes<S, CTRL, N, C>
> {
  connectionFactory: (fromNode: N, fromSocket: string, toNode: N, toSocket: string) => SCHEMES["Connection"],
  canMakeConnection?: (fromNode: N, fromSocket: S, fromLabel: string, toNode: N, toSocket: S, toLabel: string) => boolean,
  render?: {
    node?: (editor: NodeEditor<SCHEMES>, node: SCHEMES["Node"], emit: RenderEmit<SCHEMES>) => ReactElement | undefined | null;
    connection?: (editor: NodeEditor<SCHEMES>, connection: SCHEMES["Connection"]) => ReactElement | undefined | null;
    socket?: (editor: NodeEditor<SCHEMES>, socket: S) => ReactElement | undefined | null;
    control?: (editor: NodeEditor<SCHEMES>, control: CTRL) => ReactElement | undefined | null;
    contextMenu?: {
      main?: () => ComponentType;
      item?: (item: Item) => ComponentType;
      subitems?: (item: Item) => ComponentType;
      common?: () => ComponentType;
    }
  },
  events?: CreateGraphEditorPropsEvents<S, CTRL, N, C>,
  contextMenu?: {
    renderDelay?: number
  },
  panningBoundary?: {
    enabled?: boolean,
    padding?: number,
    intensity?: number
  }
}

export function useCreateReteBaseGraphEditor<
  S extends BaseGraphSocket,
  CTRL extends BaseGraphControl<S>,
  N extends BaseGraphNode<S, CTRL>,
  C extends BaseGraphNodeConnection<S, CTRL, N, N>,
  SCHEMES extends BaseGraphSchemes<S, CTRL, N, C>
>(props: CreateGraphEditorProps<S, CTRL, N, C, SCHEMES>) {
  return useCallback(
    (container: HTMLElement) => {
      return createBaseGraphEditor(container, props);
    },
    []
  )
}

async function createBaseGraphEditor<
  S extends BaseGraphSocket,
  CTRL extends BaseGraphControl<S>,
  N extends BaseGraphNode<S, CTRL>,
  C extends BaseGraphNodeConnection<S, CTRL, N, N>,
  SCHEMES extends BaseGraphSchemes<S, CTRL, N, C>
>(
  container: HTMLElement,
  props: CreateGraphEditorProps<S, CTRL, N, C, SCHEMES>
): Promise<GraphEditorContext<S, CTRL, N, C, SCHEMES>> {
  type AreaExtra = ReactArea2D<SCHEMES> | ContextMenuExtra;

  const editor = new NodeEditor<SCHEMES>();
  const area = new AreaPlugin<SCHEMES, AreaExtra>(container);
  const connection = new ConnectionPlugin<SCHEMES, AreaExtra>();
  const render = new ReactPlugin<SCHEMES, AreaExtra>({ createRoot });
  const arrange = new AutoArrangePlugin<SCHEMES>();
  const selector = AreaExtensions.selector();
  const history = new HistoryPlugin<SCHEMES>();
  const readonly = new ReadonlyPlugin<SCHEMES>();

  AreaExtensions.selectableNodes(area, selector, {
    accumulating: AreaExtensions.accumulateOnCtrl(),
  });

  // Configure render
  render.addPreset(
    Presets.classic.setup({
      customize: {
        node(data) {
          return ({ emit }) => {
            return props.render?.node?.(editor, data.payload, emit) ?? <Presets.classic.Node data={data.payload} emit={emit} />
          };
        },
        connection(data) {
          // const { source, target } = getConnectionSockets(editor, data.payload);
          return () => {
            return props.render?.connection?.(editor, data.payload) ?? <Presets.classic.Connection data={data.payload} />
          };
        },
        socket(data) {
          return () => {
            return props.render?.socket?.(editor, data.payload as S) ?? <Presets.classic.Socket data={data.payload} />
          };
        },
        control(data) {
          return () => {
            return props.render?.control?.(editor, data.payload as CTRL) ?? <div>UNKNOWN RENDER</div>;
          };
        }
      },
    })
  );

  render.addPreset(Presets.contextMenu.setup({
    customize: {
      main: () => props.render?.contextMenu?.main
        ? props.render?.contextMenu?.main?.()
        : ContextMenuContainer({
          className: "p-2 bg-white rounded-[.5rem] shadow-2xl min-w-[256px]"
        }),
      item: (item) => props.render?.contextMenu?.item
        ? props.render?.contextMenu?.item?.(item)
        : ContextMenuItem(item, {
          className: " flex flex-row justify-between items-center " +
            "rounded-[.25rem] pt-2 pb-2 pl-4 pr-4 " +
            "bg-white text-black group " +
            "hover:bg-blue-500 hover:text-white transition cursor-pointer"
        }, {
          className: " flex flex-row justify-between items-center " +
            "rounded-[.25rem] pt-2 pb-2 pl-4 pr-4 " +
            "bg-white text-black group " +
            "hover:bg-red-400 hover:text-white transition cursor-pointer"
        }),
      subitems: (item) => props.render?.contextMenu?.subitems
        ? props.render?.contextMenu?.subitems?.(item)
        : ContextMenuSubItem({
          className: "p-2 rounded-[.5rem] shadow-2xl min-w-[256px] " +
            "bg-white text-black border border-white/10"
        }),
      common: () => props.render?.contextMenu?.common
        ? props.render?.contextMenu?.common?.()
        : () => <div className="p-2 flex flex-row items-center space-x-2 bg-white text-black">
          <SquareFunction size="20" />
          <p>Create Node</p>
        </div>
    },
    delay: props.contextMenu?.renderDelay ?? 500
  }));

  // Configure connection
  connection.addPreset(() => {
    return new ClassicFlow({
      canMakeConnection(from: SocketData, to: SocketData) {
        const [source, target] = getSourceTarget(from, to) || [null, null];

        if (!source || !target || from === to) return false;

        if (readonly.enabled) {
          connection.drop();
          props.events?.onInvalidConnection?.('readonly');
          return false;
        }

        const sourceNode = editor.getNode(source.nodeId)!;
        const targetNode = editor.getNode(target.nodeId)!;

        const sockets = getConnectionSockets<S, CTRL, N, C, SCHEMES>(
          editor,
          props.connectionFactory(
            sourceNode,
            source.key,
            targetNode,
            target.key
          )
        );

        if (!sockets.source!.isCompatibleWith(sockets.target!)) {
          props.events?.onInvalidConnection?.('incapable-socket');
          connection.drop();
          return false;
        }

        const connected = editor
          .getConnections()
          .find((conn) => conn.source == sourceNode.id &&
            conn.target == targetNode.id &&
            conn.sourceOutput == source.key &&
            conn.targetInput == target.key
          ) != null;

        // Already connected before
        if (connected) {
          props.events?.onInvalidConnection?.('already-connected');
          connection.drop();
          return false;
        }

        // Additional check
        if (props?.canMakeConnection) {
          if (!props.canMakeConnection(
            sourceNode,
            sockets.source!,
            source.key,
            targetNode,
            sockets.target!,
            target.key)
          ) {
            props.events?.onInvalidConnection?.('additional-validation-failed');
            connection.drop();
            return false;
          }
        }

        return Boolean(source && target);
      },
      makeConnection(from: SocketData, to: SocketData, context) {
        const [source, target] = getSourceTarget(from, to) || [null, null];
        const { editor } = context;

        if (source && target) {
          void editor.addConnection(
            props.connectionFactory(
              editor.getNode(source.nodeId)!,
              source.key,
              editor.getNode(target.nodeId)!,
              target.key
            )
          );
          return true;
        }
      }
    });
  });

  // Configure arrange
  const transitionApplier = new ArrangeAppliers.TransitionApplier<SCHEMES, never>({
    duration: 500,
    timingFunction: (t) => t,
    async onTick() {
      await AreaExtensions.zoomAt(area, editor.getNodes());
    }
  });

  arrange.addPreset(ArrangePresets.classic.setup({
    spacing: 100
  }));

  // Panning boundary
  const panningBoundary = setupPanningBoundary({
    editor,
    area,
    selector,
    padding: props?.panningBoundary?.padding ?? 20,
    intensity: (props?.panningBoundary?.enabled ?? true)
      ? (props?.panningBoundary?.intensity ?? 4)
      : 0
  });

  // Configure history plugin
  HistoryExtensions.keyboard(history);
  history.addPreset(HistoryPresets.classic.setup());

  editor.use(area);
  editor.use(readonly.root);
  area.use(connection);
  area.use(render);
  area.use(arrange);
  area.use(history);
  area.use(readonly.area);

  AreaExtensions.simpleNodesOrder(area);

  const ctx: GraphEditorContext<S, CTRL, N, C, SCHEMES> = {
    rete: {
      editor: editor,
      area: area,
      connection: connection,
    },
    autoFitViewport: () => {
      AreaExtensions.zoomAt(area, editor.getNodes())
    },
    autoArrangeNodes: async (animated: boolean) => {
      await arrange.layout({ applier: animated ? transitionApplier : undefined });
    },
    historyUndo: async () => {
      await history.undo();
    },
    historyRedo: async () => {
      await history.redo();
    },
    getSelectedNodes: () => {
      return editor
        .getNodes()
        .filter((node) => node?.selected ?? null)
    },
    enableReadonly: () => {
      readonly.enable();
    },
    disableReadonly: () => {
      readonly.disable();
    },
    registerContextMenu: (menus) => {
      // Configure context menu
      const resolveContextMenu = (item: EditorContextMenuItem<S, CTRL>): ItemDefinition<SCHEMES> => {
        const [key, factoryOrItems] = item;
        if (typeof factoryOrItems == 'function') {
          return [key, async () => {
            const fx = (factoryOrItems as GraphNodeFactory<S, CTRL>)
            return fx() as SCHEMES["Node"];
          }]
        } else {
          return [key, (factoryOrItems as EditorContextMenuItem<S, CTRL>[]).map((it) => resolveContextMenu(it))]
        }
      }

      const contextMenu = new ContextMenuPlugin<SCHEMES>({
        items: ContextMenuPresets.classic.setup(
          menus.map((item) => resolveContextMenu(item)) ?? []
        ),
      });

      area.use(contextMenu);
    },
    destroy: () => {
      area.destroy();
      panningBoundary.destroy();
    },
  };

  // Configure event pipes
  area.addPipe((context) => {
    if (context.type == 'nodepicked') {
      props?.events?.onNodeSelected?.(editor.getNode(context.data.id)!);
      props?.events?.onSelectedNodesChanged?.(ctx.getSelectedNodes());
    } else if (context.type == 'pointerup') {
      props?.events?.onSelectedNodesChanged?.(ctx.getSelectedNodes());
    }

    return context;
  });

  editor.addPipe((context) => {
    if (context.type == 'connectioncreate') {
      // Pre Connection Create Event
      if (readonly.enabled) {
        connection.drop();
        return;
      }
    } else if (context.type == 'connectionremove') {
      // Pre Connection Remove Event
      if (readonly.enabled) {
        connection.drop();
        return;
      }
    }

    if (context.type == 'nodecreated') {
      props?.events?.onNodeCreated?.(context.data);
      props?.events?.onNodesChanged?.('created', editor.getNodes());
    } else if (context.type == 'noderemoved') {
      props?.events?.onNodeRemoved?.(context.data);
      props?.events?.onNodesChanged?.('removed', editor.getNodes());
    } else if (context.type == 'connectioncreated') {
      props?.events?.onConnectionCreated?.(context.data);
      props?.events?.onConnectionsChanged?.('created', editor.getConnections());
    } else if (context.type == 'connectionremoved') {
      props?.events?.onConnectionRemoved?.(context.data);
      props?.events?.onConnectionsChanged?.('removed', editor.getConnections());
    }

    return context;
  });

  return ctx;
}
