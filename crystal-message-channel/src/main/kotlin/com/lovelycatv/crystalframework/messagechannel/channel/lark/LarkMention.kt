package com.lovelycatv.crystalframework.messagechannel.channel.lark

/**
 * Resolved Lark @ target. [userId] accepts open_id / union_id / user_id —
 * Lark's @ tag is tolerant of all three identifier formats.
 */
data class LarkMention(
    val userId: String,
    val displayName: String? = null,
)
