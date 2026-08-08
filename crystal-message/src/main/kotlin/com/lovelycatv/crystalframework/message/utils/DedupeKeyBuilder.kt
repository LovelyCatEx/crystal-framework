package com.lovelycatv.crystalframework.message.utils

import com.lovelycatv.crystalframework.message.constants.MessageConstants
import com.lovelycatv.crystalframework.message.types.ConversationKind
import com.lovelycatv.crystalframework.sdk.message.Party
import com.lovelycatv.crystalframework.sdk.message.Scope

/**
 * Builds a conversation dedupe key from `scope | kind | normalized-parties`. This
 * is the single point where conversation isolation is realized: distinct scopes
 * produce distinct keys (so the same pair of users is isolated per scope), and the
 * party segment is order-independent (sorted) so (A,B) and (B,A) collapse to one
 * conversation. The core never inspects a concrete tenant — only the scope key.
 */
object DedupeKeyBuilder {
    fun build(scope: Scope, kind: ConversationKind, parties: Collection<Party>): String {
        val partySegment = parties
            .map { "${it.type.typeId}:${it.id ?: ""}" }
            .sorted()
            .joinToString(MessageConstants.DEDUPE_PARTY_SEPARATOR)

        return listOf(scope.key(), kind.typeId.toString(), partySegment)
            .joinToString(MessageConstants.DEDUPE_SEGMENT_SEPARATOR)
    }
}
