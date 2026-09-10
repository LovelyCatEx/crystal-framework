---
paths:
  - "crystal-*/**/*.kt"
  - "crystal-*/**/*.java"
---

# 后端基础编码规范

## 模块结构

标准包结构（以 `com.lovelycatv.crystalframework.<module>` 为基础）：

- **annotations**: 自定义注解
- **aspect**: AOP 切面逻辑
- **config**: 配置类、Bean 定义
- **controller**: API 入口（子包：dto/vo/manager）
- **entity**: 数据库实体类
- **repository**: 数据访问层接口
- **service**: 业务逻辑接口（子包：impl/manager）
- **types**: 领域类型、值对象、枚举
- **constants**: 常量定义
- **event**: 领域事件、监听器
- **utils**: 工具方法

## 强制规则

### 单文件单定义
**禁止单个文件中存在多个类、接口、枚举定义。**

❌ 错误：
```kotlin
// UserTypes.kt
data class UserDTO(...)
data class UserVO(...)
enum class UserStatus { ... }
```

✅ 正确：
```
UserDTO.kt
UserVO.kt
UserStatus.kt
```

### 禁止魔法值
所有可命名的值必须使用 `constants` 包或对应常量类中的常量。

❌ 错误：
```kotlin
val key = "member.max_count"
```

✅ 正确：
```kotlin
val key = TenantBenefit.MEMBER_MAX_COUNT.featureKey
```

**适用范围**：生产代码和测试代码（feature key、permission name、table name、setting key 等）。

若常量不存在，先在对应常量类中定义再使用。

### 工具方法优先用 Kotlin 扩展函数
编写工具方法前，先在项目内搜索是否已有扩展函数：
- `crystal-shared/src/main/kotlin/utils/`
- `crystal-shared/src/main/kotlin/extensions/`
- 各模块的 `utils/` 包

禁止不搜索直接写。

### 枚举 when 必须穷尽所有分支
对枚举类型进行 `when` 匹配时，必须显式列出每个分支，**禁止使用 `else` 兜底**。

❌ 错误：
```kotlin
when (status) {
    Status.ACTIVE -> handle()
    else -> defaultHandle()  // 新增枚举值会被静默处理
}
```

✅ 正确：
```kotlin
when (status) {
    Status.ACTIVE -> handle()
    Status.INACTIVE -> defaultHandle()
    Status.PENDING -> defaultHandle()
    // 新增枚举值时编译器会强制报错
}
```

**此规则仅针对 `enum class`**，非枚举类型（Int、String、sealed 之外的开放类型）不受此限。

## 其他规则

### 调用内部方法前先阅读签名
调用项目内部 Service/工具方法时，必须先阅读对应接口或类的方法签名，确认方法名、参数和返回值。

禁止想当然编造不存在的方法名（如用 `delete` 代替实际的 `removeKey`）。

### 文档修改必须跨语言同步
修改 `docs/` 下某语言版本的文档时，必须同步更新其他语言版本（如 `docs/en/`）。

条目数量必须一一对应，禁止在翻译中额外添加原文没有的细节。

### 前端枚举对应规则
所有后端枚举类型必须在前端有对应 TypeScript 枚举定义，并按四步流程实现翻译：
1. `src/types/` 定义枚举常量
2. `locales/{locale}.ts` 的 `enums` 命名空间添加翻译键
3. `enum-helpers.ts` 注册 `getXxx()` 函数
4. 组件中通过 `getXxx(EnumType.VALUE)` 获取标签文本

详见 `docs/develop/frontend/i18n.md` 的枚举翻译章节。

### Aspect 和 Filter 优先级
- 所有 Aspect 必须有 `@Order` 注解，优先级使用 `GlobalConstants.AspectPriority` 定义
- 所有 Filter 必须有 `@Order` 注解，优先级使用 `GlobalConstants.FilterPriority` 定义
