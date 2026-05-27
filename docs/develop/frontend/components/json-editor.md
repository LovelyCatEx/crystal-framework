# JSON 编辑器

`JsonEditor` —— 可视化 + 源码双模式的 JSON 编辑器。支持嵌套对象/数组的增删改、类型切换、键重命名和实时校验。

## 使用

```tsx
import { JsonEditor } from '@/components/JsonEditor.tsx';

<JsonEditor
    value='{"name": "Alice", "age": 25}'
    onChange={(json) => console.log(json)}
    placeholder="请输入 JSON"
/>
```

## Props

| 参数 | 类型 | 说明 |
|------|------|------|
| `value` | `string` | JSON 字符串，默认为 `'{}'` |
| `onChange` | `(value: string) => void` | 值变化回调，返回格式化后的 JSON 字符串 |
| `placeholder` | `string` | 源码模式下的占位文字 |

## 功能

- **可视化模式**：逐节点编辑，支持展开/折叠、添加子节点、删除、改键名、切换类型
- **源码模式**：直接编辑 JSON 文本，实时校验语法
- 支持 6 种值类型：`string`、`number`、`boolean`、`object`、`array`、`null`
- 底部状态栏实时显示 JSON 是否合法
