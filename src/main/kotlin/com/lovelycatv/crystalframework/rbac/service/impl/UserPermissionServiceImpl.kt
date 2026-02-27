package com.lovelycatv.crystalframework.rbac.service.impl

import com.lovelycatv.crystalframework.rbac.entity.UserPermissionEntity
import com.lovelycatv.crystalframework.rbac.repository.UserPermissionRepository
import com.lovelycatv.crystalframework.rbac.service.UserPermissionService
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class UserPermissionServiceImpl(
    private val userPermissionRepository: UserPermissionRepository
) : UserPermissionService {
    override fun getRepository(): UserPermissionRepository {
        return this.userPermissionRepository
    }

    override suspend fun getAllPermissions(): List<UserPermissionEntity> {
        return userPermissionRepository.findAll().awaitListWithTimeout()
    }
}