/*
 * Copyright 2025 lovelycat
 *
 * Use of this source code is governed by the Apache License, Version 2.0,
 * that can be found in the LICENSE file.
 */
import type {BaseGraphNode} from "@/rete/node/BaseGraphNode.ts";
import type {BaseGraphSocket} from "@/rete/socket/BaseGraphSocket.ts";
import type {BaseGraphControl} from "@/rete/control/BaseGraphControl.ts";

export type GraphNodeFactory<
  S extends BaseGraphSocket,
  CTRL extends BaseGraphControl<S>
> = () => (BaseGraphNode<S, CTRL> | Promise<BaseGraphNode<S, CTRL>>)

export type EditorContextMenuItem<
  S extends BaseGraphSocket,
  CTRL extends BaseGraphControl<S>
> = [
  string, GraphNodeFactory<S, CTRL> | EditorContextMenuItem<S, CTRL>[]
];
