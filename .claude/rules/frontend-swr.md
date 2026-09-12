---
paths:
  - "web/src/**/*.tsx"
  - "web/src/**/*.ts"
---

# SWR 数据缓存刷新规则

本项目使用 SWR，全局默认 `revalidateOnFocus: false`（见 `App.tsx` 的 `<SWRConfig>`）。

刷新时机有两类，**必须组合使用**。

## （一）被动 revalidate（缓存的定义方负责，声明式）

对"数据源可能被其他页面/操作变更"的 SWR，定义方在 `useSWR(...)` 里用第三个参数覆盖全局默认值，声明它自己的自动 revalidate 时机：

- `revalidateOnFocus: true`：tab/窗口重新聚焦时刷新（覆盖全局 `false`）
- `refreshInterval: N`：按毫秒周期轮询（例：`use-total-unread.ts` 的 30_000）
- `revalidateOnMount: true`：每次组件挂载时刷新

这一层是**保底** —— 用户切走再切回、或组件重挂载时都能拿到新数据，但**不能覆盖"同 tab 内改完立刻要生效"的场景**。

## （二）主动 invalidate（缓存的写入方负责，命令式）

当一个 SWR 的数据源被某个明确的写入操作修改（如系统设置保存、维护模式切换），**写入方必须在成功回调里主动刷相关 SWR**，让订阅方立刻拿到新值。

### 写法

1. **拥有该 SWR 的 hook/context 文件必须导出 SWR key 常量**，命名以 `SWR_KEY_` 前缀开头：

```typescript
// web/src/context/SystemIntegratedContext.tsx
export const SWR_KEY_SYSTEM_INTEGRATED_INFO = 'systemIntegratedInfo';
```

2. **写入方 import 该 key + swr 的 `mutate`，成功后按 key 刷**：

```typescript
import {mutate as swrMutate} from "swr";
import {SWR_KEY_SYSTEM_INTEGRATED_INFO} from "@/context/SystemIntegratedContext.tsx";

updateSystemSettings(props).then(() => {
    void swrMutate(SWR_KEY_SYSTEM_INTEGRATED_INFO)
})
```

## 规则细节

### 禁止 hardcode SWR key 字符串
所有 `mutate('xxx')` 的 key 都必须来自定义方 `export const SWR_KEY_XXX`。

key 只有一处定义，所有权仍然属于定义它的 hook/context。

### 禁止封装 revalidateXxxInfo() 函数
禁止封装 `revalidateXxxInfo()` 之类的公共刷新函数放在 hook/context 里让写入方调用。

直接用 `swrMutate(SWR_KEY_XXX)` 即可，不需要一层无意义的封装。

### 禁止只做被动 revalidate 而不做主动 invalidate
像"改完系统设置立刻要生效"这类场景，光靠 `revalidateOnFocus` 拿不到最新值（用户没切走窗口），必须在写入回调里主动 `swrMutate(...)`。

### 一个写入可能牵连多个 SWR
例如系统设置保存同时影响 `SWR_KEY_SYSTEM_INTEGRATED_INFO`（模块开关、水印、OAuth 平台、维护标志）和 `SWR_KEY_SYSTEM_MAINTENANCE_STATUS`（维护状态端点），必须把关联的 key 全部刷一遍。

写入方要清楚自己动了哪些数据源，一个都不能漏。

## 例外：自己刷自己

`use-total-unread.ts` / `use-broadcast-inbox.ts` 里 `globalMutate(自己的 KEY)` **仅用于让同文件内其他函数触发自身的 revalidate**（自己刷自己），不属于跨模块推刷新，允许。

判断标准：`globalMutate` 的 key 必须是**同一个文件里定义的常量**，且刷的是同一个 hook 暴露出去的 SWR。
