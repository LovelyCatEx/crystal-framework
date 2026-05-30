# Generic Controller

## Overview

A generic controller is a standard Spring `@RestController` that does not extend any framework base class. Use it when your business logic doesn't fit the CRUD template.

## When to Use

- Non-CRUD operations (login, registration, upload)
- Business APIs with custom permission logic
- Public-facing (non-admin) endpoints

## Comparison with ManagerController

| | ManagerController | Generic Controller |
|--|--|--|
| Base class | Extends `StandardManagerController` | None, pure Spring |
| Permission check | `@ManagerPermissions` + framework AOP | `@PreAuthorize` |
| Endpoints | 5 standard endpoints from base class | Fully custom |
| Method body | Typically zero | Hand-written |

## Usage

### Create the Controller Directly

```kotlin
@Validated
@RestController
@RequestMapping("\${GlobalConstants.REQUEST_MAPPING_PREFIX}/ext/my-plugin/custom")
class ExtMyPluginCustomController(
    private val myService: ExtMyPluginService,
) {
    @PreAuthorize("hasAnyAuthority('ext_my_plugin.custom_execute')")
    @PostMapping("/execute")
    suspend fun execute(
        userAuthentication: UserAuthentication,
        @RequestBody body: Map<String, Any>
    ): ApiResponse<*> {
        myService.execute(body)
        return ApiResponse.success(null)
    }

    @GetMapping("/status")
    suspend fun status(): ApiResponse<*> {
        return ApiResponse.success(mapOf("online" to true))
    }
}
```

### Register Permissions

Register the permission constants in a `SystemRbacConfigurer`:

```kotlin
@Component
class MyPermissionConfigurer : SystemRbacConfigurer {
    override fun configure(registry: SystemRbacRegistry) {
        registry.registerPermission(
            SystemRbacPermissionDeclaration.action(
                "ext_my_plugin.custom_execute",
                "Execute custom operation",
                group = "ext_my_plugin"
            )
        )
    }
}
```

::: warning Return Type

**All controller methods must explicitly return `ApiResponse<*>`** using `ApiResponse.success(data)` or `ApiResponse.failed(message)`. Never return raw entities, List, Map, or other primitive types — the framework does not provide a uniform wrapping layer, and the frontend `doGet` / `doPost` rely on the `ApiResponse` structure (`code`, `message`, `data`).
:::

## Key Points

- `UserAuthentication` is auto-injected as a method parameter by the framework
- Endpoints without `@PreAuthorize` still require a valid token. For fully public access (no login), add `@Unauthorized` to the method
- Always use `ApiResponse.success(...)` / `ApiResponse.error(...)` for return values
