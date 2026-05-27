# API Integration

## Directory Convention

Recommended structure for plugin frontend files:

```
src/
  api/
    my-plugin.api.ts       — API functions with inline DTOs
  types/
    my-plugin.types.ts     — Response VO type definitions
```

- **DTOs** (request interfaces) are inlined in `*.api.ts`, kept alongside the functions that use them
- **VOs / Entities** (response data structures) are defined in `types/*.types.ts` for use by page components

## Making Requests with system-request

The framework provides `system-request.ts`, which handles auth header injection and unified error handling. Use its exported functions directly — no need to manage tokens or error toasts manually.

```typescript
import { doGet, doPost, doPut, doDelete } from "@/api/system-request.ts";
```

### Header Pre-processing

Every request automatically injects the following headers — no manual setup needed:

- `Authorization: Bearer <token>` — read from local storage for the current session
- POST / PUT / PATCH requests automatically set `Content-Type: application/x-www-form-urlencoded`

To add custom headers, pass them as the third argument `headers`. They are merged with the defaults; duplicate keys are overridden by your values.

::: tip Content-Type and backend annotation mapping
Most framework endpoints use `@ModelAttribute` to receive parameters, which corresponds to `application/x-www-form-urlencoded`. Using `doPost` / `doPut` / `doPatch` directly works out of the box.

A small number of endpoints (e.g. batch role/permission assignment that pass arrays or nested objects) use `@RequestBody` and expect `application/json`. Override `Content-Type` via the `headers` argument in those cases:

```typescript
doPost('/api/xxx', payload, { 'Content-Type': 'application/json' });
```
:::

### Available Functions

| Function | HTTP Method | Notes |
|----------|-------------|-------|
| `doGet<T>(url, query?, headers?)` | GET | `query` is a URL query params object |
| `doPost<T>(url, body?, headers?)` | POST | `body` is a form params object |
| `doPut<T>(url, body?, headers?)` | PUT | `body` is a form params object |
| `doPatch<T>(url, body?, headers?)` | PATCH | `body` is a form params object |
| `doDelete<T>(url, query?, headers?)` | DELETE | `query` is a URL query params object |

### Return Value

All functions return `Promise<ApiResponse<T>>`:

```typescript
interface ApiResponse<T> {
    code: number;     // 200 means success
    message: string;  // status message
    data: T | null;   // response payload
}
```

On success (`code === 200`), use `.data` directly.

### Error Handling

The framework has built-in unified error handling — no per-call handling needed:

- **401**: Automatically shows a session expired toast and redirects to the login page
- **403**: Automatically shows a forbidden toast
- **Other errors**: Automatically shows the `message` from the response or a generic error toast, and throws

### Example

```typescript
// types/my-plugin.types.ts
export interface MyItem {
    id: string;
    name: string;
    createdTime: number;
}

// api/my-plugin.api.ts
import { doGet, doPost } from "@/api/system-request.ts";
import type { MyItem } from "@/types/my-plugin.types.ts";

// GET request
export async function getMyItem(id: string) {
    return doGet<MyItem>("/api/ext/my-plugin/item", { id });
}

// POST request
export async function createMyItem(name: string) {
    return doPost<MyItem>("/api/ext/my-plugin/item", { name });
}
```

## URL Paths

- Frontend API paths start with `/api/` (e.g. `/api/ext/my-plugin/item`)
- In development, Vite proxy maps `/api/` to the backend automatically
- Backend controllers use `GlobalConstants.REQUEST_MAPPING_PREFIX` as the path prefix

## Frontend Menu ↔ Backend Permission Mapping

```
Frontend addAdminMenu path    → Backend SystemRbacPermissionDeclaration.menu name
  /manager/ext/my-page        →   my.plugin.page:/manager/ext/my-page
```

The `path` in `SystemRbacPermissionDeclaration.menu` must match the frontend `addAdminMenu.path` exactly for the permission system to work correctly.

See [System Permission](../sdk/system-permission).
