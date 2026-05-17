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