package com.lovelycatv.crystalframework.messagechannel.types.content

import com.lovelycatv.crystalframework.messagechannel.types.chain.MessageChain

/**
 * The single content type accepted by [com.lovelycatv.crystalframework.messagechannel.service.MessageChannelService].
 *
 * @param title optional title — used as email subject; ignored by plain-text channels.
 * @param chain message body as a [MessageChain].
 */
data class ChainMessage(
    val title: String? = null,
    val chain: MessageChain,
)
