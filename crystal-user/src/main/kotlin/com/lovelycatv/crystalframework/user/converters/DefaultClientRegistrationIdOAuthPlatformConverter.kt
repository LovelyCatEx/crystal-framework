/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

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