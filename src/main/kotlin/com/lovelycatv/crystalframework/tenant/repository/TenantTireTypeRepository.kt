package com.lovelycatv.crystalframework.tenant.repository

import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.tenant.entity.TenantTireTypeEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface TenantTireTypeRepository : BaseRepository<TenantTireTypeEntity> {
    @Query(
        """
        SELECT * FROM tenant_tire_types 
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
    ): Flux<TenantTireTypeEntity>

    @Query(
        """
        SELECT COUNT(*) FROM tenant_tire_types 
        WHERE LOWER(name) LIKE LOWER(CONCAT('%', :keyword, '%')) 
           OR LOWER(description) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """
    )
    override fun countByKeyword(
        keyword: String
    ): Mono<Long>

    fun findByName(name: String): Mono<TenantTireTypeEntity>
}
