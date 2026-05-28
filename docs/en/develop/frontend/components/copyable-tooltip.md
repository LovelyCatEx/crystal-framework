# Copyable Tooltip

`CopyableToolTip` — a tooltip with a built-in copy button. Hover to see the content and copy it to the clipboard with one click.

## Usage

```tsx
import { CopyableToolTip } from '@/components/CopyableToolTip.tsx';

// Wrapping a text element
<CopyableToolTip title="hello@example.com">
    <span>Contact Email</span>
</CopyableToolTip>

// As a copyable tooltip only
<CopyableToolTip title="192.168.1.1">
    <Tag>IP Address</Tag>
</CopyableToolTip>
```

## Props

| Prop | Type | Description |
|------|------|-------------|
| `title` | `string \| ReactNode` | Tooltip text, also used as the copy content |
| `children` | `ReactNode` | The trigger element wrapped by the tooltip |

::: tip i18n
Text labels in the examples should use i18n keys under the appropriate page or component namespace. See [i18n](../i18n).
:::
