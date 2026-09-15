/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
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
