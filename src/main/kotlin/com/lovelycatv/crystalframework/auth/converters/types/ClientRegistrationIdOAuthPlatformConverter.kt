package com.lovelycatv.crystalframework.auth.converters.types

import com.lovelycatv.crystalframework.user.types.OAuthPlatform

interface ClientRegistrationIdOAuthPlatformConverter {
    fun convert(clientRegistrationId: String): OAuthPlatform?
}