/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import type {ReactNode} from "react";
import type {I18nRules} from "@/i18n/i18n-rules.ts";
import type {SettingsGroupExtraRenderer, SettingsItemRenderer} from "@/components/settings/types.ts";

export interface PluginMenuGroup {
    name: string;
    icon: ReactNode;
    label: string;
}

export interface PluginRouteItem {
    key: string;
    path: string;
    icon?: ReactNode;
    label: string;
    page?: ReactNode;
    group?: string;
}

export interface PluginTopLevelRoute {
    path: string;
    element: ReactNode;
}

type DeepPartial<T> = T extends object ? {
    [P in keyof T]?: DeepPartial<T[P]>;
} : T;

export type PluginI18nResources = Record<string, DeepPartial<I18nRules>>;

export type SettingsScope = 'system' | 'tenant';

export interface CrystalWebPlugin {
    configure(registry: IMenuRegistry): void;
    i18nResources?: PluginI18nResources;
}

export interface IMenuRegistry {
    addMenuGroup(group: PluginMenuGroup): void;

    addAdminMenu(item: PluginRouteItem): void;

    addTenantMenu(item: PluginRouteItem): void;

    addPublicMenu(item: PluginRouteItem): void;

    addTopLevelRoute(route: PluginTopLevelRoute): void;

    addSettingsKey(scope: SettingsScope, key: string): void;

    addSettingsGroup(scope: SettingsScope, group: string): void;

    addSettingsTab(scope: SettingsScope, tab: string): void;

    addSettingsItemRenderer(scope: SettingsScope, key: string, renderer: SettingsItemRenderer): void;

    addSettingsGroupExtraRenderer(scope: SettingsScope, group: string, renderer: SettingsGroupExtraRenderer): void;
}
