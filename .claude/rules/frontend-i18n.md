---
paths:
  - "web/src/i18n/**/*"
---

# 国际化规范

国际化文件在 `i18n` 文件夹中，以 `web/i18n` 作为工作目录。

## 语言文件

语言文件在 `locales` 文件夹中，文件命名必须是语言标准全名，例如 `en-US` / `zh-CN` 等。

语言文件导出的对象必须使用 `i18n-rules.ts` 中的类型，严格遵守类型规范。

## 枚举翻译四步流程

所有后端枚举类型必须在前端有对应 TypeScript 枚举定义，并按以下流程实现翻译：

1. `src/types/` 定义枚举常量
2. `locales/{locale}.ts` 的 `enums` 命名空间添加翻译键
3. `enum-helpers.ts` 注册 `getXxx()` 函数
4. 所有组件中通过 `getXxx(EnumType.VALUE)` 获取标签文本

**禁止**在组件中使用原始数字直接比较或分散的 `t('components.columns.xxx')` 键代替枚举翻译函数。

详见 `docs/develop/frontend/i18n.md` 的枚举翻译章节。

## 系统设置国际化

关于系统设置的国际化，请看 `system-settings.tsx` 文件。
