# 异常处理

## 设计概述

异常处理由两道防线构成：

1. `GlobalExceptionHandler`（`@RestControllerAdvice`）拦截 Controller 方法抛出的异常
2. `GlobalErrorWebExceptionHandler`（`AbstractErrorWebExceptionHandler`）兜底路由级错误——路由不匹配、Filter 中抛出的异常、`GlobalExceptionHandler` 未覆盖的场景

两者最终都调用 `ApiResponse` 的工厂方法产出统一响应。

## GlobalExceptionHandler 源码

位于 `crystal-shared/exception/GlobalExceptionHandler.kt`：

```kotlin
@Component
@RestControllerAdvice
class GlobalExceptionHandler(private val auditEventRepository: AuditEventRepository) {

    fun handle(e: Exception): ApiResponse<*> = when (e) {
        is ForbiddenException -> handleForbiddenException(e)
        is UnauthorizedException -> handleUnauthorizedException(e)
        is BusinessException -> handleBusinessException(e)
        is MissingRequestValueException -> handleMissingRequestValueException(e)
        is AuthorizationDeniedException -> handleAuthorizationDeniedException(e)
        is WebExchangeBindException -> handleWebExchangeBindException(e)
        else -> handleException(e)
    }

    // 每个具体的 handler 用 @ExceptionHandler 注解重复暴露一遍
    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ApiResponse<*> = ...
}
```

### 双重暴露：when 分派 + @ExceptionHandler

同一 handler 方法既被 `handle()` 里的 `when` 表达式调用，又用 `@ExceptionHandler` 暴露给 Spring。这不是重复，而是为了让 `GlobalErrorWebExceptionHandler` 复用：

- `@RestControllerAdvice` 只能拦截**方法调用链内部**抛出的异常
- Filter、路由匹配、认证阶段的异常不会进入 `@ExceptionHandler`
- `GlobalErrorWebExceptionHandler` 捕获这些异常后直接调 `handle(error as Exception)`，复用同一套映射，避免维护两份 when

### 异常映射表

| 异常 | 转换方法 | code | 日志级别 |
|---|---|---|---|
| `BusinessException` | `ApiResponse.badRequest()` | 400 | debug |
| `ForbiddenException` | `ApiResponse.forbidden()` | 403 | debug |
| `UnauthorizedException` | `ApiResponse.unauthorized()` | 401 | debug |
| `AuthorizationDeniedException` | `ApiResponse.forbidden()` | 403 | debug |
| `WebExchangeBindException` | `ApiResponse.badRequest()`（拼接 field errors） | 400 | debug |
| `MissingRequestValueException` | `ApiResponse.badRequest()`（标注缺失参数名） | 400 | debug |
| `ServerWebInputException` | `ApiResponse.badRequest()` | 400 | debug |
| `DuplicateKeyException` | `ApiResponse.badRequest("duplicate resource id")` | 400 | debug |
| 其他 `Exception` | `ApiResponse.internalServerError()` | 500 | error |

### 日志分级

已知业务异常打 debug，未知异常打 error。避免 `BusinessException` 之类高频业务错误刷屏，只有未预期异常才触发告警。

## GlobalErrorWebExceptionHandler 源码

```kotlin
@Component
@Order(-2)
class GlobalErrorWebExceptionHandler(
    errorAttributes: ErrorAttributes,
    webProperties: WebProperties,
    applicationContext: ApplicationContext,
    private val globalExceptionHandler: GlobalExceptionHandler
) : AbstractErrorWebExceptionHandler(errorAttributes, webProperties.resources, applicationContext) {

    override fun getRoutingFunction(errorAttributes: ErrorAttributes): RouterFunction<ServerResponse> {
        return RouterFunctions.route(RequestPredicates.all()) { request ->
            val error = getError(request)
            when (error) {
                is Exception -> ServerResponse.status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(globalExceptionHandler.handle(error))

                else -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(ApiResponse.internalServerError<Nothing>(...))
            }
        }
    }
}
```

### 关键点

- `@Order(-2)`：优先级高于 Spring Boot 默认的 `DefaultErrorWebExceptionHandler`（默认 `-1`），保证业务响应格式统一
- 返回 HTTP 200 + JSON body：即使业务错误，HTTP 状态仍为 200，前端只需按 `code` 字段判断
- `else` 分支返回 HTTP 500：`getError(request)` 返回的可能不是 `Exception`（例如 `Error`），此类严重错误走 HTTP 500 触发监控告警
- 复用 `globalExceptionHandler.handle(...)`：映射逻辑集中在 `GlobalExceptionHandler`，本类仅做转发

## 异常类结构

三个自定义异常都在 `crystal-shared.exception` 包：

```kotlin
// BusinessException
open class BusinessException(
    message: String = "",
    cause: Exception? = null
) : RuntimeException(message, cause)

// UnauthorizedException / ForbiddenException 结构相同
```

设计要点：

- `open` 而非 `final`：允许业务模块继承出更具体的异常类（如 `TenantMemberQuotaExceededException`）
- `message` 默认空字符串：避免 null，让 handler 里的 `?: e.message ?: "..."` 拿到确定值
- `cause: Exception?`：便于包装底层异常保留堆栈链

## 与其他基础设施的耦合

- `ManagerControllerPermissionAspect` 抛 `AuthorizationDeniedException`，由 `handleAuthorizationDeniedException` 转 403
- Spring Security 认证失败抛出的异常也走 `AuthorizationDeniedException` 路径
- `@Valid` + JSR-303 校验失败抛 `WebExchangeBindException`，`handleWebExchangeBindException` 拼接每个字段的 `defaultMessage`
- R2DBC 唯一键冲突抛 `DuplicateKeyException`，转 `"duplicate resource id"` 400

## 现有使用位置

- 全局注册为 `@Component` + `@RestControllerAdvice`，业务代码不引用
- `GlobalErrorWebExceptionHandler` 也是 `@Component`，Spring Boot 自动挂载到 WebFlux 错误处理链
- 业务代码只关心抛哪种异常
