package com.lovelycatv.crystalframework.messagechannel.types.chain

import com.lovelycatv.crystalframework.messagechannel.types.chain.xml.MessageChainXmlParser
import com.lovelycatv.crystalframework.messagechannel.types.chain.xml.MessageChainXmlWriter

/**
 * Ordered sequence of [MessageSegment]s representing one message body.
 *
 * Construction options:
 *  - DSL: [com.lovelycatv.crystalframework.messagechannel.types.chain.dsl.messageChain]
 *  - XML string: [MessageChain.parse]
 *  - Direct: `MessageChain(listOf(...))`
 */
data class MessageChain(val segments: List<MessageSegment>) {
    constructor(vararg segments: MessageSegment) : this(segments.toList())

    operator fun plus(segment: MessageSegment): MessageChain = MessageChain(segments + segment)

    operator fun plus(other: MessageChain): MessageChain = MessageChain(segments + other.segments)

    fun toXml(): String = MessageChainXmlWriter.write(this)

    companion object {
        val EMPTY: MessageChain = MessageChain(emptyList())

        fun parse(xml: String): MessageChain = MessageChainXmlParser.parse(xml)
    }
}
