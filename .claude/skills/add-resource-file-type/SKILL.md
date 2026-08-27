---
name: add-resource-file-type
description: 通过 crystal-sdk 向 ResourceFileTypeRegistry 注册一个新的资源文件类型（如企业头像、合同附件等），使框架的上传/下载/权限流程识别该类型。
---

# 新增资源文件类型（ResourceFileType）

## 触发条件

当用户需要在 crystal-resource 提供的三种内置文件类型（`USER_AVATAR` / `TENANT_ICON` / `TENANT_MEMBER_AVATAR`）之外，为业务或第三方模块新增一种文件类型时使用。典型场景：

- 新业务模块需要上传一类专属文件（例如"公司认证文件"、"课程封面"）
- 第三方插件希望把自己特有的资源类型接入框架的上传/下载/可见性体系
- 需要为某种文件类型定义独立的 MIME/扩展名白名单和默认可见性

> 若只想调整**已有**内置文件类型的可见性，请改用系统设置页 `resource.visibility.builtin.*`，不要改代码。

## 输入格式

用户需要提供：

1. **业务归属模块名**（用作 vendor 命名空间前缀，如 `acme`、`myplugin`）
2. **文件类型 key**（小写字母 / 数字 / `_` / `-`，禁止 `.`，命名建议 `<vendor>_<purpose>`，如 `acme_corporate_avatar`）
3. **typeId**（`Int`，第三方**必须** `>= 1000`；`0..999` 为框架保留段）
4. **displayName**（管理端展示的人类可读名称）
5. **允许的 MIME content-types**（`Set<String>`，空集 = 拒绝一切上传）
6. **允许的文件扩展名**（`Set<String>`，小写，无点前缀）
7. **默认可见性**（`ResourceVisibility` 枚举之一）
8. **对象存储路径前缀**（`objectKeyPrefix`，可选，默认取 key 的最后一段；**一旦有数据入库就不允许改**）

## 前提信息

### 涉及的 SDK 类型

| 类型 | 位置 | 作用 |
|---|---|---|
| `ResourceFileTypeDeclaration` | `crystal-sdk/.../sdk/resource/file/types/ResourceFileTypeDeclaration.kt` | 声明接口，第三方类型必须实现 |
| `ResourceFileTypeRegistry` | `crystal-sdk/.../sdk/resource/file/ResourceFileTypeRegistry.kt` | 全局注册表，按 `typeId` 和 `key` 双维度唯一 |
| `ResourceFileTypeConfigurer` | `crystal-sdk/.../sdk/resource/file/config/ResourceFileTypeConfigurer.kt` | SPI 函数接口 `fun configure(registry)`，注册为 Spring `@Component` |
| `ResourceVisibility` | `crystal-shared/.../shared/types/common/ResourceVisibility.kt` | 可见性枚举：`PUBLIC` / `AUTHENTICATED` / `SCOPE_MEMBER` / `OWNER_ONLY` / `SYSTEM_ADMIN` |

### 声明接口字段（`ResourceFileTypeDeclaration`）

| 字段 | 类型 | 说明 |
|---|---|---|
| `typeId` | `Int` | **主键**，写入 `file_resources.type`；第三方 `>= 1000` |
| `key` | `String` | 全局唯一字符串键；**禁止包含 `.`**（会与系统设置 group/tab 分隔符冲突）；建议 `<vendor>_<purpose>` |
| `displayName` | `String` | 管理端展示名 |
| `description` | `String` | 长描述，默认空 |
| `supportedContentTypes` | `Set<String>` | 允许的 MIME；默认空集（拒绝一切） |
| `supportedFileExtensions` | `Set<String>` | 允许的扩展名（小写，无点）；默认空集 |
| `defaultVisibility` | `ResourceVisibility` | 默认可见性；对第三方类型为**最终生效值**，内置类型才优先读系统设置 |
| `objectKeyPrefix` | `String` | 对象存储路径前缀；默认取 `key.substringAfterLast('.')`，**一旦有数据入库不可再改** |

### 内置类型的写法（可仿照）

`crystal-resource/.../resource/types/ResourceFileType.kt` 是内置 enum 直接实现 `ResourceFileTypeDeclaration` 的范式：

