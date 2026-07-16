import {useTranslation} from "react-i18next";

/**
 * Permission names (e.g. `permission.create`, `i.tenant.member.read`) contain dots which
 * i18next treats as key separators. We store the mapping as a flat object under
 * `pages.permissionCatalog.byName` and read it directly via `getResource`, so the dotted
 * permission name is used as a plain JS property key.
 */
export function usePermissionTranslator() {
    const {i18n} = useTranslation();
    return (permissionName: string): string | null => {
        for (const lang of i18n.languages) {
            const byName = i18n.getResource(
                lang,
                'translation',
                'pages.permissionCatalog.byName',
            ) as Record<string, string> | undefined;
            const value = byName?.[permissionName];
            if (typeof value === 'string' && value.length > 0) {
                return value;
            }
        }
        return null;
    };
}
