package com.lovelycatv.crystalframework.shared.exception

enum class ForbiddenReason {
    MISSING_PERMISSION,
    SCOPE_MISMATCH,
    PROTECTED_RESOURCE,
    NOT_TENANT_MEMBER,
    ROLE_PROTECTED,
    PERMISSION_ESCALATION,
}
