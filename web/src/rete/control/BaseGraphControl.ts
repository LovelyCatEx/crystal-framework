/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {ClassicPreset} from "rete";
import {BaseGraphSocket} from "@/rete/socket/BaseGraphSocket.ts";
import type {BaseGraphNode} from "@/rete/node/BaseGraphNode.ts";

export class BaseGraphControl<S extends BaseGraphSocket> extends ClassicPreset.Control {
  public readonly node: BaseGraphNode<S, any>
  public readonly portLabel: string;

  constructor(node: BaseGraphNode<S, any>, portLabel: string) {
    super();

    this.node = node;
    this.portLabel = portLabel;
  }
}