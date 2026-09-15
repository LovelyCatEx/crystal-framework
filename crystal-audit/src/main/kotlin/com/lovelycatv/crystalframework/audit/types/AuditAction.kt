/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.audit.types

enum class AuditAction(val code: Int) {
    UNKNOWN(0),
    CREATE(1),
    READ(2),
    UPDATE(3),
    DELETE(4)
}
