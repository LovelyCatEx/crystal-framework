/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.shared.exception

import org.springframework.security.authentication.LockedException

class AccountBannedException(
    message: String,
    val context: BanContext,
) : LockedException(message)
