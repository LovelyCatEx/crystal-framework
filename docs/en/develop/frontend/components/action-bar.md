# Page Title Bar

`ActionBarComponent` — a page title bar component that displays a title, subtitle, and optional action area on the right.

## Usage

```tsx
import { ActionBarComponent } from '@/components/ActionBarComponent.tsx';

<ActionBarComponent
    title="User Management"
    subtitle="Manage all users in the system"
    titleActions={
        <Button type="primary" icon={<PlusOutlined />}>Add User</Button>
    }
/>
```

## Props

| Prop | Type | Description |
|------|------|-------------|
| `title` | `string` | Main title |
| `subtitle` | `string` | Subtitle, optional |
| `titleActions` | `ReactNode` | Action area on the right side, e.g. buttons |
