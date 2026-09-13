/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.shared.controller.dto

/**
 * Base create DTO for scoped resources.
 *
 * @property scope  The scope type identifier (e.g. 0=SYSTEM, 1=TENANT)
 * @property scopeId The ID within that scope (e.g. tenantId when scope=TENANT)
 */
open class BaseManagerCreateScopedDTO(
    open val scope: Int,
    open val scopeId: Long,
)
