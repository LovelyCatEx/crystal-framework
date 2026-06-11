import type {ReactNode} from "react";
import {Tabs} from "antd";
import type {GetSystemSettingsSchemaData} from "@/types/system/system-settings.types.ts";
import {buildTabItems} from "./build-tab-items.tsx";
import {SettingsGroup} from "./SettingsGroup.tsx";
import type {SettingsGroupExtraRenderer, SettingsItemRenderer} from "./types.ts";

export interface SettingsRendererContainerProps {
    data?: GetSystemSettingsSchemaData | null;
    loading: boolean;
    tabTranslationMap: Map<string, string>;
    groupTranslationMap: Map<string, { label: string; icon?: ReactNode }>;
    keyTranslationMap: Map<string, string>;
    enumTranslator: (settingsKey: string, enumValue: string) => string;
    itemRenderers?: Map<string, SettingsItemRenderer>;
    groupExtraRenderers?: Map<string, SettingsGroupExtraRenderer>;
    showTabs?: boolean;
    /** Maximum number of columns per group. Defaults to 3. */
    maxColumns?: 1 | 2 | 3;
    /** Active tab key (controlled). When provided, tab state is managed externally. */
    activeTab?: string;
    /** Callback when tab changes. Use with activeTab for URL persistence. */
    onTabChange?: (key: string) => void;
}

export function SettingsRendererContainer(props: SettingsRendererContainerProps) {
    if (!props.data) {
        return null;
    }

    const showTabs = props.showTabs ?? true;

    if (showTabs) {
        return (
            <Tabs
                activeKey={props.activeTab}
                onChange={props.onTabChange}
                items={buildTabItems({
                    data: props.data,
                    loading: props.loading,
                    tabTranslationMap: props.tabTranslationMap,
                    groupTranslationMap: props.groupTranslationMap,
                    keyTranslationMap: props.keyTranslationMap,
                    enumTranslator: props.enumTranslator,
                    itemRenderers: props.itemRenderers,
                    groupExtraRenderers: props.groupExtraRenderers,
                    maxColumns: props.maxColumns,
                })}
                className="settings-tabs"
            />
        );
    }

    return (
        <div className="py-2">
            {props.data.groups.map((group, index) => {
                const items = Object.entries(props.data!.items)
                    .filter(([_, value]) => value.group === group)
                    .sort(([, a], [, b]) => a.sort - b.sort);

                if (items.length === 0) {
                    return null;
                }

                return (
                    <SettingsGroup
                        key={group}
                        group={group}
                        items={items}
                        loading={props.loading}
                        isFirst={index === 0}
                        groupTranslationMap={props.groupTranslationMap}
                        keyTranslationMap={props.keyTranslationMap}
                        enumTranslator={props.enumTranslator}
                        itemRenderers={props.itemRenderers}
                        groupExtraRenderers={props.groupExtraRenderers}
                        maxColumns={props.maxColumns}
                    />
                );
            })}
        </div>
    );
}
