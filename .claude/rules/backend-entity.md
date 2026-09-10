---
paths:
  - "**/entity/**/*.kt"
  - "**/entity/**/*.java"
---

# Entity 实体类规范

关于实体类详细内容请见项目根目录下的 `.claude/skills/` 中关于添加实体的技能。

## BaseEntity 规范

后端 `BaseEntity`（`crystal-shared` 模块）包含 4 个字段，序列化到前端时 `Long` 变为 `String`：

| 字段           | Kotlin 类型 | 前端接收类型 | 说明                                      |
| -------------- | ----------- | ------------ | ----------------------------------------- |
| `id`           | `Long`      | `string`     | 雪花算法主键，不可用自增                  |
| `createdTime`  | `Long`      | `string`     | 创建时间戳（毫秒）                        |
| `modifiedTime` | `Long`      | `string`     | 修改时间戳（毫秒），`onUpdate()` 自动维护 |
| `deletedTime`  | `Long?`     | —            | 软删除时间戳，SQL 拦截器自动过滤          |

### 核心方法
- `newEntity()` - 标记为新记录
- `onUpdate()` - 刷新 `modifiedTime`
- `softDelete()` / `restore()` / `isDeleted()` - 管理软删除

## Long 字段序列化

所有 `Long` 类型字段必须使用 `ToStringSerializer`：

```kotlin
@get:JsonSerialize(using = ToStringSerializer::class)
val id: Long
```

**可空 `Long?` 字段**不受全局 `addSerializer(Long::class.java)` 覆盖（Kotlin module 的类型包装不匹配），必须手动加注解。

参考 `BaseEntity` 中的写法。

## 枚举字段规则

当实体字段对应枚举类型时：
- **数据库存储**：枚举的 `typeId`（`Int`）
- **Entity 提供**：`getRealXxx()` 方法转换为强类型枚举

详见 `docs/contribute/add-entity.md` 的「枚举字段」章节。

## Jackson 版本

本项目使用 **Jackson 3（`tools.jackson` 包）** 进行 HTTP JSON 序列化/反序列化。

所有 `@JsonSerialize`、`@JsonDeserialize` 等注解必须使用 `tools.jackson.databind.*` 下的版本，**禁止使用 Jackson 2**（`com.fasterxml.jackson.databind.*`）。

## @NotQueryable 注解

用于标记实体字段，使其排除在 QueryNode 过滤之外。

**适用场景**：敏感字段（密码、token、密钥、凭证等），其值绝不能通过 `POST /manager/xxx/query` 的 `ConditionNode.field = "password"` 式布尔探测攻击访问。

```kotlin
@NotQueryable
var password: String? = null

@NotQueryable
var properties: String? = null  // 存储 OSS/COS 凭证
```

标记后，该字段不会出现在 `BaseManagerService` 自动解析的可查询字段列表中，前端无法通过 QueryNode 查询该字段。

## 前端对应 BaseEntity

前端 `BaseEntity`（`types/BaseEntity.ts`）：

```typescript
export interface BaseEntity {
    id: string;          // 后端 Long 经 ToStringSerializer 序列化为 String
    createdTime: string;
    modifiedTime: string;
}
```

对于后端继承 BaseEntity 的实体类，前端对应也必须继承该接口。
