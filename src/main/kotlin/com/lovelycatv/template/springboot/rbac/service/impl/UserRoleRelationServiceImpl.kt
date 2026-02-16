package com.lovelycatv.template.springboot.rbac.service.impl

import com.lovelycatv.template.springboot.rbac.entity.UserRoleEntity
import com.lovelycatv.template.springboot.rbac.repository.UserRoleRelationRepository
import com.lovelycatv.template.springboot.rbac.service.UserRoleRelationService
import com.lovelycatv.template.springboot.rbac.service.UserRoleService
import com.lovelycatv.template.springboot.shared.exception.BusinessException
import com.lovelycatv.template.springboot.shared.utils.awaitListWithTimeout
import com.lovelycatv.template.springboot.shared.utils.toJSONString
import org.springframework.stereotype.Service

@Service
class UserRoleRelationServiceImpl(
    private val userRoleRelationRepository: UserRoleRelationRepository,
    private val userRoleService: UserRoleService
) : UserRoleRelationService {
    override fun getRepository(): UserRoleRelationRepository {
        return this.userRoleRelationRepository
    }

    override suspend fun getUserRoles(userId: Long): List<UserRoleEntity> {
        val relations = this.getRepository()
            .findByUserId(userId)
            .awaitListWithTimeout()

        val mapping = userRoleService.getAllRolesAssociatedById()

        return relations.map {
            mapping[it.roleId]
                ?: throw BusinessException("Role ${it.roleId} not found, roles: ${mapping.toJSONString()}")
        }
    }
}