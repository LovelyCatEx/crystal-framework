package com.lovelycatv.crystalframework.messagechannel.channel.email

import com.lovelycatv.crystalframework.messagechannel.types.chain.AtSegment
import com.lovelycatv.crystalframework.messagechannel.types.chain.BytesImageSource
import com.lovelycatv.crystalframework.messagechannel.types.chain.ImageSegment
import com.lovelycatv.crystalframework.messagechannel.types.chain.LinkSegment
import com.lovelycatv.crystalframework.messagechannel.types.chain.MessageChain
import com.lovelycatv.crystalframework.messagechannel.types.chain.NewlineSegment
import com.lovelycatv.crystalframework.messagechannel.types.chain.ResourceImageSource
import com.lovelycatv.crystalframework.messagechannel.types.chain.TextSegment
import com.lovelycatv.crystalframework.messagechannel.types.chain.UrlImageSource
import com.lovelycatv.vertex.log.logger
import org.springframework.stereotype.Component

@Component
class ChainToHtmlRenderer {
    private val logger = logger()

    suspend fun render(chain: MessageChain, atResolver: EmailAtResolver?): String {
        val sb = StringBuilder()
        for (segment in chain.segments) {
            when (segment) {
                is TextSegment -> sb.append(escape(segment.text))
                is AtSegment -> sb.append(renderAt(segment, atResolver))
                is ImageSegment -> sb.append(renderImage(segment))
                is LinkSegment -> sb.append(renderLink(segment))
                NewlineSegment -> sb.append("<br/>")
            }
        }
        return sb.toString()
    }

    private suspend fun renderAt(at: AtSegment, resolver: EmailAtResolver?): String {
        val fragment = resolver?.resolve(at)
        val displayName = fragment?.displayName ?: at.displayName ?: FALLBACK_AT_NAME
        val safeName = escape("@$displayName")
        return fragment?.profileUrl
            ?.let { "<a href=\"${escapeAttr(it)}\">$safeName</a>" }
            ?: "<strong>$safeName</strong>"
    }

    private fun renderImage(segment: ImageSegment): String {
        val src = when (val source = segment.source) {
            is UrlImageSource -> source.url
            is ResourceImageSource -> {
                logger.debug("ResourceImageSource not resolved by email renderer; emitting placeholder")
                return "[image:${escape(source.resourceId)}]"
            }
            is BytesImageSource -> {
                logger.warn("BytesImageSource is not supported by ChainToHtmlRenderer; skipping")
                return ""
            }
        }
        return "<img src=\"${escapeAttr(src)}\" alt=\"\"/>"
    }

    private fun renderLink(segment: LinkSegment): String {
        val text = segment.title ?: segment.url
        return "<a href=\"${escapeAttr(segment.url)}\">${escape(text)}</a>"
    }

    private fun escape(text: String): String {
        val sb = StringBuilder(text.length)
        for (c in text) {
            when (c) {
                '&' -> sb.append("&amp;")
                '<' -> sb.append("&lt;")
                '>' -> sb.append("&gt;")
                else -> sb.append(c)
            }
        }
        return sb.toString()
    }

    private fun escapeAttr(value: String): String {
        val sb = StringBuilder(value.length)
        for (c in value) {
            when (c) {
                '&' -> sb.append("&amp;")
                '<' -> sb.append("&lt;")
                '>' -> sb.append("&gt;")
                '"' -> sb.append("&quot;")
                else -> sb.append(c)
            }
        }
        return sb.toString()
    }

    private companion object {
        const val FALLBACK_AT_NAME = "unknown"
    }
}
