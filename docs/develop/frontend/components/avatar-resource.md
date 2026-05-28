# 头像资源

`AvatarResource` —— 根据文件资源 ID 自动加载并展示头像。内部使用 SWR 缓存，同一个 `fileEntityId` 不会重复请求。

## 使用

```tsx
import { AvatarResource } from '@/components/AvatarResource.tsx';

// 通过文件资源 ID 显示头像
<AvatarResource fileEntityId={user.avatarFileEntityId} />

// 无头像时显示默认图标
<AvatarResource fileEntityId={null} defaultIcon={<TeamOutlined />} />
```

## Props

| 参数 | 类型 | 说明 |
|------|------|------|
| `fileEntityId` | `string \| null \| undefined` | 文件资源的 ID，传 `null`/`undefined` 时显示默认图标 |
| `defaultIcon` | `ReactNode` | 无头像时的默认图标，默认为 `<UserOutlined />` |

## 行为

- 传入有效 `fileEntityId`：自动调用 `getResourceFileDownloadUrlById` 获取下载链接，通过 SWR 缓存结果
- 传入 `null`/`undefined`：显示默认图标
- 请求失败或无 URL：显示默认图标
