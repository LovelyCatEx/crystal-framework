/**
 * Copyright 2025 lovelycat
 *
 * Use of this source code is governed by the Apache License, Version 2.0,
 * that can be found in the LICENSE file.
 */
import {ClassicPreset, NodeEditor} from "rete";
import type {BaseGraphSchemes} from "../types/schemes.ts";
import type {BaseGraphSocket} from "../socket/BaseGraphSocket.ts";
import {BaseGraphNode} from "../node/BaseGraphNode.ts";
import {BaseGraphNodeConnection} from "../types/connection.ts";
import type {BaseGraphControl} from "@/rete/control/BaseGraphControl.ts";

type Input<S extends BaseGraphSocket> = ClassicPreset.Input<S>;
type Output<S extends BaseGraphSocket> = ClassicPreset.Output<S>;

/**
 * Find out the SocketType of source and target
 *
 * @param editor NodeEditor
 * @param connection
 */
export function getConnectionSockets<
  S extends BaseGraphSocket,
  CTRL extends BaseGraphControl<S>,
  N extends BaseGraphNode<S, CTRL>,
  C extends BaseGraphNodeConnection<S, CTRL, N, N>,
  SCHEMES extends BaseGraphSchemes<S, CTRL, N, C>
>(
  editor: NodeEditor<SCHEMES>,
  connection: SCHEMES["Connection"]
): { source?: S, target?: S } {
  const source = editor.getNode(connection.source);
  const target = editor.getNode(connection.target);

  const output =
    source &&
    (source.outputs as Record<string, Output<S>>)[connection.sourceOutput];
  const input =
    target && (target.inputs as Record<string, Input<S>>)[connection.targetInput];

  return {
    source: output?.socket,
    target: input?.socket
  };
}
