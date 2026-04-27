package com.lovelycatv.crystalframework.shared.config

import org.springframework.context.annotation.Configuration
import org.springframework.session.data.redis.config.annotation.web.server.EnableRedisIndexedWebSession

@Configuration
@EnableRedisIndexedWebSession
class WebFluxSessionConfig
