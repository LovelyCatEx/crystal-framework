package com.lovelycatv.crystalframework.audit.annotations

import com.lovelycatv.crystalframework.audit.types.AuditAction

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Audit(
    val action: AuditAction,
    val resourceType: String,
    val resourceIds: String = "",
)
