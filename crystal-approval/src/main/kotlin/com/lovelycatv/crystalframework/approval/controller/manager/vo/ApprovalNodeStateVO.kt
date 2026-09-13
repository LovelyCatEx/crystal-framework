/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.approval.controller.manager.vo

data class ApprovalNodeStateVO(
    val status: Int,
    val taskIds: List<String>,
)
