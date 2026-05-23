# Database Migration

## What is Flyway

Flyway is an open-source database version control tool. It manages database structure changes through versioned SQL scripts. On each application startup, it automatically checks for and applies pending migration scripts, ensuring database consistency across all environments.

Crystal Framework uses Flyway **only for database migrations** (DDL operations). Runtime database access is handled through R2DBC.

## Configuration

The migration configuration lives in `crystal-starter` at `com.lovelycatv.crystalframework.config.FlywayConfig`:

```kotlin
val flyway = Flyway.configure(classLoader)
    .dataSource(url, username, password)
    .locations("classpath:db/migration")
    .baselineOnMigrate(true)
    .baselineVersion("0")
    .load()
```

All migration SQL files are located at:

```
crystal-starter/src/main/resources/db/migration/
```

## Naming Convention

Each migration file must follow this format:

```
{VERSION}__{DESCRIPTION}.sql
```

- **Version**: dot-separated numbers, e.g. `20250513.01`
- **Double underscore**: `__` between version and description
- **Description**: brief English, underscores for spaces

Existing migrations:

| File | Description |
|------|-------------|
| `V20250513.01__init_schema.sql` | Core tables (users, tenants, etc.) |
| `V20250513.02__init_snail_job.sql` | Distributed job scheduler tables |
| `V20250514.01__create_audit_logs.sql` | Audit log tables |
| `V20250517.01__create_system_mail_send_logs.sql` | System mail send logs |
| `V20250517.02__create_user_login_logs.sql` | User login logs |

### Version Convention

Use `VYYYYMMDD.NN` format. When collaborating, use your own commit date and sequence number — do not modify others' version numbers.

## Adding a Migration

1. Create a `.sql` file in `crystal-starter/src/main/resources/db/migration/`
2. Follow the naming convention
3. Verify locally by starting the application
4. Commit the file

## SQL Interceptor Compatibility

The framework's `CrystalFrameworkSQLModifier` automatically modifies all SQL. **Every new table must include:**

| Column | Type | Description |
|--------|------|-------------|
| `id` | `BIGINT` | Primary key (snowflake algorithm via `SnowIdGenerator`) |
| `created_time` | `BIGINT NOT NULL` | Creation timestamp (ms) |
| `modified_time` | `BIGINT NOT NULL` | Last modified timestamp (ms), auto-maintained |
| `deleted_time` | `BIGINT DEFAULT NULL` | Soft-delete timestamp, `SELECT` auto-filters |

```sql
CREATE TABLE ext_example (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_time BIGINT NOT NULL,
    modified_time BIGINT NOT NULL,
    deleted_time BIGINT DEFAULT NULL
);
```

## Extension Plugin Migrations

Extension plugins can also ship migration SQL files inside their JARs. Place them under `src/main/resources/db/migration/` in the plugin project. The main project's Flyway will discover and execute them automatically.

See the [Plugin Dev Guide](../develop/db-migration) for details.
