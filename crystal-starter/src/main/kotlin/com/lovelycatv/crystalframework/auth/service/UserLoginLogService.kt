package com.lovelycatv.crystalframework.auth.service

import com.lovelycatv.crystalframework.auth.event.UserLoginEvent

fun interface UserLoginLogService {
    suspend fun recordLoginLog(event: UserLoginEvent)
}