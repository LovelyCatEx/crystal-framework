package com.lovelycatv.crystalframework.auth.converters

import com.lovelycatv.crystalframework.auth.converters.types.ClientRegistrationIdOAuthPlatformConverter
import com.lovelycatv.crystalframework.auth.converters.types.OAuth2AuthenticationTokenAccountConverter
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

        val platformType = clientRegistrationIdOAuthPlatformConverter.convert(clientRegistrationId)
            ?: throw BusinessException("could not convert clientRegistrationId $clientRegistrationId to platform type")

        val converters = applicationContext.getBeansOfType<OAuth2AuthenticationTokenAccountConverter>().values

        val converter = converters
            .filter { it.getPlatform() == platformType }
            .minWith(OrderComparator.INSTANCE)

        return converter.convert(token).apply {
            this.platform = platformType.typeId
        }
    }
}