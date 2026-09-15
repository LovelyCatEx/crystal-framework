/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.audit.annotations

import com.lovelycatv.crystalframework.audit.types.AuditAction

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Audit(
    val action: AuditAction,
    val resourceType: String,
    val resourceIds: String = "",
)
