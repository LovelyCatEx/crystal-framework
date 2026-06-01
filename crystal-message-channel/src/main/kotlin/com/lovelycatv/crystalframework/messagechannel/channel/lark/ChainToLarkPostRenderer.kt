package com.lovelycatv.crystalframework.messagechannel.channel.lark

import com.lovelycatv.crystalframework.messagechannel.types.chain.AtSegment
import com.lovelycatv.crystalframework.messagechannel.types.chain.BytesImageSource
import com.lovelycatv.crystalframework.messagechannel.types.chain.ImageSegment
import com.lovelycatv.crystalframework.messagechannel.types.chain.LinkSegment
import com.lovelycatv.crystalframework.messagechannel.types.chain.MessageChain
import com.lovelycatv.crystalframework.messagechannel.types.chain.NewlineSegment
import com.lovelycatv.crystalframework.messagechannel.types.chain.RawHtmlSegment
import com.lovelycatv.crystalframework.messagechannel.types.chain.ResourceImageSource
import com.lovelycatv.crystalframework.messagechannel.types.chain.TextSegment
import com.lovelycatv.crystalframework.messagechannel.types.chain.UrlImageSource
import com.lovelycatv.vertex.log.logger
import org.springframework.stereotype.Component

/**
 * Renders a [MessageChain] into the Lark `post` msg_type payload structure
 * (zh_cn locale, paragraph array, node array).
 *
 * Behavior:
 *  - Each [NewlineSegment] starts a new paragraph.
 *  - Each [ImageSegment] occupies its own paragraph (required by Lark).
 *  - [RawHtmlSegment] is dropped with a warn log; Lark post does not accept raw HTML.
 *  - Image sources that have not been uploaded to Lark (only [ResourceImageSource]
 *    with a key that does not look like an `img_*` image_key, or [UrlImageSource]
 *    or [BytesImageSource]) are dropped with a warn log; future upload integration
 *    should pre-upload these and replace the source.
 */
@Component
class ChainToLarkPostRenderer {
    private val logger = logger()

    suspend fun render(
        title: String?,
        chain: MessageChain,
        atResolver: LarkAtResolver?,
    ): Map<String, Any?> {
        val paragraphs = mutableListOf<MutableList<Map<String, Any?>>>()
        var current = mutableListOf<Map<String, Any?>>()
        paragraphs += current

        fun breakParagraph() {
            current = mutableListOf()
            paragraphs += current
        }

        for (segment in chain.segments) {
            when (segment) {
                is TextSegment -> if (segment.text.isNotEmpty()) {
                    current += mapOf("tag" to TAG_TEXT, "text" to segment.text)
                }
                is AtSegment -> renderAt(segment, atResolver)?.let { current += it }
                is LinkSegment -> current += mapOf(
                    "tag" to TAG_A,
                    "text" to (segment.title ?: segment.url),
                    "href" to segment.url,
                )
                NewlineSegment -> breakParagraph()
                is ImageSegment -> renderImage(segment)?.let { node ->
                    if (current.isNotEmpty()) breakParagraph()
                    current += node
                    breakParagraph()
                }
                is RawHtmlSegment ->
                    logger.warn("ChainToLarkPostRenderer: RawHtmlSegment dropped; Lark post does not support raw HTML")
            }
        }

        val cleanedParagraphs = paragraphs.filter { it.isNotEmpty() }

        val localeBlock = mutableMapOf<String, Any?>("content" to cleanedParagraphs)
        title?.takeIf { it.isNotBlank() }?.let { localeBlock["title"] = it }

        return mapOf(DEFAULT_LOCALE to localeBlock)
    }

    private suspend fun renderAt(at: AtSegment, resolver: LarkAtResolver?): Map<String, Any?>? {
        val mention = resolver?.resolve(at)
        return if (mention != null) {
            mapOf("tag" to TAG_AT, "user_id" to mention.userId)
        } else {
            val displayName = at.displayName ?: FALLBACK_AT_NAME
            mapOf("tag" to TAG_TEXT, "text" to "@$displayName")
        }
    }

    private fun renderImage(segment: ImageSegment): Map<String, Any?>? = when (val src = segment.source) {
        is ResourceImageSource -> {
            // crystal-resource id is not a Lark image_key; needs upload integration to be added later
            logger.warn(
                "ChainToLarkPostRenderer: ResourceImageSource '{}' cannot be sent directly to Lark; integrate upload pipeline first",
                src.resourceId,
            )
            null
        }
        is UrlImageSource -> {
            logger.warn(
                "ChainToLarkPostRenderer: UrlImageSource '{}' cannot be sent directly to Lark; upload to Lark first to obtain image_key",
                src.url,
            )
            null
        }
        is BytesImageSource -> {
            logger.warn("ChainToLarkPostRenderer: BytesImageSource cannot be sent directly to Lark; upload to Lark first to obtain image_key")
            null
        }
    }

    private companion object {
        const val DEFAULT_LOCALE = "zh_cn"
        const val TAG_TEXT = "text"
        const val TAG_AT = "at"
        const val TAG_A = "a"
        const val FALLBACK_AT_NAME = "unknown"
    }
}
