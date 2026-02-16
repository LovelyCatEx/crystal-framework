package com.lovelycatv.template.springboot.user.service

import com.lovelycatv.template.springboot.rbac.service.UserRolePermissionRelationService
import com.lovelycatv.template.springboot.rbac.service.UserRoleRelationService
import com.lovelycatv.template.springboot.shared.exception.BusinessException
import com.lovelycatv.template.springboot.shared.utils.SnowIdGenerator
import com.lovelycatv.template.springboot.user.entity.UserEntity
import com.lovelycatv.template.springboot.user.repository.UserRepository
import com.lovelycatv.template.springboot.user.service.result.UserRbacQueryResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val passwordEncoder: PasswordEncoder,
    private val userRoleRelationService: UserRoleRelationService,
    private val rolePermissionRelationService: UserRolePermissionRelationService
) : UserService {
    override fun getRepository(): UserRepository {
        return this.userRepository
    }

    override fun findByUsername(username: String): Mono<UserDetails> {
        return this
            .getRepository()
            .findByUsernameOrEmail(username, username)
            .switchIfEmpty {
                Mono.error(BusinessException("User $username not found"))
            }
            .map {
                it.apply {
                    setInternalRawAuthorities(
                        runBlocking(Dispatchers.IO) {
                            getUserRbacAccessInfo(it.id).permissions.map { it.name }
                        }
                    )
                }
            }
    }

    override suspend fun register(
        username: String,
        password: String,
        email: String,
        emailConfirmationCode: String
    ) {
        val existingUser = this
            .getRepository()
            .findByUsernameOrEmail(username, email)
            .awaitSingleOrNull()

        if (existingUser != null) {
            if (existingUser.username == username) {
                throw BusinessException("User $username already exists")
            }

            if (existingUser.email == email) {
                throw BusinessException("Email $username already exists")
            }
        }

        this.getRepository().save(
            UserEntity(
                id = snowIdGenerator.nextId(),
                username = username,
                password = passwordEncoder.encode(password)
                    ?: throw BusinessException("Could not create user entity due to missing encoded password, encoding failed"),
                email = email,
                nickname = username
            ) newEntity true
        ).awaitSingleOrNull()
    }

    override suspend fun requestRegisterEmailConfirmationCode(email: String) {
        TODO("Not yet implemented")
    }

    override suspend fun getUserRbacAccessInfo(userId: Long): UserRbacQueryResult {
        return UserRbacQueryResult(
            userId = userId,
            rolesWithPermissions = userRoleRelationService.getUserRoles(userId).map {
                UserRbacQueryResult.UserRoleWithPermissions(
                    role = it,
                    permissions = rolePermissionRelationService.getRolePermissions(it.id)
                )
            }
        )
    }
}