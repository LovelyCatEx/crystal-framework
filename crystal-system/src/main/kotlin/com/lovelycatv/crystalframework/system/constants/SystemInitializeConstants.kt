package com.lovelycatv.crystalframework.system.constants

import java.time.Duration

object SystemInitializeConstants {
    const val TOKEN_BYTES = 32
    val LOCK_TTL: Duration = Duration.ofMinutes(5)
    const val LOCK_BUSY_MESSAGE = "System initialization is being processed, please retry later"
}
