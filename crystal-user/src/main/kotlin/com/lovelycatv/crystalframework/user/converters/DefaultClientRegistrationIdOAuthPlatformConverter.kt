package com.lovelycatv.crystalframework.user.converters

import com.lovelycatv.crystalframework.user.converters.types.ClientRegistrationIdOAuthPlatformConverter
import com.lovelycatv.crystalframework.shared.types.auth.OAuthPlatform

class DefaultClientRegistrationIdOAuthPlatformConverter : ClientRegistrationIdOAuthPlatformConverter {
    override fun convert(clientRegistrationId: String): OAuthPlatform? {
        return when (val clientId = clientRegistrationId.lowercase()) {
            "github" -> OAuthPlatform.GITHUB
            "google" -> OAuthPlatform.GOOGLE
            "oicq" -> OAuthPlatform.OICQ
            else -> OAuthPlatform.entries.find { it.name.lowercase() == clientId }
        }
    }
}