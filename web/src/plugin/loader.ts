import type {CrystalWebPlugin, PluginI18nResources} from "./types.ts";
import {pluginRegistry} from "./registry.ts";
import i18n from "@/i18n";

const pluginModules = import.meta.glob<{ default: CrystalWebPlugin }>(
    '../../extensions/*/index.{ts,tsx}',
    {eager: true}
);

type FlatEntry = { lng: string; ns: string; key: string; value: string };

function flattenResources(
    lang: string,
    resources: PluginI18nResources[string],
): FlatEntry[] {
    const entries: FlatEntry[] = [];

    function walk(prefix: string, obj: Record<string, unknown>) {
        for (const [key, value] of Object.entries(obj)) {
            const path = prefix ? `${prefix}.${key}` : key;
            if (value !== null && typeof value === 'object' && !Array.isArray(value)) {
                walk(path, value as Record<string, unknown>);
            } else {
                entries.push({lng: lang, ns: 'translation', key: path, value: String(value ?? '')});
            }
        }
    }

    walk('', resources as Record<string, unknown>);
    return entries;
}

export function loadPlugins(): void {
    for (const [, module] of Object.entries(pluginModules)) {
        if (module.default) {
            module.default.configure(pluginRegistry);

            if (module.default.i18nResources) {
                for (const [lang, resource] of Object.entries(module.default.i18nResources)) {
                    const flat = flattenResources(lang, resource);
                    for (const entry of flat) {
                        i18n.addResource(entry.lng, entry.ns, entry.key, entry.value);
                    }
                }
            }
        }
    }
}
