# Create a Plugin

A plugin is a frontend extension that injects pages, routes, menus, and i18n resources into the admin panel through a standard interface, without modifying framework source code.

## Directory Structure

Create a plugin directory under `web/src/extensions/` with the entry file named `index.tsx`:

```
web/src/extensions/my-plugin/
  index.tsx              — Plugin entry (required)
  pages/                 — Page components
  api/                   — API calls
  types/                 — Response types
```

## Plugin Interface

The entry file exports a `CrystalWebPlugin` as default:

```tsx
import type {CrystalWebPlugin} from "@/plugin/types.ts";

const myPlugin: CrystalWebPlugin = {
    configure(registry) {
        // Register routes, menus, groups
    },
    i18nResources: {
        // i18n resources
    },
};

export default myPlugin;
```

### Interface

```typescript
interface CrystalWebPlugin {
    configure(registry: IMenuRegistry): void;
    i18nResources?: Record<string, DeepPartial<I18nRules>>;
}
```

- `configure(registry)` — Register routes, menu items, groups, system settings
- `i18nResources` — Optional translation key-value pairs

## Auto-discovery

`web/src/plugin/loader.ts` uses Vite's `import.meta.glob` for auto-discovery:

```typescript
const pluginModules = import.meta.glob<{ default: CrystalWebPlugin }>(
    '../extensions/*/index.{ts,tsx}',
    {eager: true}
);
```

- Matches `extensions/*/index.ts` or `extensions/*/index.tsx`
- `loadPlugins()` is called in `main.tsx` before React renders
- No manual registration or framework code modification needed

## i18n

Resources are declared in the plugin's `i18nResources` property and flattened into i18next at startup:

```tsx
i18nResources: {
    'zh-CN': {
        menu: {
            admin: { myPage: '我的页面' },
            groups: { myGroup: '我的分组' },
        },
        pages: {
            myPage: { title: '我的页面' },
        },
    },
    'en-US': {
        menu: {
            admin: { myPage: 'My Page' },
            groups: { myGroup: 'My Group' },
        },
        pages: {
            myPage: { title: 'My Page' },
        },
    },
},
```

- Only provide the keys your plugin uses (thanks to `DeepPartial` typing)
- Menu labels use `menu.admin.xxx` (admin) or `menu.pub.xxx` (public)
- Page titles should use `pages.xxx.title`
- All keys follow the `I18nRules` interface structure

## Registry Methods Overview

`IMenuRegistry` provides the following registration methods:

| Method | Usage |
|--------|-------|
| `addAdminMenu(...)` | Add admin menu items |
| `addPublicMenu(...)` | Add public menu items (visible to all authenticated users) |
| `addTenantMenu(...)` | Add tenant menu items |
| `addTopLevelRoute(...)` | Add a top-level route (outside `/manager/*` layout) |
| `addMenuGroup(...)` | Add a custom menu group |

See [Adding a Page](./add-page) and [API Integration](./api-integration) for details.
