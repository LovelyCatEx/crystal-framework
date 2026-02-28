package com.lovelycatv.crystalframework.user.converter

import com.lovelycatv.crystalframework.user.types.OAuthPlatform

interface ClientRegistrationIdOAuthPlatformConverter {
    fun convert(clientRegistrationId: String): OAuthPlatform?
}