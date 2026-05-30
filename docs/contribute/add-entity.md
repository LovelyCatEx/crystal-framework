# 添加实体类

## 表类型选择

### BaseEntity 表

需要软删除和时间审计的核心业务表。框架的 SQL 拦截器会自动处理：

- `SELECT` 追加 `AND deleted_time IS NULL`
- `UPDATE` 自动设置 `modified_time`
- `DELETE` 转换为软删除

表必须包含以下四个字段：

```sql
CREATE TABLE new_table (
    id BIGINT PRIMARY KEY,
    -- 业务字段 ...
    created_time BIGINT NOT NULL,
    modified_time BIGINT NOT NULL,
    deleted_time BIGINT DEFAULT NULL
);
```

### 非 BaseEntity 表

纯日志、时序数据等不需要软删除的表。SQL 拦截器跳过，表结构自由设计：

```sql
CREATE TABLE operation_logs (
    value DOUBLE PRECISION NOT NULL,
    created_time BIGINT NOT NULL
);
```

## Entity 类写法

### BaseEntity

继承 `BaseEntity`，使用雪花 ID：

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

- `class` 而非 `data class`，属性用 `var`
- `id`、`createdTime`、`modifiedTime`、`deletedTime` 透传给 `BaseEntity`
- `id` 在 Service 层通过 `SnowIdGenerator.nextId()` 赋值

### 枚举字段

当实体字段对应枚举类型时，数据库存储枚举的 `ordinal`（`Int`），Entity 中提供 `getRealXxx()` 方法转换为强类型枚举，便于 Service 层安全使用：

```kotlin
@Table("tenant_tire_benefit_features")
class TenantTireBenefitFeatureEntity(
    id: Long = 0,
    @Column("feature_type") var featureType: Int = 0,
    // ...
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null,
) : BaseEntity(id, createdTime, modifiedTime, deletedTime) {

    @JsonIgnore
    fun getRealFeatureType(): TenantBenefitType {
        return TenantBenefitType.entries.getOrNull(this.featureType)
            ?: throw BusinessException("invalid featureType id $featureType")
    }
}
```

**规则：**

| 规则 | 说明 |
|------|------|
| 存储 `ordinal` | 实体属性为 `var xxxType: Int`，存枚举的 `typeId` / `ordinal` |
| `getRealXxx()` | 返回强类型枚举值，`@JsonIgnore` 不序列化到前端 |
| 异常处理 | `entries.getOrNull()` 对非法值抛 `BusinessException` |
| 使用时调用 | Service 中通过 `entity.getRealXxx()` 判断，`when` 分支覆盖所有枚举值 |

现有参考：`TenantEntity.getRealStatus()`、`TenantMemberEntity.getRealStatus()`、`TenantPermissionEntity.getRealPermissionType()`。

### 非 BaseEntity

```kotlin
@Table("request_logs")
data class RequestLogs(
    val value: Double,
    val createdTime: Long = System.currentTimeMillis(),
)
```

## 表注册

### TableConstants

在 `TableConstants.kt`（crystal-shared）中添加常量：

```kotlin
const val TABLE_OPERATION_LOGS = "operation_logs"
```

### TableRegistryInitializer

在 `TableRegistryInitializer.kt`（crystal-starter）中注册：

```kotlin
registry.register(TableConstants.TABLE_OPERATION_LOGS, isBaseEntity = false)
```

### 注册流程对比

| 表类型 | `isBaseEntity` | SQL 拦截 | 迁移要求 |
|--------|---------------|----------|---------|
| BaseEntity | `true` | 改写 | 必须包含四字段 |
| 非 BaseEntity | `false` | 跳过 | 无约束 |

## SDK SPI

- `TableRegistration`（crystal-sdk/database）— 表注册数据类
- `TableRegistry`（crystal-sdk/database）— 注册器
- `TableConfigurer`（crystal-sdk/database/config）— 插件实现此接口注册自己的表

## 迁移文件

在 `crystal-starter/src/main/resources/db/migration/` 下创建 `V{YYYYMMDD.NN}__{description}.sql`。详见[数据库迁移](./db-migration)。
