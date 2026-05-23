# Add Entity

## Table Type Selection

### BaseEntity Tables

For tables requiring soft-delete and time auditing. The SQL interceptor will automatically handle soft-delete filtering, `modified_time` maintenance, and DELETE-to-soft-delete conversion.

Must include these four columns:

```sql
CREATE TABLE operation_logs (
    id BIGINT PRIMARY KEY,
    -- business columns ...
    created_time BIGINT NOT NULL,
    modified_time BIGINT NOT NULL,
    deleted_time BIGINT DEFAULT NULL
);
```

### Non-BaseEntity Tables

For log or time-series data without soft-delete. The SQL interceptor skips these tables. No column constraints:

```sql
CREATE TABLE request_logs (
    value DOUBLE PRECISION NOT NULL,
    created_time BIGINT NOT NULL
);
```

## Entity Class

### BaseEntity

Extend `BaseEntity` with snowflake ID:

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

### Non-BaseEntity

Standalone data class:

```kotlin
@Table("request_logs")
data class RequestLogs(
    val value: Double,
    val createdTime: Long = System.currentTimeMillis(),
)
```

## Migration Script

Create versioned SQL files in `src/main/resources/db/migration/`. See [Database Migration](./db-migration).

## Table Registration

Register all tables via `TableConfigurer`:

```kotlin
@Component
class MyTableConfigurer : TableConfigurer {
    override fun configure(registry: TableRegistry) {
        registry.register("operation_logs", isBaseEntity = true)
        registry.register("request_logs", isBaseEntity = false)
    }
}
```

- `isBaseEntity = true`: SQL interceptor rewrites queries for this table
- `isBaseEntity = false`: SQL interceptor skips this table
