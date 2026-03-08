package com.lovelycatv.crystalframework.shared.config

import org.springframework.boot.web.server.autoconfigure.ServerProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.server.WebSession
import org.springframework.web.server.session.DefaultWebSessionManager
import org.springframework.web.server.session.InMemoryWebSessionStore
import org.springframework.web.server.session.WebSessionIdResolver
import org.springframework.web.server.session.WebSessionManager
import reactor.core.publisher.Mono
import java.time.Duration

@Configuration
class WebFluxSessionConfig(
    private val webSessionIdResolver: WebSessionIdResolver,
    private val serverProperties: ServerProperties
) {
    @Bean
    fun webSessionManager(): WebSessionManager {
        val webSessionManager = DefaultWebSessionManager()
        val timeout: Duration = this.serverProperties.reactive.session.timeout
        val maxSessions: Int = this.serverProperties.reactive.session.maxSessions
        val sessionStore = object : InMemoryWebSessionStore() {
            override fun createWebSession(): Mono<WebSession> {
                return super.createWebSession().doOnSuccess {
                    it?.maxIdleTime = timeout
                }
            }
        }
        sessionStore.maxSessions = maxSessions
        webSessionManager.setSessionStore(sessionStore)
        webSessionManager.setSessionIdResolver(this.webSessionIdResolver)
        return webSessionManager
    }
}