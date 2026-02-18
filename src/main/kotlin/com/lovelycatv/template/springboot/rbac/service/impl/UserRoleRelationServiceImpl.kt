package com.lovelycatv.template.springboot.rbac.service.impl

import com.lovelycatv.template.springboot.rbac.entity.UserRoleEntity
import com.lovelycatv.template.springboot.rbac.entity.UserRoleRelationEntity
import com.lovelycatv.template.springboot.rbac.repository.UserRoleRelationRepository
import com.lovelycatv.template.springboot.rbac.service.UserRoleRelationService
import com.lovelycatv.template.springboot.rbac.service.UserRoleService
import com.lovelycatv.template.springboot.shared.exception.BusinessException
import com.lovelycatv.template.springboot.shared.utils.analyzeExecutionTimeSuspend
import com.lovelycatv.template.springboot.shared.utils.awaitListWithTimeout
import com.lovelycatv.template.springboot.shared.utils.toJSONString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitLast
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import java.util.concurrent.CountDownLatch
import kotlin.coroutines.resume

@Service
class UserRoleRelationServiceImpl(
    private val userRoleRelationRepository: UserRoleRelationRepository,
    private val userRoleService: UserRoleService
) : UserRoleRelationService {
    override fun getRepository(): UserRoleRelationRepository {
        return this.userRoleRelationRepository
    }

    override suspend fun getUserRoles(userId: Long): List<UserRoleEntity> {
        val relations = getRepository()
            .findByUserId(userId)
            .awaitListWithTimeout()

        val mapping = userRoleService.getAllRolesAssociatedById()

        return relations.map {
            mapping[it.roleId]
                ?: throw BusinessException("Role ${it.roleId} not found, roles: ${mapping.toJSONString()}")
        }
    }
}