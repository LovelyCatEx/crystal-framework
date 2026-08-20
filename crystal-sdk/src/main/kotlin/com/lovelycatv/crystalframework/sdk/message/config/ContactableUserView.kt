package com.lovelycatv.crystalframework.sdk.message.config

/**
 * Minimal read-only user view returned by [ContactableUserProvider]. Deliberately minimal —
 * id / username / nickname / avatar — to avoid leaking user-internal details (email, phone,
 * authorities) through the messaging surface.
 */
data class ContactableUserView(
    val id: String,        // Long serialized as String (framework convention)
    val username: String,
    val nickname: String,
    val avatar: String?,   // avatar file id, Long serialized as String; null when unset
)
