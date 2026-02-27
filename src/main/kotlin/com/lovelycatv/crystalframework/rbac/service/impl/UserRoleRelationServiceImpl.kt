package com.lovelycatv.crystalframework.rbac.service.impl

import com.lovelycatv.crystalframework.rbac.entity.UserRoleEntity
import com.lovelycatv.crystalframework.rbac.entity.UserRoleRelationEntity
import com.lovelycatv.crystalframework.rbac.repository.UserRoleRelationRepository
import com.lovelycatv.crystalframework.rbac.repository.UserRoleRepository
import com.lovelycatv.crystalframework.rbac.service.UserRoleRelationService
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserRoleRelationServiceImpl(
    private val userRoleRelationRepository: UserRoleRelationRepository,
    private val userRoleRepository: UserRoleRepository,
    private val snowIdGenerator: SnowIdGenerator
) : UserRoleRelationService {
    override fun getRepository(): UserRoleRelationRepository {
        return userRoleRelationRepository
    }

    override suspend fun getUserRoles(userId: Long): List<UserRoleEntity> {
        return userRoleRepository.findAllById(
            userRoleRelationRepository
                .findByUserId(userId)
                .map { it.roleId }
        ).awaitListWithTimeout()
    }

    @Transactional
    override suspend fun setUserRoles(userId: Long, roleIds: List<Long>) {
        // Delete existing relations
        val existing = userRoleRelationRepository
            .findByUserId(userId)
            .awaitListWithTimeout()

        existing.forEach {
            userRoleRelationRepository.delete(it).awaitFirstOrNull()
        }

        // Create new relations
        roleIds.forEach { roleId ->
            val entity = UserRoleRelationEntity(
                id = snowIdGenerator.nextId(),
                userId = userId,
                roleId = roleId
            ).apply { newEntity() }
            userRoleRelationRepository.save(entity).awaitFirstOrNull()
        }
    }
}
