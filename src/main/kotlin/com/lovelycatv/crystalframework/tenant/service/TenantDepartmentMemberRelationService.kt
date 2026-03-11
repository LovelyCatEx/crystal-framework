package com.lovelycatv.crystalframework.tenant.service

import com.lovelycatv.crystalframework.cache.service.CachedBaseService
import com.lovelycatv.crystalframework.tenant.controller.manager.department.member.vo.TenantDepartmentMemberVO
import com.lovelycatv.crystalframework.tenant.entity.TenantDepartmentMemberRelationEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantDepartmentMemberRelationRepository

interface TenantDepartmentMemberRelationService : CachedBaseService<TenantDepartmentMemberRelationRepository, TenantDepartmentMemberRelationEntity> {
    suspend fun getDepartmentMembers(departmentId: Long): List<TenantDepartmentMemberVO>

    suspend fun setDepartmentMembers(departmentId: Long, memberIds: List<Long>)
}
