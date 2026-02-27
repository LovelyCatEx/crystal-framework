package com.lovelycatv.crystalframework.rbac.repository

import com.lovelycatv.crystalframework.rbac.entity.UserPermissionEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface UserPermissionRepository : BaseRepository<UserPermissionEntity> {
    fun id(id: Long): MutableList<UserPermissionEntity>

    @Query(
        """
        SELECT * FROM user_permissions 
        WHERE (LOWER(name) LIKE LOWER(CONCAT('%', :keyword, '%')) 
           OR LOWER(description) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(path) LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY created_time DESC
        LIMIT :limit
        OFFSET :offset
    """
    )
    override fun searchByKeyword(
        keyword: String,
        limit: Int,
        offset: Int
    ): Flux<UserPermissionEntity>

    @Query(
        """
        SELECT COUNT(*) FROM user_permissions 
        WHERE LOWER(name) LIKE LOWER(CONCAT('%', :keyword, '%')) 
           OR LOWER(description) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(path) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """
    )
    override fun countByKeyword(
        keyword: String
    ): Mono<Long>

    @Query(
        """
        SELECT * FROM user_permissions 
        WHERE (:#{#keyword == null} = true
           OR (LOWER(name) LIKE LOWER(CONCAT('%', :keyword, '%')) 
           OR LOWER(description) LIKE LOWER(CONCAT('%', :keyword, '%')))
           OR LOWER(path) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:#{#type == null} = true OR type = :type)
        ORDER BY created_time DESC
        LIMIT :limit
        OFFSET :offset
    """
    )
    fun advanceSearch(
        keyword: String?,
        type: Int?,
        limit: Int,
        offset: Int
    ): Flux<UserPermissionEntity>

    @Query(
        """
        SELECT COUNT(*) FROM user_permissions 
        WHERE (:#{#keyword == null} = true
           OR (LOWER(name) LIKE LOWER(CONCAT('%', :keyword, '%')) 
           OR LOWER(description) LIKE LOWER(CONCAT('%', :keyword, '%')))
           OR LOWER(path) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:#{#type == null} = true OR type = :type)
    """
    )
    fun countAdvanceSearch(
        keyword: String?,
        type: Int?,
    ): Mono<Long>

    fun findByName(name: String): Mono<UserPermissionEntity>
}