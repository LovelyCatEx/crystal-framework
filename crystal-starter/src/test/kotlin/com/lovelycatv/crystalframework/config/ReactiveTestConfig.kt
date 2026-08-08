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
