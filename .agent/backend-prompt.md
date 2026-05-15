# Crystal Framework 后端开发 Agent 提示词

## 项目概述

Crystal Framework 是一个基于 **Spring Boot 4.0.2 + Kotlin + WebFlux（响应式）+ R2DBC（PostgreSQL）+ Redis** 的全栈多租户 SaaS 平台后端。使用 Maven 构建，JDK 17，依赖自研库 VertexLib（`com.lovelycatv.vertex`）。

---

## 技术栈

- **框架**: Spring Boot 4.0.2, Spring WebFlux, Spring Security (Reactive)
- **语言**: Kotlin 2.2.21
- **数据库**: PostgreSQL (R2DBC 响应式驱动, r2dbc-postgresql)
- **缓存**: Redis (Reactive, Lettuce)
- **ORM**: Spring Data R2DBC (非 JPA)
- **ID 生成**: Snowflake 分布式 ID（通过 SnowIdGenerator）
- **认证**: JWT (jjwt 0.12.6) + Spring Security + OAuth2 (GitHub/Google/QQ)
- **数据库迁移**: Flyway
- **任务调度**: SnailJob (aizuda)
- **对象存储**: 阿里云 OSS, 腾讯云 COS, 本地文件系统
- **AOP**: Spring AOP (权限检查、审计日志)
- **序列化**: Jackson (tools.jackson + fasterxml)
- **构建**: Maven + kotlin-maven-plugin (spring allopen)

---

## 包结构 (`com.lovelycatv.crystalframework`)

### `shared` — 核心框架基础设施

这是整个项目的基石，所有模块都依赖它。

| 子包 | 用途 |
|------|------|
| `shared/entity/BaseEntity.kt` | 所有实体的基类。包含 `id`(Long, Snowflake), `createdTime`, `modifiedTime`, `deletedTime`(软删除)。实现 `Persistable<Long>`，通过 `isNew` 标记区分新建/更新。ID 序列化为 String（`@JsonSerialize(using = ToStringSerializer::class)`）防止前端精度丢失 |
| `shared/entity/ScopedEntity.kt` | 标记属于某个作用域（如租户）的实体，暴露 `getDirectParentId()` |
| `shared/repository/BaseRepository.kt` | 所有 Repository 的基接口，继承 `R2dbcRepository<ENTITY, Long>`。提供 `findAllByPage`、`searchByKeyword`、`countByKeyword` 默认方法。使用 `#{#tableName}` SpEL 表达式自动解析表名 |
| `shared/service/BaseService.kt` | 基础 Service 接口，提供 `getRepository()`、`getByIdOrNull`、`getByIdOrThrow`、`withUpdateById` |
| `shared/service/BaseManagerService.kt` | **管理端 CRUD 核心**。泛型接口，提供 `query()`（支持分页、关键字搜索、高级查询）、`create()`、`update()`、`batchDelete()`。高级查询通过 `isAdvanceQuery` + `doAdvanceQuery` lambda 实现 |
| `shared/service/EntityRelationshipCheckService.kt` | 实体关系校验接口，用于多租户场景下验证资源归属 |
| `shared/controller/StandardManagerController.kt` | **标准管理端 Controller 基类**。提供 5 个标准端点：`GET /list`、`POST /create`、`GET /query`、`POST /update`、`POST /delete`。所有端点使用 `version = "1"` |
| `shared/controller/dto/` | `BaseManagerReadDTO`(分页+搜索)、`BaseManagerUpdateDTO`(id)、`BaseManagerDeleteDTO`(ids) |
| `shared/request/PageQuery.kt` | 分页参数基类，`page`(>=1)、`pageSize`(0-20) |
| `shared/request/PaginatedResponseData.kt` | 分页响应数据结构 |
| `shared/response/ApiResponse.kt` | 统一 API 响应格式：`{code, message, data}`。状态码：200/400/401/403/500 |
| `shared/annotations/ManagerPermissions.kt` | Controller 级权限注解，声明 read/readAll/create/update/delete 所需权限 |
| `shared/annotations/Unauthorized.kt` | 标记不需要认证的端点 |
| `shared/aspect/ManagerControllerPermissionAspect.kt` | AOP 切面，拦截 StandardManagerController 方法，检查 @ManagerPermissions 声明的权限 |
| `shared/constants/GlobalConstants.kt` | 全局常量：`REQUEST_MAPPING_PREFIX = "/api/{version}"`、Filter/Aspect 优先级 |
| `shared/types/UserAuthentication.kt` | 当前用户认证信息：`userId`, `username`, `tenantId` |
| `shared/utils/SnowIdGenerator.kt` | Snowflake ID 生成器 |
| `shared/utils/JwtUtil.kt` | JWT 工具类（HS512 签名，SHA-512 派生密钥） |
| `shared/config/ShardingConfiguration.kt` | Snowflake 配置 |
| `shared/config/RedisConfig.kt` | Redis 序列化配置（JSON） |
| `shared/config/R2dbcSQLInterceptorConfig.kt` | SQL 拦截器，通过 `CrystalFrameworkSQLModifier` 处理 SQL（如软删除过滤） |
| `shared/exception/BusinessException.kt` | 业务异常 |
| `shared/exception/UnauthorizedException.kt` | 认证异常 |

