package com.lovelycatv.template.springboot.rbac.service.impl

import com.lovelycatv.template.springboot.rbac.controller.manager.role.dto.ManagerCreateRoleDTO
import com.lovelycatv.template.springboot.rbac.controller.manager.role.dto.ManagerDeleteRoleDTO
import com.lovelycatv.template.springboot.rbac.controller.manager.role.dto.ManagerReadRoleDTO
import com.lovelycatv.template.springboot.rbac.controller.manager.role.dto.ManagerUpdateRoleDTO
import com.lovelycatv.template.springboot.rbac.entity.UserRoleEntity
import com.lovelycatv.template.springboot.rbac.repository.UserRoleRepository
import com.lovelycatv.template.springboot.rbac.service.UserRoleManagerService
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service

@Service
class UserRoleManagerServiceImpl(
    private val userRoleRepository: UserRoleRepository
) : UserRoleManagerService {
    override fun getRepository(): UserRoleRepository {
        return userRoleRepository
    }

    override suspend fun create(dto: ManagerCreateRoleDTO): UserRoleEntity {
        val entity = UserRoleEntity(
            name = dto.name,
            description = dto.description
        ).apply { newEntity() }
        return userRoleRepository.save(entity).awaitFirstOrNull()
            ?: throw RuntimeException("Could not create role")
    }

    override suspend fun applyDTOToEntity(dto: ManagerUpdateRoleDTO, original: UserRoleEntity): UserRoleEntity {
        return original.apply {
            dto.name?.let { name = it }
            dto.description?.let { description = it }
        }
    }
}
