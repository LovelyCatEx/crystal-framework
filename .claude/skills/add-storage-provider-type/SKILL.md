---
name: add-storage-provider-type
description: 通过 crystal-sdk 向 StorageProviderTypeRegistry 注册一个新的对象存储厂商类型，并实现 FileResourceServiceFactory 让新类型可读写运行。第三方厂商可完整接入。
---

# 新增存储服务商类型（StorageProviderType）

## 触发条件

当用户需要在 crystal-resource 提供的四种内置存储厂商（`LOCAL_FILE_SYSTEM` / `ALIYUN_OSS` / `TENCENT_COS` / `VOLCENGINE_TOS`）之外，接入一种新的对象存储实现（如 MinIO、AWS S3、七牛云等）时使用。

## 输入格式

用户需要提供：

1. **业务归属模块名**（vendor 命名空间前缀，如 `acme`）
2. **厂商 key**（全局唯一字符串，如 `acme.minio`；SDK 声明层允许含 `.`，与文件类型不同）
3. **typeId**（`Int`，第三方 `>= 1000`；`0..3` 为框架保留）
4. **displayName**（管理端展示名）
5. **description**（可选长描述）
6. **Properties 字段**（该厂商 SDK 需要的连接参数，如 `endpoint`、`accessKey`、`secretKey`、`bucket` 等）

## 架构说明（必读）

第三方厂商已可完整接入运行时。核心机制：

- `FileResourceServiceFactory.getStorageProviderTypeId(): Int` 返回声明的 `typeId`
- `FileResourceServiceManager` 通过 `it.getStorageProviderTypeId() == provider.type` 匹配 factory
- 管理端前端 `GET /manager/storage-provider/types` 端点返回 Registry 全量声明，UI 下拉/表格标签自动包含第三方
- 前端 i18n 优先，覆盖不到的第三方 typeId 会 fallback 到后端 `displayName`

## 前提信息

### 涉及的 SDK 类型

| 类型 | 位置 | 作用 |
|---|---|---|
| `StorageProviderTypeDeclaration` | `crystal-sdk/.../sdk/resource/storage/types/StorageProviderTypeDeclaration.kt` | 声明接口 |
| `StorageProviderTypeRegistry` | `crystal-sdk/.../sdk/resource/storage/StorageProviderTypeRegistry.kt` | 全局注册表，`typeId` 与 `key` 双维度唯一 |
| `StorageProviderTypeConfigurer` | `crystal-sdk/.../sdk/resource/storage/config/StorageProviderTypeConfigurer.kt` | SPI 函数接口 |
| `FileResourceServiceFactory<S>` | `crystal-resource/.../resource/service/api/factory/FileResourceServiceFactory.kt` | 运行时工厂接口，`getStorageProviderTypeId(): Int` |
| `AbstractFileResourceService` | `crystal-resource/.../resource/service/api/AbstractFileResourceService.kt` | 运行时上传/下载抽象类，覆写 `hasVendorSignedUrl()` 可切换 URL 策略 |

### 声明接口字段（`StorageProviderTypeDeclaration`）

| 字段 | 类型 | 说明 |
|---|---|---|
| `typeId` | `Int` | **主键**，写入 `storage_providers.type`；第三方 `>= 1000` |
| `key` | `String` | 全局唯一字符串键（允许含 `.`，与 `ResourceFileTypeDeclaration.key` 不同） |
| `displayName` | `String` | 管理端展示名，前端 i18n 覆盖不到时的 fallback |
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

### 第 1 步：定义 Declaration 实现

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

### 第 2 步：创建 Configurer

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

### 第 3 步：定义 Properties 数据类

**文件：** `<your-module>/.../types/MinIOFileResourceServiceProperties.kt`

参考 `crystal-resource/.../resource/types/AliyunOSSFileResourceServiceProperties.kt`。字段是该厂商 SDK 连接所需的参数（endpoint、accessKey 等），存于 `storage_providers.properties` 的 JSON。

### 第 4 步：实现 AbstractFileResourceService

**文件：** `<your-module>/.../service/impl/MinIOFileResourceService.kt`

继承 `AbstractFileResourceService`，实现上传/下载/删除等方法。若厂商支持自签 URL（如 S3 预签名），覆写 `hasVendorSignedUrl(): Boolean = true`；否则保持默认 `false`（走 base URL + 路径拼接）。

