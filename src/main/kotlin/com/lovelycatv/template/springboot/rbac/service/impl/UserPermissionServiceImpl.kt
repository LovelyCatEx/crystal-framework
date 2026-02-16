package com.lovelycatv.template.springboot.rbac.service.impl

import com.lovelycatv.template.springboot.rbac.repository.UserPermissionRepository
import com.lovelycatv.template.springboot.rbac.service.UserPermissionService
import org.springframework.stereotype.Service

@Service
class UserPermissionServiceImpl(
    private val userPermissionRepository: UserPermissionRepository
) : UserPermissionService {
    override fun getRepository(): UserPermissionRepository {
        return this.userPermissionRepository
    }
}