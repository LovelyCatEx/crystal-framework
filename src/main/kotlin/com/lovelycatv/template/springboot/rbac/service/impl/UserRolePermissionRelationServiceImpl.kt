package com.lovelycatv.template.springboot.rbac.service.impl

import com.lovelycatv.template.springboot.rbac.entity.UserPermissionEntity
import com.lovelycatv.template.springboot.rbac.entity.UserRolePermissionRelationEntity
import com.lovelycatv.template.springboot.rbac.repository.UserPermissionRepository
import com.lovelycatv.template.springboot.rbac.repository.UserRolePermissionRelationRepository
import com.lovelycatv.template.springboot.rbac.service.UserRolePermissionRelationService
import com.lovelycatv.template.springboot.shared.exception.BusinessException
import com.lovelycatv.template.springboot.shared.utils.SnowIdGenerator
import com.lovelycatv.template.springboot.shared.utils.awaitListWithTimeout
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserRolePermissionRelationServiceImpl(
    private val userRolePermissionRelationRepository: UserRolePermissionRelationRepository,
    private val userPermissionRepository: UserPermissionRepository,
    private val snowIdGenerator: SnowIdGenerator
) : UserRolePermissionRelationService {
    override fun getRepository(): UserRolePermissionRelationRepository {
        return userRolePermissionRelationRepository
    }

    override suspend fun getRolePermissions(roleId: Long): List<UserPermissionEntity> {
        val relationIds = this.getRepository()
            .findByRoleId(roleId)
            .awaitListWithTimeout()

        return relationIds.map {
            userPermissionRepository.findById(it.permissionId).awaitFirstOrNull()
                ?: throw BusinessException("permission with id $it not found")
        }
    }

    @Transactional
    override suspend fun setRolePermissions(roleId: Long, permissionIds: List<Long>) {
        // Delete existing relations
        val existing = this.getRepository()
            .findByRoleId(roleId)
            .awaitListWithTimeout()

        existing.forEach {
            userRolePermissionRelationRepository.delete(it).awaitFirstOrNull()
        }

        // Create new relations
        permissionIds.forEach { permissionId ->
            val entity = UserRolePermissionRelationEntity(
                id = snowIdGenerator.nextId(),
                roleId = roleId,
                permissionId = permissionId
            ).apply { newEntity() }
            userRolePermissionRelationRepository.save(entity).awaitFirstOrNull()
        }
    }
}
