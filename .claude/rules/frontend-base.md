---
paths:
  - "web/src/**/*.ts"
  - "web/src/**/*.tsx"
---

# 前端基础编码规范

工作目录：`web/`

## 目录结构

| 目录                | 用途                                     |
| ------------------- | ---------------------------------------- |
| `src/api/`        | Axios API 客户端                         |
| `src/assets/`     | 静态资源（图片、字体等）                 |
| `src/components/` | 可复用 UI 组件                           |
| `src/compositions/` | 自定义 React Hooks                     |
| `src/config/`     | 应用配置                                 |
| `src/contexts/`   | React Context 提供者                     |
| `src/global/`     | 全局状态（用户、租户信息）               |
| `src/i18n/`       | i18next + react-i18next 国际化          |
| `src/pages/`      | 页面组件                                 |
| `src/plugin/`     | 插件系统                                 |
| `src/router/`     | 路由定义（react-router-dom 7）          |
| `src/types/`      | TypeScript 类型定义                      |
| `src/utils/`      | 工具函数                                 |

路径别名：`@/` → `src/`（定义在 `vite.config.ts` 和 `tsconfig.app.json`）

## Context vs Compositions

- **Context**（`src/contexts/`）：需要创建并提供上下文的组件
- **Compositions**（`src/compositions/`）：自定义 Hooks，文件名和函数名必须以 `use-` 开头

## Config

配置文件位于 `config` 文件夹。

如需添加环境变量，必须在 `config/env.ts` 同步添加，并使用 `currentEnvironment` 获取。

## 插件

前端插件相关代码在 `plugin` 文件夹中，**非必要禁止修改**。

## 路由

Manager 页面侧边栏的路由文件位于 `router` 文件夹：

1. **Public**: 登录后无需权限即可访问
2. **Admin**: 登录后需要相关权限才可访问
3. **Tenant**: 以租户身份登录后才可访问

所有路由通过 `computeAccessibleMenus` 函数计算得出。

非 Manager 页面的路由见 `App.tsx` 中的 `<Routes>` 标签。

## 工具函数

所有工具函数必须放入 `utils` 文件夹。
