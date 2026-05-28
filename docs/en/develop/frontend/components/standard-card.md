# Standard Card

`StandardCard` — a standard card container based on antd `Card`, preset with borderless, rounded corners, and shadow styling.

## Usage

```tsx
import { StandardCard } from '@/components/card/StandardCard.tsx';

<StandardCard>
    <p>Card content</p>
</StandardCard>

// Override className when needed
<StandardCard className="mt-4">
    <EntityTable ... />
</StandardCard>
```

## Props

Inherits all antd `Card` props, with these preset defaults:

- `className` is appended to the built-in `border-none shadow-sm rounded-2xl overflow-hidden`
