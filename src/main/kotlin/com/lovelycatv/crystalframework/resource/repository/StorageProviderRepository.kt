package com.lovelycatv.crystalframework.resource.repository

import com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import org.springframework.data.r2dbc.repository.Query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface StorageProviderRepository : BaseRepository<StorageProviderEntity> {
    @Query(
        """
        SELECT * FROM storage_providers 
        WHERE (:#{#keyword == null} = true
           OR (LOWER(name) LIKE LOWER(CONCAT('%', :keyword, '%')) 
           OR LOWER(description) LIKE LOWER(CONCAT('%', :keyword, '%'))))
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
    ): Flux<StorageProviderEntity>

    @Query(
        """
        SELECT COUNT(*) FROM storage_providers 
        WHERE (:#{#keyword == null} = true
           OR (LOWER(name) LIKE LOWER(CONCAT('%', :keyword, '%')) 
           OR LOWER(description) LIKE LOWER(CONCAT('%', :keyword, '%'))))
        AND (:#{#type == null} = true OR type = :type)
    """
    )
    fun countAdvanceSearch(
        keyword: String?,
        type: Int?,
    ): Mono<Long>
}