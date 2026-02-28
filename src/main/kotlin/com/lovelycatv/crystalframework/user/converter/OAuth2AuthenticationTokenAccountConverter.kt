package com.lovelycatv.crystalframework.user.converter

import com.lovelycatv.crystalframework.user.entity.OAuthAccountEntity
import com.lovelycatv.crystalframework.user.types.OAuthPlatform
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken

interface OAuth2AuthenticationTokenAccountConverter {
    fun getPlatform(): OAuthPlatform

    fun convert(token: OAuth2AuthenticationToken): OAuthAccountEntity
}