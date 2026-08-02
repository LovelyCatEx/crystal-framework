package com.lovelycatv.crystalframework.shared.types.common

/**
 * Read-access policy bound to a [com.lovelycatv.crystalframework.resource.types.ResourceFileType].
 *
 * Configured by system administrators through system settings. The concrete access decision is
 * computed by combining this policy with the resource's own `(scope, scopeId)` and the current
 * viewer context (see the resource module's access service).
 *
 * - [PUBLIC]: readable by anyone, including anonymous requests (e.g. product images).
 * - [AUTHENTICATED]: readable by any logged-in user.
 * - [SCOPE_MEMBER]: when the resource scope is TENANT, readable only by members of that tenant;
 *   when the scope is SYSTEM, degrades to any logged-in user.
 * - [OWNER_ONLY]: readable only by the uploader.
 * - [SYSTEM_ADMIN]: readable only by system administrators.
 *
 * Serialized as the enum [name] (mirrors [ApiEncryptionScope]); this is a settings-value enum,
 * not an entity-field enum, so it intentionally carries no `typeId`.
 */
enum class ResourceVisibility {
    PUBLIC,
    AUTHENTICATED,
    SCOPE_MEMBER,
    OWNER_ONLY,
    SYSTEM_ADMIN,
}
