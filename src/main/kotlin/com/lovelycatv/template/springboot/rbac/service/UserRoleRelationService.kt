package com.lovelycatv.template.springboot.rbac.service

import com.lovelycatv.template.springboot.rbac.entity.UserRoleEntity
import com.lovelycatv.template.springboot.rbac.entity.UserRoleRelationEntity
import com.lovelycatv.template.springboot.rbac.repository.UserRoleRelationRepository
import com.lovelycatv.template.springboot.shared.service.BaseService

interface UserRoleRelationService : BaseService<UserRoleRelationRepository, UserRoleRelationEntity> {
    suspend fun getUserRoles(userId: Long): List<UserRoleEntity>
}