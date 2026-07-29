# URL 命名规范

Manager Controller 的 URL 结构为三段式：`${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/<resource>/<action>`。三段拼起来只有一套命名规范。

## 规则

- 所有 URL path 段一律使用 **kebab-case**（小写字母、数字、`-`）
- 资源段使用**单数**形式：`user` / `oauth-account` / `mail-send-log`，不使用 `users` / `mail-send-logs`
- 动作段沿用 base 类提供的 `list` / `create` / `query` / `update` / `delete`；自定义额外端点也必须 kebab-case
- 单个单词的额外端点可直接使用（`/my` / `/tree` / `/start` / `/handle`）
- 禁止在 URL path 中出现大写字母、下划线、camelCase、PascalCase

## 示例

正例：

```kotlin
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/oauth-account")
class ManagerOAuthAccountController : StandardManagerController<...>

@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/approval-flow-definition")
class ManagerApprovalFlowDefinitionController : StandardScopedManagerController<...> {
    @PostMapping("/update-graph")
    suspend fun updateGraph(...): ApiResponse<*> = ...

    @GetMapping("/details-by-id")
    suspend fun getDetails(...): ApiResponse<*> = ...
}
```

反例：

```kotlin
// 资源段使用复数
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/approval-flow-definitions")

// 额外端点使用 camelCase
@PostMapping("/updateGraph")
@GetMapping("/detailsById")
```

## 前端对齐

前端 `.api.ts` 中 `BaseManagerController` 构造参数、`doGet` / `doPost` 硬编码路径必须与后端保持一致：

```typescript
class Controller extends BaseManagerController<...> {
    constructor() {
        super('/manager/approval-flow-definition');
    }
}

export async function getDetails(id: string) {
    return doGet('/api/manager/approval-flow-definition/details-by-id', { id });
}
```

## 自动化保障

`crystal-starter` 集成测试 `ControllerUrlConventionTest` 会扫描 Spring context 中所有 `@RequestMapping` / `@GetMapping` / `@PostMapping` / `@PutMapping` / `@DeleteMapping` / `@PatchMapping` 的 path 值，命中大写字母或下划线时测试失败。
