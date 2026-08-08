package com.lovelycatv.crystalframework.spi.message.audience

import com.lovelycatv.crystalframework.sdk.message.Audience
import com.lovelycatv.crystalframework.sdk.message.AudienceCandidate
import com.lovelycatv.crystalframework.sdk.message.config.MessageAudienceResolver
import com.lovelycatv.crystalframework.sdk.message.types.AudienceType
import org.springframework.stereotype.Component

/**
 * Reserved segment audience. No segmentation engine exists yet, so nobody matches;
 * this resolver is registered explicitly to keep the registry total over
 * [AudienceType] and avoid a runtime "no resolver" failure if such a broadcast is
 * ever read before the feature lands.
 */
@Component
class SegmentMessageAudienceResolver : MessageAudienceResolver {
    override val audienceType: AudienceType = AudienceType.SEGMENT

    override suspend fun matches(audience: Audience, candidate: AudienceCandidate): Boolean = false
}
