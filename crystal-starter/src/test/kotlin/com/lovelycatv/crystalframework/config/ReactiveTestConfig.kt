/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.config

import com.lovelycatv.vertex.log.logger
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.test.context.TestConfiguration

@TestConfiguration
class ReactiveTestConfig : InitializingBean {
    private val logger = logger()

    override fun afterPropertiesSet() {
        logger.info("ReactiveTestConfig afterPropertiesSet()")
    }
}
