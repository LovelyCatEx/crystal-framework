/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.approval.types

/** One selectable option for SELECT / RADIO / CHECKBOX field types. */
data class ApprovalFieldOption(
    val value: String,
    val label: String,
)
