package com.lovelycatv.template.springboot.rbac.service.impl

import com.lovelycatv.template.springboot.rbac.entity.UserPermissionEntity
import com.lovelycatv.template.springboot.rbac.repository.UserPermissionRepository
import com.lovelycatv.template.springboot.rbac.service.UserPermissionService
import com.lovelycatv.template.springboot.shared.utils.awaitListWithTimeout
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