package com.lovelycatv.crystalframework.rbac.user.repository

import com.lovelycatv.crystalframework.rbac.user.entity.UserRoleEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface UserRoleRepository : BaseRepository<UserRoleEntity> {
    fun findByName(name: String): Mono<UserRoleEntity>
}
