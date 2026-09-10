package com.lovelycatv.crystalframework.ai.service.manager.impl

import com.lovelycatv.crystalframework.ai.controller.manager.usergroupmember.dto.ManagerCreateAiUserGroupMemberDTO
import com.lovelycatv.crystalframework.ai.controller.manager.usergroupmember.dto.ManagerUpdateAiUserGroupMemberDTO
import com.lovelycatv.crystalframework.ai.entity.AiUserGroupMemberEntity
import com.lovelycatv.crystalframework.ai.repository.AiUserGroupMemberRepository
import com.lovelycatv.crystalframework.ai.service.manager.AiUserGroupMemberManagerService
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class AiUserGroupMemberManagerServiceImpl(
    private val repository: AiUserGroupMemberRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : AiUserGroupMemberManagerService {
    override val cacheStore: ReactiveExpiringKVStore<String, AiUserGroupMemberEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<AiUserGroupMemberEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<AiUserGroupMemberEntity> = AiUserGroupMemberEntity::class

    override fun getRepository(): AiUserGroupMemberRepository = repository

    override fun getEntityTemplate(): R2dbcEntityTemplate = r2dbcEntityTemplate

    override suspend fun create(dto: ManagerCreateAiUserGroupMemberDTO): AiUserGroupMemberEntity {
        val entity = AiUserGroupMemberEntity(
            id = snowIdGenerator.nextId(),
            userGroupId = dto.userGroupId,
            userId = dto.userId,
        )
        return withInvalidateEntityCacheContext(entity.id) {
            repository.save(entity newEntity true).awaitFirstOrNull()
                ?: throw BusinessException("Could not create AI user group member association")
        }
    }

    override suspend fun applyDTOToEntity(
        dto: ManagerUpdateAiUserGroupMemberDTO,
        original: AiUserGroupMemberEntity,
    ): AiUserGroupMemberEntity {
        return original.apply {
            dto.userGroupId?.let { userGroupId = it }
            dto.userId?.let { userId = it }
        }
    }
}
