package com.lovelycatv.crystalframework.auth.converters.types

import com.lovelycatv.crystalframework.user.types.OAuthPlatform

fun interface ClientRegistrationIdOAuthPlatformConverter {
    fun convert(clientRegistrationId: String): OAuthPlatform?
}