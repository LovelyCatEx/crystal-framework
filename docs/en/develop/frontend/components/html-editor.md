# HTML Editor

`HtmlEditor` — a dual-mode HTML editor with code and preview tabs. Useful for mail templates and scenarios requiring visual HTML editing.

## Usage

```tsx
import { HtmlEditor } from '@/components/HtmlEditor.tsx';

<HtmlEditor
    value="<h1>Hello World</h1>"
    onChange={(html) => console.log(html)}
    placeholder="Enter HTML code"
    height={400}
/>
```

## Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `value` | `string` | `''` | HTML string |
| `onChange` | `(value: string) => void` | — | Change callback |
| `placeholder` | `string` | — | Placeholder text in code mode |
| `height` | `number` | `400` | Editor/preview area height in px |

::: tip i18n
The `placeholder` text in the example should use an i18n key under `components.htmlEditor`. See [i18n](../i18n).
:::
