package com.lovelycatv.template.springboot.rbac.service

import com.lovelycatv.template.springboot.rbac.entity.UserPermissionEntity
import com.lovelycatv.template.springboot.rbac.repository.UserPermissionRepository
import com.lovelycatv.template.springboot.shared.service.BaseService

interface UserPermissionService : BaseService<UserPermissionRepository, UserPermissionEntity> {
}