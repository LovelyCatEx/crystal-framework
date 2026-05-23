# Add Entity

## Table Type Selection

### BaseEntity Tables

Core business tables with soft-delete. The SQL interceptor will:

- Add `AND deleted_time IS NULL` to `SELECT`
- Auto-set `modified_time` on `UPDATE`
- Convert `DELETE` to soft-delete

```sql
CREATE TABLE new_table (
    id BIGINT PRIMARY KEY,
    -- business columns ...
    created_time BIGINT NOT NULL,
    modified_time BIGINT NOT NULL,
    deleted_time BIGINT DEFAULT NULL
);
```

### Non-BaseEntity Tables

Log or time-series tables without soft-delete. SQL interceptor skips entirely:

```sql
CREATE TABLE request_logs (
    value DOUBLE PRECISION NOT NULL,
    created_time BIGINT NOT NULL
);
```

## Entity Class

### BaseEntity

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

```kotlin
@Table("request_logs")
data class RequestLogs(
    val value: Double,
    val createdTime: Long = System.currentTimeMillis(),
)
```

## Table Registration

### TableConstants

Add constant in `TableConstants.kt` (crystal-shared):

```kotlin
const val TABLE_OPERATION_LOGS = "operation_logs"
```

### TableRegistryInitializer

Register in `TableRegistryInitializer.kt` (crystal-starter):

```kotlin
registry.register(TableConstants.TABLE_OPERATION_LOGS, isBaseEntity = false)
```

### Migration Script

Create versioned SQL files in `crystal-starter/src/main/resources/db/migration/`. See [Database Migration](./db-migration).

### Registration Summary

| Type | `isBaseEntity` | SQL Interceptor |
|------|---------------|-----------------|
| BaseEntity | `true` | Rewrites |
| Non-BaseEntity | `false` | Skips |
