package com.lovelycatv.crystalframework.auth.event

import com.lovelycatv.crystalframework.rbac.user.repository.UserRoleRelationRepository
import com.lovelycatv.crystalframework.rbac.user.service.UserRbacQueryService
import com.lovelycatv.crystalframework.shared.types.rbac.SystemRoleAuthoritiesInvalidationEvent
import com.lovelycatv.crystalframework.shared.types.rbac.TenantMemberAuthoritiesInvalidationEvent
import com.lovelycatv.crystalframework.shared.types.rbac.TenantRoleAuthoritiesInvalidationEvent
import com.lovelycatv.crystalframework.shared.types.rbac.UserAuthoritiesCacheInvalidationEvent
import com.lovelycatv.crystalframework.shared.types.rbac.UserAuthoritiesInvalidationEvent
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.tenant.repository.TenantMemberRepository
import com.lovelycatv.crystalframework.rbac.tenant.repository.TenantMemberRoleRelationRepository
import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import java.util.concurrent.Executors

/**
 * Listens to [com.lovelycatv.crystalframework.shared.types.rbac.UserAuthoritiesCacheInvalidationEvent]s and drops the affected
 * `userAuthorities:*` cache entries so the next request rebuilds them.
 *
 * Wired through [org.springframework.transaction.event.TransactionalEventListener] so the cache is only dropped after
 * the role/permission mutation has actually committed; the [org.springframework.scheduling.annotation.Async] executor keeps
 * the fan-out off the request thread.
 */
@Component
class UserAuthoritiesCacheInvalidator(
    private val userRbacQueryService: UserRbacQueryService,
    private val userRoleRelationRepository: UserRoleRelationRepository,
    private val tenantMemberRepository: TenantMemberRepository,
    private val tenantMemberRoleRelationRepository: TenantMemberRoleRelationRepository,
) {
    private val logger = logger()

    private val coroutineScope = CoroutineScope(
        Executors.newScheduledThreadPool(4).asCoroutineDispatcher()
    )

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    fun handle(event: UserAuthoritiesCacheInvalidationEvent) {
        coroutineScope.launch(Dispatchers.IO) {
            when (event) {
                is UserAuthoritiesInvalidationEvent -> {
                    invalidate(event.userId)
                }

                is SystemRoleAuthoritiesInvalidationEvent -> {
                    val userIds = userRoleRelationRepository
                        .findByRoleId(event.roleId)
                        .awaitListWithTimeout()
                        .map { it.userId }
                        .toSet()
                    userIds.forEach { invalidate(it) }
                }

                is TenantMemberAuthoritiesInvalidationEvent -> {
                    val userId = tenantMemberRepository
                        .findById(event.memberId)
                        .awaitFirstOrNull()
                        ?.memberUserId
                    if (userId != null) {
                        invalidate(userId)
                    }
                }

                is TenantRoleAuthoritiesInvalidationEvent -> {
                    val memberIds = tenantMemberRoleRelationRepository
                        .findAllByRoleId(event.roleId)
                        .awaitListWithTimeout()
                        .map { it.memberId }
                        .toSet()
                    if (memberIds.isEmpty()) {
                        return@launch
                    }
                    val userIds = tenantMemberRepository
                        .findAllById(memberIds)
                        .awaitListWithTimeout()
                        .map { it.memberUserId }
                        .toSet()
                    userIds.forEach { invalidate(it) }
                }
            }
        }
    }

    private suspend fun invalidate(userId: Long) {
        runCatching {
            userRbacQueryService.clearUserAuthoritiesCache(userId)
            logger.debug("User $userId authorities cache invalidated")
        }.onFailure { logger.warn("failed to invalidate authorities cache for user $userId", it) }
    }
}