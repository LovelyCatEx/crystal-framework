package com.lovelycatv.crystalframework.auth.service.impl

import com.lovelycatv.crystalframework.rbac.user.service.UserRoleRelationService
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.tenant.entity.TenantEntity
import com.lovelycatv.crystalframework.tenant.entity.TenantMemberEntity
import com.lovelycatv.crystalframework.tenant.service.TenantMemberRelationService
import com.lovelycatv.crystalframework.tenant.service.TenantService
import com.lovelycatv.crystalframework.shared.types.tenant.TenantMemberStatus
import com.lovelycatv.crystalframework.shared.types.tenant.TenantStatus
import com.lovelycatv.crystalframework.shared.types.tenant.entity.UserAuthenticatedTenantVO
import com.lovelycatv.crystalframework.user.entity.UserEntity
import com.lovelycatv.crystalframework.user.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.flux
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.runBlocking
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toMono

/**
 * [ReactiveUserDetailsService] used by Spring Security to resolve a [UserEntity]
 * during authentication.
 *
 * This carries the authentication concern that previously lived in the user module
 * (UserServiceImpl.findByUsername): besides loading the user and its system-level
 * authorities, it resolves and validates the authenticated tenant membership.
 */
@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository,
    private val userRoleRelationService: UserRoleRelationService,
    private val tenantService: TenantService,
    private val tenantMemberRelationService: TenantMemberRelationService
) : ReactiveUserDetailsService {

    /**
     * Why username split by ":"
     *
     * See: [com.lovelycatv.crystalframework.auth.filter.CustomLoginFilter]
     *
     * @param username Pattern: Username:TenantId
     */
    override fun findByUsername(username: String): Mono<UserDetails> {
        val (realUsername, tenantIdStr) = if (username.contains(":")) {
            username.split(":")
        } else {
            listOf(username, "0")
        }

        val tenantMono: Mono<TenantEntity> = getTenantMonoById(tenantIdStr.toLong())

        return this.userRepository
            .findByUsernameOrEmail(realUsername, realUsername)
            .switchIfEmpty {
                Mono.error(BusinessException("User $realUsername not found"))
            }
            .map { userEntity ->
                userEntity.apply {
                    setInternalRawAuthorities(
                        runBlocking(Dispatchers.IO) {
                            userRoleRelationService
                                .getUserRoles(userEntity.id)
                                .map { it.name }
                        }
                    )
                }
            }
            .flatMap { userEntity ->
                checkIsTenantValid(userEntity, tenantMono)
            }
    }

    private fun checkIsTenantValid(userEntity: UserEntity, tenantMono: Mono<TenantEntity>): Mono<UserDetails> {
        return tenantMono
            .flatMap<UserDetails> { tenantEntity ->
                flux {
                    val targetMember = tenantMemberRelationService
                        .getUserTenantMembers(userEntity.id)
                        .find { it.tenantId == tenantEntity.id }

                    if (targetMember != null) {
                        this.send(targetMember)

                        userEntity.setAuthenticatedTenant(
                            UserAuthenticatedTenantVO(
                                tenantEntity.id,
                                tenantEntity.ownerUserId,
                                tenantEntity.description,
                                tenantEntity.icon,
                                tenantEntity.status,
                                tenantEntity.tireTypeId,
                                tenantEntity.subscribedTime,
                                tenantEntity.expiresTime,
                                tenantEntity.contactName,
                                tenantEntity.settings,
                                tenantEntity.contactEmail,
                                tenantEntity.contactPhone,
                                tenantEntity.address,
                                tenantEntity.createdTime,
                                tenantEntity.modifiedTime,
                                tenantEntity.deletedTime,
                                userEntity.id,
                                targetMember.id
                            )
                        )
                    } else {
                        Flux.error<TenantMemberEntity>(BusinessException("You are not the member of the tenant"))
                    }
                }.collectList()
                    .flatMap {
                        val memberRecord = it.find { it.memberUserId == userEntity.id }
                        if (memberRecord == null) {
                            Mono.error(BusinessException("You are not the member of the tenant"))
                        } else if (memberRecord.getRealStatus() != TenantMemberStatus.ACTIVE) {
                            Mono.error(BusinessException("Your account is reviewing or closed by the tenant"))
                        } else {
                            Mono.empty()
                        }
                    }
            }
            .switchIfEmpty {
                userEntity.toMono()
            }
    }

    private fun getTenantMonoById(tenantId: Long): Mono<TenantEntity> {
        return mono {
            if (tenantId > 0) {
                val tenant = tenantService.getByIdOrThrow(
                    tenantId,
                    BusinessException("tenant $tenantId is not found")
                )

                if (tenant.getRealStatus() == TenantStatus.ACTIVE) {
                    tenant
                } else {
                    throw BusinessException("tenant is inactive or closed")
                }
            } else {
                null
            }
        }
    }
}
