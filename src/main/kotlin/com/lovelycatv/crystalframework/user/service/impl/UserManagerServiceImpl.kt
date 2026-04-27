package com.lovelycatv.crystalframework.user.service.impl

import com.lovelycatv.crystalframework.rbac.service.UserRoleRelationService
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.user.controller.manager.user.dto.ManagerCreateUserDTO
import com.lovelycatv.crystalframework.user.controller.manager.user.dto.ManagerUpdateUserDTO
import com.lovelycatv.crystalframework.user.entity.UserEntity
import com.lovelycatv.crystalframework.user.repository.UserRepository
import com.lovelycatv.crystalframework.user.service.UserManagerService
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.reflect.KClass

@Service
class UserManagerServiceImpl(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val snowIdGenerator: SnowIdGenerator,
    private val redisService: RedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val userRoleRelationService: UserRoleRelationService,
) : UserManagerService {
    override val cacheStore: ExpiringKVStore<String, UserEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<UserEntity>>
        get() = redisService.asKVStore()
    override val entityClass: KClass<UserEntity> = UserEntity::class

    override fun getRepository(): UserRepository {
        return userRepository
    }

    override suspend fun create(dto: ManagerCreateUserDTO): UserEntity {
        userRepository.findByUsername(dto.username).awaitFirstOrNull()?.let {
            throw BusinessException("username '${dto.username}' is already taken")
        }
        userRepository.findByEmail(dto.email).awaitFirstOrNull()?.let {
            throw BusinessException("email '${dto.email}' is already registered")
        }

        val entity = UserEntity(
            id = snowIdGenerator.nextId(),
            username = dto.username,
            password = passwordEncoder.encode(dto.password)!!,
            email = dto.email,
            nickname = dto.nickname
        ).apply { newEntity() }
        return userRepository.save(entity).awaitFirstOrNull()
            ?: throw RuntimeException("Could not create user")
    }

    override suspend fun applyDTOToEntity(dto: ManagerUpdateUserDTO, original: UserEntity): UserEntity {
        return original.apply {
            dto.email?.let { email = it }
            dto.nickname?.let { nickname = it }
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    override suspend fun batchDelete(ids: List<Long>) {
        super.batchDelete(ids)

        userRoleRelationService.deleteByUserIdIn(ids)
    }
}
