package com.lovelycatv.crystalframework.auth.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
@ConfigurationProperties("crystalframework.auth")
class AuthConfiguration {
    var jwt: Jwt = Jwt()

    class Jwt {
        var expiration: Duration = Duration.ofDays(7)
    }
}
