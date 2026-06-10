package com.lovelycatv.crystalframework.config

import com.lovelycatv.crystalframework.shared.constants.RedisConstants
import com.lovelycatv.crystalframework.shared.types.auth.OAuthPlatform
import com.lovelycatv.crystalframework.shared.types.system.SystemSettings
import com.lovelycatv.crystalframework.system.service.SystemSettingsService
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.runBlocking
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class DynamicClientRegistrationRepository(
    private val systemSettingsService: SystemSettingsService,
    private val redisMessageListenerContainer: ReactiveRedisMessageListenerContainer,
) : ReactiveClientRegistrationRepository {

    @Volatile
    private var registrations: Map<String, ClientRegistration> = emptyMap()

    @Volatile
    private var initialized = false

    private val buildLock = Any()

    @PostConstruct
    fun subscribeRefreshTopic() {
        redisMessageListenerContainer
            .receive(ChannelTopic(RedisConstants.SYSTEM_SETTINGS_REFRESH_TOPIC))
            .subscribe { initialized = false }
    }

    override fun findByRegistrationId(registrationId: String): Mono<ClientRegistration> {
        if (!initialized) {
            synchronized(buildLock) {
                if (!initialized) {
                    rebuild()
                }
            }
        }
        return Mono.justOrEmpty(registrations[registrationId])
    }

    private fun rebuild() {
        runBlocking {
            val basicSettings = systemSettingsService.getSystemBasicSettings()
            val oauthSettings = systemSettingsService.getSystemOAuthSettings()
            val redirectUri = "${basicSettings.frontendBaseUrl.trimEnd('/')}/auth/oauth2-code"

            val result = mutableMapOf<String, ClientRegistration>()
            PLATFORM_CONFIGS.forEach { config ->
                val platformSettings = config.extract(oauthSettings)
                if (!platformSettings.enabled || platformSettings.clientId.isBlank()) return@forEach

                val registration = if (platformSettings.useDefault == true && config.commonProvider != null) {
                    config.commonProvider.getBuilder(config.registrationId)
                        .clientId(platformSettings.clientId)
                        .clientSecret(platformSettings.clientSecret)
                        .scope(*platformSettings.scope.toTypedArray())
                        .redirectUri(redirectUri)
                        .build()
                } else {
                    if (platformSettings.authorizationUri.isBlank() || platformSettings.tokenUri.isBlank() || platformSettings.userInfoUri.isBlank()) return@forEach
                    ClientRegistration.withRegistrationId(config.registrationId)
                        .clientId(platformSettings.clientId)
                        .clientSecret(platformSettings.clientSecret)
                        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                        .redirectUri(redirectUri)
                        .authorizationUri(platformSettings.authorizationUri)
                        .tokenUri(platformSettings.tokenUri)
                        .userInfoUri(platformSettings.userInfoUri)
                        .userNameAttributeName(platformSettings.userNameAttribute)
                        .scope(*platformSettings.scope.toTypedArray())
                        .build()
                }
                result[config.registrationId] = registration
            }
            registrations = result
        }
        initialized = true
    }

    private data class PlatformConfig(
        val registrationId: String,
        val commonProvider: CommonOAuth2Provider?,
        val extract: (SystemSettings.OAuth) -> SystemSettings.OAuth.OAuthPlatformSettings,
    )

    companion object {
        private val PLATFORM_CONFIGS = listOf(
            PlatformConfig(
                registrationId = OAuthPlatform.GITHUB.name.lowercase(),
                commonProvider = CommonOAuth2Provider.GITHUB,
                extract = { it.github },
            ),
            PlatformConfig(
                registrationId = OAuthPlatform.GOOGLE.name.lowercase(),
                commonProvider = CommonOAuth2Provider.GOOGLE,
                extract = { it.google },
            ),
            PlatformConfig(
                registrationId = OAuthPlatform.OICQ.name.lowercase(),
                commonProvider = null,
                extract = { it.oicq },
            ),
        )
    }
}
