package com.lovelycatv.crystalframework.resource.types

import com.lovelycatv.crystalframework.sdk.resource.file.types.ResourceFileTypeDeclaration
import com.lovelycatv.crystalframework.shared.types.common.ResourceVisibility

/**
 * Built-in file-resource types, published as the first batch of [ResourceFileTypeDeclaration]s
 * registered in `ResourceFileTypeRegistry`. The enum stays for compile-time convenience inside
 * the framework (`ResourceFileType.USER_AVATAR.typeId`), while runtime dispatch always goes
 * through the registry so third-party types (`typeId >= 1000`) participate on equal footing.
 *
 * Do **not** switch on this enum in consumer code — read the declaration from the registry
 * instead. `typeId` values are frozen: they land in `file_resources.type` and any change would
 * break existing rows.
 *
 * `supportedContentTypes` / `supportedFileExtensions` here are the built-in defaults; deployment
 * operators can layer a YAML override on top via `crystalframework.resource.file-type-overrides.<key>`
 * without a code change. `defaultVisibility` is only used as a fall-through when the runtime
 * cannot find a system-settings-configured visibility for the type (i.e. third-party types).
 */
enum class ResourceFileType(
    override val typeId: Int,
    override val key: String,
    override val displayName: String,
    override val description: String,
    override val supportedContentTypes: Set<String>,
    override val supportedFileExtensions: Set<String>,
    override val defaultVisibility: ResourceVisibility,
    override val objectKeyPrefix: String,
) : ResourceFileTypeDeclaration {
    USER_AVATAR(
        typeId = 0,
        key = "builtin.user_avatar",
        displayName = "User avatar",
        description = "Avatar image uploaded by an end user for their own profile.",
        supportedContentTypes = setOf("image/png", "image/jpeg", "image/webp"),
        supportedFileExtensions = setOf("png", "jpg", "jpeg", "webp"),
        defaultVisibility = ResourceVisibility.PUBLIC,
        objectKeyPrefix = "user_avatar",
    ),
    TENANT_ICON(
        typeId = 1,
        key = "builtin.tenant_icon",
        displayName = "Tenant icon",
        description = "Icon image representing a tenant, visible to tenant members and administrators.",
        supportedContentTypes = setOf("image/png", "image/jpeg", "image/webp"),
        supportedFileExtensions = setOf("png", "jpg", "jpeg", "webp"),
        defaultVisibility = ResourceVisibility.PUBLIC,
        objectKeyPrefix = "tenant_icon",
    ),
    TENANT_MEMBER_AVATAR(
        typeId = 2,
        key = "builtin.tenant_member_avatar",
        displayName = "Tenant member avatar",
        description = "Avatar image uploaded by a member within the context of a specific tenant.",
        supportedContentTypes = setOf("image/png", "image/jpeg", "image/webp"),
        supportedFileExtensions = setOf("png", "jpg", "jpeg", "webp"),
        defaultVisibility = ResourceVisibility.SCOPE_MEMBER,
        objectKeyPrefix = "tenant_member_avatar",
    ),
    ;

    companion object {
        fun getByTypeId(typeId: Int): ResourceFileType? {
            return entries.find { it.typeId == typeId }
        }
    }
}
