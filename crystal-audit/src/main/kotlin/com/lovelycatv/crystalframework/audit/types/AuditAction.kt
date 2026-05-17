package com.lovelycatv.crystalframework.audit.types

enum class AuditAction(val code: Int) {
    UNKNOWN(0),
    CREATE(1),
    READ(2),
    UPDATE(3),
    DELETE(4)
}
