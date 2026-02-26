package com.lovelycatv.template.springboot.rbac.repository

import com.lovelycatv.template.springboot.rbac.entity.UserRoleEntity
import com.lovelycatv.template.springboot.shared.repository.BaseRepository
import org.springframework.data.r2dbc.repository.Query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface UserRoleRepository : BaseRepository<UserRoleEntity> {
    @Query(
        """
        SELECT * FROM user_roles 
        WHERE (LOWER(name) LIKE LOWER(CONCAT('%', :keyword, '%')) 
           OR LOWER(description) LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY created_time DESC
        LIMIT :limit
        OFFSET :offset
    """
    )
    override fun searchByKeyword(
        keyword: String,
        limit: Int,
        offset: Int
    ): Flux<UserRoleEntity>

    @Query(
        """
        SELECT COUNT(*) FROM user_roles 
        WHERE LOWER(name) LIKE LOWER(CONCAT('%', :keyword, '%')) 
           OR LOWER(description) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """
    )
    override fun countByKeyword(
        keyword: String
    ): Mono<Long>

    fun findByName(name: String): Mono<UserRoleEntity>
}