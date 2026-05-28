# 页面标题栏

`ActionBarComponent` —— 页面顶部的标题栏组件，展示标题、副标题和右侧操作区。

## 使用

```tsx
import { ActionBarComponent } from '@/components/ActionBarComponent.tsx';

<ActionBarComponent
    title="用户管理"
    subtitle="管理系统中的所有用户"
    titleActions={
        <Button type="primary" icon={<PlusOutlined />}>新增用户</Button>
    }
/>
```

## Props

| 参数 | 类型 | 说明 |
|------|------|------|
| `title` | `string` | 主标题 |
| `subtitle` | `string` | 副标题，可选 |
| `titleActions` | `ReactNode` | 标题右侧的操作区，放置按钮等控件 |
