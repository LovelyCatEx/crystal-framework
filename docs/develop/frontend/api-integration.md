# API 对接

## 目录规范

```
api/
  my-plugin.api.ts    — API 调用函数 + 内联 DTO
types/
  my-plugin.types.ts  — 响应 VO 类型
```

- **DTO**（请求参数接口）内联在 `*.api.ts` 中
- **VO**（后端返回的数据结构）定义在 `types/*.types.ts` 中

例：

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

## 路径说明

- 前端 API 路径以 `/api/` 开头（如 `/api/ext/system-monitor/metrics/CPU_USAGE`）
- 开发环境下 Vite proxy 自动映射到后端 `/api/v1/...`
- 后端 Controller 使用 `GlobalConstants.REQUEST_MAPPING_PREFIX`

## 前端 Menu 与后端权限的关系

这是最重要的对应关系：

```
前端 addAdminMenu path        → 后端 SystemRbacPermissionDeclaration.menu name
  /manager/ext/my-page         →    my.plugin.page:/manager/ext/my-page
                                  （name 的 path 部分与前端 path 一致）
```

即 `SystemRbacPermissionDeclaration.menu` 的 `path` 参数必须与前端 `addAdminMenu` 的 `path` 一致，这样权限系统才能识别用户能否访问该菜单。

详见[系统权限](../sdk/system-permission)。
