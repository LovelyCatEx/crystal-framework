/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
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