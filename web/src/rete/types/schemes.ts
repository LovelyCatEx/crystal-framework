/**
 * Copyright 2025 lovelycat
 *
 * Use of this source code is governed by the Apache License, Version 2.0,
 * that can be found in the LICENSE file.
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