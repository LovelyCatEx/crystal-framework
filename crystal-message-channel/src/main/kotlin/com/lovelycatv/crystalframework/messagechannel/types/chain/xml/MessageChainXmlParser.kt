package com.lovelycatv.crystalframework.messagechannel.types.chain.xml

import com.lovelycatv.crystalframework.messagechannel.constants.MessageChainXmlTags
import com.lovelycatv.crystalframework.messagechannel.types.chain.AtSegment
import com.lovelycatv.crystalframework.messagechannel.types.chain.ImageSegment
import com.lovelycatv.crystalframework.messagechannel.types.chain.ImageSource
import com.lovelycatv.crystalframework.messagechannel.types.chain.LinkSegment
import com.lovelycatv.crystalframework.messagechannel.types.chain.MessageChain
import com.lovelycatv.crystalframework.messagechannel.types.chain.MessageSegment
import com.lovelycatv.crystalframework.messagechannel.types.chain.NewlineSegment
import com.lovelycatv.crystalframework.messagechannel.types.chain.ResourceImageSource
import com.lovelycatv.crystalframework.messagechannel.types.chain.TextSegment
import com.lovelycatv.crystalframework.messagechannel.types.chain.UrlImageSource

/**
 * Hand-written tokenizer for [MessageChain] XML form.
 *
 * Recognized tags (all self-closing): &lt;at/&gt;, &lt;image/&gt;, &lt;link/&gt;, &lt;br/&gt;.
 * Anything outside of recognized tags is treated as plain text (with XML entity decoding).
 * Unknown tags are kept verbatim as text — caller's responsibility to validate input.
 */
object MessageChainXmlParser {

    private val SUPPORTED_TAGS = setOf(
        MessageChainXmlTags.TAG_AT,
        MessageChainXmlTags.TAG_IMAGE,
        MessageChainXmlTags.TAG_LINK,
        MessageChainXmlTags.TAG_BR,
    )

    fun parse(xml: String): MessageChain {
        if (xml.isEmpty()) return MessageChain.EMPTY

        val segments = mutableListOf<MessageSegment>()
        val textBuffer = StringBuilder()
        var i = 0

        fun flushText() {
            if (textBuffer.isNotEmpty()) {
                segments += TextSegment(textBuffer.toString())
                textBuffer.clear()
            }
        }

        while (i < xml.length) {
            val c = xml[i]
            if (c != '<') {
                textBuffer.append(c)
                i++
                continue
            }

            val tagEnd = xml.indexOf('>', i + 1)
            if (tagEnd < 0) {
                textBuffer.append(xml, i, xml.length)
                i = xml.length
                continue
            }

            val raw = xml.substring(i + 1, tagEnd).trim()
            val tagName = raw.takeWhile { !it.isWhitespace() && it != '/' }
            if (tagName.lowercase() !in SUPPORTED_TAGS) {
                textBuffer.append(xml, i, tagEnd + 1)
                i = tagEnd + 1
                continue
            }

            val attrs = parseAttrs(raw.drop(tagName.length))
            val segment = buildSegment(tagName.lowercase(), attrs)
            if (segment != null) {
                flushText()
                if (segment is TextSegment) {
                    textBuffer.append(segment.text)
                } else {
                    segments += segment
                }
            }
            i = tagEnd + 1
        }
        flushText()

        return MessageChain(decodeTextEntitiesInTextSegments(segments))
    }

    private fun buildSegment(tag: String, attrs: Map<String, String>): MessageSegment? = when (tag) {
        MessageChainXmlTags.TAG_AT -> AtSegment(
            userId = attrs[MessageChainXmlTags.ATTR_AT_USER],
            tenantId = attrs[MessageChainXmlTags.ATTR_AT_TENANT],
            displayName = attrs[MessageChainXmlTags.ATTR_AT_DISPLAY_NAME],
        )
        MessageChainXmlTags.TAG_IMAGE -> {
            val src = attrs[MessageChainXmlTags.ATTR_IMAGE_SRC]
            if (src.isNullOrEmpty()) null else ImageSegment(parseImageSource(src))
        }
        MessageChainXmlTags.TAG_LINK -> {
            val href = attrs[MessageChainXmlTags.ATTR_LINK_HREF]
            if (href.isNullOrEmpty()) null
            else LinkSegment(url = href, title = attrs[MessageChainXmlTags.ATTR_LINK_TITLE])
        }
        MessageChainXmlTags.TAG_BR -> NewlineSegment
        else -> null
    }

    private fun parseImageSource(src: String): ImageSource =
        if (src.startsWith("resource:")) ResourceImageSource(src.removePrefix("resource:"))
        else UrlImageSource(src)

    private fun parseAttrs(raw: String): Map<String, String> {
        val trimmed = raw.trim().removeSuffix("/").trim()
        if (trimmed.isEmpty()) return emptyMap()

        val result = mutableMapOf<String, String>()
        var i = 0
        while (i < trimmed.length) {
            while (i < trimmed.length && trimmed[i].isWhitespace()) i++
            if (i >= trimmed.length) break

            val nameStart = i
            while (i < trimmed.length && trimmed[i] != '=' && !trimmed[i].isWhitespace()) i++
            val name = trimmed.substring(nameStart, i)
            if (i >= trimmed.length || trimmed[i] != '=') {
                if (name.isNotEmpty()) result[name] = ""
                continue
            }
            i++ // skip '='

            if (i >= trimmed.length) break
            val quote = trimmed[i]
            if (quote != '"' && quote != '\'') {
                val valStart = i
                while (i < trimmed.length && !trimmed[i].isWhitespace()) i++
                result[name] = decodeAttr(trimmed.substring(valStart, i))
            } else {
                i++ // skip opening quote
                val valStart = i
                while (i < trimmed.length && trimmed[i] != quote) i++
                result[name] = decodeAttr(trimmed.substring(valStart, i))
                if (i < trimmed.length) i++ // skip closing quote
            }
        }
        return result
    }

    private fun decodeAttr(value: String): String = decodeEntities(value)

    private fun decodeTextEntitiesInTextSegments(segments: List<MessageSegment>): List<MessageSegment> =
        segments.map { if (it is TextSegment) TextSegment(decodeEntities(it.text)) else it }

    private fun decodeEntities(s: String): String {
        if ('&' !in s) return s
        val sb = StringBuilder(s.length)
        var i = 0
        while (i < s.length) {
            val c = s[i]
            if (c != '&') { sb.append(c); i++; continue }
            val semi = s.indexOf(';', i + 1)
            if (semi < 0) { sb.append(c); i++; continue }
            val entity = s.substring(i + 1, semi)
            val resolved = when (entity) {
                "amp" -> "&"
                "lt" -> "<"
                "gt" -> ">"
                "quot" -> "\""
                "apos" -> "'"
                else -> null
            }
            if (resolved != null) {
                sb.append(resolved)
                i = semi + 1
            } else {
                sb.append(c)
                i++
            }
        }
        return sb.toString()
    }
}
