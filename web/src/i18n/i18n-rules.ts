/**
 * Type definition structure for system/tenant settings manager pages.
 * Used by: SystemSettingsManagerPage, TenantSettingsManagerPage
 */
export interface SettingsManagerI18nNode {
    /** Setting item keys - e.g., pages.systemSettingsManager.keys.mail.smtp.host */
    keys: { [key: string]: string };
    /** Setting group labels - e.g., pages.systemSettingsManager.groups.mail.smtp */
    groups: { [key: string]: string };
    /** Setting tab labels - e.g., pages.systemSettingsManager.tabs.mail */
    tabs: { [key: string]: string };
    /** Enum value translations for setting items - e.g., pages.systemSettingsManager.enums.xxx.yyy */
    enums: { [key: string]: { [value: string]: string } };
}

/**
 * !!!ABSOLUTELY FORBIDDEN TO MODIFY THIS TYPE DEFINITION!!!
 * !!!VIOLATING THIS RULE = STOP ALL WORK IMMEDIATELY!!!
 *
 * This interface defines the ONLY allowed i18n structure.
 * All translation additions MUST follow this structure.
 *
 * DO NOT:
 * - Add new top-level fields
 * - Create "common" or any generic top-level field
 * - Add fields outside of this type definition
 * - Cross-reference between pages/components (each must have its own complete translations)
 *
 * MANDATORY WORKFLOW:
 * 1. Read this file to confirm the allowed structure
 * 2. Only add translations in positions allowed by this type
 * 3. Synchronize changes across all language files (zh-CN.ts, en-US.ts)
 */
export interface I18nRules {
    /**
     * Page-specific translations.
     * Usage: Page components under /pages directory
     * Example: pages.aiProviderManager.modal.name.label
     *
     * WHEN TO USE:
     * - Modal form labels, placeholders, validation messages within a specific page
     * - Page titles, subtitles
     * - Page-specific action buttons, hints, error messages
     *
     * STRUCTURE:
     * pages.{pageName}.modal.{field}.{label|placeholder|required|...}
     * pages.{pageName}.action.{actionName}
     * pages.{pageName}.{title|subtitle|...}
     */
    pages: {
        /** System settings manager page translations (keys, groups, tabs, enums) */
        systemSettingsManager: SettingsManagerI18nNode & { [key: string]: unknown };
        /** Tenant settings manager page translations (keys, groups, tabs, enums) */
        tenantSettingsManager: SettingsManagerI18nNode & { [key: string]: unknown };
        /** Other page-specific translations - each page should have its own complete translation set */
        [key: string]: object;
    },
    /**
     * Reusable component translations.
     * Usage: Shared components under /components directory
     *
     * WHEN TO USE:
     * - Column definitions for entity tables (components.columns.{entityName}.{columnName})
     * - Pop card content (components.popCard.{cardType}.{field})
     * - Any reusable component that appears across multiple pages
     *
     * STRUCTURE:
     * components.columns.{entityName}.{columnName}
     * components.popCard.{cardType}.{field}
     * components.{componentName}.{field}
     */
    components: {
        /**
         * Table column translations for entity tables.
         * Usage: Column title definitions in /components/columns/*Columns.tsx
         * Example: components.columns.aiModel.name, components.columns.aiModel.providerId
         *
         * WHEN TO USE:
         * - When defining EntityTableColumns in useXxxTableColumns()
         * - Column headers that appear in ManagerPageContainer tables
         *
         * DO NOT USE FOR:
         * - Modal form field labels (those go in pages.xxx.modal)
         */
        columns: { [key: string]: object };
        /**
         * Pop card content translations.
         * Usage: PopCard component variations
         * Example: components.popCard.mailTemplateType.yes
         */
        popCard: { [key: string]: object };
        /** Other component-specific translations */
        [key: string]: object;
    },
    /**
     * Enum value translations.
     * Usage: Backend enum types that need frontend display names
     * Example: enums.forbiddenReason.MISSING_PERMISSION, enums.storageProviderType[1000]
     *
     * WHEN TO USE:
     * - Translating backend enum values (ForbiddenReason, StorageProviderType, etc.)
     * - Status tags, type selectors that map to backend enums
     *
     * STRUCTURE:
     * enums.{enumName}.{VALUE} for string enums
     * enums.{enumName}[typeId] for numeric enums
     *
     * REGISTRATION:
     * Must also register in enum-helpers.ts using translateEnum()
     */
    enums: {
        [key: string]: string | ({ [key: number | string]: string });
    },
    /**
     * Entity name translations.
     * Usage: Entity display names used in CRUD operations
     * Example: entityNames.user, entityNames.tenant
     *
     * WHEN TO USE:
     * - Modal titles like "Create {entityName}", "Edit {entityName}"
     * - Delete confirmations like "Delete {entityName}?"
     * - Passed to ManagerPageContainer's entityName prop
     *
     * STRUCTURE:
     * entityNames.{entityName} (singular form, lowercase key)
     */
    entityNames: {
        [key: string]: string;
    },
    /**
     * Menu item and group translations.
     * Usage: Sidebar menu in /router/index.tsx
     *
     * WHEN TO USE:
     * - Menu item labels in getPublicMenus(), getTenantMenus(), getAdminMenus()
     * - Menu group labels in getMenuGroups()
     *
     * STRUCTURE:
     * menu.pub.{menuKey} - Public menus (no auth required)
     * menu.myTenant.{menuKey} - Tenant member menus
     * menu.admin.{menuKey} - System admin menus
     * menu.groups.{groupName} - Menu group labels
     */
    menu: {
        /** Public menu items (no authentication required) */
        pub: { [key: string]: string; };
        /** Tenant member menu items */
        myTenant: { [key: string]: string; };
        /** System admin menu items */
        admin: { [key: string]: string; };
        /** Menu group labels */
        groups: {
            [key: string]: string;
        };
    },
    /**
     * API error message translations.
     * Usage: Backend error codes and API response messages
     * Example: api.errors.USER_NOT_FOUND
     *
     * WHEN TO USE:
     * - Translating backend error codes to user-friendly messages
     * - API-specific status messages
     */
    api: {
        [key: string]: string | object;
    }
}