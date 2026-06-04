import type {SettingsGroupExtraRenderer, SettingsItemRenderer} from "@/components/settings/types.ts";
import type {
    IMenuRegistry,
    PluginMenuGroup,
    PluginRouteItem,
    PluginTopLevelRoute,
    SettingsScope,
} from "./types.ts";

interface SettingsScopeBucket {
    keys: string[];
    groups: string[];
    tabs: string[];
    itemRenderers: Map<string, SettingsItemRenderer>;
    groupExtraRenderers: Map<string, SettingsGroupExtraRenderer>;
}

function createScopeBucket(): SettingsScopeBucket {
    return {
        keys: [],
        groups: [],
        tabs: [],
        itemRenderers: new Map(),
        groupExtraRenderers: new Map(),
    };
}

class MenuRegistryImpl implements IMenuRegistry {
    private _menuGroups: PluginMenuGroup[] = [];
    private _adminMenus: PluginRouteItem[] = [];
    private _tenantMenus: PluginRouteItem[] = [];
    private _publicMenus: PluginRouteItem[] = [];
    private _topLevelRoutes: PluginTopLevelRoute[] = [];
    private _settings: Record<SettingsScope, SettingsScopeBucket> = {
        system: createScopeBucket(),
        tenant: createScopeBucket(),
    };

    addMenuGroup(group: PluginMenuGroup): void {
        this._menuGroups.push(group);
    }

    addAdminMenu(item: PluginRouteItem): void {
        this._adminMenus.push(item);
    }

    addTenantMenu(item: PluginRouteItem): void {
        this._tenantMenus.push(item);
    }

    addPublicMenu(item: PluginRouteItem): void {
        this._publicMenus.push(item);
    }

    addTopLevelRoute(route: PluginTopLevelRoute): void {
        this._topLevelRoutes.push(route);
    }

    addSettingsKey(scope: SettingsScope, key: string): void {
        const bucket = this._settings[scope];
        if (!bucket.keys.includes(key)) {
            bucket.keys.push(key);
        }
    }

    addSettingsGroup(scope: SettingsScope, group: string): void {
        const bucket = this._settings[scope];
        if (!bucket.groups.includes(group)) {
            bucket.groups.push(group);
        }
    }

    addSettingsTab(scope: SettingsScope, tab: string): void {
        const bucket = this._settings[scope];
        if (!bucket.tabs.includes(tab)) {
            bucket.tabs.push(tab);
        }
    }

    addSettingsItemRenderer(scope: SettingsScope, key: string, renderer: SettingsItemRenderer): void {
        const bucket = this._settings[scope];
        if (bucket.itemRenderers.has(key)) {
            console.warn(`[plugin] settings item renderer for '${scope}:${key}' is being overridden`);
        }
        bucket.itemRenderers.set(key, renderer);
    }

    addSettingsGroupExtraRenderer(scope: SettingsScope, group: string, renderer: SettingsGroupExtraRenderer): void {
        const bucket = this._settings[scope];
        if (bucket.groupExtraRenderers.has(group)) {
            console.warn(`[plugin] settings group extra renderer for '${scope}:${group}' is being overridden`);
        }
        bucket.groupExtraRenderers.set(group, renderer);
    }

    get menuGroups(): PluginMenuGroup[] {
        return this._menuGroups;
    }

    get adminMenus(): PluginRouteItem[] {
        return this._adminMenus;
    }

    get tenantMenus(): PluginRouteItem[] {
        return this._tenantMenus;
    }

    get publicMenus(): PluginRouteItem[] {
        return this._publicMenus;
    }

    get topLevelRoutes(): PluginTopLevelRoute[] {
        return this._topLevelRoutes;
    }

    getSettingsKeys(scope: SettingsScope): string[] {
        return this._settings[scope].keys;
    }

    getSettingsGroups(scope: SettingsScope): string[] {
        return this._settings[scope].groups;
    }

    getSettingsTabs(scope: SettingsScope): string[] {
        return this._settings[scope].tabs;
    }

    getSettingsItemRenderers(scope: SettingsScope): Map<string, SettingsItemRenderer> {
        return this._settings[scope].itemRenderers;
    }

    getSettingsGroupExtraRenderers(scope: SettingsScope): Map<string, SettingsGroupExtraRenderer> {
        return this._settings[scope].groupExtraRenderers;
    }
}

export const pluginRegistry = new MenuRegistryImpl();
