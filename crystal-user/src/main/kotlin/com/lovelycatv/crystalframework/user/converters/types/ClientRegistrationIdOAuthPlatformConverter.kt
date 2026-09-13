/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.user.converters.types

import com.lovelycatv.crystalframework.shared.types.auth.OAuthPlatform

fun interface ClientRegistrationIdOAuthPlatformConverter {
    fun convert(clientRegistrationId: String): OAuthPlatform?
}