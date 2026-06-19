/*
 * Copyright 2025 lovelycat
 *
 * Use of this source code is governed by the Apache License, Version 2.0,
 * that can be found in the LICENSE file.
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