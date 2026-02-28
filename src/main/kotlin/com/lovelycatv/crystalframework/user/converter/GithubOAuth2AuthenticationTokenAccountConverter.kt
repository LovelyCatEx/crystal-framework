package com.lovelycatv.crystalframework.user.converter

import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.user.entity.OAuthAccountEntity
import com.lovelycatv.crystalframework.user.types.OAuthPlatform
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority
import org.springframework.stereotype.Component

@Component
class GithubOAuth2AuthenticationTokenAccountConverter : OAuth2AuthenticationTokenAccountConverter {
    override fun getPlatform(): OAuthPlatform {
        return OAuthPlatform.GITHUB
    }

    override fun convert(token: OAuth2AuthenticationToken): OAuthAccountEntity {
        val oauth2Authority = token.authorities
            .filterIsInstance<OAuth2UserAuthority>()
            .firstOrNull()
            ?: throw BusinessException("could not find oauth2 authority")

        return OAuthAccountEntity(
            userId = null,
            identifier = oauth2Authority.attributes["id"]?.toString()
                ?: throw BusinessException("could not find identifier from the token"),
            nickname = oauth2Authority.attributes["login"]?.toString()
                ?: oauth2Authority.attributes["name"]?.toString(),
            avatar = oauth2Authority.attributes["avatar_url"]?.toString()
        )
    }
}