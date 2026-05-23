# 添加实体类

## 表类型选择

### BaseEntity 表

需要软删除和时间审计的表，选择 BaseEntity。框架的 SQL 拦截器会自动处理软删除过滤、维护 `modified_time`、转换 DELETE 为软删除。

表必须包含以下四个字段：

```sql
CREATE TABLE ext_my_plugin_entity (
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
| `modified_time` | `BIGINT NOT NULL` | 修改时间戳（毫秒） |
| `deleted_time` | `BIGINT DEFAULT NULL` | 软删除时间戳 |

### 非 BaseEntity 表

纯日志、时序数据等不需要软删除的表。SQL 拦截器会跳过这些表。无字段约束，按业务需求设计：

```sql
CREATE TABLE ext_my_plugin_logs (
    value DOUBLE PRECISION NOT NULL,
    created_time BIGINT NOT NULL
);
```

## Entity 类写法

### BaseEntity

继承 `BaseEntity`，使用雪花 ID：

```kotlin
@Table("ext_my_plugin_entity")
class ExtMyPluginEntity(
    id: Long = 0,
    @Column("name") var name: String = "",
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null,
) : BaseEntity(id, createdTime, modifiedTime, deletedTime)
```

- 使用 `class` 而非 `data class`
- 属性用 `var`
- `id`、`createdTime`、`modifiedTime`、`deletedTime` 透传给 `BaseEntity`
- `id` 默认 `0`，在 Service 层通过 `SnowIdGenerator.nextId()` 赋值

### 非 BaseEntity

独立类，不继承 BaseEntity：

```kotlin
@Table("ext_my_plugin_logs")
data class ExtMyPluginLogs(
    val value: Double,
    val createdTime: Long = System.currentTimeMillis(),
)
```

## 表注册

所有插件表必须通过 `TableConfigurer` 注册：

```kotlin
@Component
class MyTableConfigurer : TableConfigurer {
    override fun configure(registry: TableRegistry) {
        registry.register("ext_my_plugin_entity", isBaseEntity = true)
        registry.register("ext_my_plugin_logs", isBaseEntity = false)
    }
}
```

- `isBaseEntity = true`：SQL 拦截器对此表执行改写
- `isBaseEntity = false`：SQL 拦截器跳过此表

## 表名规范

插件表加 `ext_` 前缀避免冲突，使用小写蛇形命名：

```
ext_my_plugin_users
ext_my_plugin_logs
```

## 迁移文件

配合 Flyway 迁移脚本一起使用，在 `src/main/resources/db/migration/` 下创建 SQL 文件。详见[数据库迁移](./db-migration)。