```kotlin
enum class ResourceFileType(
    override val typeId: Int,
    override val key: String,
    override val displayName: String,
    ...
) : ResourceFileTypeDeclaration {
    USER_AVATAR(
        typeId = 0,
        key = "builtin_user_avatar",
        displayName = "User avatar",
        supportedContentTypes = setOf("image/png", "image/jpeg", "image/webp"),
        supportedFileExtensions = setOf("png", "jpg", "jpeg", "webp"),
        defaultVisibility = ResourceVisibility.PUBLIC,
        objectKeyPrefix = "user_avatar",
        ...
    ),
    ...
}
```

内置类型的注册器（可仿照）：`crystal-resource/.../resource/config/BuiltinResourceFileTypeConfigurer.kt`。

### 冲突检测（Registry 会 fail-fast）

`ResourceFileTypeRegistry.register()` 在以下情形会直接抛 `IllegalStateException`，导致应用**启动失败**：

- `key` 为空
- `key` 含 `.`
- `typeId` 与已注册项重复
- `key` 与已注册项重复

内置 configurer 的 `@Order(Ordered.HIGHEST_PRECEDENCE)` 确保**内置总是先注册**，第三方 configurer 一旦冲突就会在启动时暴露。

## 执行步骤

### 第 1 步：定义你的 Declaration 实现

**文件：** `<your-module>/.../types/MyResourceFileTypes.kt`（放在你自己模块的 `types` 包下）

**操作：** 用 `enum class` 实现 `ResourceFileTypeDeclaration`（推荐，便于集中管理多个第三方类型），或用普通 `object` / `data class`（只有一个类型时也可以）。

**示例（enum 方式，推荐）：**

```kotlin
package com.acme.myplugin.resource.types

import com.lovelycatv.crystalframework.sdk.resource.file.types.ResourceFileTypeDeclaration
import com.lovelycatv.crystalframework.shared.types.common.ResourceVisibility

enum class AcmeResourceFileType(
    override val typeId: Int,
    override val key: String,
    override val displayName: String,
    override val description: String,
    override val supportedContentTypes: Set<String>,
    override val supportedFileExtensions: Set<String>,
    override val defaultVisibility: ResourceVisibility,
    override val objectKeyPrefix: String,
) : ResourceFileTypeDeclaration {
    CORPORATE_AVATAR(
        typeId = 1000,
        key = "acme_corporate_avatar",
        displayName = "Corporate avatar",
        description = "Company logo shown on corporate profile pages.",
        supportedContentTypes = setOf("image/png", "image/jpeg", "image/webp", "image/svg+xml"),
        supportedFileExtensions = setOf("png", "jpg", "jpeg", "webp", "svg"),
        defaultVisibility = ResourceVisibility.PUBLIC,
        objectKeyPrefix = "acme_corporate_avatar",
    ),
    CONTRACT_ATTACHMENT(
        typeId = 1001,
        key = "acme_contract_attachment",
        displayName = "Contract attachment",
        description = "PDF or scanned contract uploaded during onboarding.",
        supportedContentTypes = setOf("application/pdf"),
        supportedFileExtensions = setOf("pdf"),
        defaultVisibility = ResourceVisibility.OWNER_ONLY,
        objectKeyPrefix = "acme_contract",
    ),
    ;
}
```

**关键注意：**

- `typeId` 必须 `>= 1000`，且在你自己所有类型之间也不能重复
- `key` **禁止含 `.`**，使用 `_` / `-`；建议 `<vendor>_<purpose>` 形式
- `objectKeyPrefix` 一旦发布上线就**冻结**，不可修改，否则旧文件全部对不上
- `defaultVisibility` 对第三方类型是最终生效值——内置类型才走系统设置覆盖

### 第 2 步：创建 Configurer 并注册为 Spring Bean

**文件：** `<your-module>/.../config/AcmeResourceFileTypeConfigurer.kt`

**操作：** 实现 `ResourceFileTypeConfigurer`，标注 `@Component`。

**示例：**

```kotlin
package com.acme.myplugin.resource.config

import com.acme.myplugin.resource.types.AcmeResourceFileType
import com.lovelycatv.crystalframework.sdk.resource.file.ResourceFileTypeRegistry
import com.lovelycatv.crystalframework.sdk.resource.file.config.ResourceFileTypeConfigurer
import org.springframework.stereotype.Component

@Component
class AcmeResourceFileTypeConfigurer : ResourceFileTypeConfigurer {
    override fun configure(registry: ResourceFileTypeRegistry) {
        registry.registers(AcmeResourceFileType.entries)
    }
}
```

