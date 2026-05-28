# 右键菜单

`ContextMenu` —— 包裹任意元素后使其支持右键弹出菜单，同时支持键盘快捷键。

## 使用

```tsx
import { ContextMenu } from '@/components/contextmenu';

<ContextMenu
    items={[
        { key: 'close', label: '关闭', shortcut: { mac: 'Cmd+W', win: 'Ctrl+W', binding: 'mod+w' } },
        { key: 'divider', divider: true },
        { key: 'delete', label: '删除', icon: <DeleteOutlined />, danger: true },
    ]}
    onAction={(key) => {
        if (key === 'close') handleClose();
        if (key === 'delete') handleDelete();
    }}
>
    <div>右键点击此处</div>
</ContextMenu>
```

## Props

| 参数 | 类型 | 说明 |
|------|------|------|
| `items` | `ContextMenuItem[]` | 菜单项列表 |
| `onAction` | `(key: string) => void` | 菜单项点击回调 |
| `children` | `ReactNode` | 被包裹的元素 |
| `isActive` | `boolean` | 是否激活键盘快捷键。设为 `false` 可禁用该实例的快捷键响应，常用于标签栏中确保仅活动标签响应快捷键 |

## ContextMenuItem 类型

```ts
// 操作项
interface ContextMenuActionItem {
    key: string;          // 唯一标识，点击时传给 onAction
    label: ReactNode;     // 菜单项文字
    icon?: ReactNode;     // 图标
    disabled?: boolean;   // 是否禁用
    danger?: boolean;     // 是否展示为危险操作（红色）
    shortcut?: {          // 键盘快捷键
        mac: string;      // macOS 显示文本，如 "Cmd+W"
        win: string;      // Windows 显示文本，如 "Ctrl+W"
        binding: string;  // 实际绑定的按键，如 "mod+w"（mod 在 Mac = meta，Win = ctrl）
    };
}

// 分割线
interface ContextMenuDivider {
    key: string;
    divider: true;
}
```

## 快捷键绑定语法的规则

- `mod` — macOS 下为 `Cmd`，Windows 下为 `Ctrl`
- `ctrl` / `alt` / `shift` / `meta` — 对应各修饰键
- 最后一段为按键名，匹配 `e.key` 或 `e.code`（如 `w`、`escape`、`delete`）
- 输入框内不触发快捷键

::: tip 国际化
示例中菜单项的 `label` 文字应使用 i18n key。菜单相关 key 放在 `components.contextMenu` 下，详见[国际化](../i18n)。
:::
