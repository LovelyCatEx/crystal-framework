package com.lovelycatv.crystalframework.message.utils

import com.lovelycatv.crystalframework.sdk.message.types.ScopeType
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope

/**
 * Bridges the messaging core's tenant-agnostic [ScopeType] and the RBAC layer's
 * [ResourceScope]. The two enums intentionally live in different vocabularies (the core
 * never references RBAC), so exposing a broadcast through the scoped manager pipeline needs
 * this one-place mapping. `when` is exhaustive by design: a new scope on either side breaks
 * compilation here until it is mapped.
 */
fun ScopeType.toResourceScope(): ResourceScope = when (this) {
    ScopeType.SYSTEM -> ResourceScope.SYSTEM
    ScopeType.TENANT -> ResourceScope.TENANT
}

fun ResourceScope.toScopeType(): ScopeType = when (this) {
    ResourceScope.SYSTEM -> ScopeType.SYSTEM
    ResourceScope.TENANT -> ScopeType.TENANT
}
