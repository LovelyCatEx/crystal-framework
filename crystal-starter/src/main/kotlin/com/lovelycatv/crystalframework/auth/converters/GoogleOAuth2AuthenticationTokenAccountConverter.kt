package com.lovelycatv.crystalframework.auth.converters

import com.lovelycatv.crystalframework.auth.converters.types.OAuth2AuthenticationTokenAccountConverter
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.user.entity.OAuthAccountEntity
import com.lovelycatv.crystalframework.user.types.OAuthPlatform
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority
import org.springframework.stereotype.Component

@Component
class GoogleOAuth2AuthenticationTokenAccountConverter : OAuth2AuthenticationTokenAccountConverter {
    override fun getPlatform(): OAuthPlatform {
        return OAuthPlatform.GOOGLE
    }

    override fun convert(token: OAuth2AuthenticationToken): OAuthAccountEntity {
        val oauth2Authority = token.authorities
            .filterIsInstance<OAuth2UserAuthority>()
            .firstOrNull()
            ?: throw BusinessException("could not find oauth2 authority")

        return OAuthAccountEntity(
            userId = null,
            identifier = oauth2Authority.attributes["sub"]?.toString()
                ?: throw BusinessException("could not find identifier from the token"),
            nickname = oauth2Authority.attributes["name"]?.toString()
                ?: "${oauth2Authority.attributes["given_name"]?.toString()} ${oauth2Authority.attributes["family_name"]?.toString()}",
            avatar = oauth2Authority.attributes["picture"]?.toString(),
            email = oauth2Authority.attributes["email"]?.toString(),
        )
    }
}