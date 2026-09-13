/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {ClassicPreset} from "rete";
import type {IGraphSocket} from "./IGraphSocket.ts";

export abstract class BaseGraphSocket extends ClassicPreset.Socket implements IGraphSocket {
  public readonly multipleConnections: boolean;

  protected constructor(socketId: string, multipleConnections: boolean = false) {
    super(socketId);
    this.multipleConnections = multipleConnections;
  }

  abstract isCompatibleWith(socket: ClassicPreset.Socket): boolean
}
