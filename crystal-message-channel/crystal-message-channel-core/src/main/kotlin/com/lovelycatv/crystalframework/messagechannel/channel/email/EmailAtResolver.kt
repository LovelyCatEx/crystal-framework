package com.lovelycatv.crystalframework.messagechannel.channel.email

import com.lovelycatv.crystalframework.messagechannel.types.chain.AtSegment
import com.lovelycatv.crystalframework.messagechannel.types.resolver.AtResolver

/**
 * Implement and expose as a Spring bean to enable rich rendering of [AtSegment] in email HTML.
 * Without an implementation, the email renderer degrades [AtSegment] to plain text using
 * the segment's own `displayName` (or a `@unknown` placeholder).
 */
interface EmailAtResolver : AtResolver<EmailAtFragment>
