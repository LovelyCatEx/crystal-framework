/**
 * Frontend-owned mirror of backend `SystemModulePathConstants` module keys, mapped to the
 * sidebar menu path prefixes the frontend should hide when the corresponding module is
 * disabled.
 *
 * Backend reports `disabledModules: string[]` via `/system/integratedInfo`; the frontend
 * looks each key up here and filters `getPublicMenus / getTenantMenus / getAdminMenus`
 * accordingly. Keep the module keys in sync with `SystemModulePathConstants.<Module>.KEY`
 * on the backend.
 */
export const SystemModuleKey = {
    TENANT: 'tenant',
    APPROVAL: 'approval',
} as const;

export type SystemModuleKeyValue = (typeof SystemModuleKey)[keyof typeof SystemModuleKey];

export const SYSTEM_MODULE_MENU_PATH_PREFIXES: Record<string, string[]> = {
    [SystemModuleKey.TENANT]: [
        '/manager/tenant/',
        '/manager/tenants',
        '/manager/tenant-',
    ],
    [SystemModuleKey.APPROVAL]: [
        '/manager/approval/',
        '/manager/approval-flow-',
        // /manager/tenant/approval-flow-* and /manager/tenant-approval-flow-* are also covered
        // by the tenant module's prefixes, but listed explicitly so they remain hidden when
        // only approval is disabled.
        '/manager/tenant/approval-flow-',
        '/manager/tenant-approval-flow-',
    ],
};

export function resolveDisabledMenuPrefixes(disabledModules: string[]): string[] {
    return disabledModules.flatMap((moduleKey) => SYSTEM_MODULE_MENU_PATH_PREFIXES[moduleKey] ?? []);
}

export function isModuleDisabled(disabledModules: string[], moduleKey: SystemModuleKeyValue): boolean {
    return disabledModules.includes(moduleKey);
}
