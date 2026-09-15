/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.auth.service

interface OAuthBindingTokenService {
    suspend fun issue(oauthAccountId: Long): String

    suspend fun consume(token: String): Long
}
