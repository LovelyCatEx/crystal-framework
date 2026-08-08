package com.lovelycatv.crystalframework.sdk.message.types

/**
 * The isolation boundary a conversation / announcement belongs to.
 *
 * Serialized into the conversation dedupe key, so the same pair of users inside
 * different scopes yields distinct, naturally isolated conversations. New scope
 * types are added by registering a new
 * [com.lovelycatv.crystalframework.sdk.message.config.MessageScopeResolver] — the
 * core never references any concrete boundary (e.g. tenant) directly.
 */
enum class ScopeType(val typeId: Int) {
    /** Global boundary (platform-level messaging, system announcements). No scopeId. */
    SYSTEM(0),

    /** A single tenant boundary. scopeId = tenantId. */
    TENANT(1);

    companion object {
        fun getByTypeId(typeId: Int): ScopeType? = entries.find { it.typeId == typeId }
    }
}
