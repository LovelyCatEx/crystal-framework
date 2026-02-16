package com.lovelycatv.template.springboot.rbac.service.impl

import com.lovelycatv.template.springboot.rbac.entity.UserPermissionEntity
import com.lovelycatv.template.springboot.rbac.repository.UserRolePermissionRelationRepository
import com.lovelycatv.template.springboot.rbac.service.UserPermissionService
import com.lovelycatv.template.springboot.rbac.service.UserRolePermissionRelationService
import com.lovelycatv.template.springboot.shared.utils.awaitListWithTimeout
import org.springframework.stereotype.Service

@Service
class UserRolePermissionRelationServiceImpl(
    private val userRolePermissionRelationRepository: UserRolePermissionRelationRepository,
    private val userPermissionService: UserPermissionService
) : UserRolePermissionRelationService {
    override fun getRepository(): UserRolePermissionRelationRepository {
        return this.userRolePermissionRelationRepository
    }

    override suspend fun getRolePermissions(roleId: Long): List<UserPermissionEntity> {
        val relations = this.getRepository()
            .findByRoleId(roleId)
            .awaitListWithTimeout()

        return userPermissionService
            .getRepository()
            .findAllById(relations.map { it.permissionId })
            .awaitListWithTimeout()
    }
}