import type {IMenuRegistry, PluginMenuGroup, PluginRouteItem, PluginTopLevelRoute} from "./types.ts";

class MenuRegistryImpl implements IMenuRegistry {
    private _menuGroups: PluginMenuGroup[] = [];
    private _adminMenus: PluginRouteItem[] = [];
    private _tenantMenus: PluginRouteItem[] = [];
    private _publicMenus: PluginRouteItem[] = [];
    private _topLevelRoutes: PluginTopLevelRoute[] = [];
    private _systemSettingsKeys: string[] = [];
    private _systemSettingsGroups: string[] = [];
    private _systemSettingsTabs: string[] = [];

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

    addSystemSettingsKey(key: string): void {
        if (!this._systemSettingsKeys.includes(key)) {
            this._systemSettingsKeys.push(key);
        }
    }

    addSystemSettingsGroup(group: string): void {
        if (!this._systemSettingsGroups.includes(group)) {
            this._systemSettingsGroups.push(group);
        }
    }

    addSystemSettingsTab(tab: string): void {
        if (!this._systemSettingsTabs.includes(tab)) {
            this._systemSettingsTabs.push(tab);
        }
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

    get systemSettingsKeys(): string[] {
        return this._systemSettingsKeys;
    }

    get systemSettingsGroups(): string[] {
        return this._systemSettingsGroups;
    }

    get systemSettingsTabs(): string[] {
        return this._systemSettingsTabs;
    }
}

export const pluginRegistry = new MenuRegistryImpl();
