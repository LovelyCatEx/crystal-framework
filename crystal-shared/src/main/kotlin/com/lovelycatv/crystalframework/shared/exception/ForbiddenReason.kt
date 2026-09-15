/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.shared.exception

enum class ForbiddenReason {
    MISSING_PERMISSION,
    SCOPE_MISMATCH,
    PROTECTED_RESOURCE,
    NOT_TENANT_MEMBER,
    ROLE_PROTECTED,
    PERMISSION_ESCALATION,
    INVALID_INITIALIZATION_TOKEN,
}
