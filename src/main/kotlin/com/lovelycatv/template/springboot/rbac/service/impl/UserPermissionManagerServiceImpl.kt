package com.lovelycatv.template.springboot.rbac.service.impl

import com.lovelycatv.template.springboot.rbac.controller.manager.permission.dto.ManagerCreatePermissionDTO
import com.lovelycatv.template.springboot.rbac.controller.manager.permission.dto.ManagerUpdatePermissionDTO
import com.lovelycatv.template.springboot.rbac.entity.UserPermissionEntity
import com.lovelycatv.template.springboot.rbac.repository.UserPermissionRepository
import com.lovelycatv.template.springboot.rbac.service.UserPermissionManagerService
import com.lovelycatv.template.springboot.shared.exception.BusinessException
import com.lovelycatv.template.springboot.shared.utils.SnowIdGenerator
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service

@Service
class UserPermissionManagerServiceImpl(
    private val userPermissionRepository: UserPermissionRepository,
    private val snowIdGenerator: SnowIdGenerator
) : UserPermissionManagerService {
    override fun getRepository(): UserPermissionRepository {
        return this.userPermissionRepository
    }

    override suspend fun create(dto: ManagerCreatePermissionDTO): UserPermissionEntity {
        return this.getRepository().save(
            UserPermissionEntity(
                id = snowIdGenerator.nextId(),
                name = dto.name,
                description = dto.description,
                type = dto.type,
                path = dto.path
            ) newEntity true
        ).awaitFirstOrNull() ?: throw BusinessException("Could not create user permission")
    }

    override suspend fun applyDTOToEntity(
        dto: ManagerUpdatePermissionDTO,
        original: UserPermissionEntity
    ): UserPermissionEntity {
        return original.apply {
            if (dto.name != null) {
                this.name = dto.name
            }

            this.description = dto.description

            if (dto.type != null) {
                this.type = dto.type
            }
        }
    }
}