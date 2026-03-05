package com.lovelycatv.crystalframework.auth.converters

import com.lovelycatv.crystalframework.auth.converters.types.ClientRegistrationIdOAuthPlatformConverter
import com.lovelycatv.crystalframework.user.types.OAuthPlatform

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