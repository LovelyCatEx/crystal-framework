package com.lovelycatv.crystalframework.shared.exception

import org.springframework.security.authentication.LockedException

class AccountBannedException(
    message: String,
    val context: BanContext,
) : LockedException(message)
