import type {ReactNode} from "react";
import type {GetSystemSettingsSchemaData} from "@/types/system/system-settings.types.ts";
import {SettingsGroup} from "./SettingsGroup.tsx";
import type {SettingsGroupExtraRenderer, SettingsItemRenderer} from "./types.ts";

export interface BuildTabItemsOptions {
    data: GetSystemSettingsSchemaData;
    loading: boolean;
    tabTranslationMap: Map<string, string>;
    groupTranslationMap: Map<string, { label: string; icon?: ReactNode }>;
    keyTranslationMap: Map<string, string>;
    enumTranslator: (settingsKey: string, enumValue: string) => string;
    itemRenderers?: Map<string, SettingsItemRenderer>;
    groupExtraRenderers?: Map<string, SettingsGroupExtraRenderer>;
}

export function buildTabItems(options: BuildTabItemsOptions) {
    const {data, loading} = options;

    const tabs = Array.from(
        new Set(
            Object.values(data.items)
                .map(item => item.tab)
                .filter(tab => tab !== null)
        )
    ).sort();

    return tabs.map(tab => {
        const groupsInTab = data.groups.filter(group =>
            Object.values(data.items).some(item =>
                item.tab === tab && item.group === group
            )
        );

        const groupElements = groupsInTab.map((group, index) => {
            const items = Object.entries(data.items)
                .filter(([_, value]) => value.group === group)
                .sort(([, a], [, b]) => a.sort - b.sort);

            return (
                <SettingsGroup
                    key={group}
                    group={group}
                    items={items}
                    loading={loading}
                    isFirst={index === 0}
                    groupTranslationMap={options.groupTranslationMap}
                    keyTranslationMap={options.keyTranslationMap}
                    enumTranslator={options.enumTranslator}
                    itemRenderers={options.itemRenderers}
                    groupExtraRenderers={options.groupExtraRenderers}
                />
            );
        });

        return {
            key: tab,
            label: options.tabTranslationMap.get(tab) ?? tab,
            children: <div className="py-4">{groupElements}</div>
        };
    });
}
