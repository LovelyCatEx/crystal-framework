---
name: add-base-entity
description: 为项目添加新的数据库实体（BaseEntity 或 non-BaseEntity），包括 entity 类、SQL 迁移和表注册。
---

# 添加数据库实体

## 触发条件

当用户要求新增数据表、实体类、数据库模型时使用。需要先确认该表是否需要软删除和时间审计字段（即是否继承 BaseEntity）。

## 判断标准

### BaseEntity（是）
表需要以下特性时选择 BaseEntity：
- 软删除（记录不物理删除，标记删除时间）
- 自动时间审计（created_time / modified_time）
- 与框架 SQL 拦截器兼容（自动加 `deleted_time IS NULL` 过滤、自动设置 `modified_time`）

所有框架核心业务实体均继承 BaseEntity。

### non-BaseEntity（否）
表满足以下任一条件时选择 non-BaseEntity：
- 纯日志/时序数据，不需要软删除
- 由第三方组件管理（如 SnailJob 的 `sj_*` 表）
- 外部系统映射表，不需要框架的时间审计

## 输入格式

用户需提供：
1. 表名
2. 字段列表（名称 + 类型 + 约束）
3. 是否 BaseEntity（如果不确定，按 BaseEntity 处理）
4. 所属模块

## 前提信息

### 表结构要求

BaseEntity 表必须包含以下四个字段：

```sql
CREATE TABLE operation_logs (
    id BIGINT PRIMARY KEY,
    -- 业务字段 ...
    created_time BIGINT NOT NULL,
    modified_time BIGINT NOT NULL,
    deleted_time BIGINT DEFAULT NULL
);
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | `BIGINT` | 主键，通过 `SnowIdGenerator` 生成（不要用自增） |
| `created_time` | `BIGINT NOT NULL` | 创建时间戳（毫秒） |
| `modified_time` | `BIGINT NOT NULL` | 修改时间戳（毫秒），SQL 拦截器自动维护 |
| `deleted_time` | `BIGINT DEFAULT NULL` | 软删除时间戳，SQL 拦截器自动过滤 |

non-BaseEntity 表不需要上述四个字段，按业务需求设计即可。

### Entity 类结构

**BaseEntity** — 继承 `BaseEntity`，使用雪花 ID：

```kotlin
@Table("operation_logs")
class OperationLogs(
    id: Long = 0,
    @Column("action") var action: String = "",
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null,
) : BaseEntity(id, createdTime, modifiedTime, deletedTime)
```

关键点：
- `class` 而不是 `data class`
- 属性用 `var`
- 不覆写 `id`、`createdTime`、`modifiedTime`、`deletedTime`，直接透传给 `BaseEntity`
- `id` 默认 `0`，在 service 层通过 `SnowIdGenerator.nextId()` 赋值

**non-BaseEntity** — 独立类，不需要继承 BaseEntity：

```kotlin
@Table("request_logs")
data class RequestLogs(
    @Id
    val id: Long? = null,
    @Column("path") val path: String = "",
    @Column("duration") val duration: Long = 0,
)
```

### 迁移文件命名

```
V{YYYYMMDD}.{NN}__{description}.sql
```

示例：`V20250525.01__create_operation_logs.sql`

- `YYYYMMDD`：创建日期
- `NN`：当日序号
- `description`：简短英文，下划线分隔

### 表名规范

- 表名使用小写蛇形命名：`users`、`tenants`、`system_settings`、`operation_logs`

### 表注册

所有表必须在 `TableRegistry` 中注册，Register 在初始化阶段桥接到 SQL 拦截器，只有 `isBaseEntity = true` 的表才会走 SQL 改写。

**主项目** — 在 `TableConstants.kt` 添加常量，然后在 `TableRegistryInitializer.kt` 中 register：

```kotlin
// TableConstants.kt
const val TABLE_OPERATION_LOGS = "operation_logs"

// TableRegistryInitializer.kt
registry.register(TableConstants.TABLE_OPERATION_LOGS)
```

**插件** — 实现 `TableConfigurer`（仅在独立插件项目中需要）：
- `true`（默认）：SQL 拦截器会对此表添加 `deleted_time IS NULL` 等改写
- `false`：SQL 拦截器跳过此表，不进行任何改写

## 执行步骤

### 主项目添加 BaseEntity

1. 阅读对应模块的现有 entity，确认表名不重复
2. 在 `TableConstants.kt` 添加 `const val TABLE_XXX = "表名"`
3. 在对应模块的 `entity` 包下创建 Entity 类，继承 `BaseEntity`
4. 在 `crystal-starter/src/main/resources/db/migration/` 下创建迁移 SQL 文件
5. 在 `TableRegistryInitializer.kt` 中 register 该表
6. 创建 `@Repository` 或 `Repository` 接口（视模块情况）

### 主项目添加 non-BaseEntity

1. 创建 Entity 类（不继承 BaseEntity）
2. 创建迁移 SQL 文件（不需要 `deleted_time` 等字段）
3. 在 `TableRegistryInitializer.kt` 中 register 该表，`isBaseEntity = false`
4. 创建对应的 Repository

### Service 层注入 SnowIdGenerator

BaseEntity 表的 ID 通过雪花算法生成：

```kotlin
@Service
class OperationLogsService(
    private val repository: OperationLogsRepository,
    private val snowIdGenerator: SnowIdGenerator,
) {
    fun create(request: CreateRequest): Mono<OperationLogs> {
        val entity = OperationLogs(
            id = snowIdGenerator.nextId(),
            action = request.action,
        )
        return repository.insert(entity)
    }
}
```

## 输出格式

完成后说明：
1. 新增的表名和所属模块
2. 是 BaseEntity 还是 non-BaseEntity
3. SQL 迁移文件路径
4. Entity 类路径
5. 注册位置（`TableConstants.kt` 常量和 `TableRegistryInitializer.kt`）
