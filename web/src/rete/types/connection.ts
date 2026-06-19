/**
 * Copyright 2025 lovelycat
 *
 * Use of this source code is governed by the Apache License, Version 2.0,
 * that can be found in the LICENSE file.
 */
import {ClassicPreset} from "rete";
import type {BaseGraphNode} from "../node/BaseGraphNode.ts";
import type {BaseGraphSocket} from "../socket/BaseGraphSocket.ts";
import type {BaseGraphControl} from "@/rete/control/BaseGraphControl.ts";

export class BaseGraphNodeConnection<
  S extends BaseGraphSocket,
  CTRL extends BaseGraphControl<S>,
  A extends BaseGraphNode<S, CTRL>,
  B extends BaseGraphNode<S, CTRL>
> extends ClassicPreset.Connection<A, B> {}