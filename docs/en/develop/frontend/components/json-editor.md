# JSON Editor

`JsonEditor` — a dual-mode JSON editor with visual tree mode and source code mode. Supports add/delete/edit for nested objects and arrays, type switching, key renaming, and real-time validation.

## Usage

```tsx
import { JsonEditor } from '@/components/JsonEditor.tsx';

<JsonEditor
    value='{"name": "Alice", "age": 25}'
    onChange={(json) => console.log(json)}
    placeholder="Enter JSON"
/>
```

## Props

| Prop | Type | Description |
|------|------|-------------|
| `value` | `string` | JSON string, defaults to `'{}'` |
| `onChange` | `(value: string) => void` | Change callback, receives formatted JSON string |
| `placeholder` | `string` | Placeholder text in source mode |

## Features

- **Visual mode**: Edit node by node with expand/collapse, add child, delete, rename key, switch type
- **Source mode**: Edit raw JSON text with real-time syntax validation
- 6 value types: `string`, `number`, `boolean`, `object`, `array`, `null`
- Status bar at the bottom shows whether the JSON is valid

::: tip i18n
The `placeholder` text in the example should use an i18n key under `components.jsonEditor`. See [i18n](../i18n).
:::