### `cache` — Redis 缓存层

| 文件 | 用途 |
|------|------|
| `cache/service/CachedBaseService.kt` | 缓存基础接口。提供 `cacheStore`、`listCacheStore`（`ExpiringKVStore`）、自动缓存 `getByIdOrNull`、`updateCache`、`removeCache`、`withUpdateEntityContext`、`withBatchDeleteEntityContext` |
| `cache/service/CachedBaseManagerService.kt` | 继承 BaseManagerService + CachedBaseService，自动在 update/delete 时失效缓存 |
| `cache/store/ServiceCacheStore.kt` | 服务级缓存存储 |
| `cache/event/` | 缓存事件：Created/Updated/Deleted（单个和列表） |
| `cache/config/CacheStoreProviderConfig.kt` | 缓存 Bean 配置 |

### `auth` — 认证与授权

| 文件 | 用途 |
|------|------|
| `auth/config/SecurityConfig.kt` | WebFlux Security 配置。禁用 CORS/CSRF/formLogin/httpBasic，配置 OAuth2 登录，注册 CustomAuthFilter 和 CustomLoginFilter |
| `auth/config/UnauthorizedPathScanner.kt` | 扫描所有 @Unauthorized 注解的端点，生成 PathPattern 列表 |
| `auth/filter/CustomAuthFilter.kt` | JWT 认证过滤器。解析 Authorization header，验证 token，加载用户权限到 SecurityContext |
| `auth/filter/CustomLoginFilter.kt` | 表单登录过滤器。处理 `/api/v1/user/login`，返回 JWT token |
| `auth/service/UserAuthorizationService.kt` | 登录成功响应构建、OAuth2 处理 |
| `auth/stores/JWTSignKeyStore.kt` | JWT 签名密钥存储 |

### `rbac` — 系统级 RBAC

| 文件 | 用途 |
|------|------|
| `rbac/constants/SystemPermission.kt` | **所有系统权限常量**。格式：MENU_xxx = "name:/path"（菜单权限）、ACTION_xxx = "name.action"（操作权限）、COMPONENT_xxx = "name@path"（组件权限） |
| `rbac/constants/SystemRole.kt` | 系统角色常量：ROLE_ROOT, ROLE_ADMIN, ROLE_USER |
| `rbac/constants/SystemRolePermissionRelation.kt` | 角色-权限映射。ROOT 拥有所有权限（反射获取），ADMIN/USER 显式列出 |
| `rbac/types/PermissionType.kt` | 权限类型枚举：ACTION(0), MENU(1), COMPONENT(2) |
| `rbac/entity/` | UserPermissionEntity, UserRoleEntity, UserRolePermissionRelationEntity, UserRoleRelationEntity |
| `rbac/service/` | UserPermissionManagerService, UserRoleManagerService, UserRolePermissionRelationService, UserRoleRelationService |
| `rbac/controller/manager/` | 对应的 Manager Controller |

### `user` — 用户管理

| 文件 | 用途 |
|------|------|
| `user/entity/UserEntity.kt` | 用户实体：username, password, email, nickname, avatar |
| `user/entity/OAuthAccountEntity.kt` | OAuth 账号绑定 |
| `user/service/UserManagerService.kt` | 用户管理 Service（继承 CachedBaseManagerService） |
| `user/service/UserRbacQueryService.kt` | 查询用户权限（合并系统权限+租户权限） |
| `user/controller/manager/user/` | ManagerUserController + DTOs |
| `user/controller/manager/oauth/` | ManagerOAuthAccountController + DTOs |
| `user/controller/UserController.kt` | 用户端 API（profile, avatar, menus） |

