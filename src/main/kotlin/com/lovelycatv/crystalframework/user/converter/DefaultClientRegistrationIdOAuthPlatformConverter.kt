package com.lovelycatv.crystalframework.user.converter

import com.lovelycatv.crystalframework.user.types.OAuthPlatform
import org.springframework.stereotype.Component

@Component
class DefaultClientRegistrationIdOAuthPlatformConverter : ClientRegistrationIdOAuthPlatformConverter {
    override fun convert(clientRegistrationId: String): OAuthPlatform? {
        return when (clientRegistrationId.lowercase()) {
            "github" -> OAuthPlatform.GITHUB
            else -> null
        }
    }
}