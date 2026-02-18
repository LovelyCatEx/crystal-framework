package com.lovelycatv.template.springboot.rbac.repository

import com.lovelycatv.template.springboot.rbac.entity.UserPermissionEntity
import com.lovelycatv.template.springboot.shared.request.PaginatedResponseData
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface UserPermissionRepository : R2dbcRepository<UserPermissionEntity, Long> {
    fun id(id: Long): MutableList<UserPermissionEntity>

    @Query("SELECT * FROM user_permissions LIMIT :limit OFFSET :offset")
    fun findAllByPage(
        limit: Int,
        offset: Int
    ): Flux<UserPermissionEntity>

    @Query("""
        SELECT * FROM user_permissions 
        WHERE (LOWER(name) LIKE LOWER(CONCAT('%', :keyword, '%')) 
           OR LOWER(description) LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY created_time DESC
        LIMIT :limit
        OFFSET :offset
    """)
    fun searchByKeywordWithCursor(
        keyword: String,
        limit: Int,
        offset: Int,
    ): Flux<UserPermissionEntity>

    @Query("""
        SELECT COUNT(*) FROM user_permissions 
        WHERE LOWER(name) LIKE LOWER(CONCAT('%', :keyword, '%')) 
           OR LOWER(description) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    fun countByKeyword(keyword: String): Mono<Long>
}