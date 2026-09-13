/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.messagechannel.config

import com.lovelycatv.vertex.log.logger
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.Configuration

/**
 * Module entry point. Picked up by crystal-starter's component scan
 * (root package `com.lovelycatv.crystalframework`). All beans (providers, services,
 * renderers) are wired by ordinary [org.springframework.stereotype.Component] /
 * [org.springframework.stereotype.Service] annotations — nothing to explicitly
 * declare here for now.
 */
@Configuration
class MessageChannelAutoConfigure : InitializingBean {
    private val logger = logger()

    override fun afterPropertiesSet() {
        logger.info("crystal-message-channel module loaded")
    }
}
