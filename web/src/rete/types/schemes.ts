/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import type {BaseGraphNode} from "../node/BaseGraphNode.ts";
import {type GetSchemes} from "rete";
import {BaseGraphNodeConnection} from "./connection.ts";
import type {BaseGraphSocket} from "../socket/BaseGraphSocket.ts";
import type {ClassicScheme} from "rete-react-plugin";
import type {BaseGraphControl} from "@/rete/control/BaseGraphControl.ts";

export type BaseGraphSchemes<
  S extends BaseGraphSocket,
  CTRL extends BaseGraphControl<S>,
  N extends BaseGraphNode<S, CTRL>,
  C extends BaseGraphNodeConnection<S, CTRL, N, N>
> = GetSchemes<N, C> & ClassicScheme