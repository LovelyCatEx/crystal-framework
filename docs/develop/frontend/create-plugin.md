# 创建插件

插件是一个前端扩展包，通过标准接口向管理后台注入页面、路由、菜单和国际化资源，无需修改框架源码。

## 目录结构

在 `web/src/extensions/` 下创建插件目录，入口文件约定为 `index.tsx`：

```
web/src/extensions/my-plugin/
  index.tsx              — 插件入口（必填）
  pages/                 — 页面组件
  api/                   — API 调用
  types/                 — 响应类型定义
```

## 插件接口

入口文件默认导出一个 `CrystalWebPlugin` 对象：

```tsx
import type {CrystalWebPlugin} from "@/plugin/types.ts";

const myPlugin: CrystalWebPlugin = {
    configure(registry) {
        // 注册路由、菜单、分组
    },
    i18nResources: {
        // 国际化资源
    },
};

export default myPlugin;
```

### 接口定义

```typescript
interface CrystalWebPlugin {
    configure(registry: IMenuRegistry): void;
    i18nResources?: Record<string, DeepPartial<I18nRules>>;
}
```

- `configure(registry)` — 注册路由、菜单项、菜单分组、系统设置等
- `i18nResources` — 可选，国际化翻译键值对

## 自动发现

`web/src/plugin/loader.ts` 通过 Vite 的 `import.meta.glob` 自动扫描加载：

```typescript
const pluginModules = import.meta.glob<{ default: CrystalWebPlugin }>(
    '../extensions/*/index.{ts,tsx}',
    {eager: true}
);
```

- 匹配 `extensions/*/index.ts` 或 `extensions/*/index.tsx`
- 在 `main.tsx` 中 `loadPlugins()` 在 React 渲染前执行
- 无需手动注册或修改框架代码

## 国际化

i18n 资源在插件对象的 `i18nResources` 属性中声明，启动时扁平化注入到 i18next：

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

- 由于 `DeepPartial` 类型约束，只需提供插件用到的键
- 菜单 label 使用 `menu.admin.xxx`（管理菜单）或 `menu.pub.xxx`（公共菜单）
- 页面标题建议使用 `pages.xxx.title`
- 所有键遵循 `I18nRules` 接口结构

## 注册方式总览

`IMenuRegistry` 提供以下注册方法（将在后续文档中详述）：

| 方法 | 用途 |
|------|------|
| `addAdminMenu(...)` | 添加管理后台菜单项 |
| `addPublicMenu(...)` | 添加公共菜单项（所有登录用户可见） |
| `addTenantMenu(...)` | 添加租户菜单项 |
| `addTopLevelRoute(...)` | 添加顶层路由（脱离 `/manager/*` 布局） |
| `addMenuGroup(...)` | 添加自定义菜单分组 |

具体用法见[添加页面](./add-page)和[API 对接](./api-integration)。
