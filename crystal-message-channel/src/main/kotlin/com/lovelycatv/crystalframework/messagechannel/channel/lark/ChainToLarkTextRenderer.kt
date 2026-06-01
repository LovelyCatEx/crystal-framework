package com.lovelycatv.crystalframework.messagechannel.channel.lark

import com.lovelycatv.crystalframework.messagechannel.types.chain.AtSegment
import com.lovelycatv.crystalframework.messagechannel.types.chain.ImageSegment
import com.lovelycatv.crystalframework.messagechannel.types.chain.LinkSegment
import com.lovelycatv.crystalframework.messagechannel.types.chain.MessageChain
import com.lovelycatv.crystalframework.messagechannel.types.chain.NewlineSegment
import com.lovelycatv.crystalframework.messagechannel.types.chain.RawHtmlSegment
import com.lovelycatv.crystalframework.messagechannel.types.chain.TextSegment
import com.lovelycatv.vertex.log.logger
import org.springframework.stereotype.Component

/**
 * Renders a [MessageChain] into the Lark `text` msg_type payload — a single string
 * with inline `<at user_id="...">name</at>` mentions and markdown-style hyperlinks.
 *
 * Limitations of this path (caller should fall back to post renderer when present):
 *  - [ImageSegment] is unsupported and dropped with a warn log.
 *  - [RawHtmlSegment] is unsupported and dropped with a warn log.
 */
@Component
class ChainToLarkTextRenderer {
    private val logger = logger()

    suspend fun render(chain: MessageChain, atResolver: LarkAtResolver?): String {
        val sb = StringBuilder()
        for (segment in chain.segments) {
            when (segment) {
                is TextSegment -> sb.append(segment.text)
                is AtSegment -> sb.append(renderAt(segment, atResolver))
                is LinkSegment -> sb.append(renderLink(segment))
                NewlineSegment -> sb.append('\n')
                is ImageSegment -> {
                    logger.warn("ChainToLarkTextRenderer: ImageSegment dropped; switch to post renderer if image is required")
                }
                is RawHtmlSegment -> {
                    logger.warn("ChainToLarkTextRenderer: RawHtmlSegment dropped; raw HTML is not supported in Lark messages")
                }
            }
        }
        return sb.toString()
    }

    private suspend fun renderAt(at: AtSegment, resolver: LarkAtResolver?): String {
        val mention = resolver?.resolve(at)
        return if (mention != null) {
            val nameAttr = mention.displayName?.let { it } ?: ""
            "<at user_id=\"${escapeAttr(mention.userId)}\">${nameAttr}</at>"
        } else {
            "@${at.displayName ?: FALLBACK_AT_NAME}"
        }
    }

    private fun renderLink(segment: LinkSegment): String {
        val text = segment.title ?: segment.url
        return "[$text](${segment.url})"
    }

    private fun escapeAttr(value: String): String = value.replace("\"", "&quot;")

    private companion object {
        const val FALLBACK_AT_NAME = "unknown"
    }
}
