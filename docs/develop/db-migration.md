# 数据库迁移

## Flyway 是什么

Flyway 是一款数据库版本控制工具。它通过版本化的 SQL 脚本管理数据库结构变更，应用启动时自动检查并执行未应用的脚本，确保所有环境的数据库结构保持一致。

在本框架中，Flyway 由主项目统一管理，扩展插件只需提供 SQL 文件即可。

## 插件如何添加数据库迁移

### 目录结构

在插件项目的 `src/main/resources/` 下创建 `db/migration/` 目录，放入版本化的 SQL 文件：

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

### 命名规则

迁移文件名必须遵循以下格式：

```
{VERSION}__{DESCRIPTION}.sql
```

- **版本号**：用 `.` 分隔的数字，如 `20250525.01`
- **双下划线**：`__` 分隔版本号和描述
- **描述**：简短英文，单词用下划线分隔

### 版本号约定

建议使用 `VYYYYMMDD.NN` 格式，并在描述中加入插件名，避免与其他插件冲突：

```
V20250525.01__my_plugin_create_tables.sql
V20250526.01__my_plugin_add_index.sql
```

打包部署后，重启应用，主项目的 Flyway 会自动发现插件 JAR 内的迁移脚本并执行。

### 表结构要求

为保证与框架兼容，**你的表必须包含以下四个字段**：

```sql
CREATE TABLE ext_my_plugin_table (
    id BIGINT PRIMARY KEY,
    -- 你的业务字段 ...
    created_time BIGINT NOT NULL,
    modified_time BIGINT NOT NULL,
    deleted_time BIGINT DEFAULT NULL
);
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | `BIGINT` | 主键，在应用层通过 `SnowIdGenerator` 生成 |
| `created_time` | `BIGINT NOT NULL` | 创建时间戳（毫秒） |
| `modified_time` | `BIGINT NOT NULL` | 修改时间戳（毫秒） |
| `deleted_time` | `BIGINT DEFAULT NULL` | 软删除时间戳，框架自动过滤已删除记录 |

> `id` 不要使用自增列，由框架提供的雪花算法生成器在服务层赋值。

### 表名规范

建议所有插件表加 `ext_` 前缀，避免与主项目或其他插件的表名冲突：

```
ext_my_plugin_users
ext_my_plugin_logs
```

### 完整示例

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

## 常见问题

**迁移脚本没有执行？**

检查 JAR 包中是否包含 `db/migration/` 目录及 SQL 文件：

```bash
jar tf my-plugin.jar | grep migration
```

确认文件命名是否正确（严格按 `V{版本号}__{描述}.sql` 格式）。

**版本号冲突怎么办？**

每个插件独立管理自己的版本号，使用不同的日期或序号即可避免冲突。
