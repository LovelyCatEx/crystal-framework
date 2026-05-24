# 异常处理

## 概述

框架通过 `GlobalExceptionHandler` 全局捕获 Controller 抛出的异常，自动转换为 `ApiResponse`。你只需在业务代码中直接抛出异常，无需手动 try-catch。

## 可用异常

| 异常 | 对应 code | 说明 |
|------|-----------|------|
| `BusinessException(message)` | 400 | 业务校验不通过，前端会展示 message |
| `UnauthorizedException(message)` | 401 | 未登录或 token 过期 |
| `ForbiddenException(message)` | 403 | 无权限访问 |
| 参数校验失败 | 400 | `@Valid` 注解自动触发，无需手动抛出 |

## 使用方式

### 抛业务异常

```kotlin
@Service
class MyService {
    fun getById(id: Long): Entity {
        val entity = repository.findById(id) ?: throw BusinessException("Entity not found")
        return entity
    }
}
```

### 抛权限异常

```kotlin
if (!hasPermission) {
    throw ForbiddenException("You cannot access this resource")
}
```

### 参数校验

使用 `@Valid` 注解 DTO 参数，校验失败由框架自动处理：

```kotlin
@PostMapping("/create")
suspend fun create(
    @ModelAttribute @Valid dto: CreateDTO
): ApiResponse<*> {
    // 如果 dto 校验失败，框架直接返回 400，不会进入方法体
}
```

## 关键点

- 不要自己在 Controller 中 catch 异常后返回 `ApiResponse.badRequest()`，直接 `throw BusinessException` 即可
- `BusinessException` 的 `message` 会透传到前端，文案应面向用户
- 日志由框架统一打印，无需在业务代码中写 `logger.error`
