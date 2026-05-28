# HTML 编辑器

`HtmlEditor` —— 代码/预览双模式的 HTML 编辑器，适用于邮件模板等需要可视化 HTML 编辑的场景。

## 使用

```tsx
import { HtmlEditor } from '@/components/HtmlEditor.tsx';

<HtmlEditor
    value="<h1>Hello World</h1>"
    onChange={(html) => console.log(html)}
    placeholder="请输入 HTML 代码"
    height={400}
/>
```

## Props

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `value` | `string` | `''` | HTML 字符串 |
| `onChange` | `(value: string) => void` | — | 代码变化回调 |
| `placeholder` | `string` | — | 编辑模式下的占位文字 |
| `height` | `number` | `400` | 编辑/预览区域高度（px） |
