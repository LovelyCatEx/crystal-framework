package com.lovelycatv.crystalframework.messagechannel.types.chain

/**
 * Abstract user reference. Carries platform-level identifiers only;
 * each channel resolves them via its own [com.lovelycatv.crystalframework.messagechannel.types.resolver.AtResolver].
 *
 * @param userId   platform user id (null when mentioning a non-user target, e.g. a role)
 * @param tenantId tenant id, present when the @ scope is "a member inside this tenant"
 * @param displayName fallback display name shown when the channel cannot resolve the target
 */
data class AtSegment(
    val userId: String? = null,
    val tenantId: String? = null,
    val displayName: String? = null,
) : MessageSegment
