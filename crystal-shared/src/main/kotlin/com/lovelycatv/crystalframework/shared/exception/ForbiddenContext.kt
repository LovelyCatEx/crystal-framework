/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.shared.exception

import com.lovelycatv.crystalframework.shared.types.common.ResourceScope

data class ForbiddenContext(
    val requiredPermissions: List<String> = emptyList(),
    val reason: ForbiddenReason = ForbiddenReason.MISSING_PERMISSION,
    val scope: ResourceScope,
)
