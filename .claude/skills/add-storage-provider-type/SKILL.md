---
name: add-storage-provider-type
description: 通过 crystal-sdk 向 StorageProviderTypeRegistry 注册一个新的对象存储厂商类型，让框架管理端识别该类型；如需运行时可用，还需实现 FileResourceServiceFactory。
---

# 新增存储服务商类型（StorageProviderType）

## 触发条件

当用户需要在 crystal-resource 提供的四种内置存储厂商（`LOCAL_FILE_SYSTEM` / `ALIYUN_OSS` / `TENCENT_COS` / `VOLCENGINE_TOS`）之外，接入一种新的对象存储实现（如 MinIO、AWS S3、七牛云等）时使用。

## ⚠️ 现状与限制（必读）

当前框架对"扩展存储厂商"的支持**只完成了一半**，动手前请与用户确认目标：

| 能力 | 现状 | 说明 |
|---|---|---|
| 通过 SDK 注册厂商声明（`StorageProviderTypeDeclaration`）到 `StorageProviderTypeRegistry` | ✅ 已就绪 | 管理端 UI、`storage_providers.type` 的显示/映射能识别第三方 `typeId` |
| 通过实现 `FileResourceServiceFactory` 提供实际的上传/下载运行时 | ⚠️ **暂不支持** | `FileResourceServiceFactory.getStorageProviderType()` 返回类型仍是**内置 enum** `StorageProviderType`，无法返回第三方声明；`FileResourceServiceManager` 里的匹配逻辑 `it.getStorageProviderType() == provider.getRealStorageProviderType()` 也只能用内置枚举比较 |

**结论：** 第三方今天**可以**注册声明让管理端认识新类型，**但无法**让新类型跑起来。要真正打通运行时，需要框架先把 factory 与 manager 的匹配改为按 `typeId: Int` 而非 enum 比较——这一步需要用户先决定是否要动。

**如果用户只需要"内部新加一个内置厂商"**（在 `crystal-resource` 内直接扩展），流程见本文档"内置厂商扩展路径"章节；这条路不受上述限制。

## 输入格式

用户需要提供：

1. **业务归属模块名**（vendor 命名空间前缀，如 `acme`）
2. **厂商 key**（全局唯一字符串，如 `acme.minio`；SDK 声明层允许含 `.`，与文件类型不同）
3. **typeId**（`Int`，第三方 `>= 1000`；`0..3` 为框架保留）
4. **displayName**（管理端展示名）
5. **description**（可选长描述）
6. **是否要求实现运行时 factory**（若"是"，先阅读上文限制章节）

## 前提信息

### 涉及的 SDK 类型

| 类型 | 位置 | 作用 |
|---|---|---|
| `StorageProviderTypeDeclaration` | `crystal-sdk/.../sdk/resource/storage/types/StorageProviderTypeDeclaration.kt` | 声明接口 |
| `StorageProviderTypeRegistry` | `crystal-sdk/.../sdk/resource/storage/StorageProviderTypeRegistry.kt` | 全局注册表，`typeId` 与 `key` 双维度唯一 |
| `StorageProviderTypeConfigurer` | `crystal-sdk/.../sdk/resource/storage/config/StorageProviderTypeConfigurer.kt` | SPI 函数接口 |

### 声明接口字段（`StorageProviderTypeDeclaration`）

| 字段 | 类型 | 说明 |
|---|---|---|
| `typeId` | `Int` | **主键**，写入 `storage_providers.type`；第三方 `>= 1000` |
| `key` | `String` | 全局唯一字符串键（允许含 `.`，与 `ResourceFileTypeDeclaration.key` 不同） |
| `displayName` | `String` | 管理端展示名 |
| `description` | `String` | 长描述，默认空 |

### 内置类型写法（可仿照）

`crystal-resource/.../resource/types/StorageProviderType.kt`：

```kotlin
enum class StorageProviderType(
    override val typeId: Int,
    override val key: String,
    override val displayName: String,
    override val description: String,
) : StorageProviderTypeDeclaration {
    LOCAL_FILE_SYSTEM(typeId = 0, key = "builtin.local_file_system", ...),
    ALIYUN_OSS(typeId = 1, key = "builtin.aliyun_oss", ...),
    TENCENT_COS(typeId = 2, key = "builtin.tencent_cos", ...),
    VOLCENGINE_TOS(typeId = 3, key = "builtin.volcengine_tos", ...),
    ;
}
```

内置注册器：`crystal-resource/.../resource/config/BuiltinStorageProviderTypeConfigurer.kt`（`@Order(Ordered.HIGHEST_PRECEDENCE)`，先于第三方注册）。

### Registry 冲突检测

`StorageProviderTypeRegistry.register()` 会 fail-fast：`key` 为空、`typeId` 重复、`key` 重复都会抛 `IllegalStateException`。

## 执行步骤

### 场景 A：仅注册 SDK 声明（管理端可见即可）

#### 第 1 步：定义 Declaration 实现

**文件：** `<your-module>/.../types/AcmeStorageProviderType.kt`

```kotlin
package com.acme.myplugin.resource.types

import com.lovelycatv.crystalframework.sdk.resource.storage.types.StorageProviderTypeDeclaration

enum class AcmeStorageProviderType(
    override val typeId: Int,
    override val key: String,
    override val displayName: String,
    override val description: String,
) : StorageProviderTypeDeclaration {
    MINIO(
        typeId = 1000,
        key = "acme.minio",
        displayName = "MinIO",
        description = "Self-hosted S3-compatible object storage.",
    ),
    ;
}
```

