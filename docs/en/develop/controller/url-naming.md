# URL Naming Convention

Manager Controller URLs are three-segment: `${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/<resource>/<action>`. A single convention applies to all three segments.

## Rules

- All URL path segments use **kebab-case** (lowercase letters, digits, `-`)
- The resource segment is **singular**: `user` / `oauth-account` / `mail-send-log`, not `users` / `mail-send-logs`
- The action segment reuses the base-class methods `list` / `create` / `query` / `update` / `delete`; custom endpoints must also be kebab-case
- Single-word custom endpoints may be used as-is (`/my` / `/tree` / `/start` / `/handle`)
- Uppercase letters, underscores, camelCase, and PascalCase are forbidden in URL paths

## Examples

Good:

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

Bad:

```kotlin
// Plural resource segment
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/approval-flow-definitions")

// camelCase custom endpoint
@PostMapping("/updateGraph")
@GetMapping("/detailsById")
```

## Frontend alignment

Frontend `.api.ts` files must stay in sync — both the `BaseManagerController` constructor argument and any hardcoded `doGet` / `doPost` paths:

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

## Automated enforcement

The integration test `ControllerUrlConventionTest` in `crystal-starter` scans every `@RequestMapping` / `@GetMapping` / `@PostMapping` / `@PutMapping` / `@DeleteMapping` / `@PatchMapping` path value from the Spring context and fails when it detects uppercase letters or underscores.
