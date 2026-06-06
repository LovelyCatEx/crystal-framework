package com.lovelycatv.crystalframework.user.converters.types

import com.lovelycatv.crystalframework.shared.types.auth.OAuthPlatform

fun interface ClientRegistrationIdOAuthPlatformConverter {
    fun convert(clientRegistrationId: String): OAuthPlatform?
}