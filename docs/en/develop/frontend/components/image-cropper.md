# Image Cropper

`ImageCropper` — a Canvas-based image cropping modal component supporting zoom, rotation, drag, and rectangular/circular crop shapes.

## Usage

```tsx
import { ImageCropper } from '@/components/ImageCropper.tsx';
import { useState } from 'react';

function MyComponent() {
    const [open, setOpen] = useState(false);
    const [imageUrl, setImageUrl] = useState('');

    const handleCrop = (blob: Blob) => {
        // blob is the cropped JPEG image
        const url = URL.createObjectURL(blob);
        // upload or preview...
    };

    return (
        <ImageCropper
            open={open}
            imageUrl={imageUrl}
            onCancel={() => setOpen(false)}
            onConfirm={handleCrop}
            aspectRatio={1}
            shape="circle"
            title="Crop Avatar"
        />
    );
}
```

## Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `open` | `boolean` | — | Whether the crop modal is visible |
| `imageUrl` | `string` | — | Source image URL |
| `onCancel` | `() => void` | — | Cancel callback |
| `onConfirm` | `(blob: Blob) => void` | — | Confirm callback, receives the cropped JPEG Blob |
| `aspectRatio` | `number` | `1` | Crop area aspect ratio |
| `shape` | `'rect' \| 'circle'` | `'rect'` | Crop shape |
| `title` | `string` | — | Modal title |
| `confirmText` | `string` | — | Confirm button text |
| `cancelText` | `string` | — | Cancel button text |
| `minZoom` | `number` | `0.5` | Minimum zoom level |
| `maxZoom` | `number` | `3` | Maximum zoom level |
| `quality` | `number` | `0.9` | JPEG output quality (0-1) |
