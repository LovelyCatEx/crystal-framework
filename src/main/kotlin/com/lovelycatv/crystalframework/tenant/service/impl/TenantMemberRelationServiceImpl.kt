package com.lovelycatv.crystalframework.tenant.service.impl

import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.tenant.entity.TenantMemberEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantMemberRepository
import com.lovelycatv.crystalframework.tenant.service.TenantMemberRelationService
import com.lovelycatv.crystalframework.user.entity.UserEntity
import com.lovelycatv.crystalframework.user.repository.UserRepository
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.reflect.KClass

@Service
class TenantMemberRelationServiceImpl(
    private val tenantMemberRepository: TenantMemberRepository,
    private val userRepository: UserRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val redisService: RedisService,
    override val eventPublisher: ApplicationEventPublisher,
) : TenantMemberRelationService {
    override val cacheStore: ExpiringKVStore<String, TenantMemberEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<TenantMemberEntity>>
        get() = redisService.asKVStore()
    override val entityClass: KClass<TenantMemberEntity> = TenantMemberEntity::class

    override fun getRepository(): TenantMemberRepository {
        return tenantMemberRepository
    }

    override suspend fun getTenantMembers(tenantId: Long): List<UserEntity> {
        val relations = this.getRepository()
            .findAllByTenantId(tenantId)
            .awaitListWithTimeout()

        return relations.map {
            userRepository.findById(it.memberUserId).awaitFirstOrNull()
                ?: throw RuntimeException("user with id ${it.memberUserId} not found")
        }
    }

    @Transactional
    override suspend fun setTenantMembers(tenantId: Long, userIds: List<Long>) {
        // Delete existing relations
        val existing = this.getRepository()
            .findAllByTenantId(tenantId)
            .awaitListWithTimeout()

        existing.forEach {
            tenantMemberRepository.delete(it).awaitFirstOrNull()
        }

        // Create new relations
        userIds.forEach { userId ->
            val entity = TenantMemberEntity(
                id = snowIdGenerator.nextId(),
                tenantId = tenantId,
                memberUserId = userId,
                status = 0 // Default status
            ).apply { newEntity() }
            tenantMemberRepository.save(entity).awaitFirstOrNull()
        }
    }

    override suspend fun deleteByTenantIdIn(tenantIds: Collection<Long>) {
        tenantIds.forEach { tenantId ->
            val existing = this.getRepository()
                .findAllByTenantId(tenantId)
                .awaitListWithTimeout()
            existing.forEach {
                tenantMemberRepository.delete(it).awaitFirstOrNull()
            }
        }
    }

    override suspend fun deleteByUserIdIn(userIds: Collection<Long>) {
        userIds.forEach { userId ->
            val existing = this.getRepository()
                .findAllByMemberUserId(userId)
                .awaitListWithTimeout()
            existing.forEach {
                tenantMemberRepository.delete(it).awaitFirstOrNull()
            }
        }
    }
}
