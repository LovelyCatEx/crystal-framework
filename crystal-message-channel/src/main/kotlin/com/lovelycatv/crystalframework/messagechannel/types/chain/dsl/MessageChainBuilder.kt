package com.lovelycatv.crystalframework.messagechannel.types.chain.dsl

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

@DslMarker
annotation class MessageChainDsl

@MessageChainDsl
class MessageChainBuilder {
    private val segments = mutableListOf<MessageSegment>()

    fun add(segment: MessageSegment) {
        segments += segment
    }

    fun text(text: String) {
        if (text.isNotEmpty()) segments += TextSegment(text)
    }

    fun at(userId: String? = null, tenantId: String? = null, displayName: String? = null) {
        segments += AtSegment(userId = userId, tenantId = tenantId, displayName = displayName)
    }

    fun image(source: ImageSource) {
        segments += ImageSegment(source)
    }

    fun imageByUrl(url: String) {
        segments += ImageSegment(UrlImageSource(url))
    }

    fun imageByResource(resourceId: String) {
        segments += ImageSegment(ResourceImageSource(resourceId))
    }

    fun link(url: String, title: String? = null) {
        segments += LinkSegment(url, title)
    }

    fun newline() {
        segments += NewlineSegment
    }

    fun build(): MessageChain = MessageChain(segments.toList())
}

fun messageChain(block: MessageChainBuilder.() -> Unit): MessageChain =
    MessageChainBuilder().apply(block).build()