### `tenant` — 多租户

| 子包 | 用途 |
|------|------|
| `tenant/entity/` | TenantEntity, TenantMemberEntity, TenantRoleEntity, TenantPermissionEntity, TenantDepartmentEntity, TenantInvitationEntity, TenantTireTypeEntity 等 |
| `tenant/service/` | 各实体的 Service 接口 |
| `tenant/service/manager/` | Manager Service（含高级查询逻辑） |
| `tenant/service/impl/` | Service 实现 |
| `tenant/controller/manager/` | Manager Controller |
| `tenant/controller/` | 租户端 API（成员自己的操作） |
| `tenant/constants/TenantPermission.kt` | 租户级权限常量 |

### `system` — 系统设置与维护

| 文件 | 用途 |
|------|------|
| `system/service/SystemSettingsService.kt` | 系统设置 CRUD（key-value 存储） |
| `system/types/SystemSettings.kt` | 设置数据类（basic, bootstrap, mail.smtp） |
| `system/types/SystemSettingsConstants.kt` | 设置项声明 |
| `system/filter/SystemMaintenanceGuardFilter.kt` | 维护模式过滤器，阻止无权限用户访问 |
| `system/controller/ReadinessController.kt` | 维护模式状态管理 |

### `mail` — 邮件模板

| 文件 | 用途 |
|------|------|
| `mail/config/MailModuleConfigure.kt` | 邮件模块配置接口（注册分类、类型、模板） |
| `mail/service/` | MailService, MailTemplateManagerService, MailTemplateTypeManagerService, MailTemplateCategoryManagerService |
| `mail/entity/` | MailTemplateEntity, MailTemplateTypeEntity, MailTemplateCategoryEntity |

### `resource` — 文件存储

| 文件 | 用途 |
|------|------|
| `resource/interfaces/StorageProviderRouter.kt` | 存储路由接口（根据用户/文件类型选择存储提供商） |
| `resource/entity/` | FileResourceEntity, StorageProviderEntity |
| `resource/service/` | FileResourceManagerService, StorageProviderManagerService |
| `resource/types/ResourceFileType.kt` | 文件类型枚举 |

### `audit` — 审计日志

| 文件 | 用途 |
|------|------|
| `audit/aspect/ManagerControllerAuditAspect.kt` | AOP 切面，自动记录所有 StandardManagerController 操作 |
| `audit/context/AuditRequestContext.kt` | 从 Reactor Context 获取请求信息 |
| `audit/filter/AuditRequestContextFilter.kt` | WebFilter，将请求信息写入 Reactor Context |
| `audit/entity/AuditLogEntity.kt` | 审计日志实体 |
| `audit/service/AuditLogService.kt` | 记录审计日志的 Service |
| `audit/service/AuditLogManagerService.kt` | 审计日志管理端 Service（含高级查询） |
| `audit/controller/manager/` | ManagerAuditLogController |

### `schedule` — 任务调度

| 文件 | 用途 |
|------|------|
| `schedule/api/ScheduledTask.kt` | 任务接口 |
| `schedule/annotations/CronTaskExecutor.kt` | Cron 任务注解 |
| `schedule/adapter/` | SnailJob 和 Spring 调度适配器 |

---

## 启动时数据初始化 (CommandLineRunner)

项目启动时按 `@Order` 顺序执行：
1. `ResourceModuleDatabaseCheckRunner` — 检查存储提供商
2. `RbacTableDataCheckRunner` — 自动注册 SystemPermission 中声明的所有权限到数据库，创建角色，建立角色-权限关系
3. `SystemSettingsTableDataCheckRunner` — 初始化系统设置
4. `TenantRbacTableDataCheckRunner` — 初始化租户权限

---

## 约定与规范

### 新增模块的标准流程

