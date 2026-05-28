# 标准卡片

`StandardCard` —— 基于 antd `Card` 的标准卡片容器，预设了无边框、圆角、阴影样式。

## 使用

```tsx
import { StandardCard } from '@/components/card/StandardCard.tsx';

<StandardCard>
    <p>卡片内容</p>
</StandardCard>

// 也可以覆盖 className
<StandardCard className="mt-4">
    <EntityTable ... />
</StandardCard>
```

## Props

继承 antd `Card` 的所有 props，额外预设：

- `className` 会追加到内置的 `border-none shadow-sm rounded-2xl overflow-hidden` 之后
