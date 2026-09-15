/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.auth.event

import com.lovelycatv.crystalframework.auth.service.UserLoginLogService
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class UserLoginEventListener(
    private val userLoginLogService: UserLoginLogService
) {

    @EventListener
    suspend fun onUserLogin(event: UserLoginEvent) {
        userLoginLogService.recordLoginLog(event)
    }
}