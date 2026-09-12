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
 * Standard structure for manager page translations.
 * Used by: Most manager pages (UserManager, TenantManager, etc.)
 */
export interface ManagerPageI18nNode {
    /** Page title */
    title: string;
    /** Page subtitle/description */
    subtitle: string;
    /** Modal form field translations */
    modal?: { [key: string]: object };
    /** Table filter translations */
    filter?: { [key: string]: object };
    /** Table action translations */
    tableActions?: { [key: string]: object };
    /** Action button translations */
    action?: { [key: string]: string | object };
    /** Message translations (success/error/confirm) */
    messages?: { [key: string]: string };
    /** Form field translations */
    form?: { [key: string]: object };
    /** Tab translations */
    tabs?: { [key: string]: string };
    /** Any other page-specific fields */
    [key: string]: unknown;
}

/**
 * !!!ABSOLUTELY FORBIDDEN TO MODIFY THIS TYPE DEFINITION!!!
 * !!!VIOLATING THIS RULE = STOP ALL WORK IMMEDIATELY!!!
 *
 * This interface defines the ONLY allowed i18n structure.
 * All translation additions MUST follow this structure.
 *
 * ====================================================================================
 * I18nRules EDITING RULES - READ BEFORE ANY i18n WORK
 * ====================================================================================
 *
 * WHO CAN MODIFY I18nRules:
 * - ONLY the project architect after careful design review
 * - NEVER AI assistants, regular developers, or during feature development
 *
 * WHAT CAN BE MODIFIED:
 * 1. ADD new interface definitions (like ManagerPageI18nNode, SettingsManagerI18nNode)
 *    - Must have clear purpose and documentation
 *    - Must be reusable across multiple pages/components
 *
 * 2. ADD new top-level fields under I18nRules (extremely rare)
 *    - Requires architectural justification
 *    - Must not overlap with existing fields
 *    - Example: adding a new major section like "errors" or "workflows"
 *
 * 3. ADD new specific page definitions under pages (like systemSettingsManager)
 *    - Only for pages with unique structure requirements
 *    - Most pages should use ManagerPageI18nNode via [key: string]
 *
 * WHAT IS ABSOLUTELY FORBIDDEN:
 * ❌ Adding "common" or "shared" top-level fields
 * ❌ Modifying existing interface definitions (ManagerPageI18nNode, SettingsManagerI18nNode)
 * ❌ Removing or renaming existing top-level fields
 * ❌ Changing the structure of pages/components/enums/menu/api
 * ❌ Adding cross-references between pages and components
 * ❌ Making types more permissive (weakening type safety)
 *
 * WHEN ADDING TRANSLATIONS (NOT modifying I18nRules):
 * - Read this file to understand allowed structure
 * - Add translations ONLY in positions defined by these types
 * - Follow ManagerPageI18nNode for manager pages (requires title + subtitle)
 * - Follow SettingsManagerI18nNode for settings pages (requires keys + groups + tabs + enums)
 * - Use components.columns for table columns
 * - Use enums for backend enum translations
 * - NEVER modify this type definition file
 *
 * MANDATORY WORKFLOW FOR i18n ADDITIONS:
 * 1. Read this file (i18n-rules.ts) to confirm the allowed structure
 * 2. Read i18n-translation skill for detailed guidelines
 * 3. Only add translations in zh-CN.ts and en-US.ts following the defined types
 * 4. Synchronize changes across all language files
 * 5. Run `pnpm tsc --noEmit` to verify type correctness
 *
 * ====================================================================================
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
     */
    pages: {
        /** System settings manager page translations (keys, groups, tabs, enums) */
        systemSettingsManager: SettingsManagerI18nNode & { [key: string]: unknown };
        /** Tenant settings manager page translations (keys, groups, tabs, enums) */
        tenantSettingsManager: SettingsManagerI18nNode & { [key: string]: unknown };
        /** Other pages: ManagerPageI18nNode (standard manager pages with title+subtitle) or object (special pages) */
        [key: string]: ManagerPageI18nNode | object;
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