**关键注意：**

- `typeId >= 1000`
- `key` 建议 `<vendor>.<name>`，方便区分内置与第三方

#### 第 2 步：创建 Configurer

**文件：** `<your-module>/.../config/AcmeStorageProviderTypeConfigurer.kt`

```kotlin
package com.acme.myplugin.resource.config

import com.acme.myplugin.resource.types.AcmeStorageProviderType
import com.lovelycatv.crystalframework.sdk.resource.storage.StorageProviderTypeRegistry
import com.lovelycatv.crystalframework.sdk.resource.storage.config.StorageProviderTypeConfigurer
import org.springframework.stereotype.Component

@Component
class AcmeStorageProviderTypeConfigurer : StorageProviderTypeConfigurer {
    override fun configure(registry: StorageProviderTypeRegistry) {
        registry.registers(AcmeStorageProviderType.entries)
    }
}
```

**关键注意：不要**加 `@Order(Ordered.HIGHEST_PRECEDENCE)`，留给内置。

#### 第 3 步：验证

**后端：**

```bash
./mvnw -pl <your-module> -am clean compile -DskipTests
```

启动后管理端"存储服务商"页面应能看到新类型；数据库里 `storage_providers.type = 1000` 的记录展示时会用 `displayName`。

---

### 场景 B：真正提供运行时上传/下载 —— 目前不完全支持

想让新厂商真的能上传/下载文件，需要提供一个 `FileResourceServiceFactory<S>` 实现（可参考 `crystal-resource/.../service/api/factory/LocalFileResourceServiceFactory.kt`）。

**但是：** 现有 `FileResourceServiceFactory` 接口签名为

```kotlin
interface FileResourceServiceFactory<S: AbstractFileResourceService> {
    fun getStorageProviderType(): StorageProviderType   // ← 内置 enum，无法返回第三方 declaration
    fun build(storageProvider: StorageProviderEntity): S
}
```

且 `FileResourceServiceManager` 通过 `.filter { it.getStorageProviderType() == provider.getRealStorageProviderType() }` 匹配 factory —— **第三方无法为自己的 `typeId` 挂上 factory**。

要打通，需要框架层先做以下改造（这是**用户/框架维护者**的工作，本 skill 不覆盖代码修改）：

1. `FileResourceServiceFactory.getStorageProviderType()` 返回类型改为 `Int typeId` 或 `StorageProviderTypeDeclaration`
2. `StorageProviderEntity.getRealStorageProviderType()` 提供 `typeId: Int` 视图或改为查 Registry
3. `FileResourceServiceManager` 的匹配逻辑改为按 `typeId` 比较

**动手前必须先与用户确认是否愿意接受这次框架层改造。** 若接受，请另开任务；本 skill 只覆盖场景 A 的 SDK 声明层。

---

### 内置厂商扩展路径（不走 SDK，直接扩 crystal-resource）

若目标是**在框架内**加一个内置厂商（例如给 `crystal-resource` 官方补一个 MinIO 支持），流程反而更直接、无上述限制：

1. 在 `crystal-resource/.../resource/types/StorageProviderType.kt` 的 enum 中**追加**一个 entry（如 `MINIO(typeId = 4, ...)`）——由于内置 configurer 用 `entries` 全量注册，新 entry 自动进 Registry
2. 在 `crystal-resource/.../resource/service/api/factory/` 下新建 `MinIOFileResourceServiceFactory : FileResourceServiceFactory<...>`，标注 `@Component`
3. 在 `crystal-resource/.../resource/service/api/impl/` 下新建对应的 `AbstractFileResourceService` 实现
4. 在 `crystal-resource/.../resource/types/` 下新建对应的 `Properties` 数据类（参考 `LocalFileResourceServiceProperties`）
5. 编译 + 启动验证

**这条路能立刻跑起来**，因为 factory 的匹配用的就是内置 enum，本身就在同一模块内。

## 输出格式

完成后向用户汇报：

1. 新增的 Declaration 类路径 + typeId / key / displayName 列表
2. Configurer 类路径
3. 若走"场景 A（仅声明）"：明确告知**运行时目前无法执行**，仅管理端可见
4. 若走"内置厂商扩展路径"：Factory / ServiceImpl / Properties 三个文件的路径 + `typeId` 值

## 关联 Skills

- `add-registry` —— 通用 Registry 套路
- `add-resource-file-type` —— 同一批 Registry 化改造中的姊妹 skill；语义相似但**限制不同**（那边运行时已完全支持第三方）

## 常见错误

### 错误 1：以为注册 Declaration 就能跑
声明只让管理端"认识"新类型，实际读写文件走的是 `FileResourceServiceFactory` → `AbstractFileResourceService`，那条路目前尚未打通第三方入口（见"场景 B"）。

### 错误 2：`typeId` 与内置冲突
`0..3` 已被内置四种厂商占用，第三方必须 `>= 1000`，否则启动抛 `duplicate typeId`。

### 错误 3：在第三方 configurer 上加 `HIGHEST_PRECEDENCE`
会导致第三方比内置更早注册，一旦你的 `typeId` 与内置某项冲突，错误信息就会指向"内置重复"，误导排查。留默认顺序即可。
