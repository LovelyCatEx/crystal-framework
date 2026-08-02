package com.lovelycatv.crystalframework.shared.exception

import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

data class BanContext(
    val reason: String,
    val bannedAt: Long,
    val banUntil: Long?,
)
