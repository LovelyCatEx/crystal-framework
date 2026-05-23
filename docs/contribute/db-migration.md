# 数据库迁移

## Flyway 简介

Flyway 是一款开源的数据库版本控制工具。它通过版本化的 SQL 脚本对数据库结构进行管理，每次启动时自动检查并执行未应用的迁移脚本，确保所有环境的数据库结构保持一致。

Crystal Framework 使用 Flyway **仅用于数据库迁移**（建表、加索引等 DDL 操作），运行期数据库访问基于 R2DBC 响应式驱动。

## 运作方式

### 配置入口

迁移配置位于 `crystal-starter` 模块的 `com.lovelycatv.crystalframework.config.FlywayConfig`：

```kotlin
val flyway = Flyway.configure(classLoader)
    .dataSource(url, username, password)
    .locations("classpath:db/migration")
    .baselineOnMigrate(true)
    .baselineVersion("0")
    .load()
```

- `.locations("classpath:db/migration")` — 扫描类路径下所有 `db/migration/` 目录
- `classLoader` — 传入 `ExternalModuleScanner` 创建的 `URLClassLoader`，因此 **扩展插件 JAR 中的迁移脚本也会被自动发现**
- `baselineOnMigrate(true)` — 对已有数据的数据库自动基线

### 迁移文件位置

所有迁移 SQL 文件存放在：

```
crystal-starter/src/main/resources/db/migration/
```

### 执行流程

应用启动 → Flyway 初始化 → 扫描 `db/migration/` → 比对 `flyway_schema_history` → 按版本号升序执行未应用的脚本 → 记录执行结果

## 命名规则

每个迁移文件必须遵循以下格式：

```
{VERSION}__{DESCRIPTION}.sql
```

- **版本号**：用 `.` 分隔的数字，如 `20250513.01`
- **双下划线**：`__` 分隔版本号和描述
- **描述**：简短英文，单词用下划线分隔

现有迁移文件一览：

| 文件 | 说明 |
|------|------|
| `V20250513.01__init_schema.sql` | 初始化核心表（users、tenants 等 18 张表） |
| `V20250513.02__init_snail_job.sql` | 分布式任务调度表 |
| `V20250514.01__create_audit_logs.sql` | 审计日志表 |
| `V20250517.01__create_system_mail_send_logs.sql` | 系统邮件发送日志 |
| `V20250517.02__create_user_login_logs.sql` | 用户登录日志 |

### 版本号约定

使用 `VYYYYMMDD.NN` 格式：

- `YYYYMMDD` — 创建日期
- `NN` — 当日序号（`.01`、`.02`…）

多人协作时，不要修改他人的版本号，使用自己提交当天的日期和序号即可。Flyway 通过 `flyway_schema_history` 表追踪已应用的脚本，不会重复执行。

## 添加迁移脚本

### 步骤

1. 在 `crystal-starter/src/main/resources/db/migration/` 下创建 SQL 文件
2. 遵循命名规则编写 DDL
3. 本地启动应用验证迁移是否正常执行
4. 提交代码

### SQL 注意事项

框架的 SQL 拦截器（`CrystalFrameworkSQLModifier`）会自动对所有 SQL 做以下修改：

- `SELECT` 自动追加 `AND deleted_time IS NULL`
- `UPDATE` 自动设置 `modified_time`
- `DELETE` 转换为软删除

因此，**新表必须包含以下字段**：

```sql
CREATE TABLE new_table (
    id BIGINT PRIMARY KEY,
    -- 业务字段 ...
    created_time BIGINT NOT NULL,
    modified_time BIGINT NOT NULL,
    deleted_time BIGINT DEFAULT NULL
);
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | `BIGINT` | 主键，通过雪花算法生成（框架自动注入 `SnowIdGenerator`） |
| `created_time` | `BIGINT NOT NULL` | 创建时间戳（毫秒） |
| `modified_time` | `BIGINT NOT NULL` | 修改时间戳（毫秒），SQL 拦截器自动维护 |
| `deleted_time` | `BIGINT DEFAULT NULL` | 软删除时间戳，`SELECT` 自动过滤 |

### 示例

```sql
-- V20250525.01__create_ext_example.sql
CREATE TABLE IF NOT EXISTS ext_example (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_time BIGINT NOT NULL,
    modified_time BIGINT NOT NULL,
    deleted_time BIGINT DEFAULT NULL
);

CREATE INDEX IF NOT EXISTS idx_ext_example_name ON ext_example (name);
CREATE INDEX IF NOT EXISTS idx_ext_example_time ON ext_example (created_time DESC);
```

## 扩展插件的迁移

扩展插件（非集成模式）同样可以包含数据库迁移。将 `.sql` 文件放在插件项目的 `src/main/resources/db/migration/` 目录下，打包后放入 `ext/` 目录即可。主项目的 Flyway 会自动发现插件 JAR 内的迁移脚本并执行。

详细说明请参考[二次开发指引](../develop/db-migration)。
