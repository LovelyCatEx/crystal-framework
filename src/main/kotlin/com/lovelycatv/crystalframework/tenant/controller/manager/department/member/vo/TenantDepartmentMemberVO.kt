package com.lovelycatv.crystalframework.tenant.controller.manager.department.member.vo

import com.lovelycatv.crystalframework.tenant.controller.manager.member.vo.TenantMemberVO

data class TenantDepartmentMemberVO(
    val member: TenantMemberVO,
    val roleType: Int
)