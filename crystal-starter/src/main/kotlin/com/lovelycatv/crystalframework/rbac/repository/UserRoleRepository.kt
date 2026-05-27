package com.lovelycatv.crystalframework.rbac.repository

import com.lovelycatv.crystalframework.rbac.entity.UserRoleEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface UserRoleRepository : BaseRepository<UserRoleEntity> {
    fun findByName(name: String): Mono<UserRoleEntity>
}
