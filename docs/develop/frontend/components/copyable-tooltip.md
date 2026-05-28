# 可复制气泡

`CopyableToolTip` —— 悬浮提示气泡，内含复制按钮，点击即可将文本复制到剪贴板。

## 使用

```tsx
import { CopyableToolTip } from '@/components/CopyableToolTip.tsx';

// 包裹文本元素
<CopyableToolTip title="hello@example.com">
    <span>联系邮箱</span>
</CopyableToolTip>

// 仅作为可复制的 tooltip
<CopyableToolTip title="192.168.1.1">
    <Tag>IP 地址</Tag>
</CopyableToolTip>
```

## Props

| 参数 | 类型 | 说明 |
|------|------|------|
| `title` | `string \| ReactNode` | 提示文字，也作为复制内容 |
| `children` | `ReactNode` | 被包裹的触发元素 |

::: tip 国际化
示例中的文字标签应使用 i18n key，key 放在所属页面或组件的命名空间下，详见[国际化](../i18n)。
:::
