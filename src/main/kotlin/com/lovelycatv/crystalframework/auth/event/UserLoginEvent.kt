package com.lovelycatv.crystalframework.auth.event

import org.springframework.context.ApplicationEvent

class UserLoginEvent(
    source: Any,
    val userId: Long?,
    val username: String?,
    val tenantId: Long?,
    val loginMethod: Int,
    val oauth2Type: Int?,
    val oauth2Username: String?,
    val oauth2AccountId: Long?,
    val success: Boolean,
    val errorMessage: String?,
    val remoteIp: String?,
    val userAgent: String?
) : ApplicationEvent(source)