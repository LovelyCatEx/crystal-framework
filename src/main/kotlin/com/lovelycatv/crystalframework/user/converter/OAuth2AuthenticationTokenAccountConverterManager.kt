package com.lovelycatv.crystalframework.user.converter

import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.user.entity.OAuthAccountEntity
import org.springframework.beans.factory.getBeansOfType
import org.springframework.context.ApplicationContext
import org.springframework.core.OrderComparator
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.stereotype.Component

@Component
class OAuth2AuthenticationTokenAccountConverterManager(
    private val clientRegistrationIdOAuthPlatformConverter: ClientRegistrationIdOAuthPlatformConverter,
    private val applicationContext: ApplicationContext
) {
    fun convert(token: OAuth2AuthenticationToken): OAuthAccountEntity {
        val clientRegistrationId = token.authorizedClientRegistrationId

        val platform = clientRegistrationIdOAuthPlatformConverter.convert(clientRegistrationId)
            ?: throw BusinessException("could not convert clientRegistrationId $clientRegistrationId to platform type")

        val converters = applicationContext.getBeansOfType<OAuth2AuthenticationTokenAccountConverter>().values

        val converter = converters
            .filter { it.getPlatform() == platform }
            .minWith(OrderComparator.INSTANCE)

        return converter.convert(token)
    }
}