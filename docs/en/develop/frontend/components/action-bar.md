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

::: tip i18n
The title, subtitle, and button text in the examples should use i18n keys. Page-level keys go under `pages.{pageName}` (e.g., `pages.userManager.title`). See [i18n](../i18n) for details.
:::
