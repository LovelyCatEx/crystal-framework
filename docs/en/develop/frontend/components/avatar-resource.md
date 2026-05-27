# Avatar Resource

`AvatarResource` — automatically loads and displays an avatar based on a file resource ID. Uses SWR caching internally — duplicate requests for the same `fileEntityId` are avoided.

## Usage

```tsx
import { AvatarResource } from '@/components/AvatarResource.tsx';

// Show avatar by file resource ID
<AvatarResource fileEntityId={user.avatarFileEntityId} />

// Show default icon when no avatar
<AvatarResource fileEntityId={null} defaultIcon={<TeamOutlined />} />
```

## Props

| Prop | Type | Description |
|------|------|-------------|
| `fileEntityId` | `string \| null \| undefined` | File resource ID. Pass `null`/`undefined` to show the default icon |
| `defaultIcon` | `ReactNode` | Default icon when no avatar, defaults to `<UserOutlined />` |

## Behavior

- Valid `fileEntityId`: Automatically calls `getResourceFileDownloadUrlById` to fetch the download URL, cached via SWR
- `null`/`undefined`: Shows the default icon
- Request fails or no URL: Shows the default icon
