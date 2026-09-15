/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import type {SettingsGroupExtraRenderer, SettingsItemRenderer} from "./types.ts";

export function mergeRenderers<R>(
    builtin: Map<string, R>,
    fromPlugins: Map<string, R>,
): Map<string, R> {
    const merged = new Map<string, R>(builtin);
    for (const [key, renderer] of fromPlugins) {
        if (!merged.has(key)) {
            merged.set(key, renderer);
        }
    }
    return merged;
}

export type SettingsItemRendererMap = Map<string, SettingsItemRenderer>;
export type SettingsGroupExtraRendererMap = Map<string, SettingsGroupExtraRenderer>;
