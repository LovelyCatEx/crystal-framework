---
name: add-registry
description: 在 crystal-sdk 中新增一个 Registry，用于管理某类可扩展的声明项（如权限、角色、设置、任务等）。
---

# 新增 Registry

## 触发条件

当用户需要在框架中引入一种新的可扩展注册机制时使用，例如：
- 新增一类需要多模块/插件共同声明的配置项
- 新增一类需要在启动时统一收集并校验的声明

## 输入格式

用户需要提供：
1. Registry 名称（如 `NotificationRegistry`）
2. 管理的声明类型（如 `NotificationChannelDeclaration`）
3. 注册的唯一键字段（如 `name`、`key`、`id`）
4. 所属模块（通常是 `crystal-sdk`）

## 前提信息

### Registry 的职责

Registry 是一个普通 Kotlin 类（不是 Spring Bean），在 Spring `@Configuration` 的 `@Bean` 方法中被实例化，由框架统一管理生命周期。它的职责是：
1. 收集来自多个 `Configurer` 的声明
2. 对外提供查询接口
3. **在注册阶段检测冲突，遇到重复 key 立即 throw `IllegalStateException`**

### 强制约束：Fail-Fast 冲突检测

**所有 Registry 的注册方法必须在检测到重复 key 时立即抛出异常，不得静默忽略。**

```kotlin
fun register(declaration: XxxDeclaration) {
    val key = declaration.name.trim()
    if (key.isBlank()) return

    if (items.putIfAbsent(key, declaration.copy(name = key)) != null) {
        throw IllegalStateException("XxxRegistry: duplicate name '$key'")
    }
}
```

这确保冲突在应用启动阶段就暴露，而不是在运行时产生难以排查的静默错误。

### 现有 Registry 参考

| Registry | 位置 | 唯一键 |
|---|---|---|
| `SystemRbacRegistry` | `crystal-sdk/.../rbac/system/` | permission.name, role.name |
| `TenantRbacRegistry` | `crystal-sdk/.../rbac/tenant/` | permission.name, role.name |
| `SystemSettingsRegistry` | `crystal-sdk/.../system/settings/` | setting.key |
| `TaskRegistry` | `crystal-schedule/.../registry/` | task name（来自 `@ScheduledTaskMetadata`） |

### 标准结构

一个完整的 Registry 由以下部分组成：

#### 1. 声明类型（Declaration）

```kotlin
// crystal-sdk/.../xxx/types/XxxDeclaration.kt
data class XxxDeclaration(
    val name: String,
    val description: String = "",
)
```

#### 2. Registry 类

```kotlin
// crystal-sdk/.../xxx/XxxRegistry.kt
class XxxRegistry {
    private val items = linkedMapOf<String, XxxDeclaration>()

    fun register(declaration: XxxDeclaration) {
        val key = declaration.name.trim()
        if (key.isBlank()) return

        if (items.putIfAbsent(key, declaration.copy(name = key)) != null) {
            throw IllegalStateException("XxxRegistry: duplicate name '$key'")
        }
    }

    fun registers(declarations: Iterable<XxxDeclaration>) {
        declarations.forEach { register(it) }
    }

    fun declarations(): List<XxxDeclaration> = items.values.toList()

    fun declarationMap(): Map<String, XxxDeclaration> = items.toMap()
}
```

#### 3. Configurer 接口

```kotlin
// crystal-sdk/.../xxx/config/XxxConfigurer.kt
interface XxxConfigurer {
    fun configure(registry: XxxRegistry)
}
```

#### 4. Spring 配置类（在 crystal-starter 中）

```kotlin
// crystal-starter/.../config/XxxRegistryConfiguration.kt
@Configuration
class XxxRegistryConfiguration {
    @Bean
    fun xxxRegistry(
        configurers: ObjectProvider<XxxConfigurer>
    ): XxxRegistry {
        return XxxRegistry().apply {
            configurers.orderedStream().forEach { it.configure(this) }
        }
    }
}
```

#### 5. 内置声明注册（在 crystal-starter 中）

```kotlin
// crystal-starter/.../config/XxxBuiltinConfigurer.kt
@Component
class XxxBuiltinConfigurer : XxxConfigurer {
    override fun configure(registry: XxxRegistry) {
        registry.registers(listOf(
            XxxDeclaration(name = "builtin.item1", description = "..."),
        ))
    }
}
```

## 执行步骤

1. 在 `crystal-sdk` 对应包下创建 `XxxDeclaration` 数据类
2. 创建 `XxxRegistry` 类，注册方法必须包含 fail-fast 冲突检测
3. 创建 `XxxConfigurer` 接口
4. 在 `crystal-starter` 中创建 `XxxRegistryConfiguration`，将 Registry 暴露为 Spring Bean
5. 如有内置声明，创建 `XxxBuiltinConfigurer` 实现 `XxxConfigurer`

## 输出格式

完成后说明：
1. 新增的 Declaration 类路径
2. Registry 类路径及唯一键字段
3. Configurer 接口路径
4. Spring 配置类路径
5. 内置声明注册位置（如有）
