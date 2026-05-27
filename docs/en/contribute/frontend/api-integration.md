# API Integration

## Directory Convention

Frontend API-related files in the source code are organized as follows:

```
web/src/
  api/
    system-request.ts          — Base request layer (auth, encryption, error handling)
    request.ts                 — Axios instance
    <module>/
      <module>.api.ts          — Module API functions with inline DTOs
  types/
    <module>/
      <module>.types.ts        — Response VO / Entity type definitions
```

- **DTOs** (request interfaces) are inlined in the corresponding `*.api.ts`
- **VOs / Entities** (response data structures) are defined in `types/<module>/*.types.ts`

## system-request.ts

`system-request.ts` is the unified entry point for all API calls. It encapsulates:

### Header Pre-processing

Before each request, `preProcessHeaders` automatically handles the following headers:

- `Authorization: Bearer <token>` — reads the token from local storage and injects it
- Each HTTP method sets the appropriate `Content-Type` automatically:

| Method | Content-Type |
|--------|-------------|
| GET / DELETE | Not set (no request body) |
| POST / PUT / PATCH | `application/x-www-form-urlencoded` |

To add custom headers, pass them as the third argument `headers`. They are merged with the defaults; duplicate keys are overridden by your values.

::: tip Content-Type and backend annotation mapping
Most framework endpoints use `@ModelAttribute` to receive parameters, which corresponds to `application/x-www-form-urlencoded`. Using `doPost` / `doPut` / `doPatch` directly works out of the box.

A small number of endpoints (e.g. batch role/permission assignment that pass arrays or nested objects) use `@RequestBody` and expect `application/json`. Override `Content-Type` via the `headers` argument in those cases:

```typescript
doPost('/api/xxx', payload, { 'Content-Type': 'application/json' });
```
:::

### Unified Error Handling

`handleApiResponse` handles responses by `code`:

| code | Behavior |
|------|----------|
| `200` | Returns normally |
| `401` | Shows session expired toast, redirects to login after 500ms with `redirectTo` param |
| `403` | Shows forbidden toast |
| Other | Shows `message` field or generic error toast, throws the response object |

### Exported Functions

```typescript
doGet<T>(url: string, query?: object, headers?: object): Promise<ApiResponse<T>>
doPost<T>(url: string, body?: object, headers?: object): Promise<ApiResponse<T>>
doPut<T>(url: string, body?: object, headers?: object): Promise<ApiResponse<T>>
doPatch<T>(url: string, body?: object, headers?: object): Promise<ApiResponse<T>>
doDelete<T>(url: string, query?: object, headers?: object): Promise<ApiResponse<T>>
```

GET / DELETE use the second parameter as URL query params. POST / PUT / PATCH use it as a form body (`Content-Type: application/x-www-form-urlencoded`).

### ApiResponse Type

```typescript
interface ApiResponse<T> {
    code: number;
    message: string;
    data: T | null;
}
```

## Adding a New Module API

Using `user-login-log` as an example:

```typescript
// types/user/user-login-log.types.ts
export interface UserLoginLogEntity {
    id: string;
    userId: string;
    username: string;
    loginMethod: number;
    success: boolean;
    remoteIp: string;
    createdTime: number;
    modifiedTime: number;
}

// api/user/user-login-log.api.ts
import { doGet } from "@/api/system-request.ts";
import type { UserLoginLogEntity } from "@/types/user/user-login-log.types.ts";
import type { PaginatedResponseData } from "@/types/api.types.ts";

export interface ManagerReadUserLoginLogDTO {
    page: number;
    pageSize: number;
    userId?: string;
    username?: string;
    loginMethod?: number;
    success?: boolean;
}

export const UserLoginLogManagerController = {
    query: (dto: ManagerReadUserLoginLogDTO) =>
        doGet<PaginatedResponseData<UserLoginLogEntity>>(
            "/api/manager/auth/login-log",
            dto
        ),
};
```

## URL Paths

- Frontend API paths start with `/api/`
- In development, Vite proxy maps `/api/` to the backend — see `web/vite.config.ts`
- Backend controllers use `GlobalConstants.REQUEST_MAPPING_PREFIX` as the path prefix; keep frontend and backend paths aligned

## Frontend Menu ↔ Backend Permission Mapping

```
Frontend addAdminMenu path    → Backend SystemRbacPermissionDeclaration.menu name
  /manager/ext/my-page        →   my.plugin.page:/manager/ext/my-page
```

The `path` in `SystemRbacPermissionDeclaration.menu` must match the frontend `addAdminMenu.path` exactly.

See [System Permission](../system-permission).
