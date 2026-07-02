# 控制器家族总览

`crystal-shared` 提供 6 个管理端 CRUD Controller 基类，覆盖不同的资源模型。在写 Controller 前先根据资源特征选定基类。

## 基类速查

| 基类 | 权限机制 | 端点 | 适用资源 |
|---|---|---|---|
| `StandardManagerController` | `@ManagerPermissions` 类注解 + AOP | `list` / `create` / `query` / `update` / `delete` | 全局 CRUD，无租户/系统之分 |
| `ReadonlyManagerController` | 同上，写操作重写为 403 | `list` / `query`（写操作返回 403） | 系统生成、不可修改的日志类 |
| `StandardScopedManagerController` | `ScopedPermissionTriad`（12 权限） | `list?scope&scopeId` / `create` / `query` / `update` / `delete` | 资源自带 `scope` + `scopeId` 列 |
| `ReadonlyScopedManagerController` | `ScopedPermissionTriad.readonly(...)` | `list` / `query`（写操作返回 403） | Scoped 家族的只读版本 |
| `StandardDerivedScopedManagerController` | `ScopedPermissionTriad` + 三个抽象钩子 | `create` / `query` / `update` / `delete`（无 `list`） | 自身无 scope 列，需从父实体推导 |
| `StandardTenantManagerController` | 8 个 String 构造参数 | `list?tenantId` / `create` / `query` / `update` / `delete` | 强制以 tenantId 为 scope |

## 选型决策

按下表从上至下判断，第一个匹配的行即为目标基类：

| 判断 | 选择 |
|---|---|
| 不是 CRUD 场景（登录、上传、动作触发） | [普通 Controller](./generic-controller) |
| 系统生成且不允许用户修改（日志、审计） | 全局资源用 [ReadonlyManagerController](./readonly-manager-controller)，带 scope 用 [ReadonlyScopedManagerController](./readonly-scoped-manager-controller) |
| 只属于租户（如租户角色、租户成员） | [StandardTenantManagerController](./tenant-manager-controller) |
| 支持 SYSTEM 与 TENANT 双 scope 挂载 | 自带 scope 列用 [StandardScopedManagerController](./scoped-manager-controller)，靠父实体推 scope 用 [StandardDerivedScopedManagerController](./derived-scoped-manager-controller) |
| 普通的全局 CRUD 资源 | [StandardManagerController](./standard-manager-controller) |

## 权限机制对比

| 机制 | 声明位置 | 适用基类 |
|---|---|---|
| `@ManagerPermissions(read=..., create=..., ...)` | 类注解 | `StandardManagerController` / `ReadonlyManagerController` |
| `ScopedPermissionTriad(...)` | 构造参数 | Scoped / DerivedScoped / ReadonlyScoped |
| 8 个独立 String 构造参数 | 构造参数 | Tenant |

`@ManagerPermissions` + AOP 的切入点写死在 `StandardManagerController.*(..)`，只对 Standard / Readonly 生效。其他家族在方法体内自行校验。

## 必备组件

| 类型 | 要求 |
|---|---|
| Entity | 继承 `BaseEntity`；Scoped 家族继承 `BaseScopedEntity`，DerivedScoped / Tenant 实现 `ScopedEntity<Long>` |
| Repository | 继承 `BaseRepository` |
| Service | Standard / Readonly 用 `CachedBaseManagerService`；Scoped 家族用 `BaseScopedManagerService`；Tenant 用 `BaseTenantResourceManagerService` |
| DTO | 4 个（Create / Read / Update / Delete），基类因家族而异，见各基类文档 |

## 包结构

Manager 相关代码统一放入 `controller/manager/` 与 `service/manager/`：

```
your-module/
├── controller/
│   ├── manager/
│   │   ├── ManagerXxxController.kt
│   │   ├── dto/
│   │   │   ├── ManagerCreateXxxDTO.kt
│   │   │   ├── ManagerReadXxxDTO.kt
│   │   │   ├── ManagerUpdateXxxDTO.kt
│   │   │   └── ManagerDeleteXxxDTO.kt
│   │   └── vo/
│   └── xxx/
├── service/
│   └── manager/
│       ├── XxxManagerService.kt
│       └── impl/
│           └── XxxManagerServiceImpl.kt
└── entity/
    └── XxxEntity.kt
```

## 通用规则

- Controller 头部必须携带 `@Validated`、`@RestController`、`@RequestMapping`
- 方法返回值必须显式声明为 `ApiResponse<*>`（详见 [ApiResponse](./api-response)）
- Controller 内禁止注入 Repository，数据库操作走 Service 层
- Manager Controller 只能注入 Manager Service，普通 Controller 只能注入普通 Service
- 请求参数命名为 `XxxDTO` 放入 `dto/`，响应参数命名为 `XxxVO` 放入 `vo/`
- 所有 `Long` 字段在 DTO / VO 中加 `@get:JsonSerialize(using = ToStringSerializer::class)`，前端接为 `string`
- 无需授权的端点用 `@Unauthorized` 显式标注
