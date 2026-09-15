/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.messagechannel.constants

/**
 * Module-wide error codes returned via [com.lovelycatv.crystalframework
 * .messagechannel.types.result.SendResult.failed].
 */
object MessageChannelErrorCodes {
    /** No provider bean is registered for the requested [ChannelType]. */
    const val NO_PROVIDER = "NO_PROVIDER"

    /** Provider does not accept the supplied recipient subtype. */
    const val UNSUPPORTED_RECIPIENT = "UNSUPPORTED_RECIPIENT"

    /** [com.lovelycatv.crystalframework.messagechannel.types.config.ChannelConfig.channelType]
     *  does not match the recipient's channelType. */
    const val INCOMPATIBLE_CHANNEL = "INCOMPATIBLE_CHANNEL"

    /** Generic "credentials missing" signal used by providers when the supplied config is empty. */
    const val NOT_CONFIGURED = "NOT_CONFIGURED"

    /** Generic "bad recipient shape for this provider" signal used by providers. */
    const val BAD_RECIPIENT = "BAD_RECIPIENT"
}
