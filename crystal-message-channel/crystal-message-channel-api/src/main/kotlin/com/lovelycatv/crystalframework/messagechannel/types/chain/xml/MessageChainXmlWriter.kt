package com.lovelycatv.crystalframework.messagechannel.types.chain.xml

import com.lovelycatv.crystalframework.messagechannel.constants.MessageChainXmlTags
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

object MessageChainXmlWriter {

    fun write(chain: MessageChain): String {
        val sb = StringBuilder()
        for (segment in chain.segments) {
            when (segment) {
                is TextSegment -> sb.append(escapeText(segment.text))
                is AtSegment -> writeAt(sb, segment)
                is ImageSegment -> writeImage(sb, segment)
                is LinkSegment -> writeLink(sb, segment)
                is RawHtmlSegment ->
                    throw IllegalArgumentException("RawHtmlSegment has no XML form; channels should consume it directly")
                NewlineSegment -> sb.append("<${MessageChainXmlTags.TAG_BR}/>")
            }
        }
        return sb.toString()
    }

    private fun writeAt(sb: StringBuilder, segment: AtSegment) {
        sb.append('<').append(MessageChainXmlTags.TAG_AT)
        segment.userId?.let { appendAttr(sb, MessageChainXmlTags.ATTR_AT_USER, it) }
        segment.tenantId?.let { appendAttr(sb, MessageChainXmlTags.ATTR_AT_TENANT, it) }
        segment.displayName?.let { appendAttr(sb, MessageChainXmlTags.ATTR_AT_DISPLAY_NAME, it) }
        sb.append("/>")
    }

    private fun writeImage(sb: StringBuilder, segment: ImageSegment) {
        val src = when (val source = segment.source) {
            is UrlImageSource -> source.url
            is ResourceImageSource -> "resource:${source.resourceId}"
            is BytesImageSource ->
                throw IllegalArgumentException("BytesImageSource cannot be serialized to XML")
        }
        sb.append('<').append(MessageChainXmlTags.TAG_IMAGE)
        appendAttr(sb, MessageChainXmlTags.ATTR_IMAGE_SRC, src)
        sb.append("/>")
    }

    private fun writeLink(sb: StringBuilder, segment: LinkSegment) {
        sb.append('<').append(MessageChainXmlTags.TAG_LINK)
        appendAttr(sb, MessageChainXmlTags.ATTR_LINK_HREF, segment.url)
        segment.title?.let { appendAttr(sb, MessageChainXmlTags.ATTR_LINK_TITLE, it) }
        sb.append("/>")
    }

    private fun appendAttr(sb: StringBuilder, name: String, value: String) {
        sb.append(' ').append(name).append("=\"").append(escapeAttr(value)).append('"')
    }

    private fun escapeText(text: String): String {
        if (text.isEmpty()) return text
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
}
