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
