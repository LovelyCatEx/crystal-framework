/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.approval.types

data class CcNodeConfig(
    val userIds: List<String> = emptyList(),
    val roleIds: List<String> = emptyList(),
    val channelIds: List<String> = emptyList(),
) : ApprovalFlowNodeConfig()
