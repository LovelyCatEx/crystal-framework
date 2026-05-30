# Image Resource

`ImageResource` — automatically loads and displays an image based on a file resource ID. More flexible than `AvatarResource`, supporting `className`, custom dimensions, and antd `Image` preview.

## Usage

```tsx
import { ImageResource } from '@/components/ImageResource.tsx';

// Default thumbnail (60px width, auto height)
<ImageResource fileEntityId={row.id} />

// Custom className and style
<ImageResource fileEntityId={row.id} className="rounded-md" width={80} />

// Landscape-friendly with explicit width, auto height
<ImageResource fileEntityId={row.id} width={80} />

// With fallback and preview disabled
<ImageResource fileEntityId={row.id} width={80} fallback="/placeholder.png" preview={false} />
```

## Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `fileEntityId` | `string \| null \| undefined` | — | File resource ID. Pass `null`/`undefined` to render nothing |
| `className` | `string` | — | CSS class name |
| `width` | `number \| string` | `60` | Image width |
| `height` | `number \| string` | — | Image height. When omitted, auto-scales to maintain aspect ratio |
| `fallback` | `string` | — | Fallback image URL when the source fails to load |
| `style` | `CSSProperties` | — | Inline CSS style |
| `preview` | `object \| boolean` | — | antd Image preview config. Pass `false` to disable preview. See [antd Image](https://ant.design/components/image#image-demo-preview-group) |

## Behavior

- Valid `fileEntityId`: Automatically calls `getResourceFileDownloadUrlById` to fetch the download URL, cached via SWR
- `null`/`undefined`: Renders nothing
- Request fails or no URL: Renders nothing (or the `fallback` image if provided)
- Image retains its original aspect ratio when `height` is not set
