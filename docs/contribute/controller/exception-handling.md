# 异常处理

## 设计概述

异常处理由两道防线构成：`GlobalExceptionHandler`（`@RestControllerAdvice`）拦截常规 Controller 异常，`GlobalErrorWebExceptionHandler`（`AbstractErrorWebExceptionHandler`）兜底处理路由级错误。两者均将异常统一转换为 `ApiResponse`。

## GlobalExceptionHandler 源码分析

位于 `crystal-shared` 模块的 `com.lovelycatv.crystalframework.shared.exception` 包：

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

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ApiResponse<*> =
        ApiResponse.badRequest(e.localizedMessage ?: e.message ?: "bad request")
    // ... 其他异常类似
}
```

映射关系：

| 异常 | 转换方法 | code |
|------|---------|------|
| `BusinessException` | `ApiResponse.badRequest()` | 400 |
| `ForbiddenException` | `ApiResponse.forbidden()` | 403 |
| `UnauthorizedException` | `ApiResponse.unauthorized()` | 401 |
| `AuthorizationDeniedException` | `ApiResponse.forbidden()` | 403 |
| `WebExchangeBindException` | `ApiResponse.badRequest()`（拼接字段错误信息） | 400 |
| `MissingRequestValueException` | `ApiResponse.badRequest()`（标注缺失参数名） | 400 |
| 其他 `Exception` | `ApiResponse.internalServerError()` | 500 |

设计的两个特点：

- **`handle()` 方法可被外部调用**：`GlobalErrorWebExceptionHandler` 直接调用此方法复用转换逻辑，避免重复
- **日志分级**：已知业务异常（BusinessException 等）使用 `logger.debug`，未知异常使用 `logger.error`，减少预期内的日志噪音

## GlobalErrorWebExceptionHandler 源码分析

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
                    .bodyValue(globalExceptionHandler.handle(error))
                else -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .bodyValue(ApiResponse.internalServerError(...))
            }
        }
    }
}
```

`AbstractErrorWebExceptionHandler` 是 Spring WebFlux 提供的最后一道防线，处理：

- 路由不匹配（404）
- Filter 中抛出的异常
- GlobalExceptionHandler 未覆盖的场景

设置 `@Order(-2)` 使其优先级高于 Spring Boot 默认的错误处理器。

## 异常类简介

| 类 | 位置 | 用途 |
|---|------|------|
| `BusinessException` | `crystal-shared/exception` | 业务校验失败 |
| `ForbiddenException` | `crystal-shared/exception` | 无权限 |
| `UnauthorizedException` | `crystal-shared/exception` | 未认证 |

三者均继承 `RuntimeException`，可携带自定义 message。
