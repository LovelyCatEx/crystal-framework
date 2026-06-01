package com.lovelycatv.crystalframework.messagechannel.types.options

/**
 * Per-call options for [com.lovelycatv.crystalframework.messagechannel.service.MessageChannelService.send].
 * Intentionally empty for v1 — placeholder for future knobs (timeout, retry, async hint, etc.).
 */
data class SendOptions(
    val placeholder: Boolean = false,
) {
    companion object {
        val DEFAULT: SendOptions = SendOptions()
    }
}
