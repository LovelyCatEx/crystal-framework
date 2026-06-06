package com.lovelycatv.crystalframework.rbac.user.repository

import com.lovelycatv.crystalframework.rbac.user.entity.UserPermissionEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface UserPermissionRepository : BaseRepository<UserPermissionEntity> {
    fun id(id: Long): MutableList<UserPermissionEntity>

    fun findByName(name: String): Mono<UserPermissionEntity>
}
