---
name: add-standard-manager-controller
description: 为普通实体（非 scoped）添加标准化的 CRUD 管理端点，继承 StandardManagerController，包括 5 个端点、DTO 分包、@ManagerPermissions 权限声明。
---

# 添加标准 Manager Controller

## 触发条件

当用户要求为某个 **BaseEntity 类型的实体** 添加管理后台的 CRUD 接口，且该实体 **不需要按 scope（SYSTEM/TENANT）区分数据可见性** 时使用。典型场景：全局用户、全局角色、系统级邮件模板等。

如果实体需要按 scope 区分（如某资源既有系统级也有租户级），改用 `add-scoped-manager-controller`。

## 判断标准

**用 StandardManagerController（是）**：
- 实体是 `BaseEntity`（不是 `BaseScopedEntity`）
- 数据无 scope 概念（全局唯一或用另一维度隔离）
- 权限模型是"平面的 CRUD 授权"（同一权限位授给所有可操作用户）

**不用（去别的 skill）**：
- 实体是 `BaseScopedEntity` 或有 `scope + scope_id` 列 → `add-scoped-manager-controller`
- 只读资源（系统自动生成，不允许外部改动）→ `add-readonly-scoped-manager-controller`（scoped）或自行 override create/update/delete 抛异常
- 非标准 CRUD（自定义端点 / 特殊参数 / 特殊返回）→ `add-generic-controller`

## 输入格式

用户需要提供：
1. 实体类（`BaseEntity` 的子类，已通过 `add-base-entity` skill 建好）
2. 端点前缀（如 `/manager/user-role`）
3. 权限常量名（4 个：create/read/update/delete，通过 `add-system-permission` skill 添加）

## 前提信息

### 端点集

`StandardManagerController` 提供 5 个固定端点，前端 `BaseManagerController.ts` 与之一一对应：

| 端点 | 方法 | 参数绑定 | 用途 |
|---|---|---|---|
| `/list` | GET | 无 | 全量列表 |
| `/create` | POST | `@ModelAttribute` (form) | 创建 |
| `/query` | POST | `@RequestBody` (JSON) | 分页查询 |
| `/update` | POST | `@ModelAttribute` (form) | 更新 |
| `/delete` | POST | `@ModelAttribute` (form) | 批量删除 |

**Content-Type 前后端必须严格对齐**（Controller 用 `@ModelAttribute` 前端就必须发 form；用 `@RequestBody` 前端必须发 JSON）。

### 5 个 DTO 分包

Controller 依赖 5 个 DTO，必须放 `controller/manager/{resource}/dto/` 目录：

| DTO | 基类 | 说明 |
|---|---|---|
| `ManagerCreateXxxDTO` | `Any` | Create 用 |
| `ManagerReadXxxDTO` | `BaseManagerReadDTO` | Read/Query 用，含 page/pageSize/id/query |
| `ManagerUpdateXxxDTO` | `BaseManagerUpdateDTO` | Update 用，含 id |
| `ManagerDeleteXxxDTO` | `BaseManagerDeleteDTO` | Delete 用，含 ids |

DTO 必须是 `data class`。

**Long 字段序列化规则**：DTO 里所有 `id` / `xxxId` / `xxxTime` 字段前端接收都是 `string`。DTO 定义时用 `Long` 类型；实体 → 响应体的自动序列化走 `ToStringSerializer`。

### 权限声明

在 Controller 类上加 `@ManagerPermissions` annotation，`ManagerControllerPermissionAspect` 自动拦截 5 个端点并做 authority 校验：

```kotlin
@ManagerPermissions(
    read = [SystemPermission.ACTION_XXX_READ],
    readAll = [SystemPermission.ACTION_XXX_READ],
    create = [SystemPermission.ACTION_XXX_CREATE],
    update = [SystemPermission.ACTION_XXX_UPDATE],
    delete = [SystemPermission.ACTION_XXX_DELETE],
)
```

`read` 对应 `/query`，`readAll` 对应 `/list`。每个字段是 `Array<String>`，写多个表示 OR 关系（持任一即可）。

### Manager Service 要求

Service 必须：
- 继承 `CachedBaseManagerService<REPOSITORY, ENTITY, C, R, U, D>`
- 位于 `service/manager/` 包，impl 在 `service/manager/impl/`
- **禁止直接在 Controller 层注入 Repository**，只注入 Service

### Controller 类顶部注解

必须严格是：

```kotlin
@ManagerPermissions(...)
@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/xxx")
class ManagerXxxController(...)
```

`GlobalConstants.REQUEST_MAPPING_PREFIX` 会加上 `/api` 前缀 + 版本号，禁止硬编码 `/api/v1/...`。

## 执行步骤

1. **确认前置资源就绪**：
   - Entity 存在（`add-base-entity` skill）
   - Service 接口 + Impl 存在（继承 `CachedBaseManagerService`）
   - 4 个权限常量存在（`add-system-permission` skill）

2. **创建 4 个 DTO 文件**：放 `controller/manager/{resource}/dto/` 目录，每个 DTO 一个 `.kt` 文件（**单文件单定义**）。

3. **创建 Controller 类**：
   ```kotlin
   @ManagerPermissions(
       read = [SystemPermission.ACTION_XXX_READ],
       readAll = [SystemPermission.ACTION_XXX_READ],
       create = [SystemPermission.ACTION_XXX_CREATE],
       update = [SystemPermission.ACTION_XXX_UPDATE],
       delete = [SystemPermission.ACTION_XXX_DELETE],
   )
   @Validated
   @RestController
   @RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/xxx")
   class ManagerXxxController(
       managerService: XxxManagerService
   ) : StandardManagerController<
           XxxManagerService,
           XxxRepository,
           XxxEntity,
           ManagerCreateXxxDTO,
           ManagerReadXxxDTO,
           ManagerUpdateXxxDTO,
           ManagerDeleteXxxDTO
   >(managerService)
   ```

4. **前端 API 对接**：
   ```typescript
   // web/src/api/xxx/xxx.api.ts
   export const XxxManagerController = new BaseManagerController<
       Xxx,
       ManagerCreateXxxDTO,
       ManagerReadXxxDTO,
       ManagerUpdateXxxDTO,
       ManagerDeleteXxxDTO
   >('/manager/xxx');
   ```

## 常见错误

| 错误 | 修正 |
|---|---|
| Controller 直接注入 Repository | 只注入 Service，Repository 通过 `managerService.getRepository()` |
| DTO 用 `class` 而不是 `data class` | 必须 `data class`，`copy()` 等场景需要 |
| DTO 里 `id: String` | 用 `Long`，前后端 JSON 序列化通过 `ToStringSerializer` 自动转 |
| 多个 DTO 写在一个文件里 | 单文件单定义，每个 DTO 独立 `.kt` |
| 硬编码 `/api/v1/manager/xxx` | 用 `${GlobalConstants.REQUEST_MAPPING_PREFIX}` |
| 忘加 `@Validated` | 必须加，否则 `@Valid` 不生效 |
| `@ManagerPermissions` 只写 `read` 不写 `readAll` | `/list` 用 `readAll`，`/query` 用 `read`，两者独立 |
| Service 不继承 `CachedBaseManagerService` | 必须继承，否则泛型对不上 |

## 输出格式

完成后说明：
1. 新增 Controller 路径 + 端点前缀
2. 4 个 DTO 路径
3. 引用的 4 个权限常量
4. 前端 API 文件路径
