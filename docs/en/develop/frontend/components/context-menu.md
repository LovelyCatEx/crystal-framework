# Context Menu

`ContextMenu` — wraps any element to enable right-click context menus with optional keyboard shortcuts.

## Usage

```tsx
import { ContextMenu } from '@/components/contextmenu';

<ContextMenu
    items={[
        { key: 'close', label: 'Close', shortcut: { mac: 'Cmd+W', win: 'Ctrl+W', binding: 'mod+w' } },
        { key: 'divider', divider: true },
        { key: 'delete', label: 'Delete', icon: <DeleteOutlined />, danger: true },
    ]}
    onAction={(key) => {
        if (key === 'close') handleClose();
        if (key === 'delete') handleDelete();
    }}
>
    <div>Right-click here</div>
</ContextMenu>
```

## Props

| Prop | Type | Description |
|------|------|-------------|
| `items` | `ContextMenuItem[]` | Menu item list |
| `onAction` | `(key: string) => void` | Callback when a menu item is clicked |
| `children` | `ReactNode` | The wrapped element |
| `isActive` | `boolean` | Whether keyboard shortcuts are active for this instance. Set to `false` to suppress shortcuts, e.g. in tab bars where only the active tab should respond |

## ContextMenuItem Types

```ts
// Action item
interface ContextMenuActionItem {
    key: string;          // Unique key passed to onAction
    label: ReactNode;     // Display text
    icon?: ReactNode;     // Icon
    disabled?: boolean;   // Whether disabled
    danger?: boolean;     // Whether to show as a danger action (red)
    shortcut?: {          // Keyboard shortcut
        mac: string;      // macOS display text, e.g. "Cmd+W"
        win: string;      // Windows display text, e.g. "Ctrl+W"
        binding: string;  // Actual key binding, e.g. "mod+w" (mod = Cmd on Mac, Ctrl on Win)
    };
}

// Divider
interface ContextMenuDivider {
    key: string;
    divider: true;
}
```

## Shortcut Binding Syntax

- `mod` — `Cmd` on macOS, `Ctrl` on Windows
- `ctrl` / `alt` / `shift` / `meta` — corresponding modifier keys
- The last segment is the key name, matched against `e.key` or `e.code` (e.g. `w`, `escape`, `delete`)
- Shortcuts are suppressed inside input fields
