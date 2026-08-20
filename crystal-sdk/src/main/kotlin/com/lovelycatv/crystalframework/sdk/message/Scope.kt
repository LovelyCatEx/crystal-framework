package com.lovelycatv.crystalframework.sdk.message

import com.lovelycatv.crystalframework.sdk.message.types.ScopeType

/**
 * The isolation boundary a conversation / announcement lives in — the "where".
 *
 * @property type  the kind of boundary.
 * @property id    the entity id for that type (tenantId); null for [ScopeType.SYSTEM].
 */
data class Scope(
    val type: ScopeType,
    val id: Long?,
) {
    /**
     * Stable string key used as the scope segment of a conversation dedupe key.
     * Isolation is realized entirely here: distinct scopes → distinct keys.
     */
    fun key(): String = "${type.typeId}:${id ?: ""}"
}