**关键注意：**

- **不要**标注 `@Order(Ordered.HIGHEST_PRECEDENCE)`——那是内置 configurer 专用的，第三方应留在默认顺序，让内置先注册
- 只上一个类型也可以用 `registry.register(AcmeResourceFileType.CORPORATE_AVATAR)`
- `ResourceFileTypeConfigurer` 是 `fun interface`，也可以写成 Lambda `@Bean` 形式

### 第 3 步：确保模块能被 Spring 扫描到

**操作：** 若你的模块未被 `crystal-starter` 的 `@SpringBootApplication` / `@ComponentScan` 覆盖，需要在你的模块提供 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`（Spring Boot 3+ 的自动装配格式）或者让用户在自己的应用类上加 `@ComponentScan`。

**关键注意：** 这一步只在你的模块作为**独立 jar 依赖** 被引入时才需要。若代码直接放在 `crystal-starter` 的可扫描包内则可跳过。

### 第 4 步：（可选）在业务代码里使用新类型

**操作：** 上传时把 `typeId` 传给 `FileResourceService`：

```kotlin
val declaration = resourceFileTypeRegistry.getByKey("acme_corporate_avatar")!!
fileResourceService.upload(
    typeId = declaration.typeId,
    ...
)
```

或直接引用你自己模块的 enum：`AcmeResourceFileType.CORPORATE_AVATAR.typeId`。

### 第 5 步：（可选）前端 i18n 展示名

**操作：** 若管理端页面需要显示新类型的本地化名称，在前端 i18n 里追加 key。参考现有系统设置对可见性的处理（`enums.ResourceVisibility.*` 是共享枚举，不需要为你的新类型重复添加）；类型自身的 `displayName` 由后端 `ResourceFileTypeDeclaration.displayName` 提供，前端可直接展示，也可覆盖为 i18n key。

### 第 6 步：验证

**后端：**

```bash
./mvnw -pl <your-module> -am clean compile -DskipTests
```

**启动验证：** 应用启动后应看到：

- 无 `ResourceFileTypeRegistry: duplicate typeId ...` / `duplicate key ...` 异常
- `ResourceFileTypeRegistry.declarations()` 返回列表中包含你的新类型

**上传验证：**

- 用允许的 content-type + 扩展名上传，成功
- 用不在白名单里的 content-type / 扩展名上传，返回上传拒绝错误
- 下载时可见性按 `defaultVisibility` 生效

**必须确认：**

- ✅ 编译通过
- ✅ 应用启动无异常
- ✅ 上传/下载符合白名单和可见性预期
- ✅ `typeId` 与所有已注册类型都不冲突

## 输出格式

完成后向用户汇报：

1. 新增的 Declaration 类路径与其中的类型列表（typeId / key / displayName）
2. Configurer 类路径
3. 是否需要用户在启动应用里补 `@ComponentScan`
4. 白名单（content-type / extension）与默认可见性
5. `objectKeyPrefix` 值，并**明确提示这是冻结值**

## 关联 Skills

- `add-registry` —— 想理解 Registry 的通用套路时参考
- `add-system-settings` —— 若想让新类型的可见性可通过管理端在运行时切换，需要另外扩展系统设置项

## 常见错误

### 错误 1：`typeId` 用了 `0..999`

启动直接抛 `duplicate typeId`。第三方必须 `>= 1000`。

### 错误 2：`key` 里有 `.`

启动直接抛 `key '...' must not contain '.'`。用 `_` 或 `-` 分词。

### 错误 3：修改了已上线类型的 `objectKeyPrefix`

数据库里的历史 `file_resources` 仍指向旧路径，导致旧文件全部下载 404。**任何时候都不要改**。

### 错误 4：期待系统设置里能出现第三方类型的可见性开关

目前系统设置中的 `resource.visibility.builtin.*` 只覆盖内置三种类型，第三方类型的可见性由 `defaultVisibility` 最终决定。若确有需求，需要单独提 issue 让框架扩展"运行时可切换可见性"到第三方类型。
