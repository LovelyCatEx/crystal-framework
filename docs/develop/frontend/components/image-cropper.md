# 图片裁剪器

`ImageCropper` —— 基于 Canvas 的图片裁剪弹窗组件，支持缩放、旋转、拖拽和矩形/圆形裁剪。

## 使用

```tsx
import { ImageCropper } from '@/components/ImageCropper.tsx';
import { useState } from 'react';

function MyComponent() {
    const [open, setOpen] = useState(false);
    const [imageUrl, setImageUrl] = useState('');

    const handleCrop = (blob: Blob) => {
        // blob 为裁剪后的 JPEG 图片
        const url = URL.createObjectURL(blob);
        // 上传或预览...
    };

    return (
        <ImageCropper
            open={open}
            imageUrl={imageUrl}
            onCancel={() => setOpen(false)}
            onConfirm={handleCrop}
            aspectRatio={1}
            shape="circle"
            title="裁剪头像"
        />
    );
}
```

## Props

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `open` | `boolean` | — | 是否显示裁剪弹窗 |
| `imageUrl` | `string` | — | 原始图片 URL |
| `onCancel` | `() => void` | — | 取消回调 |
| `onConfirm` | `(blob: Blob) => void` | — | 确认回调，返回裁剪后的 JPEG Blob |
| `aspectRatio` | `number` | `1` | 裁剪区域宽高比 |
| `shape` | `'rect' \| 'circle'` | `'rect'` | 裁剪形状 |
| `title` | `string` | — | 弹窗标题 |
| `confirmText` | `string` | — | 确认按钮文字 |
| `cancelText` | `string` | — | 取消按钮文字 |
| `minZoom` | `number` | `0.5` | 最小缩放比例 |
| `maxZoom` | `number` | `3` | 最大缩放比例 |
| `quality` | `number` | `0.9` | JPEG 输出质量（0-1） |
