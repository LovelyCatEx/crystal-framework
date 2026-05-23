# Database Migration

## What is Flyway

Flyway is a database version control tool. It manages database structure changes through versioned SQL scripts. On each application startup, pending migration scripts are automatically discovered and executed.

In this framework, Flyway is managed by the main project. Extension plugins only need to provide the SQL files.

## Adding Migrations to a Plugin

### Directory Structure

Create a `db/migration/` directory under `src/main/resources/` in your plugin project, and place versioned SQL files there:

```
my-plugin/
├── pom.xml
└── src/
    └── main/
        └── resources/
            ├── metadata.yml
            └── db/
                └── migration/
                    └── V20250525.01__my_plugin_create_tables.sql
```

### Naming Convention

Migration files must follow this format:

```
{VERSION}__{DESCRIPTION}.sql
```

- **Version**: dot-separated numbers, e.g. `20250525.01`
- **Double underscore**: `__` separates version and description
- **Description**: brief English, underscores for spaces

### Version Convention

Use `VYYYYMMDD.NN` and include your plugin name in the description to avoid conflicts:

```
V20250525.01__my_plugin_create_tables.sql
V20250526.01__my_plugin_add_index.sql
```

After packaging and deploying the JAR to the `ext/` directory, restart the application. Flyway will automatically discover and execute the migration scripts inside your plugin JAR.

### Required Table Columns

**Your table must include these four columns** for framework compatibility:

```sql
CREATE TABLE ext_my_plugin_table (
    id BIGINT PRIMARY KEY,
    -- your business columns ...
    created_time BIGINT NOT NULL,
    modified_time BIGINT NOT NULL,
    deleted_time BIGINT DEFAULT NULL
);
```

| Column | Type | Description |
|--------|------|-------------|
| `id` | `BIGINT` | Primary key, generated via `SnowIdGenerator` at the service layer |
| `created_time` | `BIGINT NOT NULL` | Creation timestamp (milliseconds) |
| `modified_time` | `BIGINT NOT NULL` | Last modified timestamp (milliseconds) |
| `deleted_time` | `BIGINT DEFAULT NULL` | Soft-delete timestamp, the framework automatically filters deleted records |

> Do not use auto-increment for `id`. Use the snowflake ID generator provided by the framework.

### Table Naming

Prefix your plugin tables with `ext_` to avoid name conflicts:

```
ext_my_plugin_users
ext_my_plugin_logs
```

### Full Example

```sql
-- V20250525.01__my_plugin_create_tables.sql
CREATE TABLE IF NOT EXISTS ext_my_plugin_users (
    id BIGINT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    created_time BIGINT NOT NULL,
    modified_time BIGINT NOT NULL,
    deleted_time BIGINT DEFAULT NULL
);

CREATE INDEX IF NOT EXISTS idx_ext_my_plugin_users_name
    ON ext_my_plugin_users (username);
```

## Troubleshooting

**Migration scripts not executing?**

Check that the JAR contains the `db/migration/` directory:

```bash
jar tf my-plugin.jar | grep migration
```

Verify the file name follows the exact `V{version}__{description}.sql` format.

**Version conflicts?**

Each plugin manages its own version numbers. Use different dates or sequence numbers to avoid conflicts.
