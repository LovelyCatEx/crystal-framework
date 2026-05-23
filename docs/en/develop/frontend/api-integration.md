# API Integration

## Directory Convention

```
api/
  my-plugin.api.ts    — API functions + inline DTOs
types/
  my-plugin.types.ts  — Response VO types
```

- **DTOs** (request interfaces) are inlined in `*.api.ts`
- **VOs** (response data structures) are defined in `types/*.types.ts`

Example:

```typescript
// types/my-plugin.types.ts
export interface MetricPoint {
    value: number;
    timestamp: number;
}

// api/my-plugin.api.ts
import {doGet} from "@/api/system-request.ts";
import type {MetricQueryResponse} from "../types/my-plugin.types.ts";

export async function queryMetric(type: string, duration: string = "1m") {
    return doGet<{ metricType: string; data: MetricPoint[] }>(
        "/api/ext/system-monitor/metrics/" + type,
        {duration},
    );
}
```

## URL Path

- Frontend API paths start with `/api/` (e.g. `/api/ext/system-monitor/metrics/CPU_USAGE`)
- In development, Vite proxy maps them to backend `/api/v1/...`
- Backend controllers use `GlobalConstants.REQUEST_MAPPING_PREFIX`

## Frontend Menu ↔ Backend Permission Mapping

This is the critical relationship:

```
Frontend addAdminMenu path    → Backend SystemRbacPermissionDeclaration.menu
  /manager/ext/my-page        →   my.plugin.page:/manager/ext/my-page
                                  (the path part must match the frontend path)
```

The `path` parameter in `SystemRbacPermissionDeclaration.menu` must match the frontend `addAdminMenu.path`, so the permission system can determine whether the user has access to that menu item.

See [System Permission](../sdk/system-permission).
