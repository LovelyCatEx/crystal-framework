package com.lovelycatv.crystalframework.messagechannel.types.chain

/**
 * A single element inside a [MessageChain].
 * Each channel provider is responsible for translating the segments it supports
 * into the channel's native representation, and for handling unsupported segments
 * (e.g. degrade to plain text, drop, or fail) on its own.
 */
sealed interface MessageSegment
