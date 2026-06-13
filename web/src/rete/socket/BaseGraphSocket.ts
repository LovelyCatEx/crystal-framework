/**
 * Copyright 2025 lovelycat
 *
 * Use of this source code is governed by the Apache License, Version 2.0,
 * that can be found in the LICENSE file.
 */
import {ClassicPreset} from "rete";
import type {IGraphSocket} from "./IGraphSocket.ts";

export abstract class BaseGraphSocket extends ClassicPreset.Socket implements IGraphSocket {
  protected constructor(socketId: string) {
    super(socketId);
  }

  abstract isCompatibleWith(socket: ClassicPreset.Socket): boolean
}
