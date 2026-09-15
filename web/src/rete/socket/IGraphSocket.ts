/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {ClassicPreset} from "rete";

export interface IGraphSocket {
  isCompatibleWith(socket: ClassicPreset.Socket): boolean;
}