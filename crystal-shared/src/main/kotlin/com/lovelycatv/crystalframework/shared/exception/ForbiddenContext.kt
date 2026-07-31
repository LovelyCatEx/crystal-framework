package com.lovelycatv.crystalframework.shared.exception

import com.lovelycatv.crystalframework.shared.types.common.ResourceScope

data class ForbiddenContext(
    val requiredPermissions: List<String> = emptyList(),
    val reason: ForbiddenReason = ForbiddenReason.MISSING_PERMISSION,
    val scope: ResourceScope,
)
