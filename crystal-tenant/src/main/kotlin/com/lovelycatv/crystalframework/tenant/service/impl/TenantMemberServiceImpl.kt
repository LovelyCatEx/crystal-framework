package com.lovelycatv.crystalframework.tenant.service.impl

import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.tenant.controller.manager.member.vo.TenantMemberVO
import com.lovelycatv.crystalframework.tenant.entity.TenantMemberEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantMemberRepository
import com.lovelycatv.crystalframework.tenant.service.TenantMemberService
import com.lovelycatv.crystalframework.user.service.UserService
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class TenantMemberServiceImpl(
    private val tenantMemberRepository: TenantMemberRepository,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val userService: UserService,
) : TenantMemberService {
    override val cacheStore: ReactiveExpiringKVStore<String, TenantMemberEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<TenantMemberEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<TenantMemberEntity> = TenantMemberEntity::class

    override fun getRepository(): TenantMemberRepository {
        return this.tenantMemberRepository
    }

    override suspend fun getByTenantIdAndUserId(
        tenantId: Long,
        userId: Long
    ): TenantMemberEntity? {
        return this.getRepository()
            .findByTenantIdAndMemberUserId(tenantId, userId)
            .awaitFirstOrNull()
    }

    override suspend fun transformTenantMemberVO(tenantMemberEntity: TenantMemberEntity): TenantMemberVO {
        val user = userService.getByIdOrNull(tenantMemberEntity.memberUserId)
        return TenantMemberVO.fromEntity(tenantMemberEntity, user)
    }
}