1. **Entity**: 继承 `BaseEntity`，使用 `@Table("table_name")` 注解
2. **Repository**: 继承 `BaseRepository<Entity>`，添加自定义查询方法
3. **Service 接口**: 继承 `CachedBaseManagerService<Repo, Entity, CreateDTO, ReadDTO, UpdateDTO, DeleteDTO>`
4. **高级查询**: 在 **Service 接口**（不是 impl）中 override `query()` 方法，提供 `isAdvanceQuery` 和 `doAdvanceQuery` lambda
5. **Service 实现**: 实现 `create()` 和 `applyDTOToEntity()`，提供 `cacheStore`、`listCacheStore`、`entityClass`、`getRepository()`
6. **Controller**: 继承 `StandardManagerController`，添加 `@ManagerPermissions`、`@Validated`、`@RestController`、`@RequestMapping`
7. **权限**: 在 `SystemPermission` 中添加 MENU_ 和 ACTION_ 常量
8. **角色映射**: 在 `SystemRolePermissionRelation` 中为 ADMIN 角色添加相应权限
9. **数据库**: 添加 Flyway 迁移脚本

### DTO 命名规范

- `ManagerCreate{Entity}DTO` — 创建
- `ManagerRead{Entity}DTO` — 查询（继承 BaseManagerReadDTO，添加额外筛选字段）
- `ManagerUpdate{Entity}DTO` — 更新（继承 BaseManagerUpdateDTO）
- `ManagerDelete{Entity}DTO` — 删除（继承 BaseManagerDeleteDTO）

### API 路径规范

- 管理端: `/api/{version}/manager/{module-name}/{action}`
- 用户端: `/api/{version}/{module-name}/{action}`
- 版本号通过 `version = "1"` 在 Mapping 注解中指定

### 权限命名规范

- 菜单权限: `"module.name:/manager/path"` → 解析为 name=module.name, path=/manager/path
- 操作权限: `"module.name.action"` → 如 `user.create`, `user.read`
- 组件权限: `"module.name@component.path"` → 前端组件级控制

### 实体 ID

- 所有 ID 使用 `Long` 类型（Snowflake 生成）
- 序列化为 String 传给前端（防止 JS 精度丢失）
- 新建实体必须调用 `newEntity()` 或使用 `entity newEntity true` 中缀函数

### 响应式编程

- 所有 Service 方法使用 `suspend` 关键字
- Repository 返回 `Mono<T>` / `Flux<T>`
- 使用 `awaitFirstOrNull()` 转换 Mono
- 使用 `awaitListWithTimeout()` 转换 Flux
- Controller 方法返回 `ApiResponse<*>`（框架自动包装为 Mono）

### 软删除

- `deletedTime` 字段非 null 表示已删除
- `CrystalFrameworkSQLModifier` 自动在查询中添加 `deleted_time IS NULL` 条件
- 不需要手动处理软删除过滤

### 缓存

- 所有 Manager Service 继承 `CachedBaseManagerService`
- 实现类必须提供 `cacheStore`、`listCacheStore`（通过 `redisService.asKVStore()`）
- 缓存自动在 update/delete 时失效
- 缓存过期时间随机 8-12 小时（防止雪崩）

### 认证

- JWT token 通过 `Authorization: Bearer {token}` 传递
- `UserAuthentication` 作为 Controller 方法参数自动注入
- 不需要认证的端点使用 `@Unauthorized` 注解
- OAuth2 支持 GitHub、Google、QQ

---

## 数据库

- **数据库**: PostgreSQL 15
- **驱动**: R2DBC (响应式)
- **迁移**: Flyway（`src/main/resources/db/migration/`）
- **命名**: 表名小写下划线，如 `audit_manager_logs`
- **时间**: 所有时间字段使用 `bigint`（毫秒时间戳）
- **ID**: `bigint`（Snowflake）

---

## 配置

- 主配置: `src/main/resources/application.yaml`
- 环境变量: POSTGRES_HOST/PORT/USERNAME/PASSWORD/DATABASE, REDIS_HOST/PORT/PASSWORD, OAUTH_*, BACKEND_PORT, COOKIE_DOMAIN, SNOWFLAKE_*
- 自定义配置前缀: `crystalframework.*`

---

## 构建与部署

- 构建: `./mvnw package -DskipTests`
- Docker: 多阶段构建（依赖缓存 → 编译 → 运行）
- CI: GitHub Actions，路径过滤触发
- 依赖: 需要先 `mvn install` VertexLib（`https://github.com/LovelyCatEx/VertexLib.git`）
