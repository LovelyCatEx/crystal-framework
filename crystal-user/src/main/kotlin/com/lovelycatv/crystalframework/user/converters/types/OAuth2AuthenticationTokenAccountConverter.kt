/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.user.converters.types

import com.lovelycatv.crystalframework.user.entity.OAuthAccountEntity
import com.lovelycatv.crystalframework.shared.types.auth.OAuthPlatform
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken

interface OAuth2AuthenticationTokenAccountConverter {
    fun getPlatform(): OAuthPlatform

    fun convert(token: OAuth2AuthenticationToken): OAuthAccountEntity
}