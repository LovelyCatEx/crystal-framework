/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.tenant.controller.manager.department.member.vo

import com.lovelycatv.crystalframework.tenant.controller.manager.member.vo.TenantMemberVO

data class TenantDepartmentMemberVO(
    val id: Long,
    val member: TenantMemberVO,
    val roleType: Int,
    val createdTime: Long,
    val modifiedTime: Long
)