# 图片资源

`ImageResource` —— 根据文件资源 ID 自动加载并展示图片。比 `AvatarResource` 更灵活，支持 `className`、自定义尺寸和 antd `Image` 预览。

## 使用

```tsx
import { ImageResource } from '@/components/ImageResource.tsx';

// 默认缩略图（60px 宽，高度自适应）
<ImageResource fileEntityId={row.id} />

// 自定义 className 和样式
<ImageResource fileEntityId={row.id} className="rounded-md" width={80} />

// 横图友好：固定宽度，高度自动按比例缩放
<ImageResource fileEntityId={row.id} width={80} />

// 设置 fallback 并禁用预览
<ImageResource fileEntityId={row.id} width={80} fallback="/placeholder.png" preview={false} />
```

## Props

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `fileEntityId` | `string \| null \| undefined` | — | 文件资源 ID，传 `null`/`undefined` 时不渲染 |
| `className` | `string` | — | CSS 类名 |
| `width` | `number \| string` | `60` | 图片宽度 |
| `height` | `number \| string` | — | 图片高度，不传时自动按比例缩放 |
| `fallback` | `string` | — | 加载失败时的占位图 URL |
| `style` | `CSSProperties` | — | 内联 CSS 样式 |
| `preview` | `object \| boolean` | — | antd Image 预览配置，传 `false` 禁用预览，详见 [antd Image](https://ant.design/components/image-cn#image-demo-preview-group) |

## 行为

- 传入有效 `fileEntityId`：自动调用 `getResourceFileDownloadUrlById` 获取下载链接，通过 SWR 缓存结果
- 传入 `null`/`undefined`：不渲染任何内容
- 请求失败或无 URL：不渲染（或显示 `fallback` 图片）
- 不设 `height` 时保持图片原始宽高比
