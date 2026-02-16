package com.lovelycatv.template.springboot.rbac.service.impl

import com.lovelycatv.template.springboot.rbac.entity.UserRoleEntity
import com.lovelycatv.template.springboot.rbac.repository.UserRoleRepository
import com.lovelycatv.template.springboot.rbac.service.UserRoleService
import com.lovelycatv.template.springboot.shared.utils.awaitListWithTimeout
import org.springframework.stereotype.Service

@Service
class UserRoleServiceImpl(
    private val userRoleRepository: UserRoleRepository
) : UserRoleService {
    override fun getRepository(): UserRoleRepository {
        return this.userRoleRepository
    }

    override suspend fun getAllRoles(): List<UserRoleEntity> {
        return this.getRepository().findAll().awaitListWithTimeout()
    }
}