参考 `AliyunOSSFileResourceServiceImpl` / `LocalFileResourceServiceImpl` 的实现。

### 第 5 步：实现 FileResourceServiceFactory

**文件：** `<your-module>/.../service/factory/MinIOFileResourceServiceFactory.kt`

```kotlin
package com.acme.myplugin.resource.service.factory

import com.acme.myplugin.resource.types.AcmeStorageProviderType
import com.acme.myplugin.resource.service.impl.MinIOFileResourceService
import com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity
import com.lovelycatv.crystalframework.resource.service.api.factory.FileResourceServiceFactory
import org.springframework.stereotype.Component

@Component
class MinIOFileResourceServiceFactory : FileResourceServiceFactory<MinIOFileResourceService> {
    override fun getStorageProviderTypeId(): Int = AcmeStorageProviderType.MINIO.typeId

    override fun build(storageProvider: StorageProviderEntity): MinIOFileResourceService {
        // 解析 properties JSON → MinIOFileResourceServiceProperties → 构造 Service
        ...
    }
}
```

**关键注意：** `getStorageProviderTypeId()` 必须返回与 Declaration 完全一致的 `typeId`，否则 `FileResourceServiceManager` 匹配失败，运行时会抛 "no factory for storageProviderType=xxx"。

### 第 6 步：（可选）前端 i18n 覆盖

**!!!绝对禁止修改 i18n-rules.ts 中的 I18nRules 类型定义!!!**
**!!!违反此规则=立即停止工作!!!**

若希望管理端使用本地化名称而不是后端 `displayName`，在 `web/src/i18n/locales/*.ts` 的 `enums.storageProviderType` 下加：

**强制要求（违反=严重违规）：**
1. **必须先阅读 `web/src/i18n/i18n-rules.ts` 确认 I18nRules 类型定义中 enums 字段的结构**
2. **只能在 `enums.storageProviderType` 下添加翻译**
3. **绝对禁止添加类型定义之外的任何字段**
4. **必须同步修改 zh-CN.ts 和 en-US.ts 两个文件**

```ts
enums: {
    storageProviderType: {
        1000: 'MinIO 对象存储',  // 中文
        // 英文文件里对应加英文
    }
}
```

若不加，管理端直接使用后端 `displayName` 字段值，也能显示。

**违反 I18n 规范=严重违规，必须立即停止所有工作。**

### 第 7 步：验证

```bash
./mvnw -pl <your-module> -am clean compile -DskipTests
```

启动后：

1. 管理端"存储服务商"页面的"类型"下拉能选到新类型
2. 新建一条 `type = 1000` 的记录并填 properties
3. 上传一个测试文件到该 provider —— 若失败查日志，通常是 properties JSON 结构与 Factory 里的解析不一致

## 输出格式

完成后向用户汇报：

1. Declaration 类路径 + typeId / key / displayName
2. Configurer 类路径
3. Properties 数据类路径
4. Service 实现类路径 + 是否覆写 `hasVendorSignedUrl()`
5. Factory 类路径 + 返回的 `typeId`
6. 是否新增了前端 i18n 覆盖

## 关联 Skills

- `add-registry` —— 通用 Registry 套路

## 常见错误

### 错误 1：Factory 的 `getStorageProviderTypeId()` 与 Declaration 的 `typeId` 不一致
`FileResourceServiceManager` 用 `Int` 相等匹配，任何一处填错都会导致运行时抛 "no factory for storageProviderType=xxx"。规范做法：Factory 里直接引用枚举 `AcmeStorageProviderType.MINIO.typeId`。

### 错误 2：`typeId` 与内置冲突
`0..3` 已被内置四种厂商占用，第三方必须 `>= 1000`，否则启动抛 `duplicate typeId`。

### 错误 3：在第三方 configurer 上加 `HIGHEST_PRECEDENCE`
会导致第三方比内置更早注册，一旦你的 `typeId` 与内置某项冲突，错误信息就会指向"内置重复"，误导排查。留默认顺序即可。

### 错误 4：Properties JSON 结构与 Factory 解析不一致
`storage_providers.properties` 存的是 JSON 字符串，Factory 需要用 Jackson 解析回 Properties 对象。字段名不匹配、类型不匹配都会抛反序列化异常。用 `objectMapper.readValue(props, MyProperties::class.java)` 前先检查数据。
