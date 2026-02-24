package com.lovelycatv.template.springboot.rbac.service.impl

import com.lovelycatv.template.springboot.rbac.entity.UserRoleEntity
import com.lovelycatv.template.springboot.rbac.entity.UserRoleRelationEntity
import com.lovelycatv.template.springboot.rbac.repository.UserRoleRelationRepository
import com.lovelycatv.template.springboot.rbac.repository.UserRoleRepository
import com.lovelycatv.template.springboot.rbac.service.UserRoleRelationService
import com.lovelycatv.template.springboot.shared.exception.BusinessException
import com.lovelycatv.template.springboot.shared.utils.SnowIdGenerator
import com.lovelycatv.template.springboot.shared.utils.awaitListWithTimeout
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
        val relationIds = userRoleRelationRepository
            .findByUserId(userId)
            .awaitListWithTimeout()

        return relationIds.map {
            userRoleRepository.findById(it.roleId).awaitFirstOrNull()
                ?: throw BusinessException("role with id $it not found")
        }
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
