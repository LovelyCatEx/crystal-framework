/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.approval.types

/** Optional one-level grouping for form layout. Nested groups are not allowed by design. */
data class ApprovalFieldGroup(
    val key: String,
    val label: String,
)
