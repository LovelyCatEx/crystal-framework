import type {ReactNode} from "react";
import type {I18nRules} from "@/i18n/locales/i18n-rules";

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
}
