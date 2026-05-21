package io.github.lovelycatex

import com.lovelycatv.vertex.log.logger
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.Configuration

@Configuration
class TestConfiguration : InitializingBean {
    override fun afterPropertiesSet() {
        logger().info("Hello, World!")
    }
}