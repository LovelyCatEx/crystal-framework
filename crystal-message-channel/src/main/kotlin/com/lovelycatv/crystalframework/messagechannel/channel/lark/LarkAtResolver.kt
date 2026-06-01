package com.lovelycatv.crystalframework.messagechannel.channel.lark

import com.lovelycatv.crystalframework.messagechannel.types.resolver.AtResolver

/**
 * Implement and expose as a Spring bean to enable @ mentions in Lark messages.
 * Resolves an abstract [com.lovelycatv.crystalframework.messagechannel.types.chain.AtSegment]
 * (platform user/tenant ids) into a Lark [LarkMention] (open_id / user_id / union_id).
 *
 * Without an implementation, the Lark renderer degrades @ segments to plain text
 * using the segment's own `displayName` (or "@unknown" as a last resort).
 */
interface LarkAtResolver : AtResolver<LarkMention>
