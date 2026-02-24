package com.lovelycatv.template.springboot.rbac.service

import com.lovelycatv.template.springboot.rbac.entity.UserPermissionEntity
import com.lovelycatv.template.springboot.rbac.repository.UserPermissionRepository
import com.lovelycatv.template.springboot.shared.service.BaseService
import reactor.core.publisher.Flux

interface UserPermissionService : BaseService<UserPermissionRepository, UserPermissionEntity> {
    suspend fun getAllPermissions(): List<UserPermissionEntity>
}