package com.lovelycatv.crystalframework.resource.types

import com.lovelycatv.crystalframework.sdk.resource.storage.types.StorageProviderTypeDeclaration

/**
 * Built-in storage-provider types, published as the first batch of [StorageProviderTypeDeclaration]s
 * registered in `StorageProviderTypeRegistry`. The enum stays for compile-time convenience inside
 * the framework (`StorageProviderType.LOCAL_FILE_SYSTEM.typeId`), while runtime dispatch always
 * goes through the registry so third-party types (`typeId >= 1000`) participate on equal footing.
 *
 * Do **not** switch on this enum in consumer code — read the declaration from the registry
 * instead. `typeId` values are frozen: they land in `storage_providers.type` and any change would
 * break existing rows.
 */
enum class StorageProviderType(
    override val typeId: Int,
    override val key: String,
    override val displayName: String,
    override val description: String,
) : StorageProviderTypeDeclaration {
    LOCAL_FILE_SYSTEM(
        typeId = 0,
        key = "builtin.local_file_system",
        displayName = "Local file system",
        description = "Files served from the application's own filesystem via LocalFileResourceController.",
    ),
    ALIYUN_OSS(
        typeId = 1,
        key = "builtin.aliyun_oss",
        displayName = "Aliyun OSS",
        description = "Object storage on Aliyun (阿里云对象存储).",
    ),
    TENCENT_COS(
        typeId = 2,
        key = "builtin.tencent_cos",
        displayName = "Tencent COS",
        description = "Object storage on Tencent Cloud (腾讯云对象存储).",
    ),
    VOLCENGINE_TOS(
        typeId = 3,
        key = "builtin.volcengine_tos",
        displayName = "Volcengine TOS",
        description = "Object storage on Volcengine (火山引擎对象存储).",
    ),
    ;

    companion object {
        fun getByTypeId(typeId: Int): StorageProviderType? {
            return entries.find { it.typeId == typeId }
        }
    }
}
