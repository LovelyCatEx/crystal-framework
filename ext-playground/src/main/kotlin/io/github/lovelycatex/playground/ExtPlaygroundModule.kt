package io.github.lovelycatex.playground

import com.lovelycatv.vertex.log.logger
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.Configuration

@Configuration
class ExtPlaygroundModule : InitializingBean {
    override fun afterPropertiesSet() {
        logger().info("Hello, World!")
    }
}