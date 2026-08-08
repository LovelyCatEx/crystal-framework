package com.lovelycatv.crystalframework.spi.message.audience

import com.lovelycatv.crystalframework.sdk.message.Audience
import com.lovelycatv.crystalframework.sdk.message.AudienceCandidate
import com.lovelycatv.crystalframework.sdk.message.config.MessageAudienceResolver
import com.lovelycatv.crystalframework.sdk.message.types.AudienceType
import org.springframework.stereotype.Component

/**
 * Platform-wide audience: every authenticated user is a recipient, regardless of
 * tenant. Used by system-scope broadcasts (e.g. system announcements).
 */
@Component
class AllUsersMessageAudienceResolver : MessageAudienceResolver {
    override val audienceType: AudienceType = AudienceType.ALL_USERS

    override suspend fun matches(audience: Audience, candidate: AudienceCandidate): Boolean = true
}
