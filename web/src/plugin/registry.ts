import type {IMenuRegistry, PluginMenuGroup, PluginRouteItem, PluginTopLevelRoute} from "./types.ts";

class MenuRegistryImpl implements IMenuRegistry {
    private _menuGroups: PluginMenuGroup[] = [];
    private _adminMenus: PluginRouteItem[] = [];
    private _tenantMenus: PluginRouteItem[] = [];
    private _publicMenus: PluginRouteItem[] = [];
    private _topLevelRoutes: PluginTopLevelRoute[] = [];

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
}

export const pluginRegistry = new MenuRegistryImpl();
